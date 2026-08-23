/**
 * Always-on render telemetry: one summary line every {@link RenderTelemetry.PERIOD_MS} into the
 * console, from where AppComponent's console hook forwards it to the server and it becomes
 * readable in Cloud Logging.
 *
 * Why this exists. The engine tick has had a server-side diagnostic for years
 * ({@code PlanetServiceTracker}), and it is what proved the tick healthy on PROD after the
 * {@code -Pprod} build fix. The renderer had nothing comparable: the F8 overlay
 * ({@link BabylonPerfOverlay}) draws a per-frame curve that only ever exists on the machine
 * drawing it, so "lags a lot" from a player is unanswerable — we do not know their frame time,
 * their GPU, or whether they saw low FPS or a one-second freeze. Those are different bugs with
 * different fixes, and an average hides the second one, which is why this reports percentiles and
 * a max rather than a mean.
 *
 * Deliberately cheap: two array pushes per frame, one sort and one log line per period. No Babylon
 * dependency — the renderer feeds it, and the scene counters arrive through a supplier so this
 * file stays testable on its own.
 *
 * A hidden tab is dropped, not measured: requestAnimationFrame is throttled to ~1 Hz in the
 * background, so a backgrounded window would otherwise report 1 fps and a 1000 ms p50 and drown
 * the real numbers. Visibility changes reset the window rather than ending it.
 *
 * Set {@link RenderTelemetry.ENABLED} back to false once the render budget question is settled:
 * deploy.ps1 builds from the working tree, so a forgotten true ships one log line per 10 s and
 * player forever.
 */

/** Scene-size counters sampled once per period, supplied by the renderer. */
export interface RenderTelemetrySceneStats {
  meshes: number;
  activeMeshes: number;
  activeIndices: number;
  materials: number;
  /**
   * Of {@link meshes}, how many are switched off. Babylon walks the whole scene.meshes array
   * every frame and calls isReady() on each entry *before* it checks isEnabled(), so a disabled
   * mesh costs nearly as much per frame as a drawn one. PROD measured renderP50 rising linearly
   * with scene.meshes (1.32 us each, R^2 = 0.90) while activeMeshes stayed flat — this number
   * says how much of that array is scenery nobody can see.
   */
  disabledMeshes: number;
  /** How many entries are InstancedMesh, i.e. cheap to draw but still a full array entry. */
  instancedMeshes: number;
  /** Size of the ShadowGenerator's render list — a second per-frame walk over the same meshes. */
  shadowCasters: number;
  /**
   * The biggest mesh-name groups, "name:count" newest-first, e.g. "Rock:12000,Palm:8000".
   * Names are normalised (ids, indices and the "#inst" suffix stripped) so one model's thousands
   * of placements collapse into one bucket and the line names the actual owner of the array.
   */
  meshTop: string;
  /**
   * Of {@link meshes}, how many the parked-mesh filter is keeping out of Babylon's per-frame walks
   * (-1 while the filter is bypassed). Together with {@link parkingFilter} this is the A/B: park
   * count and mesh count stay put across an F7 toggle, so any change in renderP50 is the filter's.
   */
  parkedMeshes: number;
  /** False after F7 — the same picture drawn the old, slower way. */
  parkingFilter: boolean;
  /** Backbuffer size in device pixels, i.e. what the GPU actually has to fill. */
  renderWidth: number;
  renderHeight: number;
  hardwareScaling: number;
  /** Unmasked GL renderer string where the browser exposes it. */
  gpu: string | null;
}

export class RenderTelemetry {
  /** Master switch — see the class comment before leaving this on. */
  static readonly ENABLED = true;

  /** Matches PlanetServiceTracker's 100-tick dump, so both lines line up in the log by timestamp. */
  private static readonly PERIOD_MS = 10_000;
  /** Below this a period says nothing: a tab that just became visible, or a stalled first second. */
  private static readonly MIN_FRAMES = 20;
  /** A frame this long is a visible hitch rather than merely a slow frame. */
  private static readonly LONG_FRAME_MS = [50, 100, 250];
  /** Keep the GPU string from turning the log line into a paragraph. */
  private static readonly GPU_MAX_CHARS = 64;

  private readonly frameMs: number[] = [];
  private readonly renderMs: number[] = [];
  private readonly tickGapMs: number[] = [];
  private readonly clientTickMs: number[] = [];
  private readonly longFrames: number[] = [0, 0, 0];

  private periodStart: number | null = null;
  private lastFrameTime: number | null = null;
  private lastTickTime: number | null = null;
  private emitted = 0;

  constructor(private readonly sceneStats: () => RenderTelemetrySceneStats) {
    document.addEventListener("visibilitychange", () => this.reset());
  }

  /**
   * One rendered frame.
   *
   * @param now      performance.now() taken right after scene.render() returned
   * @param renderMs CPU milliseconds spent inside scene.render()
   */
  recordFrame(now: number, renderMs: number): void {
    if (this.periodStart === null) {
      this.periodStart = now;
    }
    const periodStart = this.periodStart;
    if (this.lastFrameTime !== null) {
      const interval = now - this.lastFrameTime;
      this.frameMs.push(interval);
      for (let i = 0; i < RenderTelemetry.LONG_FRAME_MS.length; i++) {
        if (interval > RenderTelemetry.LONG_FRAME_MS[i]) {
          this.longFrames[i]++;
        }
      }
    }
    this.lastFrameTime = now;
    this.renderMs.push(renderMs);

    if (now - periodStart >= RenderTelemetry.PERIOD_MS) {
      this.emit(now, periodStart);
    }
  }

