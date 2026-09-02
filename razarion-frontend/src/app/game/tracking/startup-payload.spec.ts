import {collectStartupPayload, formatStartupPayload} from './startup-payload';

/**
 * Where a start's bytes go, split by what they are.
 *
 * The decision this feeds is which repair is worth an afternoon: shrinking the JavaScript, which is
 * 13% of a start, or not sending textures nobody looks at, which might be a third of it. A category
 * that silently lands in `other` would make that call on wrong numbers, so the routing is what is
 * pinned down here.
 */
describe('Startup payload', () => {
  function fakePerformance(entries: Partial<PerformanceResourceTiming>[], now = 5000): Performance {
    return {
      now: () => now,
      getEntriesByType: (type: string) => (type === 'resource' ? entries : [])
    } as unknown as Performance;
  }

  function visible(state: DocumentVisibilityState = 'visible'): Document {
    return {visibilityState: state} as unknown as Document;
  }

  const ORIGIN = 'https://x';

  function entry(name: string, transferSize: number,
                 encodedBodySize = transferSize): Partial<PerformanceResourceTiming> {
    return {name, transferSize, encodedBodySize} as any;
  }

  const KB = 1024;

  it('routes each kind of resource to its own category', () => {
    const payload = collectStartupPayload(fakePerformance([
      entry('https://x/main-ABC.js', 100 * KB),
      entry('https://x/styles-ABC.css', 10 * KB),
      entry('https://x/teavm-client/razarion-client.wasm', 600 * KB),
      entry('https://x/rest/gltf/42', 300 * KB),
      entry('https://x/rest/image/17', 50 * KB),
      entry('https://x/rest/audio/3', 20 * KB),
      entry('https://x/rest/babylon-material/8', 5 * KB)
    ]), visible(), ORIGIN)!;

    const kb = (key: string) => payload.byCategory.get(key)!.kb;
    expect(kb('js')).toBe(100);
    expect(kb('css')).toBe(10);
    expect(kb('wasm')).toBe(600);
    expect(kb('gltf')).toBe(300);
    expect(kb('img')).toBe(50);
    expect(kb('audio')).toBe(20);
    expect(kb('mat')).toBe(5);
    // Nothing quietly fell through: the whole point is being able to trust the split.
    expect(payload.byCategory.has('other')).toBeFalse();
    expect(payload.transferredKb).toBe(1085);
    expect(payload.fileCount).toBe(7);
  });

  it('counts a cached body, which is the difference between a cheap start and a fast one', () => {
    // Transferred nothing, but the browser still had to have it. A warm in-app browser shows here.
    const payload = collectStartupPayload(fakePerformance([
      entry('https://x/main-ABC.js', 0, 400 * KB),
      entry('https://x/rest/gltf/42', 200 * KB)
    ]), visible(), ORIGIN)!;

    expect(payload.transferredKb).toBe(200);
    expect(payload.cachedKb).toBe(400);
    // The category still carries the bytes: what the start needed does not change with the cache.
    expect(payload.byCategory.get('js')!.kb).toBe(400);
  });

  it('does not invent bytes for a response that reported none', () => {
    // A 204, or a cross-origin response without Timing-Allow-Origin: both report zero for both
    // sizes. Counted as a file, never guessed at as a size.
    const payload = collectStartupPayload(fakePerformance([
      entry('https://other/font.woff2', 0, 0)
    ]), visible(), ORIGIN)!;

    expect(payload.transferredKb).toBe(0);
    expect(payload.cachedKb).toBe(0);
    expect(payload.byCategory.get('other')!.files).toBe(1);
  });

  it('marks a snapshot taken in the background, which is a download and not an experience', () => {
    const payload = collectStartupPayload(fakePerformance([entry('https://x/a.js', KB)]),
      visible('hidden'), ORIGIN)!;

    expect(payload.hidden).toBeTrue();
    expect(formatStartupPayload('t60', payload)).toContain('hidden=1');
  });

  it('formats pairs that survive being split on commas and equals signs', () => {
    const payload = collectStartupPayload(fakePerformance([
      entry('https://x/main-ABC.js', 100 * KB),
      entry('https://x/rest/gltf/42', 300 * KB)
    ], 8421), visible(), ORIGIN)!;

    const detail = formatStartupPayload('playable', payload);

    expect(detail).toBe('at=playable,ms=8421,files=2,kb=400,cachedKb=0,jsKb=100,jsN=1,gltfKb=300,gltfN=1');
    for (const pair of detail.split(',')) {
      expect(pair.split('=').length).toBe(2);
    }
  });

  it('says so when resource timing dropped entries, instead of looking complete', () => {
    // The failure this guards against already happened once: 250 entries is the default buffer,
    // a start fetches 675 chunks, and the result was a confident 1.1 MB with no models in it.
    (window as any).RAZ_resourceBufferFull = 1;
    try {
      const payload = collectStartupPayload(fakePerformance([entry('https://x/a.js', KB)]),
        visible(), ORIGIN)!;

      expect(payload.truncated).toBeTrue();
      expect(formatStartupPayload('playable', payload)).toContain('truncated=1');
    } finally {
      delete (window as any).RAZ_resourceBufferFull;
    }
  });

  it('names the heaviest unrecognised resource, so `other` can be acted on', () => {
    // 2.5 MB of a 15 MB blocking start landed in `other` and the measurement could not say what
    // any of it was. One name is enough to decide whether it deserves a category of its own.
    const payload = collectStartupPayload(fakePerformance([
      entry('https://x/rest/terrainHeightMap/117?planet=2', 4000 * KB),
      entry('https://x/rest/user/self', 3 * KB),
      entry('https://x/main-ABC.js', 100 * KB)
    ]), visible(), ORIGIN)!;

    // The query string goes: it carries ids that would make every session look different.
    expect(payload.largestOther).toBe('/rest/terrainHeightMap/117');
    expect(payload.largestOtherKb).toBe(4000);
    expect(formatStartupPayload('playable', payload))
      .toContain('otherTop=/rest/terrainHeightMap/117,otherTopKb=4000');
  });

  it('keeps the host when the resource is not ours, and never breaks the pair format', () => {
    const payload = collectStartupPayload(fakePerformance([
      entry('https://cdn.example.com/a/b?x=1&y=2', 500 * KB)
    ]), visible(), ORIGIN)!;

    // A third party in the blocking path is worth seeing; an equals sign in it would not be.
    expect(payload.largestOther).toBe('cdn.example.com/a/b');
    for (const pair of formatStartupPayload('t20', payload).split(',')) {
      expect(pair.split('=').length).toBe(2);
    }
  });

  it('leaves out the categories that carried nothing', () => {
    const payload = collectStartupPayload(fakePerformance([entry('https://x/a.js', KB)]),
      visible(), ORIGIN)!;

    expect(formatStartupPayload('t20', payload)).not.toContain('audio');
  });
});
