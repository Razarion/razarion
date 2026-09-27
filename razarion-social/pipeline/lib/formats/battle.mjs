// A staged battle on the live planet, filmed, cut and worded without a person.
//
// What a person did for the September clips, in one run:
//   1. pick what to film - the unit and the bot base filmed longest ago, among the bot bases far
//      enough from every player that the Director may stage there at all
//   2. film it with record_director.mjs, portrait and landscape; that script owns the staging and
//      its clean-up, so a battle filmed here obeys exactly the rules one filmed by hand does
//   3. cut each take to the seconds where the fighting is (lib/cut.mjs), and refuse a take where
//      nothing happens
//   4. hand the writer the real numbers: how many units went in, how big the base was
//
// It writes into the shared world for the length of a take - a strike force and a base of its
// own, both removed again - which Beat agreed to on 2026-09-27 under these rules.

import { spawn } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { PIPELINE_ROOT, STATE_DIR, readJson, writeJson, toRelative } from '../paths.mjs';
import { CAMERA_STYLES, clearance, splitBases } from '../director.mjs';
import { motionProfile, actionWindow, cutClip, fightFile, fightWindow, MIN_KILLS } from '../cut.mjs';
import { probeVideo } from '../video.mjs';
import { NotReady } from './not-ready.mjs';
import { step } from '../../../src/util/log.mjs';

const LINK = 'https://www.razarion.com';

// A bot base this far from the nearest player leaves the Director room to place its own base and
// a strike force on the side away from the players. Nearer ones are refused by the server anyway.
const MIN_TARGET_CLEARANCE = 450;
// Fewer than this and the "base" is a few leftover buildings; the clip would be over in a second.
const MIN_TARGET_ITEMS = 6;

// What the strike force costs, spent on one unit type: enough that the fight lasts past the first
// turret. 200 bought seven Badgers, and five of them against Isalnd's Teslas were gone in seconds.
const BUDGET = 300;
const MIN_COUNT = 6;
const MAX_COUNT = 20;

const TAKE_SECONDS = 40;

const slug = (s) => String(s).toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

/** Player units that can drive to a bot base: armed, mobile, on land. */
function strikeUnits(items) {
  return items
    .filter((i) => i.weaponType && i.health > 0 && i.weaponType.damage > 0)
    .filter((i) => !/^\(Bot\d*\)/.test(String(i.internalName || '')))
    .filter((i) => i.physicalAreaConfig?.speed > 0 && i.physicalAreaConfig?.terrainType === 'LAND');
}

const fail = (message) => { throw new Error(message); };

// Battles that produced no post: the ledger only knows the ones that did, and a base the strike
// force cannot reach (the Datacenter's platform, from the side the staging found room on) would
// otherwise be picked first again every time, as the one never used.
const ATTEMPTS_FILE = join(STATE_DIR, 'battle-attempts.json');

function noteFailure({ unit, target, style }, reason) {
  const doc = readJson(ATTEMPTS_FILE, { failed: [] });
  doc.failed.push({ date: new Date().toISOString(), unit: slug(unit.name), target: slug(target.name), style, reason: String(reason).slice(0, 300) });
  doc.failed = doc.failed.slice(-50);
  writeJson(ATTEMPTS_FILE, doc);
}

/**
 * When each unit, base and camera style was last on screen, as "unit:viper" -> epoch ms. Read from
 * the battles in the ledger (subject battle:<unit>:<base>:<style>; the first one had no style and
 * was overhead) and from the hand-made clips before them, which were all Vipers from above.
 */
