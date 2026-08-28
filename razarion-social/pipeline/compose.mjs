#!/usr/bin/env node
// Writes one new post into the review files of all three feeds.
//
// The rest of the pipeline mirrors what X already carries. This is the other direction: a clip or
// an image plus a couple of sentences, turned into the three shapes the networks want, waiting for
// approval like everything else.
//
//   node compose.mjs --media data/clips/harvester.mp4 --text "Der Harvester sammelt jetzt..."
//   node compose.mjs --text "Nur Text" --link https://www.razarion.com
//   node compose.mjs --media shot.jpg --text "..." --tags "harvester,economy"
//
// Nothing is published here. The entries land on status "review" in captions.json, fb_posts.json
// and x_posts.json; publish.mjs, publish_fb.mjs and publish_x.mjs take it from there.

import { existsSync, copyFileSync, statSync } from 'node:fs';
import { basename, join, extname } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import {
  PIPELINE_ROOT, DATA_DIR, CAPTIONS_FILE, FB_POSTS_FILE, X_POSTS_FILE,
  ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const OWN_MEDIA_DIR = join(DATA_DIR, 'own');

const MAX_X = 280;
const MAX_IG = 2200;

// The same six that fit the account everywhere else, so a composed post is not visibly different
// from a mirrored one.
const BASE_HASHTAGS = ['rts', 'indiedev', 'opensource', 'webassembly', 'browsergame', 'gamedev'];

// X counts every link as 23 characters regardless of its real length.
const X_LINK_WEIGHT = 23;

function xLength(text) {
  return text.replace(/https?:\/\/\S+/g, 'x'.repeat(X_LINK_WEIGHT)).length;
}

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

  const hashtags = [...BASE_HASHTAGS, ...extraTags].slice(0, 10).map((t) => '#' + t);

  // X: the link belongs in the text, and the budget is what X counts, not what the string is long.
  const xText = link ? `${text}\n\n${link}` : text;
  const xFlags = [];
  if (xLength(xText) > MAX_X) xFlags.push('too-long');
  if (!media.length) xFlags.push('text-only');

  // Instagram: links are dead in a caption, so the link becomes the bio pointer and the hashtags
  // go underneath. Without media the entry needs a card, which render_cards.mjs draws from this
  // caption.
  const igCaption = [text, link ? 'Link in bio.' : null, hashtags.join(' ')]
    .filter(Boolean)
    .join('\n\n');
  const igFlags = [];
  if (igCaption.length > MAX_IG) igFlags.push('too-long');
  if (!media.length) igFlags.push('needs-card');

  // Facebook: link stays inline and clickable, nothing appended.
  const fbMessage = link ? `${text}\n\n${link}` : text;

  const common = {
    id,
    date: when.toISOString(),
    x_url: null,
    status: 'review',
    edited: false,
    source: 'composed',
  };

  const targets = [
    {
      file: CAPTIONS_FILE,
      key: 'captions',
      label: 'Instagram',
      entry: {
        ...common,
        flags: igFlags,
        notes: [],
        needs_card: media.length === 0,
        media: media.map((m) => ({ ...m })),
        caption: igCaption,
        source_text: text,
      },
    },
    {
      file: FB_POSTS_FILE,
      key: 'posts',
      label: 'Facebook',
      entry: {
        ...common,
        flags: [],
        notes: link ? ['has-links'] : [],
        media: media.map((m) => ({ ...m })),
        message: fbMessage,
        source_text: text,
      },
    },
    {
      file: X_POSTS_FILE,
      key: 'posts',
      label: 'X',
      entry: {
        ...common,
        flags: xFlags,
        notes: [],
        media: media.map((m) => ({ ...m })),
        text: xText,
        source_text: text,
      },
    },
  ];

  for (const target of targets) {
    const doc = readJson(target.file, { generated_at: null, counts: {}, [target.key]: [] });
    const list = doc[target.key] || (doc[target.key] = []);
    if (list.some((e) => e.id === id)) {
      throw new Error(`${target.label}: an entry with id ${id} already exists. Pass --date to differ.`);
    }
    list.push(target.entry);
    list.sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
    writeJson(target.file, doc);
  }

  info('');
  ok(`Composed ${id} into all three review files.`);
  info(`  X          ${xLength(xText)}/${MAX_X} characters${xFlags.length ? '  [' + xFlags.join(',') + ']' : ''}`);
  info(`  Instagram  ${igCaption.length}/${MAX_IG} characters${igFlags.length ? '  [' + igFlags.join(',') + ']' : ''}`);
  info(`  Facebook   ${fbMessage.length} characters`);
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
