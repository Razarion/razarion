/**
 * Lightweight always-available performance graph drawn over the game canvas (toggle with F8).
 *
 * Unlike the F9 PerfDebug (which opens the heavy Babylon Inspector and logs 1-second AVERAGES —
 * averaging hides the single long frame that *is* a stutter), this keeps a rolling per-frame
 * history so a stutter shows up as a spike you can look back at. It is pure <canvas> + numbers;
 * it has no Babylon dependency and is fed by the renderer's run loop.
 *
 * Three curves, one sample per rendered frame:
 *  - frame interval (ms between frames)  — the "everything froze" signal
 *  - render CPU time (ms in scene.render())
 *  - tick interval (ms between game-engine tick arrivals, derived on the main thread)
 *
 * The readout also shows "worker N/s": the worker game-engine ticks counted over the last
 * second (target 10/s) — a drop below that means the worker simulation is falling behind.
 *
 * Reading it: a spike on BOTH frame interval and tick interval = the main thread stalled
 * (GC / message processing / shader compile). A spike on tick interval ALONE (frames keep
 * flowing) = the worker / game engine hitched. A frame spike with a big render time = the
 * render itself was heavy that frame.
 */
export class BabylonPerfOverlay {
  private static readonly SAMPLES = 240;            // ~4s at 60fps of rolling history
  private static readonly WIDTH = 420;
  private static readonly HEIGHT = 150;
  private static readonly GRAPH_TOP = 30;           // px reserved for the readout text
  private static readonly SPIKE_FRAME_MS = 50;      // log a breakdown when a frame exceeds this
  private static readonly SPIKE_LOG_THROTTLE_MS = 500;

  private readonly canvas: HTMLCanvasElement;
  private readonly ctx: CanvasRenderingContext2D;
  private active = false;   // off by default; F8 toggles it on/off

  private readonly frameMs: number[] = [];
  private readonly renderMs: number[] = [];
  private readonly tickMs: number[] = [];
  private readonly fpsHistory: number[] = [];
  // Terrain-tile builds are sporadic events, not per-frame: this buffer is 0 except on the frame a
  // tile mesh was built (= its client build ms), so it draws as isolated spikes on the timeline.
  private readonly tileClientMs: number[] = [];
  private pendingTileClientMs = 0;
  private lastTileWorkerMs = 0;
  private lastTileClientMs = 0;

  private lastFrameTime: number | null = null;
  private lastTickTime: number | null = null;   // performance.now() of the last game-engine tick
  private currentTickInterval = 0;
  // Sliding 1s window of tick-arrival timestamps → worker ticks per second (target is 10/s).
  private readonly tickTimes: number[] = [];
  private ticksPerSecond = 0;
  private lastClientTickMs = 0;                  // main-thread ms spent applying the last tick
  private lastSpikeLogTime = 0;
  private fps = 0;

  constructor() {
    this.canvas = document.createElement("canvas");
    this.canvas.width = BabylonPerfOverlay.WIDTH;
    this.canvas.height = BabylonPerfOverlay.HEIGHT;
    this.canvas.style.position = "fixed";
    this.canvas.style.top = "8px";
    this.canvas.style.right = "8px";
    this.canvas.style.zIndex = "10000";
    this.canvas.style.pointerEvents = "none";
    this.canvas.style.background = "rgba(0,0,0,0.55)";
    this.canvas.style.border = "1px solid rgba(255,255,255,0.25)";
    this.canvas.style.borderRadius = "4px";
    this.canvas.style.display = "none";   // hidden by default (active = false); F8 shows it
    document.body.appendChild(this.canvas);
    this.ctx = this.canvas.getContext("2d")!;
  }

  isActive(): boolean {
    return this.active;
  }

  toggle(): void {
    this.active = !this.active;
    this.canvas.style.display = this.active ? "block" : "none";
    if (this.active) {
      // Reset so the first frame after enabling doesn't record a huge bogus interval.
      this.lastFrameTime = null;
      this.lastTickTime = null;
      this.currentTickInterval = 0;
      this.tickTimes.length = 0;
      this.ticksPerSecond = 0;
      this.frameMs.length = 0;
      this.renderMs.length = 0;
      this.tickMs.length = 0;
      this.fpsHistory.length = 0;
      this.tileClientMs.length = 0;
      this.pendingTileClientMs = 0;
    }
    console.log(`[PerfOverlay] ${this.active ? "enabled (F8 to hide)" : "disabled"}`);
  }

