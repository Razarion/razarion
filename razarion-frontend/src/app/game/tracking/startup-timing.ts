import {FirstInteractionTrackerService} from './first-interaction-tracker.service';

/**
 * Where the seconds before the game go.
 *
 * Half of the sessions from the Meta in-app browser are lost in one step, between PAGE_LOADED and
 * WASM_BOOTSTRAP: 215 sessions in fourteen days, 108 of them gone before the engine ever started.
 * It is not the device - of the 46 that reported their capabilities, every one had wasm, wasmgc and
 * webgl2. It is the wait: 5.7s median to WASM_BOOTSTRAP in-app against 2.1s on a desktop, and 14.3s
 * to a running game.
 *
 * "Make it smaller" and "show something sooner" are different repairs, and the split between
 * downloading, parsing and booting decides which one is worth building. Nothing measured that so
 * far, so the choice would have been a guess.
 *
 * Reported at the earliest point Angular can speak, not from the renderer: the sessions this exists
 * for are the ones that never reach a renderer. It still cannot describe the ones that leave before
 * Angular boots at all - they are gone before any code of ours runs - so read these numbers as the
 * survivors' timing, and the abandoners' as the same or worse.
 */
export interface StartupTiming {
  /** Document response complete, in ms since navigation start. */
  ttfb: number;
  domContentLoaded: number;
  /** Now - the moment Angular got here. */
  boot: number;
  /** Scripts and stylesheets the page pulled, and what they actually cost on the wire. */
  fileCount: number;
  transferredKb: number;
  /** How many came from the cache: transferSize 0 with a body. A stale in-app browser shows here. */
  cachedCount: number;
  /** First byte requested to last byte arrived, across all of them. */
  downloadMs: number;
  /** Last byte arrived to here: parsing, executing, and Angular starting up. */
  parseAndBootMs: number;
}

const ASSET = /\.(js|css)(\?|$)/;

export function collectStartupTiming(perf: Performance = performance): StartupTiming | null {
  try {
    const nav = perf.getEntriesByType('navigation')[0] as PerformanceNavigationTiming | undefined;
    const assets = (perf.getEntriesByType('resource') as PerformanceResourceTiming[])
      .filter(entry => ASSET.test(entry.name));

    let transferred = 0;
    let cached = 0;
    let firstStart = Number.POSITIVE_INFINITY;
    let lastEnd = 0;
    for (const asset of assets) {
      transferred += asset.transferSize || 0;
      // A body that cost nothing on the wire came from the cache. decodedBodySize keeps a genuinely
      // empty response from counting as one.
      if (!asset.transferSize && asset.decodedBodySize) {
        cached++;
      }
      firstStart = Math.min(firstStart, asset.startTime);
      lastEnd = Math.max(lastEnd, asset.responseEnd);
    }

    const boot = perf.now();
    return {
      ttfb: Math.round(nav ? nav.responseEnd : 0),
      domContentLoaded: Math.round(nav ? nav.domContentLoadedEventEnd : 0),
      boot: Math.round(boot),
      fileCount: assets.length,
      transferredKb: Math.round(transferred / 1024),
      cachedCount: cached,
      downloadMs: assets.length ? Math.round(lastEnd - firstStart) : 0,
      parseAndBootMs: assets.length ? Math.max(0, Math.round(boot - lastEnd)) : 0
    };
  } catch (ignored) {
    // Resource timing is not worth a failed start.
    return null;
  }
}

/** The pairs are read back split on commas and equals signs, so no value may contain either. */
export function formatStartupTiming(timing: StartupTiming): string {
  return [
    'ttfb=' + timing.ttfb,
    'dcl=' + timing.domContentLoaded,
    'boot=' + timing.boot,
    'files=' + timing.fileCount,
    'kb=' + timing.transferredKb,
    'cached=' + timing.cachedCount,
    'download=' + timing.downloadMs,
    'parseBoot=' + timing.parseAndBootMs
  ].join(',');
}

export function reportStartupTiming(tracker: FirstInteractionTrackerService,
                                    perf: Performance = performance): void {
  try {
    const timing = collectStartupTiming(perf);
    if (timing) {
      tracker.report('STARTUP_TIMING', formatStartupTiming(timing));
    }
  } catch (ignored) {
    // Telemetry is never a reason for a game not to start.
  }
}
