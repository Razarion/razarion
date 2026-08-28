#!/usr/bin/env node
// Step 6: pick up whatever @razariongame has posted on X since the last run and put it through the
// pipeline for every network. Runnable from a scheduled task.
//
//   node sync_new.mjs                     # fetch, prepare both networks - then stop for review
//   node sync_new.mjs --auto              # additionally mark unflagged new posts as ok
//   node sync_new.mjs --auto --publish    # ...and publish them
//   node sync_new.mjs --only ig           # one network instead of both
//
// Publishing is off by default, and so is --auto. An unattended job that posts whatever it finds,
// unread, is how an account ends up publishing a reply fragment or a link-only post that says
// nothing. The default is to prepare everything and leave the last word to a person.

import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import {
  POSTS_FILE, CAPTIONS_FILE, FB_POSTS_FILE, X_POSTS_FILE, SYNC_FILE, STATE_DIR,
  POSTED_FILE, POSTED_FB_FILE, POSTED_X_FILE,
  ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const HERE = dirname(fileURLToPath(import.meta.url));
const DEFAULT_PAUSE = '180';

/**
 * The two networks, reduced to what differs between them.
 *
 * Instagram needs a card rendered for text-only posts and its media converted; Facebook takes the
 * originals and needs neither. Everything else is the same three steps, so they are described
 * rather than duplicated.
 */
const NETWORKS = {
  ig: {
    label: 'Instagram',
    file: CAPTIONS_FILE,
    listKey: 'captions',
    state: POSTED_FILE,
    prepare: [['build_captions.mjs', []], ['render_cards.mjs', []]],
    upload: ['upload_media.mjs', []],
    publish: 'publish.mjs',
  },
  fb: {
    label: 'Facebook',
    file: FB_POSTS_FILE,
    listKey: 'posts',
    state: POSTED_FB_FILE,
    prepare: [['build_fb_posts.mjs', []]],
    upload: ['upload_media.mjs', ['--source', 'fb']],
    publish: 'publish_fb.mjs',
  },
  // X has no prepare or upload step: nothing is mirrored back to where it came from, and the only
  // entries here are the ones compose.mjs wrote, which already carry their text and attach their
  // media from disk rather than from a URL.
  x: {
    label: 'X',
    file: X_POSTS_FILE,
    listKey: 'posts',
    state: POSTED_X_FILE,
    prepare: [],
    upload: null,
    publish: 'publish_x.mjs',
  },
};

// Runs the steps as their own processes rather than importing them. Each one already writes its
// result to disk and prints what it did, so a scheduled log reads exactly like a manual run, and a
// step that fails stops the chain with its own message.
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
 * The last run's own record first, then the newest post already prepared. One second is added
 * because start_time is inclusive and re-fetching the last post would cost a read and change
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
    const newest = captions.captions.reduce((max, e) => Math.max(max, Date.parse(e.date)), 0);
    return new Date(newest + 1000).toISOString();
  }

  throw new Error('Nothing to resume from. Run fetch_posts.mjs once, or pass --since.');
}

function idsIn(file, listKey) {
  const doc = readJson(file);
  return new Set(((doc && doc[listKey]) || []).map((e) => e.id));
}

/**
 * Promotes the new entries nothing was flagged on.
 *
 * Only the ones this run added, and only when they carry no flags at all - a flag means a human
 * has to look, which is what it is for.
 */
function autoApprove(network, before) {
  const doc = readJson(network.file);
  if (!doc) return 0;
  let promoted = 0;
  for (const entry of doc[network.listKey]) {
    if (before.has(entry.id)) continue;
    if (entry.status === 'review' && entry.flags.length === 0) {
      entry.status = 'ok';
      promoted++;
    }
  }
  writeJson(network.file, doc);
  return promoted;
}

function pending(network) {
  const doc = readJson(network.file);
  const posted = readJson(network.state, { posted: {} });
  const list = (doc && doc[network.listKey]) || [];
  return {
    review: list.filter((e) => e.status === 'review').length,
    ready: list.filter((e) => e.status === 'ok' && !posted.posted[e.id]).length,
  };
}

function writeState(lastPostDate) {
  ensureDir(STATE_DIR);
  writeJson(SYNC_FILE, { last_post_date: lastPostDate, last_run: new Date().toISOString() });
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const auto = Boolean(args.auto);
  const publish = Boolean(args.publish);
  const pause = args.pause ? String(args.pause) : DEFAULT_PAUSE;
  const limit = args.limit ? String(args.limit) : null;

  const selected = args.only ? String(args.only).split(',') : Object.keys(NETWORKS);
  for (const key of selected) {
    if (!NETWORKS[key]) throw new Error(`Unknown network "${key}". Use ig, fb, or both.`);
  }

  const since = resumeFrom(args);
  info(`Syncing posts newer than ${since}`);

  const before = new Map(
    selected.map((key) => [key, idsIn(NETWORKS[key].file, NETWORKS[key].listKey)])
  );

  run('fetch_posts.mjs', ['--since', since, '--merge']);

  const posts = readJson(POSTS_FILE);
  const all = (posts && posts.posts) || [];

  // Resume from the newest thing seen, kept or not. A run that finds only replies to other
  // accounts still has to move the mark forward - otherwise every future run re-reads those same
  // replies, and reads are billed.
  const newestSeen = [...all, ...((posts && posts.skipped) || [])]
    .map((p) => p.date)
    .sort()
    .pop() || since;

  // After a merge, posts.json holds the whole history again, so its length says nothing about what
  // this run found. fetched_now is what actually came back from X.
  const freshCount = posts && posts.fetched_now ? posts.fetched_now.posts : all.length;
  if (!freshCount) {
    info('');
    ok('Nothing new worth mirroring from X.');
    writeState(newestSeen);

    // Nothing to prepare does not mean nothing to deliver. Posts written with compose.mjs sit in
    // the same review files and have no counterpart on X at all, so an empty fetch is the normal
    // case for them rather than a reason to stop.
    if (publish) {
      for (const key of selected) {
        const network = NETWORKS[key];
        const waiting = pending(network).ready;
        if (!waiting) continue;
        info('');
        info(`--- ${network.label}: ${waiting} approved and waiting ---`);
        const publishArgs = ['--live', '--pause', pause];
        if (limit) publishArgs.push('--limit', limit);
        run(network.publish, publishArgs);
      }
    }
    return;
  }

  const added = {};
  for (const key of selected) {
    const network = NETWORKS[key];
    info('');
    info(`--- ${network.label} ---`);

    for (const [script, extra] of network.prepare) run(script, extra);

    if (auto) {
      const promoted = autoApprove(network, before.get(key));
      info(`--auto: ${promoted} new post(s) marked ok; anything flagged waits.`);
    }

    if (network.upload) run(network.upload[0], network.upload[1]);

    if (publish) {
      const publishArgs = ['--live', '--pause', pause];
      if (limit) publishArgs.push('--limit', limit);
      run(network.publish, publishArgs);
    }

    const counts = pending(network);
    added[key] = counts;
  }

  writeState(newestSeen);

  info('');
  ok(`${freshCount} new post(s) from X`);
  for (const key of selected) {
    const counts = added[key];
    info(`  ${NETWORKS[key].label}: ${counts.review} awaiting review, ${counts.ready} approved and unpublished`);
  }
  if (!publish) info('  Publish with: publish.mjs / publish_fb.mjs / publish_x.mjs --live');
}

try {
  main();
} catch (err) {
  fail(err.message);
  process.exit(1);
}
