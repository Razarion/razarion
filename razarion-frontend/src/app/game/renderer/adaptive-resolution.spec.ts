import {AdaptiveResolution} from './adaptive-resolution';

describe('AdaptiveResolution', () => {
  let now: number;
  let adaptive: AdaptiveResolution;

  /**
   * Frames for a while; the level changes they caused, in order.
   * @param frameMs the frame time, fixed or depending on the current level
   */
  function run(frameMs: number | ((level: number) => number), millis: number): number[] {
    const changes: number[] = [];
    const end = now + millis;
    while (now < end) {
      now += typeof frameMs === 'number' ? frameMs : frameMs(adaptive.getLevel());
      const level = adaptive.onFrame(now);
      if (level !== null) {
        changes.push(level);
      }
    }
    return changes;
  }

  /** A GPU-bound frame: the time scales with the pixel count. */
  const pixelBound = (msAtOne: number) => (level: number) => msAtOne / (level * level);

  beforeEach(() => {
    now = 1000;
    adaptive = new AdaptiveResolution(1);
    // Past the start, which is never judged
    run(16, AdaptiveResolution.WARMUP_MS);
  });

  it('leaves a smooth game alone', () => {
    expect(run(16, 30000)).toEqual([]);
    expect(adaptive.getLevel()).toBe(1);
  });

  it('steps up while fewer pixels help, up to the ceiling', () => {
    // 10 fps at 1, 6.4 at 1.25, 4.4 at 1.5 - still slow there, but each step paid
    expect(run(pixelBound(100), 60000)).toEqual([1.25, 1.5]);
    expect(adaptive.getLevel()).toBe(AdaptiveResolution.MAX_LEVEL);
    expect(adaptive.isStopped()).toBeFalse();
  });

  it('takes a step back and stops when it did not make the frames faster', () => {
    // CPU-bound: the same 100 ms at every level
    expect(run(100, 60000)).toEqual([1.25, 1]);
    expect(adaptive.getLevel()).toBe(1);
    expect(adaptive.isStopped()).toBeTrue();
  });

  it('keeps a step that paid even when the next one does not', () => {
    // Pixel-bound down to 60 ms at 1.25, then the CPU is the limit
    const frame = (level: number) => level < 1.25 ? 94 : 60;
    expect(run(frame, 60000)).toEqual([1.25, 1.5, 1.25]);
    expect(adaptive.getLevel()).toBe(1.25);
    expect(adaptive.isStopped()).toBeTrue();
  });

  it('does not step back to a level that was just too slow', () => {
    run(pixelBound(64), 10000);
    expect(adaptive.getLevel()).toBe(1.25);
    expect(run(16, 60000)).toEqual([]);
    expect(adaptive.getLevel()).toBe(1.25);
  });

  it('gives the pixels back once the slow level is a while ago', () => {
    run(pixelBound(64), 10000);
    expect(adaptive.getLevel()).toBe(1.25);
    expect(run(16, AdaptiveResolution.SLOW_LEVEL_BLOCK_MS + 20000)).toEqual([1]);
  });

  it('steps down one level at a time', () => {
    run(pixelBound(100), 60000);
    expect(adaptive.getLevel()).toBe(1.5);
    expect(run(16, AdaptiveResolution.SLOW_LEVEL_BLOCK_MS + 40000)).toEqual([1.25, 1]);
  });

  it('does nothing once stopped', () => {
    adaptive.stop();
    expect(run(100, 60000)).toEqual([]);
    expect(adaptive.getLevel()).toBe(1);
  });

  it('does not count a hidden tab as a slow frame', () => {
    for (let i = 0; i < 20; i++) {
      now += 2000;
      expect(adaptive.onFrame(now)).toBeNull();
    }
    expect(adaptive.getLevel()).toBe(1);
  });

  it('does not judge the start, however slow it is', () => {
    adaptive = new AdaptiveResolution(1);
    expect(run(130, AdaptiveResolution.WARMUP_MS - 500)).toEqual([]);
    expect(run(16, 30000)).toEqual([]);
    expect(adaptive.getLevel()).toBe(1);
  });

  it('does not take one slow window for a slow device', () => {
    expect(run(130, AdaptiveResolution.WINDOW_MS + 200)).toEqual([]);
    expect(run(16, 30000)).toEqual([]);
    expect(adaptive.getLevel()).toBe(1);
  });

  it('ignores the middle band', () => {
    expect(run(35, 60000)).toEqual([]);
  });
});
