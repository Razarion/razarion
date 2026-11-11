/**
 * Renders the scene with fewer pixels while the frames are too slow, and gives them back once
 * there is headroom again.
 *
 * <h3>Why</h3>
 *
 * A player on integrated Intel graphics wrote "Is it just me or is it LAGGY and Glitchy as hell" in
 * the chat (2026-11-06). The render telemetry of that session: 16-21 fps on the noob island, then 4-10
 * fps on the main planet, with single frames of up to 1.4 s. The canvas was 1536x742 at hardware
 * scaling 1.0 the whole time - nothing ever traded picture for speed.
 *
 * <h3>What this can and cannot do</h3>
 *
 * In that session the render time followed the scene size - triangles, draw calls, zoom - with a
 * correlation of about 0.7, and the pixel count never changed. Fewer pixels make the fragment work
 * cheaper and nothing else, so this is expected to win back the GPU share of the frame (about 30 ms
 * of it there), not to turn 5 fps into 30. The render telemetry carries `scaling=`, so whether it
 * pays shows in the same log lines that showed the problem.
 *
 * <h3>The rule</h3>
 *
 * Frames are judged per window of {@link WINDOW_MS} by their median interval, not the mean: the
 * mean is dragged by the single long frames, which fewer pixels do not fix. A slow window raises the
 * scaling by one {@link STEP}, up to {@link MAX_LEVEL}; {@link GOOD_WINDOWS_TO_STEP_DOWN} fast
 * windows in a row lower it again, but not to a level that was too slow within the last
 * {@link SLOW_LEVEL_BLOCK_MS}, so the picture does not pump between two sizes - and once the scene
 * has changed (zoomed in, left the crowded spot) it does get the pixels back. The window right after a change is discarded: resizing
 * the back buffer costs a frame of its own.
 * <p>
 * Every step up has to pay: the next judged window must be at least {@link MIN_GAIN} faster than
 * the one that caused it. If it is not, the frames are not limited by the pixels - a slow CPU, or a
 * GPU starved by geometry - and the step only made the picture worse. It is then taken back and the
 * session is left alone for good. Found on a desktop with CPU throttling (2026-11-08): stepped up to
 * 1.5, stayed at 30 fps and would have stayed blurry for the whole session.
 *
 * MAX_LEVEL is 1.5 (44 % of the pixels) and not more, because the item labels and the placer are
 * drawn in render pixels: they grow and soften with the scaling, and at 2 they would be twice as
 * large.
 *
 * <h3>Not at the start, and not on one bad window</h3>
 *
 * The first seconds of every session are slow on every device: shaders compile, models load, the
 * base appears. Measured on a Pixel 7 (2026-11-11): 6.8 fps in the first window, 66-89 fps from the
 * second on. Judged then, the first step fired for nearly every player - in the field it coincided
 * with the scene growing from 0.9 to 3.2 M indices and made the frames 22 % slower. So nothing is
 * judged for {@link WARMUP_MS} after the first frame, and a step up needs
 * {@link SLOW_WINDOWS_TO_STEP_UP} slow windows in a row, so a single hitch (a model arriving, a
 * shader compiling) is not taken for a slow device.
 */
export class AdaptiveResolution {
  static readonly WINDOW_MS = 4000;
  /** A median frame slower than this (below 20 fps) is too slow. */
  static readonly SLOW_FRAME_MS = 50;
  /** A median frame faster than this (above ~36 fps) leaves room for more pixels. */
  static readonly FAST_FRAME_MS = 28;
  static readonly STEP = 0.25;
  static readonly MAX_LEVEL = 1.5;
  static readonly GOOD_WINDOWS_TO_STEP_DOWN = 3;
  /** How long a level that was too slow is not stepped down to again. */
  static readonly SLOW_LEVEL_BLOCK_MS = 120000;
  /** A gap this long is a hidden tab, a debugger or a sleep - not a slow frame. */
  static readonly PAUSE_MS = 1000;
  /** Fewer frames than this in a window say nothing. */
  private static readonly MIN_FRAMES = 20;
  /** How much faster the median frame has to get for a step up to stay. */
  static readonly MIN_GAIN = 0.15;
  /** How long after the first frame nothing is judged: the start is slow on every device. */
  static readonly WARMUP_MS = 15000;
  /** Slow windows in a row before a step up. */
  static readonly SLOW_WINDOWS_TO_STEP_UP = 2;

