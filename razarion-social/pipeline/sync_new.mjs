#!/usr/bin/env node
// Step 6: pick up whatever @razariongame has posted on X since the last run and put it through the
// same pipeline. Runnable from cron.
//
//   node sync_new.mjs                 # fetch, caption, render, upload - then stop for review
//   node sync_new.mjs --auto          # additionally mark unflagged new posts as ok
//   node sync_new.mjs --auto --publish  # ...and publish them
//
// Publishing is off by default, and so is --auto. An unattended job that posts whatever it finds,
// unread, is how an account ends up publishing a reply fragment or a link-only post that says
// nothing. The default is to prepare everything and leave the last word to a person.

import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import {
  POSTS_FILE, CAPTIONS_FILE, SYNC_FILE, STATE_DIR, POSTED_FILE,
  ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const HERE = dirname(fileURLToPath(import.meta.url));
const DEFAULT_PAUSE = '180';

// Runs the steps as their own processes rather than importing them. Each one already writes its
// result to disk and prints what it did, so a cron log reads exactly like a manual run, and a step
// that fails stops the chain with its own message.
function run(script, args = []) {
  step(`${script} ${args.join(' ')}`);
  const result = spawnSync(process.execPath, [join(HERE, script), ...args], {
    stdio: 'inherit',
    cwd: HERE,
  });
  if (result.status !== 0) {
    throw new Error(`${script} exited with code ${result.status}`);
  }
}

/**
 * Where to resume from.
 *
 * The last run's own record first, then the newest post already in captions.json. One second is
 * added because start_time is inclusive and re-fetching the last post would cost a read and change
 * nothing.
 */
function resumeFrom(args) {
  if (args.since) return args.since;

  const state = readJson(SYNC_FILE);
  if (state && state.last_post_date) {
    return new Date(Date.parse(state.last_post_date) + 1000).toISOString();
  }

  const captions = readJson(CAPTIONS_FILE);
  if (captions && captions.captions.length) {
    const newest = captions.captions.reduce(
      (max, entry) => Math.max(max, Date.parse(entry.date)),
      0
    );
    return new Date(newest + 1000).toISOString();
  }

  throw new Error('Nothing to resume from. Run fetch_posts.mjs once, or pass --since.');
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const auto = Boolean(args.auto);
  const publish = Boolean(args.publish);
  const pause = args.pause ? String(args.pause) : DEFAULT_PAUSE;

  const since = resumeFrom(args);
  info(`Syncing posts newer than ${since}`);

  const before = new Set(((readJson(CAPTIONS_FILE) || { captions: [] }).captions || []).map((c) => c.id));

  run('fetch_posts.mjs', ['--since', since, '--merge']);

  const posts = readJson(POSTS_FILE);
  const all = (posts && posts.posts) || [];
  // After a merge, posts.json holds the whole history again, so its length says nothing about what
  // this run found. fetched_now is what actually came back from X.
  const freshCount = posts && posts.fetched_now ? posts.fetched_now.posts : all.length;
  if (!freshCount) {
    info('');
    ok('Nothing new on X.');
    writeState(since);
    return;
  }

  run('build_captions.mjs');
  run('render_cards.mjs');

  const doc = readJson(CAPTIONS_FILE);
  const added = doc.captions.filter((entry) => !before.has(entry.id));

  if (auto) {
    // Only the new ones, and only the ones nothing was flagged on. A flag means a human has to
    // look - that is what it is for.
    let promoted = 0;
    for (const entry of added) {
      if (entry.status === 'review' && entry.flags.length === 0) {
        entry.status = 'ok';
        promoted++;
      }
    }
    writeJson(CAPTIONS_FILE, doc);
    info(`--auto: ${promoted} of ${added.length} new post(s) marked ok; the rest carry flags.`);
  }

  run('upload_media.mjs');

  if (publish) {
    run('publish.mjs', ['--live', '--pause', pause]);
  }

  writeState(all[all.length - 1].date);

  const waiting = readJson(CAPTIONS_FILE).captions.filter((e) => e.status === 'review');
  const posted = readJson(POSTED_FILE, { posted: {} });
  const ready = readJson(CAPTIONS_FILE).captions.filter(
    (e) => e.status === 'ok' && !posted.posted[e.id]
  );

  info('');
  ok(`${added.length} new post(s) from X`);
  info(`  waiting for your review: ${waiting.length}`);
  info(`  marked ok, not yet published: ${ready.length}`);
  if (!publish && ready.length) {
    info('  Publish them with: node publish.mjs --live');
  }
}

function writeState(lastPostDate) {
  ensureDir(STATE_DIR);
  writeJson(SYNC_FILE, {
    last_post_date: lastPostDate,
    last_run: new Date().toISOString(),
  });
}

try {
  main();
} catch (err) {
  fail(err.message);
  process.exit(1);
}
