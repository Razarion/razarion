#!/usr/bin/env node
// Writes a post nobody had to think about: one unit from the live game, its own description and
// numbers, on a rendered card, in all three review files.
//
//   node generate.mjs             # the next unit in the rotation
//   node generate.mjs --dry-run   # show what it would write, write nothing
//   node generate.mjs --unit Viper
//   node generate.mjs --reset     # start the rotation over
//
// The facts come from the running server, so they stay true as the game changes - which is the
// whole reason for reading them rather than keeping a list here.

import { writeFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { createCanvas, loadImage } from '@napi-rs/canvas';
import { parseArgs } from './lib/args.mjs';
import {
  DATA_DIR, STATE_DIR, ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { adminToken, baseItemTypes, fetchImage, roleOf } from './lib/razarion.mjs';
import { buildEntries, writeEntries } from './lib/entries.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const OWN_DIR = join(DATA_DIR, 'own');
const ROTATION_FILE = join(STATE_DIR, 'generate.json');

const LINK = 'https://www.razarion.com';

// 1080x1350 is Instagram's tallest feed format, and within Facebook's and X's limits.
const WIDTH = 1080;
const HEIGHT = 1350;

// The game's own loading screen, so a generated card sits beside a screenshot without looking
// like it came from somewhere else.
const BACKGROUND = '#1c1917';
const BACKGROUND_DEEP = '#0c0a09';
const ACCENT = '#10b981';
const ACCENT_DARK = '#014737';
const TEXT = '#e7e5e4';
const MUTED = '#a8a29e';
const FONT = 'Segoe UI, Arial, Helvetica, sans-serif';

// What a role is called in a sentence. The config names it by which sub-config is present, which
// is precise but not English.
const ROLE_WORDS = {
  harvester: 'harvester',
  builder: 'builder',
  factory: 'factory',
  generator: 'power plant',
  house: 'housing',
  container: 'transport',
  weapon: 'combat unit',
};

/**
 * The post text, built only from what the server actually returned.
 *
 * The unit's own description leads, because it was written for players and says the thing in five
 * words. The numbers follow, and nothing is added that the data does not support - an invented
 * sentence about how a unit "feels" would be the fastest way to make these posts worthless.
 */
function buildText(item) {
  const role = ROLE_WORDS[roleOf(item)];
  const lead = item.description
    ? `${item.name} - ${String(item.description).trim().replace(/\.$/, '')}.`
    : role
      ? `${item.name}, ${role} in Razarion.`
      : `${item.name}.`;

  // A zero is not a fact worth stating - "0 Razarion to build" reads as a mistake, and for the
  // items carrying it the price simply is not modelled. Left out rather than asserted.
  const facts = [];
  if (item.price > 0) facts.push(`${item.price} Razarion to build`);
  if (item.health > 0) facts.push(`${item.health} health`);
  if (item.spawnDurationMillis > 0) facts.push(`${Math.round(item.spawnDurationMillis / 1000)}s to spawn`);

  const blocks = [lead];
  if (facts.length) blocks.push(facts.join(', ') + '.');
  blocks.push('Open-source RTS that runs in a browser tab, no download and no account.');
  return blocks.join('\n\n');
}

/**
 * Draws the unit onto a card instead of scaling its thumbnail up.
 *
 * The stored thumbnails are 200x200. Stretched to fill a feed post they turn to mush, so the
 * image keeps a size it can carry and the rest of the card is type and ground - which also gives
 * the numbers somewhere to live.
 */
async function renderCard(item, thumbnailBuffer) {
  const canvas = createCanvas(WIDTH, HEIGHT);
  const ctx = canvas.getContext('2d');

  const backdrop = ctx.createLinearGradient(0, 0, 0, HEIGHT);
  backdrop.addColorStop(0, BACKGROUND);
  backdrop.addColorStop(1, BACKGROUND_DEEP);
  ctx.fillStyle = backdrop;
  ctx.fillRect(0, 0, WIDTH, HEIGHT);

  const rule = ctx.createLinearGradient(0, 0, 0, HEIGHT);
  rule.addColorStop(0, ACCENT);
  rule.addColorStop(1, ACCENT_DARK);
  ctx.fillStyle = rule;
  ctx.fillRect(0, 0, 10, HEIGHT);

  ctx.fillStyle = ACCENT;
  ctx.font = `700 30px ${FONT}`;
  ctx.letterSpacing = '5px';
  ctx.fillText('RAZARION', 96, 126);
  ctx.letterSpacing = '0px';

  // Twice the stored size is as far as a 200px render carries; beyond that the edges go soft.
  const image = await loadImage(thumbnailBuffer);
  const size = 400;
  ctx.imageSmoothingEnabled = true;
  ctx.imageSmoothingQuality = 'high';
  ctx.drawImage(image, (WIDTH - size) / 2, 300, size, size);

  ctx.fillStyle = TEXT;
  ctx.font = `700 78px ${FONT}`;
  ctx.fillText(item.name, 96, 860);

  const role = ROLE_WORDS[roleOf(item)];
  if (item.description || role) {
    ctx.fillStyle = MUTED;
    ctx.font = `400 38px ${FONT}`;
    ctx.fillText(item.description || role, 96, 918);
  }

  // A rule under the type, then the numbers - the part a reader scans rather than reads.
  ctx.fillStyle = '#3a352f';
  ctx.fillRect(96, 990, WIDTH - 192, 2);

  const stats = [];
  if (item.price > 0) stats.push(['COST', `${item.price}`]);
  if (item.health > 0) stats.push(['HEALTH', `${item.health}`]);
  if (item.spawnDurationMillis > 0) stats.push(['SPAWN', `${Math.round(item.spawnDurationMillis / 1000)}s`]);

  let x = 96;
  for (const [label, value] of stats) {
    ctx.fillStyle = MUTED;
    ctx.font = `600 24px ${FONT}`;
    ctx.letterSpacing = '3px';
    ctx.fillText(label, x, 1060);
    ctx.letterSpacing = '0px';
    ctx.fillStyle = ACCENT;
    ctx.font = `700 56px ${FONT}`;
    ctx.fillText(value, x, 1130);
    x += 260;
  }

  ctx.fillStyle = MUTED;
  ctx.font = `400 28px ${FONT}`;
  ctx.fillText('razarion.com', 96, HEIGHT - 96);

  return canvas.toBuffer('image/jpeg', 92);
}

/**
 * The units worth writing about, in the order a player meets them.
 *
 * The bots field their own copies of the same units, marked in internalName as (Bot1) or (Bot2)
 * while carrying the player-facing name. They are not a second unit to write about, and their
 * numbers differ - the bots' Viper costs 100 where a player pays 10 - so a post built from one
 * would state a price no player will ever see.
 *
 * Sorted cheap to expensive. Items without a price are not things you buy, so they go last.
 */
function rotationUnits(items) {
  return items
    .filter((i) => i.name && i.thumbnail)
    .filter((i) => !/^\(Bot\d*\)/.test(String(i.internalName || '')))
    .sort((a, b) => (a.price || Infinity) - (b.price || Infinity));
}

function pickUnit(items, used, wanted) {
  const usable = rotationUnits(items);

  if (wanted) {
    const found = usable.find((i) => i.name.toLowerCase() === String(wanted).toLowerCase());
    if (!found) throw new Error(`No unit called "${wanted}". Available: ${usable.map((i) => i.name).join(', ')}`);
    return found;
  }

  const next = usable.find((i) => !used.includes(i.id));
  if (!next) {
    throw new Error(
      `All ${usable.length} units have had a post. Start over with --reset, or pass --unit <name>.`
    );
  }
  return next;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const dryRun = Boolean(args['dry-run']);

  ensureDir(STATE_DIR);
  if (args.reset) {
    writeJson(ROTATION_FILE, { used: [], reset_at: new Date().toISOString() });
    ok('Rotation reset.');
    if (!args.unit) return;
  }

  const rotation = readJson(ROTATION_FILE, { used: [] });

  step('logging in to razarion.com');
  const token = await adminToken();
  const items = await baseItemTypes(token);
  step(`${items.length} unit types read`);

  const item = pickUnit(items, rotation.used, args.unit);
  const remaining = rotationUnits(items).filter((i) => !rotation.used.includes(i.id)).length;
  info('');
  info(`  ${item.name} - ${item.description || ROLE_WORDS[roleOf(item)] || 'no description'}`);
  info(`  cost ${item.price}, health ${item.health}, thumbnail ${item.thumbnail}`);
  info(`  ${remaining - 1} unit(s) left in the rotation after this one`);

  const text = buildText(item);
  const id = 'own-' + new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14);

  if (dryRun) {
    info('');
    warn('DRY RUN. Nothing written.');
    info('');
    info(text.split('\n').map((l) => '  ' + l).join('\n'));
    return;
  }

  ensureDir(OWN_DIR);
  const thumbnail = await fetchImage(item.thumbnail);
  const card = await renderCard(item, thumbnail);
  const file = join(OWN_DIR, `${id}-${item.name.toLowerCase().replace(/[^a-z0-9]+/g, '-')}.jpg`);
  writeFileSync(file, card);
  step(`card rendered to ${toRelative(file)}`);

  const entries = buildEntries({
    id,
    date: new Date().toISOString(),
    text,
    link: LINK,
    tags: roleOf(item) === 'weapon' ? ['strategygame'] : ['basebuilding'],
    media: [{ type: 'photo', file: toRelative(file), url: null }],
    source: 'composed',
  });
  writeEntries(entries);

  rotation.used = [...rotation.used, item.id];
  rotation.last = { id: item.id, name: item.name, at: new Date().toISOString() };
  writeJson(ROTATION_FILE, rotation);

  info('');
  ok(`${item.name} written to all three review files as ${id}`);
  info(`  X ${entries.lengths.x}/280   Instagram ${entries.lengths.ig}/2200   Facebook ${entries.lengths.fb}`);
  info('');
  info('Read them, set status to "ok", then upload and publish.');
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
