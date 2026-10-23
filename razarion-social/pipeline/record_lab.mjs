#!/usr/bin/env node
// Films the studio's terrain lab: the same camera move once per state of the lab's experiments, so
// the takes can be cut before/after - the lab's counterpart of record_showcase.mjs.
//
//   node record_lab.mjs --shot wall --states "cliffStep,rockRimGravel;cliffStep,grassFoot" --label grass-foot
//   node record_lab.mjs --shot wall --states "...;..." --stills          # one still per state + side by side
//   node record_lab.mjs --shot ridge --states "none;cliffStep" --both    # landscape and portrait
//   node record_lab.mjs --list                                           # the shots
//
// The lab (razarion-frontend/projects/studio, /terrain-lab) renders the game's ground over planet
// 117 without the game engine; its experiments (terrain-experiments.ts) exist only there, so the
// director cannot film them. The page is the studio dev server (default http://localhost:4300) in
// front of this checkout's backend; window.razLab switches the experiments and moves the camera.
//
// Only the canvas is recorded (MediaRecorder on canvas.captureStream), so the panel never reaches
// the file. Takes are appended to data/lab/log.json with the commit and whether the working tree
// had uncommitted changes.

import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { withStudioPage, renderingCapabilities, isSoftwareRenderer } from './lib/browser.mjs';
import { FFMPEG, probeVideo } from './lib/video.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_URL = 'http://localhost:4300/terrain-lab';
const OUT_DIR = join(PIPELINE_ROOT, 'data', 'lab');
const LOG_FILE = join(OUT_DIR, 'log.json');
const REPO_ROOT = join(PIPELINE_ROOT, '..', '..');
const MIN_FPS = 20;

const SHAPES = {
  portrait: { width: 1080, height: 1920 },
  landscape: { width: 1920, height: 1080 },
};

/** terrain-experiments.ts */
const EXPERIMENTS = ['cliffStep', 'rockRimGravel', 'grassFoot'];

/**
 * Camera moves, as in record_showcase.mjs: game x/y, orbit at elevation beta and distance radius.
 * tiles: how many 160 m tiles the lab loads from 0,0 - its 7 x 6 cast shadows for 42 ms a frame,
 * which films at 12 fps; 2 x 6 around the ridge draws at 60.
 */
const SHOTS = {
  ridge: {
    about: 'the rock ridge in the south-west, low (record_showcase ridge)',
    target: [117, 800], height: 6, tiles: [2, 6],
    keys: [{ alpha: Math.PI - 0.4, beta: 0.8, radius: 55 }, { alpha: Math.PI, beta: 0.7, radius: 48 }],
  },
  beach: {
    about: 'the lab\'s Beach view in the north-west, a slow turn over sand and shallows',
    target: [66, 275], height: 2, tiles: [2, 3],
    keys: [{ alpha: -0.5, beta: 1.0, radius: 50 }, { alpha: 0.4, beta: 0.95, radius: 44 }],
  },
  wall: {
    about: 'close along the foot of the ridge wall',
    target: [112, 812], height: 4, tiles: [2, 6],
    keys: [{ alpha: Math.PI - 0.5, beta: 0.55, radius: 30 }, { alpha: Math.PI - 0.15, beta: 0.5, radius: 26 }],
  },
};

