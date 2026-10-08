#!/usr/bin/env node
// Films the terrain look work: the same camera move, once per state, so the takes can be cut
// before/after or crossfaded into each other.
//
//   node record_showcase.mjs --shot ground --label ground-prototype          # one take, everything on
//   node record_showcase.mjs --shot island --reveal                          # bare, +light, +clouds, ... one take each
//   node record_showcase.mjs --shot hills --states "none;light,relief"       # explicit states, ';' between takes
//   node record_showcase.mjs --shot island --reveal --both                   # landscape and portrait
//   node record_showcase.mjs --list                                          # the shots
//
// The /director client on a LOCAL server (default http://localhost:8081, the razarion2 stack),
// never the live planet: the takes switch render features through window.razShowcase
// (razarion-frontend/.../renderer/showcase.ts), which only the director client installs, and the
// development state being filmed is whatever this checkout last built.
//
// Only the canvas is recorded, so chat, player names and the rest of the HUD never reach the file;
// the director also switches unit names off. Each take is appended to data/showcase/log.json with
// the commit and whether the working tree had uncommitted changes - the timeline the devlog is cut
// from later.

import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { adminToken } from './lib/razarion.mjs';
import { withStudioPage, renderingCapabilities, isSoftwareRenderer } from './lib/browser.mjs';
import { probeVideo } from './lib/video.mjs';
import { directorApi } from './lib/director.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_ORIGIN = 'http://localhost:8081';
const OUT_DIR = join(PIPELINE_ROOT, 'data', 'showcase');
const LOG_FILE = join(OUT_DIR, 'log.json');
const REPO_ROOT = join(PIPELINE_ROOT, '..', '..');
const MIN_FPS = 20;

const SHAPES = {
  portrait: { width: 1080, height: 1920 },
  landscape: { width: 1920, height: 1080 },
};

/** Same order as SHOWCASE_FEATURES in showcase.ts: the order the improvements were made in. */
const FEATURES = ['light', 'clouds', 'relief', 'wind', 'paths', 'growth'];

/**
 * Camera moves on the beginners' island (planet 117). Game x/y; the camera orbits target at
 * elevation beta and distance radius, alpha = PI looks north like the game camera does.
 */
const SHOTS = {
  island: {
    about: 'the island from high up, slow turn',
    target: [180, 125], height: 1, shadows: 'off',
    keys: [{ alpha: Math.PI - 0.05, beta: 1.0, radius: 200 }, { alpha: Math.PI + 0.25, beta: 0.95, radius: 180 }],
  },
  hills: {
    about: 'over the hills along the main path, low, the sea behind',
    target: [205, 140], height: 1.5, shadows: 'off',
    keys: [{ alpha: Math.PI - 0.35, beta: 0.6, radius: 90 }, { alpha: Math.PI + 0.05, beta: 0.55, radius: 78 }],
  },
  ground: {
    about: 'player height (about 30 m), slow drift - the ground up close',
    target: [200, 150], height: 1,
    keys: [{ alpha: Math.PI - 0.15, beta: 0.95, radius: 42 }, { alpha: Math.PI + 0.15, beta: 0.9, radius: 38 }],
  },
  coast: {
    about: 'the east coast from the land side, grass into sand into sea',
    target: [232, 172], height: 0.5, shadows: 'off',
    keys: [{ alpha: -Math.PI / 2 + 0.35, beta: 0.6, radius: 80 }, { alpha: -Math.PI / 2 - 0.05, beta: 0.55, radius: 70 }],
  },
};

