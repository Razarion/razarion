// A headless Chrome that renders the game the way a player's does, so a clip can be filmed
// without anyone sitting in front of it.
//
// Two facts decide the shape of this file, both measured rather than assumed:
//
//   1. Chrome stops the render loop in a tab that is not visible. A recording started in a
//      background tab came back with 5 frames spanning 0.17s instead of ~360 - the frames were
//      never drawn. Headless has no concept of occlusion, so the problem disappears; the flags
//      below cover the --head case, where a window can still end up behind another one.
//   2. Headless is NOT software rendering any more, and the installed Chrome carries the codec
//      the networks want: WebGL 2.0 through ANGLE on the real GPU, and MediaRecorder with
//      video/mp4;codecs=avc1. Playwright's bundled Chromium has neither guarantee - it is an
//      open-source build without H.264 - which is why this launches `channel: 'chrome'` and
//      playwright-core is the dependency rather than playwright.
//
// Logging in happens over REST, never through the login form: the token is minted the same way
// the rest of the pipeline authenticates and written into localStorage before the app boots.

import { chromium } from 'playwright-core';
import { adminToken } from './razarion.mjs';
import { info, warn } from '../../src/util/log.mjs';

/** Where the app keeps its JWT (see ThumbnailStorageService.login). */
const TOKEN_KEY = 'app.token';

const LAUNCH_ARGS = [
  // Only meaningful with --head: a window that is minimised or fully covered gets its timers and
  // its render loop throttled, which is the same broken recording as a background tab.
  '--disable-backgrounding-occluded-windows',
  '--disable-renderer-backgrounding',
  '--disable-background-timer-throttling',
  // A scaled desktop would otherwise make the viewport a different size than asked for.
  '--force-device-scale-factor=1',
  '--hide-scrollbars',
];

/**
 * Open a page with an admin session already in place and hand it to `fn`.
 *
 * `apiBase` is the server that signs the token — for a dev server this is the backend behind its
 * proxy, not the dev server itself.
 */
export async function withStudioPage({ url, apiBase, headless = true, viewport = { width: 1600, height: 900 }, downloadDir }, fn) {
  const origin = new URL(url).origin;
  const token = await adminToken(apiBase ?? origin);

  const browser = await chromium.launch({ channel: 'chrome', headless, args: LAUNCH_ARGS });
  try {
    const context = await browser.newContext({ viewport, acceptDownloads: true });
    // Runs before any script of the page, so the app finds the token during its own bootstrap
    // rather than rendering a logged-out shell first.
    await context.addInitScript(
      ([key, value]) => { try { localStorage.setItem(key, value); } catch { /* private mode */ } },
      [TOKEN_KEY, token],
    );

    const page = await context.newPage();
    // A WebGL page that fails does so in the console, and a silent failure here costs a whole
    // recording, so the interesting lines are surfaced rather than swallowed.
    page.on('pageerror', (e) => warn(`page error: ${e.message}`));
    page.on('console', (msg) => {
      const text = msg.text();
      if (msg.type() === 'error' || /\[Studio]|\[Director]/.test(text)) info(`  browser: ${text}`);
    });

    await page.goto(url, { waitUntil: 'domcontentloaded' });
    return await fn(page, { context, browser, downloadDir });
  } finally {
    await browser.close();
  }
}

/**
 * A dev server that failed to compile covers the page with an overlay, and every click after that
 * times out on "element intercepts pointer events" - which reads like a broken selector rather
 * than a broken build. One reload is worth trying first: the usual cause here is the Maven build
 * regenerating razarion-share.ts while the dev server was reading it, and that heals by itself.
 */
export async function assertPageUsable(page) {
  const overlay = page.locator('vite-error-overlay');
  if (!(await overlay.count())) return;

  warn('The dev server is showing a build error - reloading once in case it has recompiled since.');
  await page.reload({ waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(2000);
  if (!(await overlay.count())) return;

  const text = (await overlay.first().innerText().catch(() => '')).trim();
  throw new Error(`The dev server has a build error, so the page cannot be driven:\n${text.slice(0, 600)}`);
}

/**
 * Report what the browser actually gives us. Called once per recording run because "it rendered
 * yesterday" is not evidence: a driver update or a headless-mode change can silently drop the
 * page onto SwiftShader, and the first sign would be a clip that took ten minutes to record.
 */
export async function renderingCapabilities(page) {
  return page.evaluate(() => {
    const canvas = document.createElement('canvas');
    const gl = canvas.getContext('webgl2') || canvas.getContext('webgl');
    const dbg = gl && gl.getExtension('WEBGL_debug_renderer_info');
    const mp4 = typeof MediaRecorder !== 'undefined'
      && MediaRecorder.isTypeSupported('video/mp4;codecs=avc1.42E01E');
    return {
      renderer: gl ? (dbg ? gl.getParameter(dbg.UNMASKED_RENDERER_WEBGL) : gl.getParameter(gl.RENDERER)) : null,
      mp4,
    };
  });
}

/** True when the renderer string looks like a software fallback rather than a GPU. */
export function isSoftwareRenderer(renderer) {
  return !renderer || /swiftshader|llvmpipe|software/i.test(renderer);
}
