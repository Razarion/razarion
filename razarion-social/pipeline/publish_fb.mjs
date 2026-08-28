#!/usr/bin/env node
// Step 5b: publish the reviewed posts to the Facebook Page, oldest first and backdated.
//
// A dry run unless --live is passed.
//
//   node publish_fb.mjs                    # what it would do
//   node publish_fb.mjs --live --limit 1   # one post, to watch it land
//   node publish_fb.mjs --live             # the rest
//   node publish_fb.mjs --live --no-backdate
//   node publish_fb.mjs --live --granularity hour

import { parseArgs } from './lib/args.mjs';
import {
  FB_POSTS_FILE, POSTED_FB_FILE, STATE_DIR, ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { pageCredentials, post, get, FacebookError } from './lib/facebook.mjs';
import { sleep } from '../src/util/http.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_PAUSE_SECONDS = 60;
const DEFAULT_GRANULARITY = 'day';

// The same distinction as on the Instagram side: these mean "too fast", and waiting fixes them.
const THROTTLE_CODES = new Set([4, 17, 32, 613]);
const DEFAULT_THROTTLE_WAIT_SECONDS = 15 * 60;

// 368 is Facebook blocking the Page itself for posting too much. Waiting inside one run will not
// clear it.
const PAGE_BLOCK_CODE = 368;

function describe(entry) {
  const media = entry.media || [];
  if (!media.length) return 'text';
  if (media.length > 1) return `${media.length} photos`;
  return media[0].type === 'photo' ? 'photo' : 'video';
}

function firstLine(message) {
  const line = (message || '').split('\n')[0];
  return line.length > 76 ? line.slice(0, 73) + '...' : line;
}

/**
 * Sets the date the post appears under, after it exists.
 *
 * Passing backdated_time when creating the post does not work, and fails differently per endpoint:
 * /videos rejects it outright, while /photos accepts the parameter and silently drops it - the post
 * goes out carrying today's date and reporting success. Updating the post afterwards works for
 * every type, so that is the only route used here.
 *
 * The read-back is not paranoia but the lesson from that silent drop: "success" was not worth
 * believing once, so it is checked.
 */
async function backdatePost(postId, entry, { token, granularity, attempts = 5, waitMs = 15000 }) {
  const params = {
    backdated_time: Math.floor(Date.parse(entry.date) / 1000),
    backdated_time_granularity: granularity,
    access_token: token,
  };

  for (let attempt = 0; attempt < attempts; attempt++) {
    try {
      await post(`/${postId}`, params);
      const check = await get(`/${postId}`, { fields: 'backdated_time', access_token: token });
      if (check.backdated_time) return true;
    } catch (err) {
      // A video post cannot be edited while Facebook is still transcoding it, and the refusal is a
      // plain 400 that says nothing about waiting. Measured: it succeeds a minute later.
      if (!(err instanceof FacebookError) || err.status !== 400 || attempt === attempts - 1) throw err;
    }
    if (attempt === 0) step('post not editable yet, waiting for it to settle');
    await sleep(waitMs);
  }
  return false;
}

/**
 * One post, in whichever of Facebook's four shapes fits it.
 *
 * Unlike Instagram there is no container dance: each call both creates and publishes. The only
 * multi-step case is several photos, which have to exist as unpublished photo objects before a
 * feed post can attach them.
 */
async function createPost(entry, { pageId, token }) {
  const media = (entry.media || []).filter((m) => m.url);

  if (!media.length) {
    const result = await post(`/${pageId}/feed`, {
      message: entry.message,
      access_token: token,
    });
    return { kind: 'post', id: result.id };
  }

  if (media.length === 1 && media[0].type === 'photo') {
    const result = await post(`/${pageId}/photos`, {
      url: media[0].url,
      caption: entry.message,
      access_token: token,
    });
    return { kind: 'post', id: result.post_id || result.id };
  }

  if (media.length === 1) {
    const result = await post(`/${pageId}/videos`, {
      file_url: media[0].url,
      description: entry.message,
      access_token: token,
    });
    // Only the video id comes back. Which feed post it produced is a separate question, and one
    // that cannot be asked yet - hence the split: the video is published at this point, whatever
    // happens next.
    return { kind: 'video', id: result.id };
  }

  // published=false makes a photo object without putting it on the timeline; the feed post below
  // is what people actually see, and it carries all of them at once.
  const attached = {};
  for (const [index, item] of media.entries()) {
    const child = await post(`/${pageId}/photos`, {
      url: item.url,
      published: 'false',
      access_token: token,
    });
    attached[`attached_media[${index}]`] = JSON.stringify({ media_fbid: child.id });
  }

  const result = await post(`/${pageId}/feed`, {
    message: entry.message,
    access_token: token,
    ...attached,
  });
  return { kind: 'post', id: result.id };
}

/**
 * Finds the feed post a video produced.
 *
 * Right after the upload the video object is not queryable at all - the answer is a flat "object
 * does not exist", which is true only for the next minute or so. The id also needs the page prefix
 * before it addresses anything: without it the request lands on a deprecated endpoint that answers
 * "singular statuses API is deprecated".
 */
async function resolveVideoPost(videoId, { pageId, token, attempts = 6, waitMs = 15000 }) {
  for (let attempt = 0; attempt < attempts; attempt++) {
    try {
      const video = await get(`/${videoId}`, { fields: 'post_id', access_token: token });
      if (video.post_id) {
        return video.post_id.includes('_') ? video.post_id : `${pageId}_${video.post_id}`;
      }
    } catch (err) {
      if (!(err instanceof FacebookError) || err.status !== 400) throw err;
    }
    if (attempt === 0) step('video not queryable yet, waiting for it to appear');
    await sleep(waitMs);
  }
  return null;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const live = Boolean(args.live);
  const limit = args.limit ? Number(args.limit) : Infinity;
  const pauseSeconds = args.pause ? Number(args.pause) : DEFAULT_PAUSE_SECONDS;
  const throttleWait = args['throttle-wait'] ? Number(args['throttle-wait']) : DEFAULT_THROTTLE_WAIT_SECONDS;
  const granularity = args.granularity ? String(args.granularity) : DEFAULT_GRANULARITY;
  let backdate = !args['no-backdate'];

  const doc = readJson(FB_POSTS_FILE);
  if (!doc) throw new Error(`No ${toRelative(FB_POSTS_FILE)}. Run: node build_fb_posts.mjs`);

  ensureDir(STATE_DIR);
  const posted = readJson(POSTED_FB_FILE, { posted: {} });

  const queue = doc.posts
    .filter((e) => e.status === 'ok')
    .filter((e) => !posted.posted[e.id])
    .filter((e) => !(e.media || []).length || (e.media || []).every((m) => m.url))
    .sort((a, b) => Date.parse(a.date) - Date.parse(b.date));

  info(`${doc.posts.filter((e) => e.status === 'ok').length} entries marked ok`);
  info(`  already published: ${Object.keys(posted.posted).length}`);
  info(`  ready to publish:  ${queue.length}`);
  if (!queue.length) {
    info('');
    info('Nothing to do. Set status "ok" in fb_posts.json and run upload_media.mjs --source fb.');
    return;
  }

  const batch = queue.slice(0, limit === Infinity ? queue.length : limit);

  if (!live) {
    info('');
    warn('DRY RUN. Nothing will be published. Add --live to actually post.');
    info(backdate ? `Backdating on, granularity "${granularity}".` : 'Backdating off.');
    info('');
    for (const entry of batch) {
      info(`  ${entry.date.slice(0, 10)}  ${describe(entry)}`);
      info(`     ${firstLine(entry.message)}`);
    }
    info('');
    const minutes = Math.round(((batch.length - 1) * pauseSeconds) / 60);
    info(`  ${batch.length} post(s), ${pauseSeconds}s apart - about ${minutes} minute(s) in total.`);
    return;
  }

  const [pageId, token, pageName] = pageCredentials();
  info(`  Page: ${pageName} (${pageId})`);
  info(backdate ? `  Backdating on, granularity "${granularity}"` : '  Backdating off');
  info('');

  let published = 0;
  let withoutBackdate = 0;

  for (const [index, entry] of batch.entries()) {
    info(`[${index + 1}/${batch.length}] ${entry.date.slice(0, 10)}  ${describe(entry)}`);
    step(firstLine(entry.message));

    try {
      let created;

      for (let attempt = 0; ; attempt++) {
        try {
          created = await createPost(entry, { pageId, token });
          break;
        } catch (err) {
          const isThrottle = err instanceof FacebookError && THROTTLE_CODES.has(err.code);
          if (isThrottle && attempt < 2) {
            warn(`Facebook is throttling (code ${err.code}). Waiting ${Math.round(throttleWait / 60)} min.`);
            await sleep(throttleWait * 1000);
            continue;
          }
          throw err;
        }
      }

      // Recorded the moment it exists, before anything else is attempted. Dating it and finding
      // its post id can both fail; what must not happen is a post standing on the Page with no
      // record of it, because the next run would publish it a second time.
      posted.posted[entry.id] = {
        fb_post_id: created.id,
        kind: created.kind,
        published_at: new Date().toISOString(),
        backdated_to: null,
        x_url: entry.x_url,
        date: entry.date,
      };
      writeJson(POSTED_FB_FILE, posted);
      published++;
      ok(`published as ${created.id}`);

      // Everything from here on is refinement of a post that is already out, so a failure warns
      // and moves on rather than ending the run.
      let postId = created.id;

      // Backdating exists to put the 2026 archive back in chronological order. A post written
      // today has no earlier date to restore - and a backdated post stays out of followers' feeds,
      // which is what the backfill wanted and the opposite of what a new post wants.
      const composed = entry.source === 'composed';

      try {
        if (created.kind === 'video') {
          const resolved = await resolveVideoPost(created.id, { pageId, token });
          if (resolved) {
            postId = resolved;
            posted.posted[entry.id].video_id = created.id;
            posted.posted[entry.id].fb_post_id = resolved;
          } else {
            postId = null;
            warn('Could not find the feed post for this video; it stays undated.');
          }
        }

        if (composed) step('composed today, so not backdated');

        if (backdate && postId && !composed) {
          const dated = await backdatePost(postId, entry, { token, granularity });
          posted.posted[entry.id].backdated_to = dated ? entry.date : null;
          if (dated) {
            step(`dated to ${entry.date.slice(0, 10)}`);
          } else {
            warn('Facebook reported success but the date did not stick.');
          }
        }
      } catch (err) {
        warn(`Post is out, but the follow-up failed: ${err.message.split('\n')[0]}`);
      }

      if (backdate && !composed && !posted.posted[entry.id].backdated_to) withoutBackdate++;
      writeJson(POSTED_FB_FILE, posted);
    } catch (err) {
      if (err instanceof FacebookError && err.code === PAGE_BLOCK_CODE) {
        fail(`${entry.id}: ${err.message}`);
        warn('Facebook has temporarily blocked the Page from posting. Wait several hours.');
        warn(`${published} post(s) went out this run.`);
        process.exit(1);
      }
      if (err instanceof FacebookError && err.status >= 400 && err.status < 500) {
        fail(`${entry.id}: ${err.message}`);
        warn(`Stopping. ${published} post(s) went out; the rest are untouched.`);
        process.exit(1);
      }
      if (err.networkFailure) {
        fail(`${entry.id}: ${err.message}`);
        warn(`Stopping. ${published} post(s) went out this run.`);
        process.exit(1);
      }
      throw err;
    }

    if (index < batch.length - 1) {
      step(`waiting ${pauseSeconds}s`);
      await sleep(pauseSeconds * 1000);
    }
  }

  info('');
  ok(`${published} post(s) published to ${pageName}. Record in ${toRelative(POSTED_FB_FILE)}`);
  if (withoutBackdate) {
    warn(`${withoutBackdate} of them would not take backdating and carry today's date.`);
  }
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
