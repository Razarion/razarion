#!/usr/bin/env node
// Makes one post from a format, into the review files, and records it in the ledger.
//
//   node produce.mjs --list                             # formats, when each last ran
//   node produce.mjs --format duel                      # the pair that is due
//   node produce.mjs --format duel --subject viper,badger
//   node produce.mjs --format week-in-numbers
//   node produce.mjs --format devlog                    # first run: writes a draft to fill in
//   node produce.mjs --format duel --dry-run            # render into data/preview, write nothing else
//   node produce.mjs --format week-in-numbers --url http://localhost:8080
//
// The formats live in lib/formats/. Like compose.mjs and generate.mjs this publishes nothing: the
// entries land on "review", and publish*.mjs take it from there.

import { join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { DATA_DIR, ensureDir, toRelative } from './lib/paths.mjs';
import { adminToken, baseItemTypes, fetchImage } from './lib/razarion.mjs';
import { buildEntries, writeAndRecord } from './lib/entries.mjs';
import { loadLedger, lastOf } from './lib/ledger.mjs';
import { FORMATS, OTHER_FORMATS, NotReady, formatByName } from './lib/formats/index.mjs';
import { env } from '../src/config.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const OWN_DIR = join(DATA_DIR, 'own');
const PREVIEW_DIR = join(DATA_DIR, 'preview');

function list(ledger) {
  info('  format            medium    last run    ');
  for (const f of [...FORMATS, ...OTHER_FORMATS]) {
    const last = lastOf(ledger, f.name);
    const made = FORMATS.includes(f) ? '' : '  (not via produce.mjs)';
    info(`  ${f.name.padEnd(16)}  ${f.medium.padEnd(8)}  ${last ? last.date.slice(0, 10) : 'never     '}  ${f.summary}${made}`);
  }
}

/**
 * What a format gets to work with. The server calls are lazy and cached, so a format that needs
 * nothing from the game - the devlog reads git - never logs in.
 */
function context(args, ledger, id, now, dryRun) {
  const origin = String(args.url ?? env.RAZARION_BASE_URL ?? 'https://www.razarion.com').replace(/\/$/, '');
  let token = null;
  let items = null;
  const login = async () => (token ??= await adminToken(origin));
  const outDir = dryRun ? PREVIEW_DIR : OWN_DIR;
  return {
    args,
    now,
    dryRun,
    lastOf: (format, subject) => lastOf(ledger, format, subject),
    mediaFile(slug, ext) {
      ensureDir(outDir);
      return join(outDir, `${id}-${slug}.${ext}`);
    },
    async items() {
      return (items ??= await baseItemTypes(await login()));
    },
    image: (imageId) => fetchImage(imageId),
    async api(path) {
      const res = await fetch(origin + path, { headers: { Authorization: 'Bearer ' + (await login()) } });
      if (res.status === 404) throw new Error(`${origin}${path} does not exist - is the server deployed with it?`);
      if (!res.ok) throw new Error(`${path}: HTTP ${res.status}`);
      return res.json();
    },
  };
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const ledger = loadLedger();
  if (args.list || !args.format) {
    if (!args.format && !args.list) info('node produce.mjs --format <name> [--subject ...] [--dry-run]\n');
    return list(ledger);
  }

  const format = formatByName(String(args.format));
  if (!format) throw new Error(`No format "${args.format}". Known: ${FORMATS.map((f) => f.name).join(', ')}`);

  const dryRun = Boolean(args['dry-run']);
  const now = new Date();
  const id = 'own-' + now.toISOString().replace(/[-:T]/g, '').slice(0, 14);

  step(`${format.name}: ${format.summary}`);
  let made;
  try {
    made = await format.produce(context(args, ledger, id, now, dryRun));
  } catch (err) {
    if (err instanceof NotReady) {
      info('');
      warn(`Nothing made: ${err.message}`);
      return;
    }
    throw err;
  }

  for (const m of made.media) step(`media: ${m.file}`);
  info('');
  info(made.text.split('\n').map((l) => '  ' + l).join('\n'));
  info('');

  if (dryRun) {
    warn(`DRY RUN. The picture is in ${toRelative(PREVIEW_DIR)}; no review file or ledger entry was written.`);
    return;
  }

  const entries = buildEntries({
    id,
    date: now.toISOString(),
    text: made.text,
    link: made.link ?? null,
    tags: made.tags ?? [],
    media: made.media,
    source: 'composed',
  });
  for (const flag of made.flags ?? []) {
    for (const e of [entries.x, entries.ig, entries.fb, entries.yt]) if (e && !e.flags.includes(flag)) e.flags.push(flag);
  }
  const written = await writeAndRecord(entries, { format: format.name, subject: made.subject });

  ok(`${format.name} (${made.subject}) written as ${id} to ${written.join(', ')}.`);
  info(`  X ${entries.lengths.x}/280   Instagram ${entries.lengths.ig}/2200   Facebook ${entries.lengths.fb}`);
  if (entries.flags.x.includes('too-long')) warn('  The X text is over 280 characters. Shorten it before approving.');
  info('  Read them, set status to "ok", then upload and publish.');
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
