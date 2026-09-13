#!/usr/bin/env node
// Writes one new post into the review files of every feed.
//
// The rest of the pipeline mirrors what X already carries. This is the other direction: a clip or
// an image plus a couple of sentences, turned into the shapes the networks want, waiting for
// approval like everything else.
//
//   node compose.mjs --media data/clips/harvester.mp4 --text "Der Harvester sammelt jetzt..."
//   node compose.mjs --portrait data/clips/badger-portrait.mp4 --landscape data/clips/badger-landscape.mp4 --text "..."
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
import { probeVideo } from './lib/video.mjs';
import { DATA_DIR, ensureDir, toRelative } from './lib/paths.mjs';
import { buildEntries, writeEntries, xLength } from './lib/entries.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const OWN_MEDIA_DIR = join(DATA_DIR, 'own');

const MAX_X = 280;
const MAX_IG = 2200;

function videoLike(file) {
  return ['.mp4', '.mov', '.webm'].includes(extname(file).toLowerCase());
}

/**
 * Copy one media file next to the review files. Copied rather than referenced: the review files
 * point at paths that have to still be there when publishing happens, which may be days later and
 * after any amount of tidying up.
 */
function adopt(id, source) {
  if (!existsSync(source)) throw new Error(`No such file: ${source}`);
  ensureDir(OWN_MEDIA_DIR);
  const target = join(OWN_MEDIA_DIR, `${id}-${basename(source)}`);
  if (!existsSync(target)) copyFileSync(source, target);
  step(`media: ${toRelative(target)} (${(statSync(target).size / 1024 / 1024).toFixed(1)} MB)`);
  return toRelative(target);
}

/**
 * A clip as a media item with a master per shape.
 *
 * Reels and Shorts are cut from the portrait master, X from the landscape one. A clip handed over
 * with --media is measured and filed under whichever shape it has; the other shape is then cropped
 * from it at upload time, and the note says what that costs.
 */
async function clipItem(id, { media, portrait, landscape }) {
  const item = { type: 'video', file: null, portrait: null, landscape: null, url: null };
  const given = [['portrait', portrait], ['landscape', landscape], [null, media]].filter(([, f]) => f);

  for (const [declared, source] of given) {
    const probe = await probeVideo(source);
    if (!probe) throw new Error(`ffprobe cannot read ${source} - is it a valid video?`);
    const shape = probe.width < probe.height ? 'portrait' : 'landscape';
    if (declared && declared !== shape) {
      warn(`${source} is ${probe.width}x${probe.height}, not ${declared}. Filed as ${shape}.`);
    }
    if (item[shape]) throw new Error(`Two ${shape} clips given (${source}). Pass one of each shape.`);
    item[shape] = adopt(id, String(source));
  }

  item.file = item.portrait || item.landscape;
  if (!item.portrait) warn('No portrait clip: Instagram, Facebook and YouTube get the middle of the landscape one.');
  if (!item.landscape) warn('No landscape clip: X gets the middle of the portrait one.');
  return item;
}

async function main() {
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
  const mediaArg = args.media ? String(args.media) : null;
  const portrait = args.portrait ? String(args.portrait) : null;
  const landscape = args.landscape ? String(args.landscape) : null;
  if ((portrait || landscape) && mediaArg && !videoLike(mediaArg)) {
    throw new Error('A post carries either a picture or a clip. Drop --media, or drop --portrait/--landscape.');
  }
  if (portrait || landscape || (mediaArg && videoLike(mediaArg))) {
    media = [await clipItem(id, { media: mediaArg, portrait, landscape })];
  } else if (mediaArg) {
    media = [{ type: 'photo', file: adopt(id, mediaArg), url: null }];
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

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
