#!/usr/bin/env node
// Writes one new post into the review files of every feed.
//
// The rest of the pipeline mirrors what X already carries. This is the other direction: a clip or
// an image plus a couple of sentences, turned into the shapes the networks want, waiting for
// approval like everything else.
//
//   node compose.mjs --media data/clips/harvester.mp4 --text "Der Harvester sammelt jetzt..."
//   node compose.mjs --text "Nur Text" --link https://www.razarion.com
//   node compose.mjs --media shot.jpg --text "..." --tags "harvester,economy"
//
// Nothing is published here. The entries land on status "review" in captions.json, fb_posts.json,
// x_posts.json and - for a clip - yt_posts.json; the publishers take it from there.
//
// The shaping itself lives in lib/entries.mjs and is shared with generate.mjs. It used to be
// duplicated here, and the copies drifted: this one kept putting the link into the X text for
// weeks after the shared version had stopped, at thirteen times the price per post.

import { existsSync, copyFileSync, statSync } from 'node:fs';
import { basename, join, extname } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { DATA_DIR, ensureDir, toRelative } from './lib/paths.mjs';
import { buildEntries, writeEntries, xLength } from './lib/entries.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const OWN_MEDIA_DIR = join(DATA_DIR, 'own');

const MAX_X = 280;
const MAX_IG = 2200;

function videoLike(file) {
  return ['.mp4', '.mov', '.webm'].includes(extname(file).toLowerCase());
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const text = args.text ? String(args.text).trim() : '';
  if (!text) throw new Error('Nothing to say. Pass --text "..."');

  const link = args.link ? String(args.link) : null;
  const extraTags = args.tags ? String(args.tags).split(',').map((t) => t.trim()).filter(Boolean) : [];
  const when = args.date ? new Date(args.date) : new Date();
  if (Number.isNaN(when.getTime())) throw new Error(`Not a date: ${args.date}`);

  // A stable, sortable id that cannot collide with an X post id, so the merge in build_captions
  // and build_fb_posts keeps these entries instead of treating them as vanished.
  const id = 'own-' + when.toISOString().replace(/[-:T]/g, '').slice(0, 14);

  let media = [];
  if (args.media) {
    const source = String(args.media);
    if (!existsSync(source)) throw new Error(`No such file: ${source}`);
    ensureDir(OWN_MEDIA_DIR);
    // Copied rather than referenced: the review files point at paths that have to still be there
    // when publishing happens, which may be days later and after any amount of tidying up.
    const target = join(OWN_MEDIA_DIR, `${id}-${basename(source)}`);
    if (!existsSync(target)) copyFileSync(source, target);
    media = [{ type: videoLike(source) ? 'video' : 'photo', file: toRelative(target), url: null }];
    step(`media: ${toRelative(target)} (${(statSync(target).size / 1024 / 1024).toFixed(1)} MB)`);
  }

  const entries = buildEntries({
    id,
    date: when.toISOString(),
    text,
    link,
    tags: extraTags,
    media,
    source: 'composed',
  });

  const written = writeEntries(entries);

  const { x: xFlags, ig: igFlags } = entries.flags;
  const lengths = entries.lengths;

  info('');
  ok(`Composed ${id} into ${written.length} review file(s): ${written.join(', ')}.`);
  info(`  X          ${lengths.x}/${MAX_X} characters${xFlags.length ? '  [' + xFlags.join(',') + ']' : ''}`);
  info(`  Instagram  ${lengths.ig}/${MAX_IG} characters${igFlags.length ? '  [' + igFlags.join(',') + ']' : ''}`);
  info(`  Facebook   ${lengths.fb} characters`);
  if (entries.yt) {
    info(`  YouTube    ${entries.yt.title}`);
    if (entries.yt.flags.includes('title-truncated')) {
      warn('  The YouTube title was cut to fit 70 characters. Rewrite it before approving.');
    }
  } else {
    info('  YouTube    skipped - it takes video only');
  }
  info('');
  if (igFlags.includes('needs-card')) info('  Text only: run render_cards.mjs so Instagram has something to show.');
  if (xFlags.includes('too-long')) warn('  The X text is over 280 characters as X counts them. Shorten it before approving.');
  info('  Read them, set status to "ok", then: node upload_media.mjs && node publish.mjs');
}

try {
  main();
} catch (err) {
  fail(err.message);
  process.exit(1);
}