  /**
   * One game-engine tick arriving on the main thread. Separates "the worker fell behind" from
   * "the renderer fell behind" — the two feel identical to a player and have nothing in common.
   *
   * @param clientTickMs main-thread milliseconds spent applying that tick
   */
  recordTick(clientTickMs: number): void {
    const now = performance.now();
    if (this.lastTickTime !== null) {
      this.tickGapMs.push(now - this.lastTickTime);
    }
    this.lastTickTime = now;
    this.clientTickMs.push(clientTickMs);
  }

  private emit(now: number, periodStart: number): void {
    const periodMs = now - periodStart;
    const frames = this.frameMs.length;
    if (frames < RenderTelemetry.MIN_FRAMES) {
      this.reset();
      return;
    }
    const stats = this.safeSceneStats();
    const frame = this.percentiles(this.frameMs);
    const render = this.percentiles(this.renderMs);
    const gap = this.percentiles(this.tickGapMs);
    const apply = this.percentiles(this.clientTickMs);
    const fps = frames / (periodMs / 1000);

    // Flat, unique key=value pairs on one line: the analysis is a regex over Cloud Logging output,
    // and the container log splits multi-line payloads into separate entries anyway.
    console.warn(
      `[RenderTelemetry] session=${this.sessionUuid()} seq=${++this.emitted} ` +
      `periodS=${(periodMs / 1000).toFixed(1)} frames=${frames} fps=${fps.toFixed(1)} ` +
      `frameP50=${frame.p50.toFixed(1)} frameP95=${frame.p95.toFixed(1)} frameP99=${frame.p99.toFixed(1)} frameMax=${frame.max.toFixed(1)} ` +
      `long50=${this.longFrames[0]} long100=${this.longFrames[1]} long250=${this.longFrames[2]} ` +
      `renderP50=${render.p50.toFixed(1)} renderP95=${render.p95.toFixed(1)} renderMax=${render.max.toFixed(1)} ` +
      `ticks=${this.tickGapMs.length} tickGapP50=${gap.p50.toFixed(0)} tickGapMax=${gap.max.toFixed(0)} ` +
      `tickApplyP50=${apply.p50.toFixed(1)} tickApplyMax=${apply.max.toFixed(1)} ` +
      `meshes=${stats.meshes} activeMeshes=${stats.activeMeshes} activeIndices=${stats.activeIndices} materials=${stats.materials} ` +
      `disabledMeshes=${stats.disabledMeshes} instanced=${stats.instancedMeshes} shadowCasters=${stats.shadowCasters} ` +
      `parked=${stats.parkedMeshes} parkingFilter=${stats.parkingFilter} ` +
      `meshTop="${this.clean(stats.meshTop)}" ` +
      `backbuffer=${stats.renderWidth}x${stats.renderHeight} scaling=${stats.hardwareScaling.toFixed(2)} dpr=${window.devicePixelRatio} ` +
      `touch=${navigator.maxTouchPoints > 0} gpu="${this.shortGpu(stats.gpu)}"`
    );
    this.reset();
  }

  /**
   * Drop the current period and start a new one. Used both after emitting and whenever the tab's
   * visibility flips — the frames either side of that flip are not comparable.
   */
  private reset(): void {
    this.frameMs.length = 0;
    this.renderMs.length = 0;
    this.tickGapMs.length = 0;
    this.clientTickMs.length = 0;
    this.longFrames.fill(0);
    this.periodStart = null;
    this.lastFrameTime = null;
    this.lastTickTime = null;
  }

  private percentiles(values: number[]): { p50: number, p95: number, p99: number, max: number } {
    if (values.length === 0) {
      return {p50: 0, p95: 0, p99: 0, max: 0};
    }
    const sorted = [...values].sort((a, b) => a - b);
    const at = (q: number) => sorted[Math.min(sorted.length - 1, Math.floor(q * sorted.length))];
    return {p50: at(0.5), p95: at(0.95), p99: at(0.99), max: sorted[sorted.length - 1]};
  }

  /**
   * The uuid the startup tracking and the engine already share, so a lagging period can be joined
   * back to that session's device, campaign source and quest progress in MongoDB.
   */
  private sessionUuid(): string {
    return (window as any).RAZ_gameSessionUuid ?? "unknown";
  }

  private shortGpu(gpu: string | null): string {
    if (!gpu) {
      return "unknown";
    }
    const clean = this.clean(gpu);
    return clean.length > RenderTelemetry.GPU_MAX_CHARS
      ? clean.substring(0, RenderTelemetry.GPU_MAX_CHARS)
      : clean;
  }

  /** A quote inside a value would break the key="value" shape the line promises. */
  private clean(value: string): string {
    return value.replace(/"/g, "'");
  }

  /** A broken counter must not cost the whole period's numbers. */
  private safeSceneStats(): RenderTelemetrySceneStats {
    try {
      return this.sceneStats();
    } catch (e) {
      return {
        meshes: -1, activeMeshes: -1, activeIndices: -1, materials: -1,
        disabledMeshes: -1, instancedMeshes: -1, shadowCasters: -1, meshTop: "unknown",
        parkedMeshes: -1, parkingFilter: false,
        renderWidth: -1, renderHeight: -1, hardwareScaling: -1, gpu: null
      };
    }
  }
}