function usage() {
  info('node record_showcase.mjs --shot <name> [--reveal | --states "none;light;light,clouds"] [--label <word>]');
  info('                         [--seconds 14] [--shadows on|off] [--both | --shape landscape|portrait] [--settle 20] [--url http://localhost:8081] [--head]');
  info('node record_showcase.mjs --list');
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  if (args.help) return usage();
  if (args.list || !args.shot) {
    for (const [name, s] of Object.entries(SHOTS)) info(`  ${name.padEnd(8)} ${s.about}`);
    if (!args.list) throw new Error('Which shot? Pass --shot <name>.');
    return;
  }
  const shot = SHOTS[String(args.shot)];
  if (!shot) throw new Error(`Unknown shot "${args.shot}". --list shows them.`);

  const origin = String(args.url ?? DEFAULT_ORIGIN).replace(/\/$/, '');
  if (!/^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(origin)) {
    throw new Error(`The showcase films a local build, not ${origin}.`);
  }
  const seconds = Number(args.seconds ?? 14);
  const settleSeconds = Number(args.settle ?? 20);
  const shadows = String(args.shadows ?? shot.shadows ?? 'on') !== 'off';
  const shapes = args.both ? ['landscape', 'portrait'] : [String(args.shape ?? 'landscape')];
  for (const s of shapes) if (!SHAPES[s]) throw new Error(`Unknown shape "${s}" - portrait or landscape.`);
  const states = statesOf(args);
  const label = slugOf(String(args.label ?? (args.reveal ? 'reveal' : 'take')));

  const token = await adminToken(origin);
  const api = directorApi(origin, token);
  const source = sourceState();
  mkdirSync(OUT_DIR, { recursive: true });
  const runStamp = stamp();

  info(`Shot ${args.shot}: ${shot.about}`);
  info(`${states.length} state(s) x ${shapes.join(' + ')}, ${seconds}s each · ${source.commit}${source.dirty ? ' + uncommitted changes' : ''}`);

  const saved = await api.post('/plan', { name: `record_showcase ${runStamp}`, jsonContent: JSON.stringify(planOf(shot, seconds)) });
  const planId = saved.id;
  const takes = [];
  try {
    await withStudioPage({ url: `${origin}/game/director`, apiBase: origin, headless: !args.head, viewport: SHAPES[shapes[0]] }, async (page) => {
      const caps = await renderingCapabilities(page);
      step(`renderer ${caps.renderer ?? 'none'}`);
      if (isSoftwareRenderer(caps.renderer)) throw new Error(`Software rendering (${caps.renderer}); fix the GPU before filming.`);

      await waitForConsole(page, /\[Director] command channel reachable/, 120_000,
        'The /director client never reached the command channel - is the local server up and director mode on?');
      if (!(await page.evaluate(() => !!window.razShowcase))) {
        throw new Error('window.razShowcase is missing - this build has no showcase switches (rebuild the frontend).');
      }
      // The long shadows of the lower sun are part of the look; wide shots may need them off for frame rate
      await page.evaluate((keep) => window.razShowcase.keepShadows(keep), shadows);
      step(`client connected, letting the world load (${settleSeconds}s)`);
      await page.waitForTimeout(settleSeconds * 1000);

      // A fresh client half-ignores the first command it sees (DirectorService.poll)
      await api.post('/command', { type: 'STOP' });
      await page.waitForTimeout(1000);
      await api.post('/command', { type: 'LOAD_PLAN', planId });
      await page.waitForTimeout(8000);

      for (const [shapeNo, shape] of shapes.entries()) {
        if (shapeNo > 0) {
          await page.setViewportSize(SHAPES[shape]);
          await page.waitForTimeout(2000);
        }
        for (const [stateNo, features] of states.entries()) {
          const stateName = features.length === FEATURES.length ? 'all' : features.length ? features.join('+') : 'none';
          const file = join(OUT_DIR, `${runStamp}-${args.shot}-${label}-${String(stateNo + 1).padStart(2, '0')}-${stateName}-${shape}.mp4`);
          await page.evaluate((f) => { window.razShowcase.only(f); window.razShowcase.hideGui(true); }, features);
          // Tiles and sprites pick the switches up at once; a second for the shaders to settle
          await page.waitForTimeout(1500);
          step(`recording ${shape}, ${stateName}`);
          const recording = waitForConsole(page, /\[Director] recording /, 30_000, 'The client never started recording.');
          const download = page.waitForEvent('download', { timeout: (seconds + 90) * 1000 });
          download.catch(() => {});
          await api.post('/command', { type: 'RECORD_START', fileName: `showcase-${shape}.mp4`, ...SHAPES[shape] });
          await recording;
          await (await download).saveAs(file);
          try {
            await verifyTake(file, seconds);
            takes.push({ file: rel(file), shot: String(args.shot), label, shape, features, ...source, recordedAt: new Date().toISOString() });
          } catch (e) {
            warn(e.message);
          }
          // The plan back to its start for the next take
          await api.post('/command', { type: 'SEEK', timeMs: 0 });
          await page.waitForTimeout(1500);
        }
      }
    });
  } finally {
    await api.del(`/plan/${planId}`).catch((e) => warn(`Plan #${planId} not deleted: ${e.message}`));
    if (takes.length) appendLog(takes);
  }
  if (!takes.length) throw new Error('No usable take.');
  ok(`${takes.length} take(s) recorded, logged in ${rel(LOG_FILE)}.`);
}

