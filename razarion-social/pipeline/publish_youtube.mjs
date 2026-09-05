#!/usr/bin/env node
// Publishes the reviewed entries in yt_posts.json to the Razarion channel, oldest first.
//
// A dry run unless --live is passed, like every other publisher here.
//
//   node publish_youtube.mjs                    # what it would upload, and how it would be filed
//   node publish_youtube.mjs --live --limit 1
//   node publish_youtube.mjs --live
//
// Two things about YouTube that the other three do not have:
//
// Privacy. An OAuth client that has not passed Google's compliance audit may only create private
// videos. A public upload is not refused - it is silently reset - so an entry asks for private and
// this says what has to happen afterwards. YOUTUBE-AUDIT.md carries the state of that.
//
// Quota. An upload costs 1600 units against a default of 10000 a day, so six is the ceiling
// whatever the queue holds. Nothing here can read the remaining quota, so the count is reported
// and the limit is left to the operator.

import { existsSync, statSync } from 'node:fs';
import { basename, join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import {
  PIPELINE_ROOT, YT_POSTS_FILE, POSTED_YT_FILE, STATE_DIR,
  ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { probeVideo } from './lib/video.mjs';
import { becomesShort } from './lib/youtube.mjs';
import { postToYouTube } from '../src/platforms/youtube.mjs';
import { sleep } from '../src/util/http.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_PAUSE_SECONDS = 30;
const QUOTA_PER_UPLOAD = 1600;
const DAILY_QUOTA = 10000;

function short(text, width = 76) {
  const line = (text || '').split('\n')[0];
  return line.length > width ? line.slice(0, width - 3) + '...' : line;
}

/**
 * Describes what YouTube will make of the file, for the dry run.
 *
 * Nothing is declared to YouTube about Shorts - it sorts on the file itself, at a ratio of 1.05 or
 * under and at most 180 seconds - so this is reported and never sent. It is worth reporting
 * because it decides which surface the clip lands on, which is the whole reason the reel format
 * exists upstream.
 */
async function describe(file) {
  const probe = await probeVideo(file);
  if (!probe) return { line: 'ffprobe cannot read this file', short: false };
  const isShort = becomesShort({
    width: probe.width, height: probe.height, durationSeconds: probe.duration,
  });
  const size = (statSync(file).size / 1024 / 1024).toFixed(1);
  return {
    line: `${probe.width}x${probe.height}, ${probe.duration.toFixed(1)}s, ${size} MB` +
      (isShort ? ' - files as a Short' : ' - files as a normal video'),
    short: isShort,
  };
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const live = Boolean(args.live);
  const limit = args.limit ? Number(args.limit) : Infinity;
  const pauseSeconds = args.pause ? Number(args.pause) : DEFAULT_PAUSE_SECONDS;

  const doc = readJson(YT_POSTS_FILE);
  if (!doc) throw new Error(`No ${toRelative(YT_POSTS_FILE)}. Run: node compose.mjs with a clip.`);

  ensureDir(STATE_DIR);
  const posted = readJson(POSTED_YT_FILE, { posted: {} });

  const all = doc.videos || [];
  const queue = all
    .filter((e) => e.status === 'ok')
    .filter((e) => !posted.posted[e.id])
    .sort((a, b) => Date.parse(a.date) - Date.parse(b.date));

  info(`${all.filter((e) => e.status === 'ok').length} entries marked ok`);
  info(`  already published: ${Object.keys(posted.posted).length}`);
  info(`  ready to publish:  ${queue.length}`);
  if (!queue.length) {
    info('');
    info(`Nothing to do. Set status "ok" in ${toRelative(YT_POSTS_FILE)}.`);
    return;
  }

  const batch = queue.slice(0, limit === Infinity ? queue.length : limit);
  const units = batch.length * QUOTA_PER_UPLOAD;
  if (units > DAILY_QUOTA) {
    warn(`${batch.length} uploads cost ${units} quota units against a daily ${DAILY_QUOTA}. ` +
      `Use --limit ${Math.floor(DAILY_QUOTA / QUOTA_PER_UPLOAD)} or fewer.`);
  }

  if (!live) {
    info('');
    warn('DRY RUN. Nothing will be uploaded. Add --live to actually publish.');
    info('');
    for (const entry of batch) {
      const item = (entry.media || [])[0];
      const file = item ? join(PIPELINE_ROOT, item.file) : null;
      info(`  ${entry.date.slice(0, 10)}  ${entry.privacy || 'private'}`);
      info(`     ${short(entry.title)}`);
      if (!file || !existsSync(file)) {
        fail(`     ${item ? item.file : 'no media'} is missing on disk.`);
      } else {
        const d = await describe(file);
        info(`     ${basename(file)}: ${d.line}`);
      }
      if ((entry.flags || []).length) warn(`     flags: ${entry.flags.join(', ')}`);
    }
    info('');
    info(`  ${batch.length} upload(s), ${units} of ${DAILY_QUOTA} quota units.`);
    return;
  }

  info('');
  let published = 0;

  for (const [index, entry] of batch.entries()) {
    info(`[${index + 1}/${batch.length}] ${entry.date.slice(0, 10)}`);
    step(short(entry.title));

    const item = (entry.media || [])[0];
    const file = item ? join(PIPELINE_ROOT, item.file) : null;
    if (!file || !existsSync(file)) {
      fail(`${entry.id}: ${item ? item.file : 'no media'} is missing on disk.`);
      warn(`Stopping. ${published} upload(s) went out.`);
      process.exit(1);
    }

    // The master goes up unchanged. YouTube re-encodes everything it is given, so converting first
    // would only throw detail away, and unlike the reel feeds it has no shape to satisfy - a
    // landscape clip stays landscape and a portrait one becomes a Short on its own.
    const spec = {
      video: file,
      videoSize: statSync(file).size,
      youtube: {
        title: entry.title,
        description: entry.description,
        tags: entry.tags || [],
        privacy: entry.privacy || 'private',
      },
    };

    try {
      const result = await postToYouTube(spec, { dryRun: false });
      posted.posted[entry.id] = {
        video_id: result.id,
        url: result.url,
        privacy: spec.youtube.privacy,
        published_at: new Date().toISOString(),
        date: entry.date,
      };
      // Written after each upload. An upload costs 1600 quota units and a crash must never lead to
      // spending them twice on the same clip - or, worse, to two copies on the channel.
      writeJson(POSTED_YT_FILE, posted);
      published++;
    } catch (err) {
      fail(`${entry.id}: ${err.message}`);
      warn(`Stopping. ${published} upload(s) went out.`);
      process.exit(1);
    }

    if (index < batch.length - 1) {
      step(`waiting ${pauseSeconds}s`);
      await sleep(pauseSeconds * 1000);
    }
  }

  info('');
  ok(`${published} video(s) uploaded. Record in ${toRelative(POSTED_YT_FILE)}`);
  if (batch.some((e) => (e.privacy || 'private') === 'private')) {
    warn('They are private. Publish them in YouTube Studio, or pass the compliance audit and');
    warn('set "privacy": "public" in the entries - see razarion-social/YOUTUBE-AUDIT.md.');
  }
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
