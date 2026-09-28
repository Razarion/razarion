/**
 * Fetching the game engine's web worker while the page is still loading (2026-09-28).
 *
 * The worker is created by the WebAssembly client once the client is running - on a phone some
 * nine seconds in - and only then asks for its runtime and its module. Until the worker has them
 * nothing else can start: INIT_WORKER, the step that releases the game, waits for it. The terrain
 * it needs next is already fetched ahead (terrain-prefetch.ts); this does the same for the worker
 * itself. Measured on 27.09. the worker's start had become the limit of the whole start.
 * <p>
 * Both files are versioned by the build stamp and served immutable for a year, so a response
 * fetched here sits in the HTTP cache when the worker asks for the identical URL. The stamp is
 * read from the worker's own bootstrap script rather than assumed: it is set per module by Maven,
 * and a guessed stamp that differs by a second would download 270 KB nobody uses.
 * <p>
 * Low priority, because the Angular bundle and the client module arriving at the same time are
 * needed first; the worker is needed right after them.
 * <p>
 * Local server, phone profile (150 ms, 5 Mbit/s, CPU x4, empty cache), 3 runs each: RUN_GAME
 * 14.6 -> 13.2 s. The worker's module now comes from the cache the moment the worker asks, and
 * INIT_WORKER follows 0.6 s after the worker starts instead of 3 s. It costs the bundle about
 * 0.7 s of bandwidth on the way (local HTTP/1.1; PROD speaks HTTP/2, where the low priority can
 * actually act). Starting it only after Angular had booted changed nothing - that happens after
 * about a second, long before the bundle's heavy part.
 */
const WORKER_BASE = '/teavm-worker/';
const BOOTSTRAP = 'worker-bootstrap.js';
const BUILD_PATTERN = /var BUILD = '([^']+)'/;

/** The two files the worker asks for, exactly as worker-bootstrap.js names them. */
export function workerFileUrls(build: string): string[] {
  return [
    WORKER_BASE + 'classes.wasm-runtime.js?v=' + build,
    WORKER_BASE + 'razarion-worker.wasm?v=' + build
  ];
}

/** The build stamp in worker-bootstrap.js, or null if the script does not carry one (unsubstituted). */
export function readWorkerBuild(bootstrapSource: string): string | null {
  const match = BUILD_PATTERN.exec(bootstrapSource);
  if (!match || match[1].indexOf('${') >= 0) {
    return null;
  }
  return match[1];
}

export function prefetchWorker(win: any = window): void {
  if (typeof win.fetch !== 'function') {
    return;
  }
  const low = {priority: 'low'};
  win.fetch(WORKER_BASE + BOOTSTRAP + '?t=' + Date.now(), low)
    .then((response: Response) => response.ok ? response.text() : '')
    .then((source: string) => {
      const build = readWorkerBuild(source);
      if (!build) {
        return;
      }
      for (const url of workerFileUrls(build)) {
        // Read to the end: a body nobody pulls may be held back, and a half-cached response is no
        // use to the worker.
        win.fetch(url, low).then((response: Response) => response.arrayBuffer()).catch(() => {
        });
      }
    })
    .catch(() => {
      // A head start, never a condition: the worker fetches what it needs itself.
    });
}
