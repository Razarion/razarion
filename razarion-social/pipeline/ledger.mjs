#!/usr/bin/env node
// The content ledger: what has been made, in which format, and where it went.
//
//   node ledger.mjs                         # overview: formats, recent weeks, what is still waiting
//   node ledger.mjs --rebuild               # adopt every entry of the review files not yet in it
//   node ledger.mjs --external data/clips/viper4-portrait.mp4 --date 2026-09-20 --note "Reel, by hand"
//
// --rebuild is for posts that were made before the ledger existed. It never overwrites a record,
// so running it twice is harmless. --external is for anything that went out some other way: it
// is what lets compose.mjs recognise a clip that was already posted by hand.

import { basename } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { CAPTIONS_FILE, FB_POSTS_FILE, X_POSTS_FILE, YT_POSTS_FILE, readJson, toRelative } from './lib/paths.mjs';
import {
  LEDGER_FILE, loadLedger, saveLedger, record, hashMedia, mediumOf, publishedMap,
} from './lib/ledger.mjs';
import { info, ok, warn, fail } from '../src/util/log.mjs';

const REVIEW_FILES = [
  [X_POSTS_FILE, 'posts', (e) => e.text],
  [FB_POSTS_FILE, 'posts', (e) => e.message],
  [CAPTIONS_FILE, 'captions', (e) => e.caption],
  [YT_POSTS_FILE, 'videos', (e) => e.description],
];

/**
 * A format for a post made before formats existed, from what it looks like. Good enough to tell
 * the unit cards from the clips; anything finer (battle or landscape) was never recorded.
 */
function guessFormat(entry, text) {
  if (!String(entry.id).startsWith('own-')) return 'mirrored';
  const medium = mediumOf(entry.media);
  if (medium === 'video') return 'clip';
  if (/Razarion to build/.test(text || '')) return 'unit-card';
  return medium;
}

async function rebuild(ledger) {
  const known = new Set(ledger.items.map((e) => e.id));
  let added = 0;
  for (const [file, key, textOf] of REVIEW_FILES) {
    for (const entry of readJson(file, { [key]: [] })[key] || []) {
      if (known.has(entry.id)) continue;
      const text = entry.source_text || textOf(entry) || '';
      record(ledger, {
        id: entry.id,
        format: guessFormat(entry, text),
        date: entry.date,
        medium: mediumOf(entry.media),
        media: await hashMedia(entry.media || []),
        summary: text,
        source: 'backfill',
      });
      known.add(entry.id);
      added++;
    }
  }
  return added;
}

async function external(ledger, args) {
  const file = String(args.external);
  const when = args.date ? new Date(String(args.date)) : new Date();
  if (Number.isNaN(when.getTime())) throw new Error(`Not a date: ${args.date}`);
  const media = [{ type: /\.(mp4|mov|webm)$/i.test(file) ? 'video' : 'photo', file }];
  const id = `ext-${when.toISOString().slice(0, 10).replace(/-/g, '')}-${basename(file).replace(/\.[^.]+$/, '')}`;
  const entry = record(ledger, {
    id,
    format: args.format ? String(args.format) : mediumOf(media) === 'video' ? 'clip' : 'photo',
    subject: args.subject ? String(args.subject) : null,
    date: when.toISOString(),
    media: await hashMedia(media),
    summary: args.note ? String(args.note) : null,
    source: 'external',
    note: args.note ? String(args.note) : 'posted outside the pipeline',
  });
  ok(`Recorded ${entry.id} as posted outside the pipeline.`);
}

function isoWeek(date) {
  const d = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()));
  const day = d.getUTCDay() || 7;
  d.setUTCDate(d.getUTCDate() + 4 - day);
  const start = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
  return `${d.getUTCFullYear()}-W${String(Math.ceil(((d - start) / 86400000 + 1) / 7)).padStart(2, '0')}`;
}

function overview(ledger) {
  const live = publishedMap();
  const own = ledger.items.filter((e) => e.format !== 'mirrored');

  info(`${ledger.items.length} records in ${toRelative(LEDGER_FILE)}, ${own.length} of them made here or by hand`);
  info('');
  info('  format        count   last');
  const byFormat = new Map();
  for (const e of own) byFormat.set(e.format, [...(byFormat.get(e.format) || []), e]);
  for (const [format, list] of [...byFormat].sort((a, b) => b[1].length - a[1].length)) {
    info(`  ${format.padEnd(12)}  ${String(list.length).padStart(5)}   ${list.at(-1).date.slice(0, 10)}`);
  }

  info('');
  info('  week       posts  mix');
  const weeks = new Map();
  for (const e of own) weeks.set(isoWeek(new Date(e.date)), [...(weeks.get(isoWeek(new Date(e.date))) || []), e]);
  for (const [week, list] of [...weeks].sort().slice(-8)) {
    const mix = [...new Set(list.map((e) => e.format))].join(', ');
    info(`  ${week}  ${String(list.length).padStart(5)}  ${mix}`);
  }

  const waiting = own.filter((e) => e.source !== 'external' && !live.has(e.id));
  info('');
  if (waiting.length) {
    info(`  not published anywhere yet: ${waiting.length}`);
    for (const e of waiting.slice(-10)) info(`    ${e.id}  ${e.format}  ${(e.summary || '').slice(0, 60)}`);
  } else {
    info('  nothing waiting');
  }
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const ledger = loadLedger();

  if (args.rebuild) {
    const added = await rebuild(ledger);
    saveLedger(ledger);
    ok(`${added} record(s) adopted from the review files.`);
    info('');
  }
  if (args.external) {
    await external(ledger, args);
    saveLedger(ledger);
    info('');
  }
  if (!ledger.items.length) {
    warn('The ledger is empty. Run: node ledger.mjs --rebuild');
    return;
  }
  overview(ledger);
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
