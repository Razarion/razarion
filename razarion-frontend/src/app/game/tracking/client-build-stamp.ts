import {FirstInteractionTrackerService} from './first-interaction-tracker.service';

/**
 * Which build of the client this browser is actually running.
 *
 * On 2026-08-31 a phone in the Meta in-app browser ran the previous bundle for minutes after a
 * deploy and only picked up the new one once the cache was cleared by hand - although the server
 * sends `Cache-Control: no-cache, must-revalidate` for /game/index.html and every bundle below it
 * carries a content hash. Nobody clears a cache in an ad funnel, so the in-app cohorts may be
 * playing a version we are not looking at, and every measurement taken there is then about an
 * unknown build. Two deploys that "showed no effect" are worth re-reading in that light.
 *
 * The bundle's file name is the honest answer: the build changes it on every content change, and
 * a browser holding a stale index.html asks for the old name. Nothing needs to be injected at
 * build time for this - the page already says it.
 *
 * Not read from the inline script in index.html, which is where the rest of the startup beacon
 * lives: that script runs in the head, and the hashed script tags are at the end of the body, so
 * at PAGE_LOADED time there is nothing to read. Here the document is complete.
 *
 * The page carries exactly one main bundle, so the first match is the right one. `root` exists
 * because a test page does not: Karma serves a main.js of its own, and a test that worked around
 * that by not searching the document would not be testing this.
 */
export function clientBuildStamp(root: ParentNode = document): string {
  for (const script of Array.from(root.querySelectorAll('script[src]'))) {
    const src = (script as HTMLScriptElement).src;
    const name = src.substring(src.lastIndexOf('/') + 1);
    // main-SMERQXAU.js in a production build, main.js from the dev server.
    if (/^main([.-]|$)/.test(name)) {
      return name;
    }
  }
  return 'unknown';
}

/**
 * Reports the build once per session, from the renderer's setup.
 * <p>
 * Reported unconditionally rather than only when something looks wrong: a field written only on
 * failure cannot tell "the build was current" from "nothing ever checked", which is exactly what
 * made WASM_LOAD unreadable and what the capability probe had to repair.
 */
export function reportClientBuild(tracker: FirstInteractionTrackerService,
                                  root: ParentNode = document): void {
  try {
    tracker.report('CLIENT_BUILD', 'build=' + clientBuildStamp(root).replace(/[,=]/g, '_'));
  } catch (ignored) {
    // Telemetry is never a reason for a game not to start.
  }
}