/** --reveal: nothing, then one improvement more per take, in the order they were made. */
function statesOf(args) {
  if (args.reveal) return FEATURES.map((_, i) => FEATURES.slice(0, i)).concat([FEATURES]);
  if (args.states) {
    return String(args.states).split(';').map((s) => {
      const list = s.trim() === 'none' ? [] : s.trim() === 'all' ? [...FEATURES] : s.split(',').map((f) => f.trim()).filter(Boolean);
      for (const f of list) if (!FEATURES.includes(f)) throw new Error(`Unknown feature "${f}". Known: ${FEATURES.join(', ')}`);
      return list;
    });
  }
  return [[...FEATURES]];
}

function planOf(shot, seconds) {
  const [x, y] = shot.target;
  const keys = shot.keys.length > 1 ? shot.keys : [shot.keys[0], shot.keys[0]];
  const cameraKeys = keys.map((k, i) => ({
    time: Math.round((seconds * 1000 * i) / (keys.length - 1)),
    mode: 'orbit',
    target: [x, shot.height, y],
    alpha: k.alpha,
    beta: k.beta,
    radius: k.radius,
    easing: 'ease',
  }));
  return { version: 1, durationMs: seconds * 1000, cameraKeys, cues: [] };
}

/** Which development state the take shows. */
function sourceState() {
  const git = (a) => execFileSync('git', a, { cwd: REPO_ROOT, encoding: 'utf8' }).trim();
  const dirtyFiles = git(['status', '--porcelain', '--', 'razarion-frontend/src', 'razarion-share/src', 'razarion-client-teavm/src', 'razarion-client-worker-teavm/src']);
  return { commit: git(['rev-parse', '--short', 'HEAD']), dirty: dirtyFiles.length > 0 };
}

function appendLog(takes) {
  const log = existsSync(LOG_FILE) ? JSON.parse(readFileSync(LOG_FILE, 'utf8')) : [];
  log.push(...takes);
  writeFileSync(LOG_FILE, JSON.stringify(log, null, 2) + '\n');
}

function waitForConsole(page, pattern, timeout, message) {
  return page.waitForEvent('console', { predicate: (m) => pattern.test(m.text()), timeout })
    .catch(() => { throw new Error(message); });
}

async function verifyTake(file, seconds) {
  const probe = await probeVideo(file);
  if (!probe) throw new Error(`ffprobe cannot read ${rel(file)}.`);
  info(`  ${rel(file)}: ${probe.width}x${probe.height}, ${probe.duration.toFixed(1)}s, ${probe.fps.toFixed(1)} fps`);
  if (probe.fps < MIN_FPS || probe.duration < seconds * 0.8) {
    throw new Error(`Take ${rel(file)} is broken: ${probe.duration.toFixed(2)}s at ${probe.fps.toFixed(1)} fps.`);
  }
}

const slugOf = (s) => s.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
const stamp = () => new Date().toISOString().replace(/[-:]/g, '').replace('T', '-').slice(0, 13);
const rel = (f) => (f.startsWith(PIPELINE_ROOT) ? f.slice(PIPELINE_ROOT.length + 1).replace(/\\/g, '/') : f);

main().catch((e) => {
  fail(e.message);
  process.exit(1);
});