  /**
   * Called once per game-engine simulation tick (driven from Java via the bridge). This is the
   * authoritative tick signal — it fires every tick even when nothing is moving, unlike the old
   * item-movement heuristic. We measure the interval here with the same high-res clock as frames.
   * @param clientTickMs main-thread ms spent applying the tick (for the readout / spike log)
   */
  onTickArrived(clientTickMs: number): void {
    const now = performance.now();
    if (this.lastTickTime !== null) {
      this.currentTickInterval = now - this.lastTickTime;
    }
    this.lastTickTime = now;
    this.lastClientTickMs = clientTickMs;

    // Count the ticks that arrived in the last second = worker ticks per second.
    this.tickTimes.push(now);
    const windowStart = now - 1000;
    while (this.tickTimes.length > 0 && this.tickTimes[0] < windowStart) {
      this.tickTimes.shift();
    }
    this.ticksPerSecond = this.tickTimes.length;
  }

  /**
   * Called when a terrain tile was generated + built (driven from Java per tile).
   * @param workerMs worker-thread time to generate the tile
   * @param clientMs main-thread time to build the Babylon mesh (the scroll-stutter source)
   */
  onTerrainTileBuilt(workerMs: number, clientMs: number): void {
    // If several tiles land in one frame, keep the worst for the timeline marker.
    this.pendingTileClientMs = Math.max(this.pendingTileClientMs, clientMs);
    this.lastTileWorkerMs = workerMs;
    this.lastTileClientMs = clientMs;
    console.log(`[PerfOverlay] terrain tile: worker=${workerMs.toFixed(0)}ms client=${clientMs.toFixed(0)}ms`);
  }

  /**
   * Record one rendered frame.
   * @param now            performance.now() taken right after scene.render()
   * @param renderMs       CPU ms spent in scene.render() this frame
   * @param fps            engine.getFps()
   */
  record(now: number, renderMs: number, fps: number): void {
    this.fps = fps;

    let frameInterval = 0;
    if (this.lastFrameTime !== null) {
      frameInterval = now - this.lastFrameTime;
    }
    this.lastFrameTime = now;

    this.pushCapped(this.frameMs, frameInterval);
    this.pushCapped(this.renderMs, renderMs);
    this.pushCapped(this.tickMs, this.currentTickInterval);
    this.pushCapped(this.fpsHistory, fps);
    this.pushCapped(this.tileClientMs, this.pendingTileClientMs);
    this.pendingTileClientMs = 0;

    if (frameInterval > BabylonPerfOverlay.SPIKE_FRAME_MS && now - this.lastSpikeLogTime > BabylonPerfOverlay.SPIKE_LOG_THROTTLE_MS) {
      this.lastSpikeLogTime = now;
      const sinceTick = this.lastTickTime !== null ? (now - this.lastTickTime).toFixed(0) : "n/a";
      console.log(
        `[PerfOverlay] STUTTER frame=${frameInterval.toFixed(1)}ms render=${renderMs.toFixed(1)}ms ` +
        `fps=${fps.toFixed(0)} tickInterval=${this.currentTickInterval.toFixed(0)}ms clientTick=${this.lastClientTickMs.toFixed(0)}ms sinceLastTick=${sinceTick}ms ` +
        `→ ${renderMs > frameInterval * 0.6 ? "render-bound" : "main-thread stall outside render (GC / message / shader)"}`
      );
    }
  }

  private pushCapped(buffer: number[], value: number): void {
    buffer.push(value);
    if (buffer.length > BabylonPerfOverlay.SAMPLES) {
      buffer.shift();
    }
  }