function usage(ctx) {
  const used = new Map();
  const mark = (key, date) => used.set(key, Math.max(used.get(key) ?? 0, Date.parse(date)));
  for (const e of ctx.history('battle')) {
    const [, unit, target, style = 'overhead'] = String(e.subject ?? '').split(':');
    if (unit) mark(`unit:${unit}`, e.date);
    if (target) mark(`target:${target}`, e.date);
    mark(`style:${style}`, e.date);
  }
  // A failed attempt counts as the base having just been on: it goes to the back of the line, and
  // comes round again once the others have had their turn.
  for (const f of readJson(ATTEMPTS_FILE, { failed: [] }).failed) mark(`target:${f.target}`, f.date);
  for (const e of ctx.history('clip')) {
    mark('unit:viper', e.date);
    mark('style:overhead', e.date);
  }
  return used;
}

/** Runs record_director.mjs and hands back its output; a failed take throws with its last lines. */
function film({ origin, target, unit, count, style, out }) {
  const args = [
    join(PIPELINE_ROOT, 'record_director.mjs'),
    '--target', String(target.baseId), '--type', unit.internalName, '--count', String(count),
    '--seconds', String(TAKE_SECONDS), '--style', style, '--both', '--out', out, '--url', origin,
  ];
  return new Promise((resolve, reject) => {
    const child = spawn(process.execPath, args, { cwd: PIPELINE_ROOT, stdio: ['ignore', 'pipe', 'pipe'] });
    let output = '';
    const echo = (d) => {
      output += d;
      for (const line of String(d).split('\n')) if (line.trim()) step(`  ${line.trim()}`);
    };
    child.stdout.on('data', echo);
    child.stderr.on('data', echo);
    child.on('close', (code) => {
      if (code === 0) resolve(output);
      else reject(new Error(`record_director.mjs failed (exit ${code}): ${output.trim().split('\n').slice(-3).join(' | ')}`));
    });
  });
}

/** Cuts one take to its fighting, or throws when there is none worth showing. */
/**
 * Cuts one take to its fighting, or throws when there is none worth showing.
 *
 * The tally record_director.mjs kept while filming decides where: it knows when things died,
 * whatever the camera did. Picture motion is only the fallback for a take without one - it works
 * for the still overhead camera but not for a moving one, which changes the picture as much over a
 * quiet base as a fight does (the first low-orbit clip was 15 s of turrets standing still).
 */
async function cutTake(take, destination) {
  const tallyFile = fightFile(take);
  let window;
  let why;
  if (existsSync(tallyFile)) {
    const probe = await probeVideo(take);
    window = fightWindow(JSON.parse(readFileSync(tallyFile, 'utf8')), probe.duration);
    if (!window) throw new Error(`Fewer than ${MIN_KILLS} things destroyed in ${toRelative(take)} - no fight worth a post.`);
    why = `${window.kills} of ${window.total} destroyed inside`;
  } else {
    window = actionWindow(await motionProfile(take));
    if (!window) throw new Error(`Nothing moves in ${toRelative(take)} - a take of an empty square is not posted.`);
    why = `motion ${window.motion.toFixed(3)}`;
  }
  const probe = await cutClip(take, destination, window);
  step(`cut ${toRelative(take)}: ${window.start.toFixed(1)}s + ${window.length.toFixed(1)}s (${why})`);
  return probe;
}

