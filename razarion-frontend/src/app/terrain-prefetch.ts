/**
 * Fetching the terrain a new player's worker is about to ask for, while the page is still loading.
 *
 * INIT_WORKER, the step that releases the game, waits for the terrain shape and the height map -
 * 3.1 MB that the worker used to ask for only once it was running, some eight seconds into a phone's
 * start. Fetched from here instead, the responses sit in the HTTP cache when the worker asks: they
 * are served no-cache with an entity tag, so the worker's own fetch costs a 304 and no body.
 * Chrome against PROD with a phone's network, 2026-09-27: the height map alone took the start from
 * 15.9-16.3 s to 14.9-15.0 s.
 * <p>
 * Only for a visitor without a login token. Everybody else may be on another planet by now, and the
 * list is the starter planet's; a registered player loses nothing but the head start.
 */
const PREFETCH_URL = '/rest/terrainshape/prefetch';
const TOKEN_KEY = 'app.token';

export function prefetchTerrain(win: any = window): void {
  try {
    if (win.localStorage && win.localStorage.getItem(TOKEN_KEY)) {
      return;
    }
  } catch (e) {
    // Storage switched off: an anonymous visitor as far as this is concerned.
  }
  if (typeof win.fetch !== 'function') {
    return;
  }
  win.fetch(PREFETCH_URL)
    .then((response: Response) => response.ok ? response.json() : [])
    .then((urls: string[]) => {
      for (const url of urls || []) {
        // Read to the end: a body nobody pulls may be held back, and a half-cached response is no
        // use to the worker.
        win.fetch(url).then((response: Response) => response.arrayBuffer()).catch(() => {
        });
      }
    })
    .catch(() => {
      // A head start, never a condition: the worker fetches what it needs itself.
    });
}
