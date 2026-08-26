#!/usr/bin/env node
// Step 5: publish the reviewed posts to Instagram, oldest first.
//
// A dry run unless --live is passed. Publishing is not undoable and the account is public, so the
// safe path is the default one.
//
//   node publish.mjs                      # dry run: says what it would do, does nothing
//   node publish.mjs --live --limit 1     # one real post, to see it land
//   node publish.mjs --live               # the rest, 60s apart
//   node publish.mjs --live --pause 120   # slower, if Instagram starts complaining

import { parseArgs } from './lib/args.mjs';
import {
  CAPTIONS_FILE, POSTED_FILE, STATE_DIR, ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import {
  credentials, post, publishingLimit, waitForContainer, InstagramError,
} from './lib/instagram.mjs';
import { sleep } from '../src/util/http.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_PAUSE_SECONDS = 60;
const MAX_CAROUSEL = 10;

// Meta throttles an app at roughly 200 calls per hour when it has a single user, and one post
// costs four to eight of them. That ceiling is separate from the 100-posts-per-24h publishing
// quota and is hit long before it. These are the codes that mean "too fast", not "wrong".
const THROTTLE_CODES = new Set([4, 17, 32, 613]);

// Code 9 is the account itself being told to slow down, which no amount of waiting inside one run
// will fix.
const ACTION_BLOCK_CODE = 9;
const DEFAULT_THROTTLE_WAIT_SECONDS = 15 * 60;

function describe(entry) {
  const media = entry.media || [];
  if (media.length > 1) return `carousel of ${media.length}`;
  if (media.length === 1) return media[0].type === 'photo' ? 'image' : 'reel';
  return 'nothing to post';
}

function firstLine(caption) {
  const line = caption.split('\n')[0];
  return line.length > 76 ? line.slice(0, 73) + '...' : line;
}

/**
 * One post, in the two steps the API insists on: build a container, then publish it.
 *
 * A carousel needs a third layer - one container per child, each marked is_carousel_item, then a
 * CAROUSEL container holding their ids. Videos have to finish processing before any of that can be
 * published, which is what the waiting is for.
 */
async function publishEntry(entry, { userId, token }) {
  const media = entry.media.filter((m) => m.url);
  if (!media.length) throw new Error('no uploaded media - run upload_media.mjs first');

  if (media.length === 1) {
    const item = media[0];
    const params =
      item.type === 'photo'
        ? { image_url: item.url, caption: entry.caption, access_token: token }
        : {
            media_type: 'REELS',
            video_url: item.url,
            caption: entry.caption,
            share_to_feed: 'true',
            access_token: token,
          };

    const container = await post(`/${userId}/media`, params);
    // Images need this as much as videos do. The documentation presents processing as a video
    // concern, but an image container is IN_PROGRESS for a few seconds too, and publishing inside
    // that window fails with 9007 / 2207027 "Media ID is not available".
    step('waiting for Instagram to process the media');
    await waitForContainer(container.id, token);

    const published = await post(`/${userId}/media_publish`, {
      creation_id: container.id,
      access_token: token,
    });
    return published.id;
  }

  const children = [];
  for (const item of media.slice(0, MAX_CAROUSEL)) {
    const params = {
      is_carousel_item: 'true',
      access_token: token,
      ...(item.type === 'photo'
        ? { image_url: item.url }
        : { media_type: 'VIDEO', video_url: item.url }),
    };
    const child = await post(`/${userId}/media`, params);
    await waitForContainer(child.id, token);
    children.push(child.id);
  }

  const container = await post(`/${userId}/media`, {
    media_type: 'CAROUSEL',
    children: children.join(','),
    caption: entry.caption,
    access_token: token,
  });
  await waitForContainer(container.id, token);

  const published = await post(`/${userId}/media_publish`, {
    creation_id: container.id,
    access_token: token,
  });
  return published.id;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const live = Boolean(args.live);
  const limit = args.limit ? Number(args.limit) : Infinity;
  const pauseSeconds = args.pause ? Number(args.pause) : DEFAULT_PAUSE_SECONDS;
  const throttleWait = args['throttle-wait'] ? Number(args['throttle-wait']) : DEFAULT_THROTTLE_WAIT_SECONDS;

  const doc = readJson(CAPTIONS_FILE);
  if (!doc) throw new Error(`No ${toRelative(CAPTIONS_FILE)}. Run: node build_captions.mjs`);

  ensureDir(STATE_DIR);
  const posted = readJson(POSTED_FILE, { posted: {} });

  // Oldest first, so the profile ends up in the order the posts were written.
  const queue = doc.captions
    .filter((e) => e.status === 'ok')
    .filter((e) => !posted.posted[e.id])
    .filter((e) => (e.media || []).some((m) => m.url))
    .sort((a, b) => Date.parse(a.date) - Date.parse(b.date));

  const notReady = doc.captions.filter(
    (e) => e.status === 'ok' && !posted.posted[e.id] && !(e.media || []).some((m) => m.url)
  );

  info(`${doc.captions.filter((e) => e.status === 'ok').length} entries marked ok`);
  info(`  already published: ${Object.keys(posted.posted).length}`);
  info(`  ready to publish:  ${queue.length}`);
  if (notReady.length) warn(`  ${notReady.length} marked ok but their media is not uploaded yet`);
  if (!queue.length) {
    info('');
    info('Nothing to do. Set status "ok" in captions.json and run upload_media.mjs first.');
    return;
  }

  const batch = queue.slice(0, limit === Infinity ? queue.length : limit);

  if (!live) {
    info('');
    warn('DRY RUN. Nothing will be published. Add --live to actually post.');
    info('');
    for (const entry of batch) {
      info(`  ${entry.date.slice(0, 10)}  ${describe(entry)}`);
      info(`     ${firstLine(entry.caption)}`);
      for (const m of entry.media) info(`     ${m.type}: ${m.url || 'NOT UPLOADED'}`);
    }
    info('');
    const minutes = Math.round(((batch.length - 1) * pauseSeconds) / 60);
    info(`  ${batch.length} post(s), ${pauseSeconds}s apart - about ${minutes} minute(s) in total.`);
    return;
  }

  const [userId, token] = credentials();

  // The 100-per-24h window is shared with anything posted from the phone, so ask rather than
  // assume the whole quota is ours.
  const quota = await publishingLimit(userId, token);
  info(`  Instagram quota: ${quota.used}/${quota.total} used in the last 24h`);
  const room = quota.total - quota.used;
  if (room <= 0) {
    warn('Quota is spent. Try again once the 24-hour window has moved.');
    return;
  }
  const todo = batch.slice(0, room);
  if (todo.length < batch.length) {
    warn(`Only ${room} left in the window; ${batch.length - todo.length} will wait for the next run.`);
  }

  info('');
  let published = 0;
  for (const [index, entry] of todo.entries()) {
    info(`[${index + 1}/${todo.length}] ${entry.date.slice(0, 10)}  ${describe(entry)}`);
    step(firstLine(entry.caption));

    try {
      // A throttle is a "come back later", not a mistake, so it waits out the window instead of
      // ending the run. Everything already published stays recorded either way.
      let mediaId;
      for (let attempt = 0; ; attempt++) {
        try {
          mediaId = await publishEntry(entry, { userId, token });
          break;
        } catch (err) {
          if (!(err instanceof InstagramError) || !THROTTLE_CODES.has(err.code) || attempt >= 2) throw err;
          warn(`Meta is throttling (code ${err.code}). Waiting ${Math.round(throttleWait / 60)} min, then trying this post again.`);
          await sleep(throttleWait * 1000);
        }
      }

      posted.posted[entry.id] = {
        ig_media_id: mediaId,
        published_at: new Date().toISOString(),
        x_url: entry.x_url,
        date: entry.date,
      };
      // Written after every single post, not at the end: a crash halfway through must not cost the
      // record of what already went out, or the next run posts it again.
      writeJson(POSTED_FILE, posted);
      published++;
      ok(`published as ${mediaId}`);
    } catch (err) {
      // An action block is Instagram judging the account, not the request. It clears on its own
      // after hours, and retrying into it extends it - so this one stops and says to come back
      // later rather than being reported as a broken request.
      if (err instanceof InstagramError && err.code === ACTION_BLOCK_CODE) {
        fail(`${entry.id}: ${err.message}`);
        warn('The account is posting too fast for Instagram, and it has put a temporary block on it.');
        warn('Wait several hours - a day is safer - then continue with a longer --pause.');
        warn(`${published} post(s) went out this run. Nothing is lost; the queue picks up where it stopped.`);
        process.exit(1);
      }

      if (err instanceof InstagramError && err.status >= 400 && err.status < 500) {
        fail(`${entry.id}: ${err.message}`);
        warn('4xx means something is wrong with the request, the token or the media.');
        warn(`Stopping. ${published} post(s) went out; the rest are untouched.`);
        process.exit(1);
      }
      if (err.networkFailure) {
        fail(`${entry.id}: ${err.message}`);
        if (err.duringPublish) warn('Check the Instagram profile before re-running - this one may have gone out.');
        warn(`Stopping. ${published} post(s) went out this run; the rest are untouched.`);
        process.exit(1);
      }
      throw err;
    }

    if (index < todo.length - 1) {
      step(`waiting ${pauseSeconds}s`);
      await sleep(pauseSeconds * 1000);
    }
  }

  info('');
  ok(`${published} post(s) published. Record in ${toRelative(POSTED_FILE)}`);
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
