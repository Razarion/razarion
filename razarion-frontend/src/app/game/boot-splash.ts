/**
 * The page's loading screen, as seen from inside the application.
 *
 * index.html paints a card, a bar and three tips on the first paint, before the Angular bundle has
 * been fetched - it is the only thing that can draw at all before Angular exists. Once Angular
 * does exist it takes over with its own cover, and the page's splash has to go.
 *
 * There was more here for five days. A second loading screen drew the world being built on a
 * canvas and asked to be told the engine's real progress and the moment the terrain was on
 * screen, so that it could dissolve into the running game instead of being cut away. It was
 * measured against this one and lost by six points - see the epitaph in index.html - and the
 * three functions that served it went with it.
 */

/**
 * Take the page's loading screen away. Called once Angular's own cover is up, so there is no
 * moment without something on screen.
 */
export function removeSplash(): void {
  document.getElementById('raz-boot')?.remove();
}

/**
 * Size the loading-screen map (bmap=map, index.html) to the room left under the card. The page
 * defines it, because it has to run before Angular exists; absent outside the map arm.
 */
export function fitBootMap(): void {
  (window as { RAZ_fitBootMap?: () => void }).RAZ_fitBootMap?.();
}

/** As long as the cover's own fade (.cover-panel-fadeout), so the map and the card leave together. */
const BOOT_MAP_FADE_MS = 2000;

/**
 * Take the loading-screen map away (the bmap=map arm, see index.html). It outlives the splash on
 * purpose and goes with Angular's cover, once the game runs: faded like the cover, then removed.
 */
export function removeBootMap(): void {
  const map = document.getElementById('raz-bmap');
  if (!map) {
    return;
  }
  map.style.opacity = '0';
  setTimeout(() => {
    map.remove();
    document.documentElement.classList.remove('raz-bmap');
  }, BOOT_MAP_FADE_MS);
}