function usage() {
  info('node record_lab.mjs --shot <name> --states "none;cliffStep,grassFoot" [--label <word>] [--stills]');
  info('                    [--seconds 14] [--both | --shape landscape|portrait] [--settle 8] [--url http://localhost:4300/terrain-lab] [--head]');
  info('node record_lab.mjs --list');
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
  const url = String(args.url ?? DEFAULT_URL);
  if (!/^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?\//.test(url)) throw new Error(`The lab films a local build, not ${url}.`);
  const seconds = Number(args.seconds ?? 14);
  const settleSeconds = Number(args.settle ?? 8);
  const shapes = args.both ? ['landscape', 'portrait'] : [String(args.shape ?? 'landscape')];
  for (const s of shapes) if (!SHAPES[s]) throw new Error(`Unknown shape "${s}" - portrait or landscape.`);
  const states = statesOf(String(args.states ?? 'none'));
  const label = slugOf(String(args.label ?? 'take'));
  const source = sourceState();
  mkdirSync(OUT_DIR, { recursive: true });
  const runStamp = stamp();

  info(`Lab shot ${args.shot}: ${shot.about}`);
  info(`${states.length} state(s) x ${shapes.join(' + ')}, ${args.stills ? 'stills' : `${seconds}s each`} · ${source.commit}${source.dirty ? ' + uncommitted changes' : ''}`);

  const takes = [];
  const stills = [];
  await withStudioPage({ url, apiBase: backendOrigin(url), headless: !args.head, viewport: { width: 1600, height: 1000 } }, async (page) => {
    const caps = await renderingCapabilities(page);
    step(`renderer ${caps.renderer ?? 'none'}`);
    if (isSoftwareRenderer(caps.renderer)) throw new Error(`Software rendering (${caps.renderer}); fix the GPU before filming.`);
    await waitForTiles(page, 'The lab never finished loading its tiles - logged in, backend up?');
    const [tilesX, tilesY] = shot.tiles ?? [7, 6];
    if (tilesX !== 7 || tilesY !== 6) {
      step(`loading ${tilesX} x ${tilesY} tiles`);
      await page.getByLabel('Tiles x').fill(String(tilesX));
      await page.getByLabel('y', { exact: true }).fill(String(tilesY));
      await page.getByRole('button', { name: 'Reload tiles' }).click();
      await page.waitForFunction((n) => document.body.innerText.includes(`${n} tiles in`), tilesX * tilesY, { timeout: 240_000 });
    }

    // Each switch rebuilds all tiles: only the ones that change
    let current = [];
    for (const [stateNo, state] of states.entries()) {
      const stateName = state.length ? state.join('+') : 'none';
      step(`state ${stateName}: building the tiles`);
      for (const e of EXPERIMENTS) {
        if (state.includes(e) !== current.includes(e)) {
          await page.evaluate(([name, on]) => window.razLab.experiment(name, on), [e, state.includes(e)]);
        }
      }
      current = state;
      await waitForTiles(page, `The tiles for ${stateName} never finished.`);
      for (const shape of shapes) {
        await sizeCanvas(page, SHAPES[shape]);
        await page.evaluate((s) => window.razLab.orbit(s.target[0], s.height, s.target[1], s.keys[0].alpha, s.keys[0].beta, s.keys[0].radius), shot);
        // Sprites and shadows of the new view, shaders of the rebuilt tiles
        await page.waitForTimeout(settleSeconds * 1000);
        const base = join(OUT_DIR, `${runStamp}-${args.shot}-${label}-${String(stateNo + 1).padStart(2, '0')}-${stateName}-${shape}`);
        if (args.stills) {
          await page.locator('canvas.view').screenshot({ path: `${base}.png` });
          stills.push({ shape, file: `${base}.png` });
          info(`  ${rel(base)}.png`);
          continue;
        }
        const fps = await page.evaluate(() => window.razLab.scene().getEngine().getFps());
        step(`recording ${shape}, ${stateName} (page at ${fps.toFixed(0)} fps)`);
        const download = page.waitForEvent('download', { timeout: (seconds + 60) * 1000 });
        await page.evaluate(recordInPage, { shot, seconds });
        await (await download).saveAs(`${base}.mp4`);
        try {
          await verifyTake(`${base}.mp4`, seconds);
          takes.push({ file: rel(`${base}.mp4`), shot: String(args.shot), label, shape, experiments: state, ...source, recordedAt: new Date().toISOString() });
        } catch (e) {
          warn(e.message);
        }
      }
    }
  });

  if (args.stills) {
    for (const shape of shapes) {
      const files = stills.filter((s) => s.shape === shape).map((s) => s.file);
      if (files.length < 2) continue;
      const out = join(OUT_DIR, `${runStamp}-${args.shot}-${label}-compare-${shape}.png`);
      const inputs = files.flatMap((f) => ['-i', f]);
      execFileSync(FFMPEG, ['-y', '-loglevel', 'error', ...inputs, '-filter_complex', `${shape === 'portrait' ? 'h' : 'v'}stack=inputs=${files.length}`, out]);
      ok(`side by side: ${rel(out)}`);
    }
    return;
  }
  if (takes.length) appendLog(takes);
  if (!takes.length) throw new Error('No usable take.');
  ok(`${takes.length} take(s) recorded, logged in ${rel(LOG_FILE)}.`);
}

/** "none;cliffStep,grassFoot" -> [[], ['cliffStep', 'grassFoot']] */
function statesOf(text) {
  return text.split(';').map((s) => {
    const list = s.trim() === 'none' ? [] : s.split(',').map((e) => e.trim()).filter(Boolean);
    for (const e of list) if (!EXPERIMENTS.includes(e)) throw new Error(`Unknown experiment "${e}". Known: ${EXPERIMENTS.join(', ')}`);
    return list;
  });
}

/** The backend behind the dev server signs the token: RAZ_SERVER_PORT of this checkout, as proxy.conf.js. */
function backendOrigin(url) {
  if (new URL(url).port !== '4300') return new URL(url).origin;
  try {
    const env = readFileSync(join(REPO_ROOT, 'razarion-server', 'docker', '.env'), 'utf8');
    const port = env.match(/^\s*RAZ_SERVER_PORT\s*=\s*(\d+)/m)?.[1];
    return `http://127.0.0.1:${port ?? 8080}`;
  } catch {
    return 'http://127.0.0.1:8080';
  }
}

/** The lab's status line reads "42 tiles in 30.1 s" once a load or reload is done. */
async function waitForTiles(page, message) {
  await page.waitForFunction(() => !!window.razLab && /\d+ tiles in/.test(document.body.innerText), null, { timeout: 240_000 })
    .catch(() => { throw new Error(message); });
}

/** The page grown until the canvas (beside the panel, under the nav bar) has exactly the take's size. */
async function sizeCanvas(page, size) {
  for (let i = 0; i < 3; i++) {
    const box = await page.locator('canvas.view').boundingBox();
    const viewport = page.viewportSize();
    if (Math.round(box.width) === size.width && Math.round(box.height) === size.height) return;
    await page.setViewportSize({ width: viewport.width + size.width - Math.round(box.width), height: viewport.height + size.height - Math.round(box.height) });
    await page.waitForTimeout(1500);
  }
  throw new Error(`The canvas would not take ${size.width}x${size.height}.`);
}

/** Runs in the page: the camera along the shot's keys while MediaRecorder films the canvas; the file comes as a download. */
async function recordInPage({ shot, seconds }) {
  const canvas = document.querySelector('canvas.view');
  const mime = MediaRecorder.isTypeSupported('video/mp4;codecs=avc1') ? 'video/mp4;codecs=avc1' : 'video/webm;codecs=vp9';
  // One video frame per rendered frame, as the director records (a timed captureStream(30) came out
  // at 12 fps with the page drawing 25)
  const stream = canvas.captureStream(0);
  const track = stream.getVideoTracks()[0];
  const scene = window.razLab.scene();
  const observer = scene.onAfterRenderObservable.add(() => track.requestFrame());
  const recorder = new MediaRecorder(stream, { mimeType: mime, videoBitsPerSecond: 16_000_000 });
  const chunks = [];
  recorder.ondataavailable = (e) => { if (e.data.size) chunks.push(e.data); };
  const stopped = new Promise((resolve) => { recorder.onstop = resolve; });
  // As the director's 'ease' between evenly spaced keys
  const pose = (t) => {
    const keys = shot.keys.length > 1 ? shot.keys : [shot.keys[0], shot.keys[0]];
    const span = t * (keys.length - 1);
    const i = Math.min(keys.length - 2, Math.floor(span));
    const f = span - i;
    const e = f * f * (3 - 2 * f);
    const mix = (k) => keys[i][k] + (keys[i + 1][k] - keys[i][k]) * e;
    window.razLab.orbit(shot.target[0], shot.height, shot.target[1], mix('alpha'), mix('beta'), mix('radius'));
  };
  pose(0);
  recorder.start(1000);
  const start = performance.now();
  await new Promise((resolve) => {
    const tick = () => {
      const t = (performance.now() - start) / 1000 / seconds;
      pose(Math.min(1, t));
      if (t >= 1) resolve(); else requestAnimationFrame(tick);
    };
    tick();
  });
  recorder.stop();
  await stopped;
  scene.onAfterRenderObservable.remove(observer);
  stream.getTracks().forEach((t) => t.stop());
  const link = document.createElement('a');
  link.href = URL.createObjectURL(new Blob(chunks, { type: mime }));
  link.download = mime.startsWith('video/mp4') ? 'lab.mp4' : 'lab.webm';
  link.click();
}

/** Which development state the take shows. */
function sourceState() {
  const git = (a) => execFileSync('git', a, { cwd: REPO_ROOT, encoding: 'utf8' }).trim();
  const dirtyFiles = git(['status', '--porcelain', '--', 'razarion-frontend/src', 'razarion-frontend/projects/studio', 'razarion-share/src']);
  return { commit: git(['rev-parse', '--short', 'HEAD']), dirty: dirtyFiles.length > 0 };
}

function appendLog(takes) {
  const log = existsSync(LOG_FILE) ? JSON.parse(readFileSync(LOG_FILE, 'utf8')) : [];
  log.push(...takes);
  writeFileSync(LOG_FILE, JSON.stringify(log, null, 2) + '\n');
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
