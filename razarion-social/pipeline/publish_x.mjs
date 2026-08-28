#!/usr/bin/env node
// Publishes the reviewed entries in x_posts.json to X, oldest first.
//
// A dry run unless --live is passed. X bills per post - $0.015, or $0.20 once a link is in the
// text - so the estimate is printed before anything is spent, and again as a total afterwards.
//
//   node publish_x.mjs                    # what it would do, and what it would cost
//   node publish_x.mjs --live --limit 1
//   node publish_x.mjs --live

import { existsSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import {
  PIPELINE_ROOT, X_POSTS_FILE, POSTED_X_FILE, STATE_DIR,
  ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { postToX, estimateCost } from '../src/platforms/x.mjs';
import { sleep } from '../src/util/http.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_PAUSE_SECONDS = 60;

function firstLine(text) {
  const line = (text || '').split('\n')[0];
  return line.length > 76 ? line.slice(0, 73) + '...' : line;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const live = Boolean(args.live);
  const limit = args.limit ? Number(args.limit) : Infinity;
  const pauseSeconds = args.pause ? Number(args.pause) : DEFAULT_PAUSE_SECONDS;

  const doc = readJson(X_POSTS_FILE);
  if (!doc) throw new Error(`No ${toRelative(X_POSTS_FILE)}. Run: node compose.mjs`);

  ensureDir(STATE_DIR);
  const posted = readJson(POSTED_X_FILE, { posted: {} });

  const queue = doc.posts
    .filter((e) => e.status === 'ok')
    .filter((e) => !posted.posted[e.id])
    .sort((a, b) => Date.parse(a.date) - Date.parse(b.date));

  info(`${doc.posts.filter((e) => e.status === 'ok').length} entries marked ok`);
  info(`  already published: ${Object.keys(posted.posted).length}`);
  info(`  ready to publish:  ${queue.length}`);
  if (!queue.length) {
    info('');
    info('Nothing to do. Set status "ok" in x_posts.json.');
    return;
  }

  const batch = queue.slice(0, limit === Infinity ? queue.length : limit);
  const estimate = batch.reduce((sum, e) => sum + estimateCost(e.text), 0);

  if (!live) {
    info('');
    warn('DRY RUN. Nothing will be published. Add --live to actually post.');
    info('');
    for (const entry of batch) {
      const media = (entry.media || []).length ? ` + ${entry.media[0].type}` : '';
      info(`  ${entry.date.slice(0, 10)}  $${estimateCost(entry.text).toFixed(3)}${media}`);
      info(`     ${firstLine(entry.text)}`);
    }
    info('');
    info(`  ${batch.length} post(s), about $${estimate.toFixed(2)} in total.`);
    return;
  }

  warn(`About to post ${batch.length} time(s) to X, costing roughly $${estimate.toFixed(2)}.`);
  info('');

  let published = 0;
  let spent = 0;

  for (const [index, entry] of batch.entries()) {
    info(`[${index + 1}/${batch.length}] ${entry.date.slice(0, 10)}`);
    step(firstLine(entry.text));

    // X takes the file itself rather than a URL, so the local copy is what matters here - the
    // uploads to GitHub that Instagram and Facebook need are irrelevant on this side.
    const item = (entry.media || [])[0];
    const spec = { x: { text: entry.text, attachVideo: Boolean(item) } };
    if (item) {
      const file = join(PIPELINE_ROOT, item.file);
      if (!existsSync(file)) {
        fail(`${entry.id}: ${item.file} is missing on disk.`);
        warn(`Stopping. ${published} post(s) went out.`);
        process.exit(1);
      }
      spec.video = file;
      spec.videoSize = statSync(file).size;
      spec.x.mediaCategory = item.type === 'photo' ? 'tweet_image' : 'tweet_video';
    }

    try {
      const result = await postToX(spec, { dryRun: false });
      posted.posted[entry.id] = {
        x_id: result.id,
        url: result.url,
        published_at: new Date().toISOString(),
        cost: result.cost,
        date: entry.date,
      };
      // Written after each post: X charges per call, so a crash must never lead to paying twice
      // for the same text.
      writeJson(POSTED_X_FILE, posted);
      published++;
      spent += result.cost || 0;
    } catch (err) {
      fail(`${entry.id}: ${err.message}`);
      warn(`Stopping. ${published} post(s) went out, about $${spent.toFixed(2)} spent.`);
      process.exit(1);
    }

    if (index < batch.length - 1) {
      step(`waiting ${pauseSeconds}s`);
      await sleep(pauseSeconds * 1000);
    }
  }

  info('');
  ok(`${published} post(s) published to X, about $${spent.toFixed(2)}. Record in ${toRelative(POSTED_X_FILE)}`);
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