export default {
  name: 'battle',
  medium: 'video',
  summary: 'a staged battle on the live planet, filmed, cut to the fighting, worded from its numbers',
  tones: ['punchy', 'behind-the-scenes', 'question'],

  async produce(ctx) {
    const units = strikeUnits(await ctx.items());
    if (!units.length) throw new NotReady('No armed land unit to send in.');

    const { humans, bots } = splitBases(await ctx.api('/rest/director/bases'));
    const targets = bots
      .filter((b) => b.itemCount >= MIN_TARGET_ITEMS && b.name)
      .filter((b) => clearance(b.centreX, b.centreY, humans) >= MIN_TARGET_CLEARANCE);
    if (!targets.length) {
      throw new NotReady(`No bot base with ${MIN_TARGET_ITEMS}+ items lies ${MIN_TARGET_CLEARANCE} or more from every player right now.`);
    }

    // Unit, base and camera each rotate on their own: every one of them is the one used longest ago
    // (never used first), so two clips in a row differ in all three wherever there is a choice.
    // "Too similar to the others" was the verdict on the first automatic clip - same unit, same
    // base, same view from above as the September ones.
    const used = usage(ctx);
    const pick = (options, keyOf, tieBreak = () => 0) => [...options].sort((a, b) =>
      (used.get(keyOf(a)) ?? 0) - (used.get(keyOf(b)) ?? 0) || tieBreak(a, b))[0];
    const unit = ctx.args.unit
      ? units.find((u) => slug(u.name) === slug(ctx.args.unit)) ?? fail(`No unit "${ctx.args.unit}": ${units.map((u) => u.name).join(', ')}`)
      : pick(units, (u) => `unit:${slug(u.name)}`);
    const target = pick(targets, (b) => `target:${slug(b.name)}`, (a, b) => b.itemCount - a.itemCount);
    const style = ctx.args.style
      ? (CAMERA_STYLES[ctx.args.style] ? String(ctx.args.style) : fail(`No camera style "${ctx.args.style}": ${Object.keys(CAMERA_STYLES).join(', ')}`))
      : pick(Object.keys(CAMERA_STYLES), (s) => `style:${s}`);
    const count = Math.max(MIN_COUNT, Math.min(MAX_COUNT, Math.round(BUDGET / (unit.price || 10))));
    step(`filming ${count} x ${unit.name} against a bot base of ${target.itemCount} items, camera ${style}`);

    const raw = toRelative(ctx.mediaFile('battle-take', 'mp4'));
    let output;
    try {
      output = await film({ origin: ctx.origin, target, unit, count, style, out: raw });
    } catch (e) {
      noteFailure({ unit, target, style }, e.message);
      throw e;
    }
    const spawned = Number(output.match(/stage-attack: (\d+) /)?.[1] ?? count);
    // The base as record_director found it when filming began: the bots rebuild between the choice
    // above and the take, so its own count is the one the clip shows.
    const targetItems = Number(output.match(/Target #\d+ ".*?" · (\d+) items/)?.[1] ?? target.itemCount);

    const media = { type: 'video', file: null, portrait: null, landscape: null, url: null };
    const problems = [];
    for (const shape of ['portrait', 'landscape']) {
      const take = join(PIPELINE_ROOT, raw.replace(/\.mp4$/, `-${shape}.mp4`));
      if (!existsSync(take)) continue;
      const cut = ctx.mediaFile(`battle-${shape}`, 'mp4');
      try {
        await cutTake(take, cut);
        media[shape] = toRelative(cut);
      } catch (e) {
        // One shape without a fight does not sink the other: X then gets the middle of the portrait.
        problems.push(e.message);
        step(e.message);
      }
    }
    if (!media.portrait && !media.landscape) {
      noteFailure({ unit, target, style }, problems.join(' ') || 'no take');
      throw new Error(problems.length ? problems.join(' ') : 'record_director.mjs left no take behind.');
    }
    media.file = media.portrait || media.landscape;

    const unitName = spawned === 1 ? unit.name : `${unit.name}s`;
    return {
      subject: `battle:${slug(unit.name)}:${slug(target.name)}:${style}`,
      text: `${spawned} ${unitName} storm a bot base of ${targetItems} buildings and units. `
        + 'Staged on the live server and filmed in the game\'s own engine. '
        + 'Open-source RTS in a browser tab, no download and no account.',
      link: LINK,
      tags: ['battle', slug(unit.name)],
      media: [media],
      flags: media.landscape ? [] : ['no-landscape-take'],
      facts: {
        attacking_unit: unit.name,
        attackers: spawned,
        target: 'a bot base (computer-controlled)',
        target_buildings_and_units: targetItems,
        how: 'staged by the developer on the live server with the game\'s director tools, filmed in the game\'s own engine',
        what_the_clip_shows: 'the strike force reaching the base and the fight, cut to the fighting',
      },
    };
  },
};
