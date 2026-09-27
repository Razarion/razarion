#!/usr/bin/env node
// Films a staged battle on the live planet without a person in front of it.
//
//   node record_director.mjs --target "Occupy 5m" --both            # portrait and landscape
//   node record_director.mjs --target 390815 --count 20 --type Viper --seconds 40
//   node record_director.mjs --target "Occupy 5m" --both --style low-orbit   # see CAMERA_STYLES
//   node record_director.mjs --list                                 # the bot bases a battle may hit
//   node record_director.mjs --target "Occupy 5m" --head            # watch it work
//
// What the studio's Director tab does by hand, in one run:
//
//   1. create-base   the operator's own green base, placed behind the spawn point and out of frame
//   2. a plan        one follow key on the bot base, following the fighting
//   3. per take      RECORD_START in a headless /director client, then stage-attack once the
//                    pre-roll is over, so the strike force arrives on camera rather than before it
//   4. clear-staging always, also after a failed take - a shoot must leave nothing behind in a
//                    world other people play in
//
// The server refuses a battle aimed at a player or near one (DirectorStagingController); this
// script picks the spawn point furthest from every player base anyway, so a refusal means the
// target is too close to somebody, not that the geometry here was careless.
//
// Every take is a fresh strike force against what is left of the bot base. With --both the
// landscape take therefore fights over a base the portrait take has already hit.

