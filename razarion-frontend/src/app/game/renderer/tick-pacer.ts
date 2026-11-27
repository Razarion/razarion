/**
 * How long a unit takes to glide from where it is drawn to the position the latest engine tick
 * reported.
 * <p>
 * It used to be a fixed 100 ms, the engine's tick. That is only right while every tick arrives 100 ms
 * after the one before. Without a SharedArrayBuffer - the Facebook and Instagram in-app browsers,
 * almost every player - the main thread pulls each tick from the worker once it is done with its
 * frame, and on a phone the ticks arrive late and unevenly: 91 instead of 100 per 10 s in the median,
 * under 85 in four of ten windows, 35 below 10 fps (PROD RenderTelemetry, 2026-11-25). A unit reached
 * its target after 100 ms and stood still until the next tick, then moved on: a stop-and-go that the
 * follow camera put on the whole screen.
 * <p>
 * The glide now lasts a little longer than the ticks actually take to arrive, measured as a running
 * average. A new tick then almost always comes in while the unit is still on its way, and it carries
 * on from where it is drawn - steady motion, a few tens of milliseconds behind, instead of a jerk.
 */
export class TickPacer {
  static readonly TICK_MILLIS = 100;
  /** Never shorter than the engine's tick. */
  static readonly MIN_GLIDE_MILLIS = 100;
  /** A unit must not trail its real position by more than this. */
  static readonly MAX_GLIDE_MILLIS = 500;
  /** Gaps above this are a stall or a hidden tab, not the pace: they are not averaged in. */
  static readonly MAX_PACE_GAP_MILLIS = 1000;
  /** The glide is this much longer than the average gap, so the next tick usually comes first. */
  static readonly GLIDE_FACTOR = 1.25;
  /** Weight of the newest gap in the running average. */
  static readonly SMOOTHING = 0.2;

  /** ?fixedglide in the URL brings back the old fixed 100 ms glide, to compare the two on one device. */
  static readonly FIXED_GLIDE = TickPacer.readFixedGlide();

  private static readFixedGlide(): boolean {
    try {
      return typeof location !== 'undefined' && new URLSearchParams(location.search).has('fixedglide');
    } catch {
      return false;
    }
  }

  private averageGap = TickPacer.TICK_MILLIS;
  private lastTickAt: number | null = null;
  private unitFrames = 0;
  private unitWaitFrames = 0;

  onTick(now: number): void {
    if (this.lastTickAt !== null) {
      const gap = now - this.lastTickAt;
      if (gap > 0 && gap <= TickPacer.MAX_PACE_GAP_MILLIS) {
        this.averageGap += TickPacer.SMOOTHING * (gap - this.averageGap);
      }
    }
    this.lastTickAt = now;
  }

  /**
   * One drawn frame of one driving unit: whether it had already reached the position of its last
   * tick and stood there, waiting for the next. That waiting is the stop-and-go, as a number - the
   * share of driving frames spent standing, reported by RenderTelemetry as glideWaitPct, so the
   * effect of the glide can be read from PROD sessions nobody can watch.
   */
  recordUnitFrame(waiting: boolean): void {
    this.unitFrames++;
    if (waiting) {
      this.unitWaitFrames++;
    }
  }

  /**
   * Frames a unit stood at its target and then drove on, so they were waiting. Handed over in one
   * go: whether a unit at its target is waiting or has stopped is only known from the next tick.
   */
  recordUnitWaitFrames(frames: number): void {
    this.unitFrames += frames;
    this.unitWaitFrames += frames;
  }

  /** The driving-unit frames since the last call, and then starts counting anew. */
  takeUnitFrames(): { frames: number, waiting: number } {
    const result = {frames: this.unitFrames, waiting: this.unitWaitFrames};
    this.unitFrames = 0;
    this.unitWaitFrames = 0;
    return result;
  }

  glideMillis(): number {
    if (TickPacer.FIXED_GLIDE) {
      return TickPacer.TICK_MILLIS;
    }
    const glide = this.averageGap * TickPacer.GLIDE_FACTOR;
    return Math.min(TickPacer.MAX_GLIDE_MILLIS, Math.max(TickPacer.MIN_GLIDE_MILLIS, glide));
  }
}
