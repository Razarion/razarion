/**
 * What a slow tick spent its time on, on the main thread.
 * <p>
 * Applying one tick took up to 80-123 ms on a Pixel 7 in a fight (RenderTelemetry tickApplyMax,
 * 2026-11-27), against 1-2 ms otherwise - a visible jerk each time. The tick is applied in Java
 * (GameEngineControl.onTickUpdate) and calls back into the renderer for every unit; this times
 * the two callbacks that can be expensive on their own: building a unit's Babylon item when it
 * comes into view, and a unit's explosion. The rest of the tick is the remainder.
 * <p>
 * A tick of {@link SLOW_TICK_MS} or more writes one [TickApplySlow] line, which goes to the server
 * log like the telemetry.
 */
export class TickApplyProbe {
  static readonly SLOW_TICK_MS = 40;
  private static readonly MAX_LINES_PER_MINUTE = 20;

  private createMs = 0;
  private createCount = 0;
  private createMaxMs = 0;
  private createMaxType = '';
  private explodeMs = 0;
  private explodeCount = 0;
  private linesThisMinute = 0;
  private minuteStart = 0;

  /** Units built in the telemetry period and the time it took, see takePeriodCreates. */
  private periodCreates = 0;
  private periodCreateMs = 0;

  recordCreate(ms: number, typeName: string): void {
    this.periodCreates++;
    this.periodCreateMs += ms;
    this.createMs += ms;
    this.createCount++;
    if (ms > this.createMaxMs) {
      this.createMaxMs = ms;
      this.createMaxType = typeName;
    }
  }

  /** The units built since the last call and the milliseconds that took, then counts anew. */
  takePeriodCreates(): { creates: number, createMs: number } {
    const result = {creates: this.periodCreates, createMs: this.periodCreateMs};
    this.periodCreates = 0;
    this.periodCreateMs = 0;
    return result;
  }

  recordExplode(ms: number): void {
    this.explodeMs += ms;
    this.explodeCount++;
  }

  /** The end of one tick: reports it if it was slow and starts counting the next. */
  onTick(clientTickMs: number, session: string): string | null {
    let line: string | null = null;
    if (clientTickMs >= TickApplyProbe.SLOW_TICK_MS && this.mayLog(performance.now())) {
      const rest = Math.max(0, clientTickMs - this.createMs - this.explodeMs);
      line = `[TickApplySlow] session=${session} ms=${clientTickMs.toFixed(0)} ` +
        `create=${this.createMs.toFixed(0)} creates=${this.createCount} createMax=${this.createMaxMs.toFixed(0)} createMaxType="${this.createMaxType}" ` +
        `explode=${this.explodeMs.toFixed(0)} explodes=${this.explodeCount} rest=${rest.toFixed(0)}`;
    }
    this.createMs = 0;
    this.createCount = 0;
    this.createMaxMs = 0;
    this.createMaxType = '';
    this.explodeMs = 0;
    this.explodeCount = 0;
    return line;
  }

  private mayLog(now: number): boolean {
    if (now - this.minuteStart > 60000) {
      this.minuteStart = now;
      this.linesThisMinute = 0;
    }
    return this.linesThisMinute++ < TickApplyProbe.MAX_LINES_PER_MINUTE;
  }
}