import { mkdirSync, renameSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { adminToken, baseItemTypes } from './lib/razarion.mjs';
import { withStudioPage, renderingCapabilities, isSoftwareRenderer } from './lib/browser.mjs';
import { probeVideo } from './lib/video.mjs';
import { fightFile } from './lib/cut.mjs';
import { CAMERA_STYLES, MIN_HUMAN_CLEARANCE, clearance, directorApi, splitBases } from './lib/director.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_ORIGIN = 'https://www.razarion.com';
/** A clip this far below the requested frame rate is a failed take, not a slow one. */
const MIN_FPS = 20;

const SHAPES = {
  portrait: { width: 1080, height: 1920 },
  landscape: { width: 1920, height: 1080 },
};

function usage() {
  info('node record_director.mjs --target "<bot base name or id>" [--both | --shape portrait|landscape]');
  info('                         [--count 20] [--type Viper] [--seconds 40] [--distance 90] [--style overhead|low-orbit|push-in|side]');
  info('                         [--beta <elevation>] [--radius <distance>]   override the style');
  info('                         [--settle 20] [--out data/clips/<name>.mp4] [--url https://www.razarion.com] [--head]');
  info('node record_director.mjs --list');
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  if (args.help) return usage();

  const origin = String(args.url ?? DEFAULT_ORIGIN).replace(/\/$/, '');
  const token = await adminToken(origin);
  const api = directorApi(origin, token);

  const bases = await api.get('/bases');
  const { humans, bots } = splitBases(bases);

  if (args.list || !args.target) {
    info('Bot bases, biggest first (distance = to the nearest player base, edge to edge):');
    for (const b of [...bots].sort((a, c) => c.itemCount - a.itemCount)) {
      const d = clearance(b.centreX, b.centreY, humans);
      info(`  ${String(b.baseId).padEnd(8)} ${String(b.itemCount).padStart(3)} items  ${String(Math.round(d)).padStart(5)}  ${b.name ?? '(no name)'}`);
    }
    if (!args.target && !args.list) throw new Error('Which bot base? Pass --target "<name or id>".');
    return;
  }

  const target = findTarget(bots, String(args.target));
  const count = Number(args.count ?? 20);
  const seconds = Number(args.seconds ?? 40);
  const distance = Number(args.distance ?? 90);
  const settleSeconds = Number(args.settle ?? 20);
  const typeName = String(args.type ?? 'Viper');
  const shapes = args.both ? ['portrait', 'landscape'] : [String(args.shape ?? 'portrait')];
  for (const s of shapes) if (!SHAPES[s]) throw new Error(`Unknown shape "${s}" - portrait or landscape.`);

  const types = await baseItemTypes(token);
  // The player's unit, not the bot variant of the same name: "(Bot1) Viper" carries other values.
  const type = types.find((t) => t.internalName === typeName);
  if (!type) throw new Error(`No unit type "${typeName}".`);

  const candidates = spawnCandidates(target, humans, distance);
  const slug = String(args.out ?? join('data', 'clips',
    `staged-${slugOf(target.name ?? String(target.baseId))}-${stamp()}.mp4`));
  const out = resolve(PIPELINE_ROOT, slug);
  mkdirSync(dirname(out), { recursive: true });

  info(`Target #${target.baseId} "${target.name}" · ${target.itemCount} items · ${Math.round(clearance(target.centreX, target.centreY, humans))} from the nearest player`);
  info(`${count} x ${typeName} · ${seconds}s · ${shapes.join(' + ')}`);

  let planId = null;
  let goodTakes = 0;
  try {
    let { spawn, ownBaseId } = await placeStaging(api, target, candidates, distance, type.id);
    step(`own base #${ownBaseId}, strike force from ${Math.round(spawn.x)}/${Math.round(spawn.y)} `
      + `(${Math.round(spawn.clearance)} clear of players)`);

    const style = String(args.style ?? 'overhead');
    const plan = followPlan(target, seconds, {
      style, spawn,
      beta: args.beta != null ? Number(args.beta) : null,
      radius: args.radius != null ? Number(args.radius) : null,
    });
    const saved = await api.post('/plan', { name: `record_director ${stamp()}`, jsonContent: JSON.stringify(plan) });
    planId = saved.id;
    step(`plan #${planId}, camera ${style}`);

    await withStudioPage({ url: `${origin}/game/director`, apiBase: origin, headless: !args.head,
      // Upright like the take: the client holds the horizontal field when the take is narrower than
      // the window, and a portrait take from a landscape window is then mostly sky.
      viewport: shapes[0] === 'portrait' ? { width: 1080, height: 1920 } : { width: 1920, height: 1080 } }, async (page) => {
      // A crashed engine worker leaves the page rendering happily - terrain, sky, 60 fps - with
      // nothing moving in it, and the take reads as a clip of an empty square. Checked per take.
      let workerCrash = null;
      page.on('console', (m) => { if (/worker error/.test(m.text())) workerCrash ??= m.text(); });
      const assertEngineAlive = () => {
        if (workerCrash) throw new Error(`The game worker crashed, so nothing in the world moves: ${workerCrash.slice(0, 200)}`);
      };

      const caps = await renderingCapabilities(page);
      step(`renderer ${caps.renderer ?? 'none'}`);
      if (isSoftwareRenderer(caps.renderer)) {
        throw new Error(`This browser is rendering in software (${caps.renderer}); fix the GPU before filming.`);
      }

      await waitForConsole(page, /\[Director] command channel reachable/, 120_000,
        'The /director client never reached the command channel - is director mode on and the token an admin one?');
      step(`client connected, letting the world load (${settleSeconds}s)`);
      await page.waitForTimeout(settleSeconds * 1000);

      // A fresh client takes the first command it sees for one left over from an earlier session
      // and only half obeys it (DirectorService.poll), so that one is a throwaway.
      await api.post('/command', { type: 'STOP' });
      await page.waitForTimeout(1000);
      await api.post('/command', { type: 'LOAD_PLAN', planId });
      // The camera flies to the base and the client asks for the terrain and units around it.
      await page.waitForTimeout(8000);

      for (const [takeNo, shape] of shapes.entries()) {
        const file = shapes.length > 1 ? out.replace(/\.mp4$/, `-${shape}.mp4`) : out;
        if (takeNo > 0) {
          // The survivors of the last take stand on the spots the next strike force needs, and the
          // server refuses every one of them - a second take then films an empty square. A fresh
          // staging base, from the same side first, so the camera plan still looks the right way.
          await api.post('/clear-staging', {});
          ({ spawn, ownBaseId } = await placeStaging(api, target, [spawn, ...candidates.filter((c) => c !== spawn)], distance, type.id));
          step(`fresh staging for the ${shape} take: own base #${ownBaseId}`);
          // The window in the shape of the take. A landscape take drawn in the upright window of the
          // portrait one came out at 18-28 fps against 36-72 for the portrait take before it.
          await page.setViewportSize(SHAPES[shape]);
          await page.waitForTimeout(2000);
        }
        const left = (await api.get('/bases')).find((b) => b.baseId === target.baseId);
        if (!left || left.itemCount === 0) {
          warn(`Nothing left of the target for the ${shape} take - skipped.`);
          continue;
        }
        assertEngineAlive();
        step(`recording ${shape} (${left.itemCount} target items left)`);
        const recording = waitForConsole(page, /\[Director] recording /, 30_000,
          'The client never started recording.');
        const download = page.waitForEvent('download', { timeout: (seconds + 90) * 1000 });
        // A take that fails before the download leaves this promise to reject once the browser
        // closes. Unobserved, that rejection kills the process before the finally below can clear
        // the staging base off the live planet.
        download.catch(() => {});
        await api.post('/command', { type: 'RECORD_START', fileName: `director-${shape}.mp4`, ...SHAPES[shape] });
        await recording;
        // From here the clock runs with the file: what dies when, for the cut (lib/cut.mjs).
        const fight = watchTheFight(api, target.baseId, ownBaseId);

        const result = await stageAround(api, spawn, count, type, target.baseId);
        step(`stage-attack: ${result.spawned} ${type.internalName} -> #${target.baseId}`
          + ` (${result.tried} spots tried)`);
        if (!result.spawned) {
          // The server refused every spot - most likely a player has come near since the first
          // take, which is the staging rule doing its job. The recording runs on regardless; it is
          // let finish and thrown away, and the other take still counts.
          warn(`Nothing spawned for the ${shape} take - the clip would be of an empty square. Take dropped.`);
          await (await download).delete().catch(() => {});
          await fight.stop();
          continue;
        }

        await (await download).saveAs(file);
        writeFileSync(fightFile(file), JSON.stringify(await fight.stop()) + '\n');
        assertEngineAlive();
        // A broken take is put aside rather than ending the run: with --both the other shape may be
        // fine, and an unattended run (the battle format) can post that one. Only no usable take
        // at all is a failure.
        try {
          await verifyTake(file, seconds);
          goodTakes++;
        } catch (e) {
          const broken = file.replace(/\.mp4$/, '-broken.mp4');
          renameSync(file, broken);
          warn(`${e.message} Put aside as ${rel(broken)}.`);
        }
      }
      if (!goodTakes) throw new Error('No usable take.');
    });
  } finally {
    // Both, whatever happened above. A green base with twenty units in it left standing on the
    // live planet is exactly what staging promises not to do.
    try {
      const removed = await api.post('/clear-staging', {});
      step(removed ? `clear-staging removed base #${removed}` : 'clear-staging: nothing to remove');
    } catch (e) {
      fail(`clear-staging FAILED - remove the Director Base by hand: ${e.message}`);
    }
    if (planId != null) {
      await api.del(`/plan/${planId}`).catch((e) => warn(`Plan #${planId} not deleted: ${e.message}`));
    }
  }

  ok('Recorded.');
  info(shapes.length > 1
    ? `  Next: node compose.mjs --portrait ${rel(out.replace(/\.mp4$/, '-portrait.mp4'))} --landscape ${rel(out.replace(/\.mp4$/, '-landscape.mp4'))} --text "..."`
    : `  Next: node compose.mjs --${shapes[0]} ${rel(out)} --text "..."`);
}

/**
 * Follow keys on the bot base in the given style. `spawn` is where the strike force starts, which
 * decides which side is "behind the attackers". --beta and --radius override every stop, as they
 * did before there were styles.
 */
function followPlan(target, seconds, { style = 'overhead', spawn = null, beta = null, radius = null } = {}) {
  const def = CAMERA_STYLES[style];
  if (!def) throw new Error(`Unknown --style "${style}". Known: ${Object.keys(CAMERA_STYLES).join(', ')}`);
  // Babylon's orbit puts the camera at target + (sin alpha, cos alpha) on the ground plane, and the
  // game's y is Babylon's z.
  const attackSide = spawn ? Math.atan2(spawn.x - target.centreX, spawn.y - target.centreY) : 0;
  const stops = def.stops.length > 1 ? def.stops : [def.stops[0], def.stops[0]];
  const cameraKeys = stops.map((s, i) => ({
    time: Math.round((seconds * 1000 * i) / (stops.length - 1)),
    mode: 'follow',
    target: [target.centreX, 0, target.centreY],
    alpha: def.absoluteAlpha ? s.alpha : attackSide + s.alpha,
    beta: beta ?? s.beta,
    radius: radius ?? s.radius,
    easing: 'ease',
    followWhat: 'combat',
    autoRadius: false,
    followBaseId: target.baseId,
  }));
  return { version: 1, durationMs: seconds * 1000, cameraKeys, cues: [] };
}

/** Points `distance` from the target, furthest from every player base first. */
function spawnCandidates(target, humans, distance) {
  const all = [];
  for (let deg = 0; deg < 360; deg += 15) {
    const a = (deg * Math.PI) / 180;
    const x = target.centreX + distance * Math.cos(a);
    const y = target.centreY + distance * Math.sin(a);
    all.push({ x, y, clearance: clearance(x, y, humans) });
  }
  const usable = all.filter((c) => c.clearance >= MIN_HUMAN_CLEARANCE).sort((a, b) => b.clearance - a.clearance);
  if (!usable.length) {
    throw new Error(`Every spawn point is closer than ${MIN_HUMAN_CLEARANCE} to a player base. `
      + 'Pick a bot further from the players.');
  }
  return usable;
}

/**
 * Find a direction whose ground takes both the strike force and the operator's base.
 *
 * Distance to the players is all this script can compute; whether the ground is water, a cliff or
 * a slope only the server knows, and it says so by refusing to place anything. So the ground is
 * asked with the unit that will stand there: one attacker staged at the spawn point, then the whole
 * base cleared again. A create-base at the spawn point proves nothing - it places the planet's start
 * unit, not the attacker, and moves it to the nearest free spot (freeSpawnPosition), so it succeeded
 * next to ground where twenty Vipers were then all refused.
 */
async function placeStaging(api, target, candidates, distance, attackerTypeId) {
  for (const spawn of candidates) {
    const home = along(target, spawn, distance + 150);
    try {
      await api.post('/create-base', { x: home.x, y: home.y });
      const probe = await api.post('/stage-attack', {
        x: spawn.x, y: spawn.y, count: 1, baseItemTypeId: attackerTypeId, targetBaseId: target.baseId,
      });
      await api.post('/clear-staging', {});
      if (!probe.spawned) throw new Error(probe.errors?.[0] ?? 'refused');
      const ownBaseId = await api.post('/create-base', { x: home.x, y: home.y });
      return { spawn, ownBaseId };
    } catch (e) {
      step(`no room at ${Math.round(spawn.x)}/${Math.round(spawn.y)} (${String(e.message).slice(0, 60)}) - next direction`);
      await api.post('/clear-staging', {}).catch(() => {});
    }
  }
  throw new Error('No direction around the target has ground for a strike force. Try another --distance.');
}

/**
 * The strike force one unit at a time, on a spiral around the spawn point.
 *
 * One stage-attack for all of them lays the units out as a fixed grid running right and down from
 * the spawn point. On open ground that is fine; on a small platform ringed by cliffs most of the grid
 * lands on ground the unit may not stand on, and a take at the Datacenter got one Viper out of twenty.
 * Asking per spot keeps every unit that fits, wherever around the spawn point the ground allows it.
 */
async function stageAround(api, spawn, count, type, targetBaseId) {
  const spacing = (type.physicalAreaConfig?.radius ?? 2) * 4;
  let spawned = 0;
  let tried = 0;
  for (let ring = 0; spawned < count && ring <= 6; ring++) {
    const spots = ring === 0 ? 1 : ring * 6;
    for (let i = 0; i < spots && spawned < count; i++) {
      const a = (2 * Math.PI * i) / spots;
      const x = spawn.x + ring * spacing * Math.cos(a);
      const y = spawn.y + ring * spacing * Math.sin(a);
      tried++;
      const r = await api.post('/stage-attack', { x, y, count: 1, baseItemTypeId: type.id, targetBaseId })
        .catch(() => ({ spawned: 0 }));
      spawned += r.spawned ?? 0;
    }
  }
  return { spawned, tried };
}

function along(from, to, length) {
  const dx = to.x - from.centreX;
  const dy = to.y - from.centreY;
  const n = Math.hypot(dx, dy) || 1;
  return { x: from.centreX + (dx / n) * length, y: from.centreY + (dy / n) * length };
}

function findTarget(bots, wanted) {
  const byId = bots.find((b) => String(b.baseId) === wanted);
  const byName = bots.filter((b) => (b.name ?? '').toLowerCase() === wanted.toLowerCase());
  if (byId) return byId;
  if (byName.length === 1) return byName[0];
  if (byName.length > 1) throw new Error(`${byName.length} bot bases are called "${wanted}" - pass the id.`);
  throw new Error(`No bot base "${wanted}" with anything in it. --list shows them.`);
}

/**
 * Counts, once a second while a take records, what the bot base and the strike force still have.
 * Every drop is something destroyed, and its second in the take is where the cut goes - the one
 * signal that a moving camera cannot fake (a circling camera makes as much picture change over a
 * quiet base as a fight does). Written next to the take as <take>.fight.json.
 */
function watchTheFight(api, targetBaseId, ownBaseId) {
  const t0 = Date.now();
  const samples = [];
  let running = true;
  const loop = (async () => {
    while (running) {
      try {
        const bases = await api.get('/bases');
        const count = (id) => bases.find((b) => b.baseId === id)?.itemCount ?? 0;
        samples.push({ t: Math.round((Date.now() - t0) / 100) / 10, target: count(targetBaseId), own: count(ownBaseId) });
      } catch {
        // A missed second is a gap in the tally, not a reason to stop filming.
      }
      await new Promise((r) => setTimeout(r, 1000));
    }
  })();
  return {
    async stop() {
      running = false;
      await loop;
      return { samples };
    },
  };
}

function waitForConsole(page, pattern, timeout, message) {
  return page.waitForEvent('console', { predicate: (m) => pattern.test(m.text()), timeout })
    .catch(() => { throw new Error(message); });
}

async function verifyTake(file, seconds) {
  const probe = await probeVideo(file);
  if (!probe) throw new Error(`ffprobe cannot read ${file} - the download did not produce a usable file.`);
  info(`  ${rel(file)}`);
  info(`  ${probe.width}x${probe.height}, ${probe.duration.toFixed(1)}s, ${probe.fps.toFixed(1)} fps, `
    + `${(probe.bytes / 1024 / 1024).toFixed(1)} MB, ${probe.videoCodec}`);
  if (probe.fps < MIN_FPS || probe.duration < seconds * 0.8) {
    throw new Error(`That take is broken: ${probe.duration.toFixed(2)}s at ${probe.fps.toFixed(1)} fps `
      + `for a ${seconds}s clip.`);
  }
}

const slugOf = (s) => s.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
const stamp = () => new Date().toISOString().replace(/[-:]/g, '').replace('T', '-').slice(0, 13);
const rel = (f) => (f.startsWith(PIPELINE_ROOT) ? f.slice(PIPELINE_ROOT.length + 1).replace(/\\/g, '/') : f);

main().catch((e) => {
  fail(e.message);
  process.exit(1);
});
