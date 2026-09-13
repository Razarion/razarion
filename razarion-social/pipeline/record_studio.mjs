#!/usr/bin/env node
// Films a studio scene without a person in front of it.
//
//   node record_studio.mjs --scene "Badger vs Radar" --both      # portrait and landscape, one run
//   node record_studio.mjs --scene "Badger vs Radar"
//   node record_studio.mjs --scene "Badger vs Radar" --seconds 12 --out data/clips/badger.mp4
//   node record_studio.mjs --scene "Tesla" --url https://www.razarion.com/studio/scenes
//   node record_studio.mjs --scene "Tesla" --head          # watch it work
//
// Everything the recording needs must already be in the scene: the camera, and - for a scene that
// fires - the attack loop, saved per item as "Loop on open". The recorder cannot click an item in
// the viewport, and a scene that only fires after a click would be filmed standing still.
//
// --both films the scene twice in one page, portrait first, because the networks want both shapes:
// Instagram, Facebook and YouTube Shorts are cut from the portrait take, X from the landscape one.
// The scene is loaded once; only the resolution changes between the takes.
//
// The run verifies its own result. A clip whose frame rate collapsed is the one failure this
// pipeline has actually produced (a recording in a background tab returned 5 frames spanning
// 0.17s and looked like a broken container), and it is invisible until someone plays the file.

import { mkdirSync } from 'node:fs';
import { basename, dirname, extname, join, resolve } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { withStudioPage, renderingCapabilities, isSoftwareRenderer, assertPageUsable } from './lib/browser.mjs';
import { probeVideo } from './lib/video.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_URL = 'http://localhost:4300/scenes';
/** Dev server in front of a local backend: the token has to come from the backend. */
const DEV_API_BASE = 'http://127.0.0.1:8080';

const PORTRAIT = '1080 × 1920 (FHD portrait)';
const LANDSCAPE = '1920 × 1080 (FHD landscape)';

/** A clip this far below the requested frame rate is a failed take, not a slow one. */
const MIN_FPS = 20;

