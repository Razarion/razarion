#!/usr/bin/env node
// Films a staged battle on the live planet without a person in front of it.
//
//   node record_director.mjs --target "Occupy 5m" --both            # portrait and landscape
//   node record_director.mjs --target 390815 --count 20 --type Viper --seconds 40
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

import { mkdirSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { adminToken, baseItemTypes } from './lib/razarion.mjs';
import { withStudioPage, renderingCapabilities, isSoftwareRenderer } from './lib/browser.mjs';
import { probeVideo } from './lib/video.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_ORIGIN = 'https://www.razarion.com';
/** A clip this far below the requested frame rate is a failed take, not a slow one. */
const MIN_FPS = 20;
/** Kept clear of every player base, on top of what the server itself insists on. */
const MIN_HUMAN_CLEARANCE = 400;

const SHAPES = {
  portrait: { width: 1080, height: 1920 },
  landscape: { width: 1920, height: 1080 },
};

function usage() {
  info('node record_director.mjs --target "<bot base name or id>" [--both | --shape portrait|landscape]');
  info('                         [--count 20] [--type Viper] [--seconds 40] [--distance 90] [--beta 1.0] [--radius 110]');
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
  const humans = bases.filter((b) => b.character === 'HUMAN' && b.centreX != null);
  const bots = bases.filter((b) => b.character !== 'HUMAN' && b.centreX != null && b.itemCount > 0);

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
  try {
    const { spawn, ownBaseId } = await placeStaging(api, target, candidates, distance, type.id);
    step(`own base #${ownBaseId}, strike force from ${Math.round(spawn.x)}/${Math.round(spawn.y)} `
      + `(${Math.round(spawn.clearance)} clear of players)`);

    const plan = followPlan(target, seconds, Number(args.beta ?? 1.0), Number(args.radius ?? 110));
    const saved = await api.post('/plan', { name: `record_director ${stamp()}`, jsonContent: JSON.stringify(plan) });
    planId = saved.id;
    step(`plan #${planId}`);

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

      for (const shape of shapes) {
        const file = shapes.length > 1 ? out.replace(/\.mp4$/, `-${shape}.mp4`) : out;
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

        const result = await stageAround(api, spawn, count, type, target.baseId);
        step(`stage-attack: ${result.spawned} ${type.internalName} -> #${target.baseId}`
          + ` (${result.tried} spots tried)`);
        if (!result.spawned) throw new Error('Nothing spawned - the clip would be of an empty square.');

        await (await download).saveAs(file);
        assertEngineAlive();
        await verifyTake(file, seconds);
      }
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

/** Two follow keys on the bot base, following the fighting; the target is where the flight starts. */
function followPlan(target, seconds, beta, radius) {
  const key = (time) => ({
    time,
    mode: 'follow',
    target: [target.centreX, 0, target.centreY],
    // Square to the grid like the game's camera (DirectorTaskComponent.onModeChange), but steeper:
    // an upright frame at the game's elevation is half sky.
    alpha: 0,
    beta,
    radius,
    easing: 'ease',
    followWhat: 'combat',
    autoRadius: false,
    followBaseId: target.baseId,
  });
  return { version: 1, durationMs: seconds * 1000, cameraKeys: [key(0), key(seconds * 1000)], cues: [] };
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

function clearance(x, y, humans) {
  return Math.min(Infinity, ...humans.map((b) => Math.hypot(b.centreX - x, b.centreY - y) - (b.radius ?? 0)));
}

function findTarget(bots, wanted) {
  const byId = bots.find((b) => String(b.baseId) === wanted);
  const byName = bots.filter((b) => (b.name ?? '').toLowerCase() === wanted.toLowerCase());
  if (byId) return byId;
  if (byName.length === 1) return byName[0];
  if (byName.length > 1) throw new Error(`${byName.length} bot bases are called "${wanted}" - pass the id.`);
  throw new Error(`No bot base "${wanted}" with anything in it. --list shows them.`);
}

function directorApi(origin, token) {
  const call = async (method, path, body) => {
    const res = await fetch(`${origin}/rest/director${path}`, {
      method,
      headers: { Authorization: `Bearer ${token}`, ...(body ? { 'Content-Type': 'application/json' } : {}) },
      body: body ? JSON.stringify(body) : undefined,
    });
    const text = await res.text();
    if (!res.ok) throw new Error(`${method} ${path}: HTTP ${res.status} ${text.slice(0, 300)}`);
    return text ? JSON.parse(text) : null;
  };
  return {
    get: (p) => call('GET', p),
    post: (p, b) => call('POST', p, b),
    del: (p) => call('DELETE', p),
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