  draw(): void {
    if (!this.active) {
      return;
    }
    const ctx = this.ctx;
    const w = BabylonPerfOverlay.WIDTH;
    const h = BabylonPerfOverlay.HEIGHT;
    const top = BabylonPerfOverlay.GRAPH_TOP;
    ctx.clearRect(0, 0, w, h);

    // Auto-scale the Y axis to the worst sample in view (min 120ms so 60fps sits low and a
    // stutter visibly spikes), capped so a single huge freeze doesn't flatten everything.
    const maxSample = Math.max(
      this.arrayMax(this.frameMs), this.arrayMax(this.renderMs), this.arrayMax(this.tickMs),
      this.arrayMax(this.tileClientMs)
    );
    const scaleMs = Math.min(500, Math.max(120, Math.ceil(maxSample / 50) * 50));

    const yFor = (ms: number) => h - (ms / scaleMs) * (h - top);

    // Threshold guide lines.
    this.drawThreshold(ctx, yFor(16.7), "rgba(80,255,120,0.35)", w);   // 60 fps
    this.drawThreshold(ctx, yFor(33.3), "rgba(255,200,60,0.30)", w);   // 30 fps
    this.drawThreshold(ctx, yFor(100), "rgba(255,90,90,0.30)", w);     // 100ms = game tick period

    this.drawSeries(ctx, this.frameMs, scaleMs, "rgba(80,200,255,0.95)");   // frame interval — cyan
    this.drawSeries(ctx, this.renderMs, scaleMs, "rgba(255,230,80,0.95)");  // render CPU — yellow
    this.drawSeries(ctx, this.tickMs, scaleMs, "rgba(230,120,255,0.95)");   // tick interval — magenta

    // FPS rides on its OWN scale (higher = better, the mirror image of the ms curves) so it stays
    // readable instead of being squashed against the ms axis.
    const fpsScale = Math.max(70, Math.ceil(this.arrayMax(this.fpsHistory) / 10) * 10);
    this.drawSeries(ctx, this.fpsHistory, fpsScale, "rgba(255,255,255,0.95)"); // fps — white

    // Terrain-tile client build cost: isolated vertical markers (orange) on the ms axis.
    this.drawMarkers(ctx, this.tileClientMs, scaleMs, "rgba(255,150,40,0.95)");

    // Readout text.
    const last = (b: number[]) => (b.length ? b[b.length - 1] : 0);
    ctx.font = "11px monospace";
    ctx.textBaseline = "top";
    ctx.fillStyle = "rgba(80,200,255,1)";
    ctx.fillText(`frame ${last(this.frameMs).toFixed(1)}ms`, 6, 4);
    ctx.fillStyle = "rgba(255,230,80,1)";
    ctx.fillText(`render ${last(this.renderMs).toFixed(1)}ms`, 110, 4);
    ctx.fillStyle = "rgba(230,120,255,1)";
    ctx.fillText(`tick ${last(this.tickMs).toFixed(0)}ms`, 215, 4);
    ctx.fillStyle = "rgba(255,255,255,1)";
    ctx.fillText(`${this.fps.toFixed(0)} fps`, 300, 4);
    ctx.fillStyle = "rgba(255,150,40,1)";
    ctx.fillText(`tile w${this.lastTileWorkerMs.toFixed(0)}/c${this.lastTileClientMs.toFixed(0)}ms`, 6, 17);
    ctx.fillStyle = "rgba(230,120,255,1)";
    ctx.fillText(`worker ${this.ticksPerSecond}/s`, 175, 17);
    ctx.fillStyle = "rgba(200,200,200,0.8)";
    ctx.fillText(`${scaleMs}ms / ${fpsScale}fps`, 300, 17);
  }

  private drawThreshold(ctx: CanvasRenderingContext2D, y: number, color: string, w: number): void {
    ctx.strokeStyle = color;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(w, y);
    ctx.stroke();
  }

  private drawSeries(ctx: CanvasRenderingContext2D, buffer: number[], scaleMs: number, color: string): void {
    if (buffer.length < 2) {
      return;
    }
    const w = BabylonPerfOverlay.WIDTH;
    const h = BabylonPerfOverlay.HEIGHT;
    const top = BabylonPerfOverlay.GRAPH_TOP;
    const n = BabylonPerfOverlay.SAMPLES;
    ctx.strokeStyle = color;
    ctx.lineWidth = 1;
    ctx.beginPath();
    for (let i = 0; i < buffer.length; i++) {
      const x = (i / (n - 1)) * w;
      const y = h - (Math.min(buffer[i], scaleMs) / scaleMs) * (h - top);
      i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
    }
    ctx.stroke();
  }

  /** Draws isolated samples (buffer is mostly 0) as vertical bars — used for sporadic tile events. */
  private drawMarkers(ctx: CanvasRenderingContext2D, buffer: number[], scaleMs: number, color: string): void {
    const w = BabylonPerfOverlay.WIDTH;
    const h = BabylonPerfOverlay.HEIGHT;
    const top = BabylonPerfOverlay.GRAPH_TOP;
    const n = BabylonPerfOverlay.SAMPLES;
    ctx.strokeStyle = color;
    ctx.lineWidth = 2;
    for (let i = 0; i < buffer.length; i++) {
      if (buffer[i] <= 0) {
        continue;
      }
      const x = (i / (n - 1)) * w;
      const y = h - (Math.min(buffer[i], scaleMs) / scaleMs) * (h - top);
      ctx.beginPath();
      ctx.moveTo(x, h);
      ctx.lineTo(x, y);
      ctx.stroke();
    }
  }

  private arrayMax(buffer: number[]): number {
    let m = 0;
    for (let i = 0; i < buffer.length; i++) {
      if (buffer[i] > m) {
        m = buffer[i];
      }
    }
    return m;
  }
}
