import {collectStartupTiming, formatStartupTiming, reportStartupTiming} from './startup-timing';

/**
 * The split this exists for: downloading against parsing and booting.
 *
 * Half of the in-app sessions are lost between PAGE_LOADED and WASM_BOOTSTRAP, and a smaller bundle
 * and an earlier picture are different repairs. Getting the split wrong would send the work to the
 * wrong one, so the arithmetic is worth pinning down.
 */
describe('Startup timing', () => {
  function fakePerformance(assets: Partial<PerformanceResourceTiming>[], now = 3000,
                           nav?: Partial<PerformanceNavigationTiming>): Performance {
    return {
      now: () => now,
      getEntriesByType: (type: string) => {
        if (type === 'navigation') {
          return nav ? [nav] : [];
        }
        if (type === 'resource') {
          return assets;
        }
        return [];
      }
    } as unknown as Performance;
  }

  function asset(name: string, startTime: number, responseEnd: number,
                 transferSize: number, decodedBodySize = 100000): Partial<PerformanceResourceTiming> {
    return {name, startTime, responseEnd, transferSize, decodedBodySize} as any;
  }

  it('separates the download window from parsing and booting', () => {
    const timing = collectStartupTiming(fakePerformance([
      asset('https://x/main-A.js', 200, 1200, 250000),
      asset('https://x/chunk-B.js', 300, 1800, 450000)
    ], 3000))!;

    // First request to last byte, across all of them.
    expect(timing.downloadMs).toBe(1800 - 200);
    // And what happened after the last byte arrived.
    expect(timing.parseAndBootMs).toBe(3000 - 1800);
    expect(timing.boot).toBe(3000);
  });

  it('counts what came from the cache, which is how a stale in-app browser shows', () => {
    const timing = collectStartupTiming(fakePerformance([
      asset('https://x/main-A.js', 100, 200, 0, 900000),
      asset('https://x/chunk-B.js', 100, 900, 450000)
    ]))!;

    expect(timing.cachedCount).toBe(1);
    expect(timing.transferredKb).toBe(Math.round(450000 / 1024));
  });

  it('does not count an empty response as a cache hit', () => {
    const timing = collectStartupTiming(fakePerformance([
      asset('https://x/empty.js', 100, 120, 0, 0)
    ]))!;

    expect(timing.cachedCount).toBe(0);
  });

  it('looks at scripts and stylesheets, not at every image on the page', () => {
    const timing = collectStartupTiming(fakePerformance([
      asset('https://x/main-A.js', 100, 500, 1000),
      asset('https://x/styles-B.css', 100, 500, 1000),
      asset('https://x/hero.webp', 100, 9000, 4000000),
      asset('https://x/model.glb', 100, 9000, 8000000)
    ]))!;

    // The hero and the model are not what stands between the player and the engine.
    expect(timing.fileCount).toBe(2);
    expect(timing.downloadMs).toBe(400);
  });

  it('reads a hashed name with a query string too', () => {
    const timing = collectStartupTiming(fakePerformance([
      asset('https://x/main-A.js?v=20260901', 100, 500, 1000)
    ]))!;

    expect(timing.fileCount).toBe(1);
  });

  it('says zero rather than nonsense when the page reports no resources', () => {
    const timing = collectStartupTiming(fakePerformance([]))!;

    expect(timing.downloadMs).toBe(0);
    expect(timing.parseAndBootMs).toBe(0);
    expect(timing.fileCount).toBe(0);
  });

  it('takes the document timings from the navigation entry', () => {
    const timing = collectStartupTiming(fakePerformance(
      [asset('https://x/main-A.js', 100, 500, 1000)], 3000,
      {responseEnd: 180, domContentLoadedEventEnd: 640} as any))!;

    expect(timing.ttfb).toBe(180);
    expect(timing.domContentLoaded).toBe(640);
  });

  it('formats pairs that survive being split on commas and equals signs', () => {
    const detail = formatStartupTiming(collectStartupTiming(fakePerformance([
      asset('https://x/main-A.js', 200, 1200, 250000)
    ], 3000))!);

    const pairs = detail.split(',').map(p => p.split('='));
    expect(pairs.every(p => p.length === 2)).toBeTrue();
    expect(detail).toContain('download=1000');
    expect(detail).toContain('parseBoot=1800');
  });

  it('reports it as its own kind, not as a player action', () => {
    const reported: { kind: string, detail?: string }[] = [];
    const tracker: any = {report: (kind: string, detail?: string) => reported.push({kind, detail})};

    reportStartupTiming(tracker, fakePerformance([asset('https://x/main-A.js', 100, 500, 1000)]));

    expect(reported.length).toBe(1);
    expect(reported[0].kind).toBe('STARTUP_TIMING');
  });

  it('never lets a measurement stop a game from starting', () => {
    const throwing: any = {
      report: () => {
        throw new Error('tracker down');
      }
    };
    const broken = {
      now: () => 0,
      getEntriesByType: () => {
        throw new Error('no resource timing here');
      }
    } as unknown as Performance;

    expect(() => reportStartupTiming(throwing, broken)).not.toThrow();
    expect(collectStartupTiming(broken)).toBeNull();
  });
});