  private level: number;
  private readonly baseLevel: number;
  /** Level to the time it was last too slow. */
  private readonly slowAt = new Map<number, number>();
  private intervals: number[] = [];
  private windowStart: number | null = null;
  private lastFrame: number | null = null;
  private goodWindows = 0;
  private slowWindows = 0;
  private firstFrame: number | null = null;
  private discardNextWindow = false;
  /** The step up waiting for its proof: where it came from and how slow it was there. */
  private pendingStep: { fromLevel: number, medianMs: number } | null = null;
  private stopped = false;

  constructor(baseLevel: number) {
    this.baseLevel = baseLevel;
    this.level = baseLevel;
  }

  getLevel(): number {
    return this.level;
  }

  getBaseLevel(): number {
    return this.baseLevel;
  }

  /** No further changes this session: the pixels did not help, or somebody set the level by hand. */
  isStopped(): boolean {
    return this.stopped;
  }

  stop(): void {
    this.stopped = true;
  }

  /**
   * @param now the frame's timestamp in ms
   * @return the new scaling level when it changes with this frame, otherwise null
   */
  onFrame(now: number): number | null {
    if (this.stopped) {
      return null;
    }
    if (this.lastFrame !== null) {
      const interval = now - this.lastFrame;
      if (interval >= AdaptiveResolution.PAUSE_MS) {
        // Whatever this window measured was interrupted
        this.startWindow(now);
      } else if (interval > 0) {
        this.intervals.push(interval);
      }
    }
    this.lastFrame = now;
    if (this.firstFrame === null) {
      this.firstFrame = now;
    }
    if (this.windowStart === null) {
      this.windowStart = now;
      return null;
    }
    if (now - this.windowStart < AdaptiveResolution.WINDOW_MS) {
      return null;
    }
    const intervals = this.intervals;
    this.startWindow(now);
    if (this.discardNextWindow) {
      this.discardNextWindow = false;
      return null;
    }
    if (now - this.firstFrame < AdaptiveResolution.WARMUP_MS) {
      return null;
    }
    if (intervals.length < AdaptiveResolution.MIN_FRAMES) {
      return null;
    }
    return this.judge(AdaptiveResolution.median(intervals), now);
  }

  private judge(medianMs: number, now: number): number | null {
    const pending = this.pendingStep;
    if (pending) {
      this.pendingStep = null;
      if (medianMs > pending.medianMs * (1 - AdaptiveResolution.MIN_GAIN)) {
        this.stopped = true;
        this.level = pending.fromLevel;
        return pending.fromLevel;
      }
    }
    if (medianMs > AdaptiveResolution.SLOW_FRAME_MS) {
      this.goodWindows = 0;
      this.slowAt.set(this.level, now);
      if (this.level >= AdaptiveResolution.MAX_LEVEL) {
        return null;
      }
      this.slowWindows++;
      if (this.slowWindows < AdaptiveResolution.SLOW_WINDOWS_TO_STEP_UP) {
        return null;
      }
      this.slowWindows = 0;
      this.pendingStep = {fromLevel: this.level, medianMs};
      return this.change(Math.min(AdaptiveResolution.MAX_LEVEL, this.level + AdaptiveResolution.STEP));
    }
    this.slowWindows = 0;
    if (medianMs < AdaptiveResolution.FAST_FRAME_MS && this.level > this.baseLevel) {
      this.goodWindows++;
      const lower = Math.max(this.baseLevel, this.level - AdaptiveResolution.STEP);
      if (this.goodWindows >= AdaptiveResolution.GOOD_WINDOWS_TO_STEP_DOWN && !this.blocked(lower, now)) {
        this.goodWindows = 0;
        return this.change(lower);
      }
      return null;
    }
    this.goodWindows = 0;
    return null;
  }

  private blocked(level: number, now: number): boolean {
    const slowAt = this.slowAt.get(level);
    return slowAt !== undefined && now - slowAt < AdaptiveResolution.SLOW_LEVEL_BLOCK_MS;
  }

  private change(level: number): number {
    this.level = level;
    this.discardNextWindow = true;
    return level;
  }

  private startWindow(now: number): void {
    this.intervals = [];
    this.windowStart = now;
  }

  private static median(values: number[]): number {
    const sorted = [...values].sort((a, b) => a - b);
    return sorted[Math.floor(sorted.length / 2)];
  }
}
