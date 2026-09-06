/**
 * The page's loading screen, as seen from inside the application.
 *
 * index.html draws a world being built while the bundle arrives, and it is the only thing that can
 * draw at all before Angular exists. Once Angular does exist there are two things it needs from
 * here: the real progress of the engine's boot, which is the only honest source after the
 * JavaScript has landed, and the moment the terrain is actually on screen, which is when the
 * loading screen has nothing left to cover.
 *
 * That moment is later than it looks. The first engine tick - what the startup tracking calls
 * playable - arrives while the ground is still untextured, and taking the splash away there shows
 * the player an empty plane with a few items floating on it. Waiting for the first terrain tile to
 * finish its material is the difference between "the game is running" and "the game is there".
 *
 * Every call is guarded: the functions live in index.html and are defined only for the half of the
 * players who get the animation. For the other half nothing here does anything, which is what
 * keeps them a clean control group.
 */

interface BootSplashWindow {
  RAZ_bootProgress?: (fraction: number) => void;
  RAZ_bootTerrainReady?: () => void;
  RAZ_bootFadeOut?: () => void;
}

function splashWindow(): BootSplashWindow {
  return window as unknown as BootSplashWindow;
}

/** Whether the page is running the animated loading screen and expects to be told about progress. */
export function isAnimatedSplash(): boolean {
  return typeof splashWindow().RAZ_bootProgress === 'function';
}

/**
 * How far the engine's boot has come, 0 to 1. Drives the second half of the build-up: the first
 * half is the JavaScript arriving, which the page can measure on its own, and this is everything
 * after that - the worker, the configuration, the models.
 */
export function reportBootProgress(fraction: number): void {
  const report = splashWindow().RAZ_bootProgress;
  if (report) {
    report(Math.max(0, Math.min(1, fraction)));
  }
}

/**
 * The terrain has its material and is on screen. Takes the loading screen away.
 *
 * Called on the first tile that finishes, not on every one: the rest stream in behind the running
 * game, which is what they are built to do.
 */
export function reportTerrainVisible(): void {
  const ready = splashWindow().RAZ_bootTerrainReady;
  if (ready) {
    ready();
  }
}

/**
 * Take the loading screen away regardless.
 *
 * The safety net under {@link reportTerrainVisible}: a terrain that never finishes must not leave
 * the player on a loading screen forever, which would be worse than the abrupt handover this
 * replaced. Also the path for the unanimated half, where the page offers no fade at all.
 */
export function removeSplash(): void {
  const fadeOut = splashWindow().RAZ_bootFadeOut;
  if (fadeOut) {
    fadeOut();
  } else {
    document.getElementById('raz-boot')?.remove();
  }
}