function usage() {
  info('node record_studio.mjs --scene "<name>" [--seconds 5|8|12|20|30] [--out <file>]');
  info('                       [--both | --resolution "1920 × 1080 (FHD landscape)"]');
  info('                       [--url <scenes page>]');
  info('                       [--settle <seconds>] [--head]');
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  if (args.help || !args.scene) {
    usage();
    if (!args.scene) throw new Error('Which scene? Pass --scene "<name>".');
    return;
  }

  const sceneName = String(args.scene);
  const seconds = args.seconds ? Number(args.seconds) : 12;
  const url = String(args.url ?? DEFAULT_URL);
  if (args.both && args.resolution) throw new Error('--both records both resolutions; drop --resolution.');
  // The ground builds one tile per frame after the fetch is done, and the overlay that reports it
  // is advisory - it can clear while shaders are still compiling. Filming too early yields a clip
  // of flat green placeholder terrain, so there is a wait here even when the page says it is done.
  const settleSeconds = args.settle != null ? Number(args.settle) : 8;
  const slug = sceneName.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
  const out = resolve(PIPELINE_ROOT, String(args.out ?? join('data', 'clips', `${slug}.mp4`)));
  const apiBase = new URL(url).port === '4300' ? DEV_API_BASE : new URL(url).origin;

  // With --both the two takes are named after their shape, next to where the single one would go.
  const takes = args.both
    ? [
        { resolution: PORTRAIT, out: sibling(out, 'portrait'), shape: 'portrait' },
        { resolution: LANDSCAPE, out: sibling(out, 'landscape'), shape: 'landscape' },
      ]
    : [{ resolution: String(args.resolution ?? LANDSCAPE), out, shape: null }];

  mkdirSync(dirname(out), { recursive: true });

  info(`Scene "${sceneName}" · ${seconds}s · ${takes.map((t) => t.resolution).join(' + ')}`);
  step(`page ${url}`);

  await withStudioPage({ url, apiBase, headless: !args.head }, async (page) => {
    await assertPageUsable(page);
    const caps = await renderingCapabilities(page);
    step(`renderer ${caps.renderer ?? 'none'}`);
    if (isSoftwareRenderer(caps.renderer)) {
      throw new Error(
        `This browser is rendering in software (${caps.renderer}). A capture would take minutes ` +
        'per second of footage; fix the GPU before filming.'
      );
    }
    if (!caps.mp4) {
      warn('No H.264/MP4 encoder here - the clip will come out as WebM and needs a conversion.');
    }

    // The scene list is the first thing the page fetches; without a valid token it stays empty and
    // every later step would time out on a missing element.
    const sceneRow = page.locator('.scene-list li', { hasText: sceneName }).first();
    await sceneRow.waitFor({ state: 'visible', timeout: 30_000 }).catch(() => {
      throw new Error(`No scene called "${sceneName}" in the list — is the token valid for ${apiBase}?`);
    });
    await sceneRow.click();
    step('scene opened, waiting for models and ground');

    // Model library, then terrain. whenTerrainReady() is known to occasionally not settle, so the
    // overlay disappearing is treated as good news rather than a requirement.
    await page.locator('.loading-overlay').waitFor({ state: 'detached', timeout: 180_000 })
      .catch(() => warn('The loading overlay never cleared - filming anyway after the settle wait.'));
    await page.waitForTimeout(settleSeconds * 1000);

    await selectByLabel(page, 'Clip length', `${seconds} seconds`);

    // Anything selected draws its gizmo arrows into the recording.
    const deselect = page.getByRole('button', { name: 'Deselect' });
    if (await deselect.count()) await deselect.click();

    for (const take of takes) {
      await selectByLabel(page, 'Resolution', take.resolution);
      step(`recording ${seconds}s${take.shape ? ` ${take.shape}` : ''}`);
      await page.getByRole('button', { name: 'Record clip' }).click();

      const saveButton = page.getByRole('button', { name: 'Save clip' });
      await saveButton.waitFor({ state: 'visible', timeout: (seconds + 60) * 1000 });

      const [download] = await Promise.all([
        page.waitForEvent('download', { timeout: 120_000 }),
        saveButton.click(),
      ]);
      await download.saveAs(take.out);
      // Checked before the next take rather than at the end: a broken first take would otherwise
      // cost the full length of the second one before anyone hears about it.
      await verifyTake(take.out, seconds);
    }
  });

  ok(takes.length > 1 ? 'Both clips recorded.' : 'Clip recorded.');
  if (takes.length > 1) {
    info(`  Next: node compose.mjs --portrait ${relativeToPipeline(takes[0].out)} ` +
      `--landscape ${relativeToPipeline(takes[1].out)} --text "..."`);
  } else {
    info(`  Next: node compose.mjs --media ${relativeToPipeline(out)} --text "..."`);
  }
}

async function verifyTake(file, seconds) {
  const probe = await probeVideo(file);
  if (!probe) {
    throw new Error(`ffprobe cannot read ${file} — the download did not produce a usable file.`);
  }
  info(`  ${file}`);
  info(`  ${probe.width}x${probe.height}, ${probe.duration.toFixed(1)}s, ${probe.fps.toFixed(1)} fps, ` +
    `${(probe.bytes / 1024 / 1024).toFixed(1)} MB, ${probe.videoCodec}`);

  if (probe.fps < MIN_FPS || probe.duration < seconds * 0.8) {
    throw new Error(
      `That take is broken: ${probe.duration.toFixed(2)}s at ${probe.fps.toFixed(1)} fps for a ` +
      `${seconds}s clip. The frames were not drawn - check that the page was rendering.`
    );
  }
}

/** Pick an option in the <select> that sits in the settings row with this label. */
async function selectByLabel(page, label, optionLabel) {
  const select = page.locator('.prop-row', { hasText: label }).locator('select').first();
  await select.selectOption({ label: optionLabel });
}

/** data/clips/badger.mp4 -> data/clips/badger-portrait.mp4 */
function sibling(file, suffix) {
  return join(dirname(file), `${basename(file, extname(file))}-${suffix}.mp4`);
}

function relativeToPipeline(file) {
  return file.startsWith(PIPELINE_ROOT) ? file.slice(PIPELINE_ROOT.length + 1).replace(/\\/g, '/') : file;
}

main().catch((e) => {
  fail(e.message);
  process.exitCode = 1;
});
