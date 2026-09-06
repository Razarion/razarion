import {environment} from '../environments/environment';

/**
 * Fetching the WebAssembly client while the page is still downloading Angular.
 *
 * The client used to be requested from {@code GameComponent.startGame()}, which is the last thing
 * that happens in a start: the browser had to download and parse 2.4 MB of JavaScript, boot
 * Angular, resolve a route, construct the component and wait out an HTTP round trip for the token
 * before the first byte of the game engine was asked for. On an in-app webview that is six and a
 * half seconds in which nothing of the game is on the wire - measured 04.09.2026, and 76% of all
 * aborted starts happen inside that window.
 *
 * Nothing in the download needs Angular. The module is 613 KB and compiles while the bundle is
 * still arriving; only the call into {@code main} needs a wired facade, because it reaches
 * straight for the cockpit adapters. So the two are separated: the fetch starts from main.ts
 * before {@code bootstrapApplication}, and {@link notifyAngularReady} releases the call once the
 * facade is up. Whichever of the two finishes second starts the engine, so neither order can lose.
 */
export interface WasmBootLatch {
  /** Angular has wired the facade; safe to call into the module. */
  angularReady(): void;

  /** The module is compiled. Called from client-bootstrap.js, which holds the entry point. */
  wasmReady(runMain: () => void): void;
}

const SCRIPT_URL = '/teavm-client/client-bootstrap.js';

/**
 * A fresh latch. Built by a factory rather than written once at module level so that a test can
 * hold one of its own: the whole point of the thing is the order of two events, and a shared
 * instance would carry the first test's order into the second.
 */
export function createBootLatch(): WasmBootLatch {
  let angularReady = false;
  let runMain: (() => void) | null = null;

  function startIfBothReady(): void {
    if (angularReady && runMain) {
      // Taken before the call: main() runs the whole boot sequence, and it must not be entered
      // twice if anything in there were to come back through here.
      const main = runMain;
      runMain = null;
      main();
    }
  }

  return {
    angularReady: () => {
      angularReady = true;
      startIfBothReady();
    },
    wasmReady: main => {
      runMain = main;
      startIfBothReady();
    }
  };
}

const latch = createBootLatch();

/**
 * Asks for the client and its module. Idempotent: a second call finds its own script tag and does
 * nothing, which is what makes it safe to call again from {@link notifyAngularReady}.
 *
 * The timestamp is not a cache buster for the module - that one is versioned by build stamp and
 * cached for a year. It is for this file, which carries the stamp and must therefore always be
 * the one belonging to the running deployment.
 */
export function startWasmDownload(): void {
  (window as any).RAZ_boot = latch;
  const scripts = document.getElementsByTagName('script');
  for (let i = scripts.length; i--;) {
    if (scripts[i].src.indexOf(SCRIPT_URL) >= 0) {
      return;
    }
  }
  const script = document.createElement('script');
  script.src = SCRIPT_URL + '?t=' + new Date().getTime();
  script.type = 'text/javascript';
  script.charset = 'utf-8';
  document.getElementsByTagName('head')[0].appendChild(script);
}

/**
 * The facade is wired. Starts the download too, for the one path that never passed main.ts: a
 * client-side navigation out of /backend into the game.
 */
export function notifyAngularReady(): void {
  startWasmDownload();
  latch.angularReady();
}

/**
 * Whether this page should fetch the engine at all. The admin backend has no game, and the mock
 * build replaces the engine with {@code GameMockService} - fetching the real one there would run
 * two clients against one facade.
 */
export function shouldStartWasmEarly(pathname: string = location.pathname): boolean {
  return !environment.gwtMock && pathname.indexOf('/backend') < 0;
}
