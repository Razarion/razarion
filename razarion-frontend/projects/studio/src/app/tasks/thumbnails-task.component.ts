import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal} from '@angular/core';
import {ArcRotateCamera, Color4, HemisphericLight, Tools, Vector3} from '@babylonjs/core';
import {BabylonModelService} from '../../../../../src/app/game/renderer/babylon-model.service';
import {BabylonRenderServiceAccessImpl} from '../../../../../src/app/game/renderer/babylon-render-service-access-impl.service';
import {RenderObject} from '../../../../../src/app/game/renderer/render-object';
import {Diplomacy} from '../../../../../src/app/gwtangular/GwtAngularFacade';
import {ThumbnailItem, ThumbnailOverride, ThumbnailStorageService, sameItem} from './thumbnail-storage.service';

const DIPLOMACY_OPTIONS: readonly Diplomacy[] = [
  Diplomacy.OWN, Diplomacy.ENEMY, Diplomacy.FRIEND, Diplomacy.RESOURCE, Diplomacy.BOX
];

const DEFAULT_THUMBNAIL_SIZE = 200;
const MIN_THUMBNAIL_SIZE = 16;
const MAX_THUMBNAIL_SIZE = 4096;
const SIZE_STORAGE_KEY = 'razarion.studio.thumbnailSize';
const DEFAULT_ALPHA = -Math.PI / 4;
const DEFAULT_BETA = Math.PI / 2.8;
const RADIUS_MULTIPLIER = 2.2;
const MIN_RADIUS = 4;
const WARMUP_FRAMES = 3;

function loadSavedSize(): number {
  const raw = localStorage.getItem(SIZE_STORAGE_KEY);
  if (!raw) return DEFAULT_THUMBNAIL_SIZE;
  const n = parseInt(raw, 10);
  return Number.isFinite(n) && n >= MIN_THUMBNAIL_SIZE && n <= MAX_THUMBNAIL_SIZE ? n : DEFAULT_THUMBNAIL_SIZE;
}

/**
 * Studio's only task. Two responsibilities:
 *   - Grid of every baseItemType, server-driven; shows the saved thumbnail
 *     PNG (via /rest/image/{id}) or a placeholder.
 *   - On-demand positioner overlay: click an item, the Babylon engine spins
 *     up just to render that one model3D into a single ArcRotateCamera scene.
 *     User orbits, clicks Save, PNG + framing go to the server, overlay closes.
 *
 * Renderer lifecycle is intentionally lazy: nothing 3D loads until the user
 * opens the positioner the first time. The canvas lives in the DOM behind a
 * [hidden] flag so its WebGL context survives reopens (no engine leaks).
 */
@Component({
  selector: 'studio-thumbnails-task',
  standalone: true,
  template: `
    <div class="grid-panel" [class.dimmed]="positioning()">
      <header>
        <div>
          <h2>Item Thumbnails</h2>
          <p class="hint">Click an item to position its thumbnail. Saves to razarion-server.</p>
        </div>
        <div class="header-controls">
          <label class="size-input" title="Captured PNG side length in pixels (persisted)">
            <span>Size:</span>
            <input type="number"
                   [value]="thumbnailSize()"
                   (change)="onSizeChange($event)"
                   [min]="16" [max]="4096" step="8">
            <span class="unit">px²</span>
          </label>
          <button class="ghost" (click)="reload()" [disabled]="loading()">↻ Reload</button>
        </div>
      </header>

      @if (loading()) {
        <div class="status">Loading…</div>
      } @else if (storage.needsLogin()) {
        <div class="status warn">Not logged in — use the header form.</div>
      } @else if (items().length === 0) {
        <div class="status">No items returned by the server.</div>
      } @else {
        <div class="grid">
          @for (item of items(); track itemKey(item); let i = $index) {
            @if (i === 0 || items()[i - 1].kind !== item.kind) {
              <div class="kind-separator">{{ kindLabel(item.kind) }}</div>
            }
            <figure class="thumb" (click)="openPositioner(item)" [class.disabled]="item.model3DId === null">
              <div class="checker">
                @if (item.thumbnailImageId !== null) {
                  <img [src]="'/rest/image/' + item.thumbnailImageId"
                       [alt]="item.internalName"
                       loading="lazy"
                       (load)="onImageLoaded(item, $event)">
                } @else {
                  <span class="placeholder">no thumbnail</span>
                }
              </div>
              <figcaption>
                <div class="line line-main">
                  <span class="id">#{{ item.id }}</span>
                  <span class="name">{{ item.internalName }}</span>
                </div>
                <div class="line line-meta">
                  @if (imageDimensions()[itemKey(item)]; as dim) {
                    <span class="dim" [class.non-square]="dim.w !== dim.h" title="Image dimensions">{{ dim.w }}×{{ dim.h }}</span>
                    @if (dim.bytes !== null) {
                      <span class="size" title="File size on server">{{ formatBytes(dim.bytes) }}</span>
                    }
                  }
                  @if (item.override) {
                    <span class="badge" title="Has saved framing">tuned</span>
                    @if (item.override.diplomacy && item.override.diplomacy !== 'OWN') {
                      <span class="badge diplo" [title]="'Rendered as ' + item.override.diplomacy">{{ item.override.diplomacy }}</span>
                    }
                  }
                  @if (item.model3DId === null) {
                    <span class="badge warn" title="Item has no model3D">no model</span>
                  }
                </div>
              </figcaption>
            </figure>
          }
        </div>
      }
    </div>

    <!-- Positioner overlay. Canvas is rendered as a centered square (1:1)
         that fits between the header and the bottom edge — so what the user
         sees IS exactly what gets captured at 512×512 (no header crop, no
         hidden margins). Canvas stays in DOM via [hidden] so the engine
         survives reopens. -->
    <div class="positioner" [hidden]="!positioning()">
      <div class="canvas-frame" [class.ready]="!rendererStatus()">
        <canvas #positionerCanvas></canvas>
      </div>
      <div class="positioner-header">
        @if (positioning(); as item) {
          <strong>{{ kindLabelSingular(item.kind) }} #{{ item.id }} {{ item.internalName }}</strong>
        }
        <span class="sub">Drag = orbit · Wheel = zoom · Shift-Drag = pan</span>
        <label class="diplomacy-select" title="Affects material/team color baked into the thumbnail">
          <span>Diplomacy:</span>
          <select [value]="currentDiplomacy()" (change)="onDiplomacyChange($event)" [disabled]="busy() || !!rendererStatus()">
            @for (d of diplomacyOptions; track d) {
              <option [value]="d">{{ d }}</option>
            }
          </select>
        </label>
        <div class="spacer"></div>
        <button class="primary" (click)="save()" [disabled]="busy() || !!rendererStatus()">{{ busy() ? 'Saving…' : 'Save' }}</button>
        <button class="ghost" (click)="resetItem()" [disabled]="busy() || !positioning()?.override" title="Drop saved framing + image">Reset</button>
        <button (click)="cancel()" [disabled]="busy()">Cancel</button>
      </div>
      @if (rendererStatus(); as msg) {
        <div class="positioner-loading">
          <div class="spinner"></div>
          <span>{{ msg }}</span>
        </div>
      }
    </div>
  `,
  styles: [`
    :host {
      position: absolute;
      inset: 0;
      display: block;
    }
    .grid-panel {
      position: absolute;
      inset: 0;
      padding: 16px 18px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
    .grid-panel.dimmed { opacity: 0; pointer-events: none; }
    header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 12px;
    }
    h2 { margin: 0 0 4px 0; font-size: 15px; letter-spacing: 0.04em; }
    .hint { margin: 0; font-size: 12px; color: #aaa; line-height: 1.4; max-width: 540px; }
    button {
      padding: 7px 12px;
      border-radius: 5px;
      border: 1px solid #4a9eff;
      background: rgba(74, 158, 255, 0.18);
      color: #eee;
      cursor: pointer;
      font-size: 12px;
      white-space: nowrap;
    }
    button:hover:not(:disabled) { background: rgba(74, 158, 255, 0.32); }
    button.primary { background: rgba(74, 158, 255, 0.4); }
    button.primary:hover:not(:disabled) { background: rgba(74, 158, 255, 0.55); }
    button.ghost {
      background: transparent;
      border-color: rgba(255, 255, 255, 0.2);
      color: #aaa;
    }
    button:disabled { opacity: 0.5; cursor: default; }
    .header-controls {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .size-input {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 11px;
      color: #aaa;
    }
    .size-input input {
      width: 64px;
      padding: 5px 8px;
      font-size: 12px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-variant-numeric: tabular-nums;
      text-align: right;
    }
    .size-input input:focus { outline: none; border-color: #4a9eff; }
    .size-input .unit { color: #666; }
    .status {
      padding: 10px 12px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 4px;
      font-size: 12px;
      color: #bbb;
    }
    .status.warn {
      background: rgba(255, 200, 80, 0.12);
      color: #ffd56b;
      border: 1px solid rgba(255, 200, 80, 0.3);
    }
    .grid {
      flex: 1;
      overflow-y: auto;
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 14px;
      padding-right: 4px;
      align-content: start;
    }
    .kind-separator {
      grid-column: 1 / -1;
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #6b7280;
      padding: 6px 0 4px 2px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
      margin-top: 6px;
    }
    .kind-separator:first-child { margin-top: 0; }
    figure.thumb {
      margin: 0;
      display: flex;
      flex-direction: column;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 6px;
      overflow: hidden;
      cursor: pointer;
      transition: border-color 0.12s, transform 0.12s;
    }
    figure.thumb:hover {
      border-color: #4a9eff;
      transform: translateY(-1px);
    }
    figure.thumb.disabled { cursor: not-allowed; opacity: 0.6; }
    figure.thumb.disabled:hover { border-color: rgba(255, 255, 255, 0.08); transform: none; }
    .checker {
      aspect-ratio: 1;
      background-image:
        linear-gradient(45deg, #2a2d33 25%, transparent 25%),
        linear-gradient(-45deg, #2a2d33 25%, transparent 25%),
        linear-gradient(45deg, transparent 75%, #2a2d33 75%),
        linear-gradient(-45deg, transparent 75%, #2a2d33 75%);
      background-size: 16px 16px;
      background-position: 0 0, 0 8px, 8px -8px, -8px 0;
      background-color: #1d1f24;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
    }
    .checker img { width: 100%; height: 100%; object-fit: contain; display: block; }
    .checker .placeholder {
      font-size: 11px;
      color: #555;
      letter-spacing: 0.06em;
      text-transform: uppercase;
    }
    figcaption {
      padding: 6px 8px;
      font-size: 11px;
      display: flex;
      flex-direction: column;
      gap: 3px;
      border-top: 1px solid rgba(255, 255, 255, 0.06);
    }
    figcaption .line {
      display: flex;
      gap: 6px;
      align-items: baseline;
      min-width: 0;
    }
    figcaption .line-meta {
      flex-wrap: wrap;
      row-gap: 3px;
    }
    figcaption .line-meta:empty { display: none; }
    .id { color: #6b7280; font-variant-numeric: tabular-nums; }
    .name { color: #ddd; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .dim {
      color: #888;
      font-variant-numeric: tabular-nums;
      font-size: 10px;
    }
    .dim.non-square {
      color: #ffd56b;
      font-weight: 600;
    }
    .size {
      color: #888;
      font-variant-numeric: tabular-nums;
      font-size: 10px;
    }
    .badge {
      background: rgba(74, 158, 255, 0.25);
      color: #b6d6ff;
      padding: 1px 6px;
      border-radius: 8px;
      font-size: 10px;
    }
    .badge.warn { background: rgba(255, 200, 80, 0.2); color: #ffd56b; }
    .badge.diplo {
      background: rgba(255, 90, 90, 0.2);
      color: #ff9090;
      text-transform: uppercase;
      font-size: 9px;
      letter-spacing: 0.06em;
    }

    .positioner {
      position: absolute;
      inset: 0;
      background: #15171c;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 60px 16px 16px 16px;
    }
    .positioner[hidden] { display: none; }
    .canvas-frame {
      aspect-ratio: 1 / 1;
      max-width: 100%;
      max-height: 100%;
      box-sizing: border-box;
      border: 2px solid transparent;
    }
    .canvas-frame.ready {
      border-color: rgba(74, 158, 255, 0.6);
      border-style: dashed;
      box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.4);
    }
    .canvas-frame canvas {
      width: 100%;
      height: 100%;
      display: block;
      outline: none;
    }
    .positioner-header {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      z-index: 10;
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 14px;
      background: rgba(20, 22, 26, 0.92);
      border-bottom: 1px solid #2a2e35;
    }
    .positioner-header strong { font-size: 13px; }
    .positioner-header .sub { font-size: 11px; color: #888; }
    .positioner-header .spacer { flex: 1; }
    .diplomacy-select {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 11px;
      color: #aaa;
    }
    .diplomacy-select select {
      padding: 4px 6px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.4);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-family: inherit;
    }
    .diplomacy-select select:focus { outline: none; border-color: #4a9eff; }
    .diplomacy-select select:disabled { opacity: 0.5; }

    .positioner-loading {
      position: absolute;
      inset: 0;
      z-index: 5;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 14px;
      background: rgba(21, 23, 28, 0.85);
      color: #ccc;
      font-size: 13px;
      letter-spacing: 0.04em;
    }
    .spinner {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      border: 3px solid rgba(74, 158, 255, 0.2);
      border-top-color: #4a9eff;
      animation: spin 0.9s linear infinite;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class ThumbnailsTaskComponent implements OnInit, AfterViewInit, OnDestroy {
  protected readonly storage = inject(ThumbnailStorageService);
  private readonly babylonModelService = inject(BabylonModelService);
  private readonly babylonRenderServiceAccessImpl = inject(BabylonRenderServiceAccessImpl);

  readonly items = this.storage.items;
  readonly positioning = signal<ThumbnailItem | null>(null);
  readonly busy = signal(false);
  readonly loading = signal(false);
  readonly rendererStatus = signal<string>('');
  /** Native dimensions + transferred byte count per loaded grid image. */
  readonly imageDimensions = signal<Record<string, {w: number; h: number; bytes: number | null}>>({});
  /** Side length in px of the captured PNG. Persisted in localStorage. */
  readonly thumbnailSize = signal(loadSavedSize());
  /** Active diplomacy for the model currently in the positioner. */
  readonly currentDiplomacy = signal<Diplomacy>(Diplomacy.OWN);
  readonly diplomacyOptions = DIPLOMACY_OPTIONS;

  @ViewChild('positionerCanvas', {static: true}) private canvasRef!: ElementRef<HTMLCanvasElement>;

  private rendererReady = false;
  private rendererInitPromise: Promise<void> | null = null;
  private currentModel: RenderObject | null = null;
  private camera: ArcRotateCamera | null = null;
  private light: HemisphericLight | null = null;

  async ngOnInit(): Promise<void> {
    if (this.storage.needsLogin()) return;
    await this.reload();
  }

  ngAfterViewInit(): void {
    // canvas is in DOM (hidden) — engine setup deferred until first openPositioner().
  }

  ngOnDestroy(): void {
    this.cleanupModel();
    this.babylonRenderServiceAccessImpl.getScene()?.getEngine()?.stopRenderLoop();
  }

  itemKey(item: ThumbnailItem): string {
    return `${item.kind}:${item.id}`;
  }

  kindLabel(kind: ThumbnailItem['kind']): string {
    return kind === 'resource' ? 'Resources' : kind === 'box' ? 'Boxes' : 'Base Items';
  }

  kindLabelSingular(kind: ThumbnailItem['kind']): string {
    return kind === 'resource' ? 'Resource' : kind === 'box' ? 'Box' : 'Base';
  }

  async onImageLoaded(item: ThumbnailItem, event: Event): Promise<void> {
    const img = event.target as HTMLImageElement;
    if (!img.naturalWidth) return;
    // PerformanceResourceTiming may not be flushed by the time `load` fires;
    // wait a frame, then try encoded/decoded/transfer in that order. Cached
    // hits with all three at 0 fall through to a same-URL fetch (HTTP cache
    // serves it instantly, no real network).
    await new Promise(r => requestAnimationFrame(r));
    let bytes: number | null = null;
    const entries = performance.getEntriesByName(img.src) as PerformanceResourceTiming[];
    for (let i = entries.length - 1; i >= 0; i--) {
      const e = entries[i];
      const n = e.encodedBodySize || e.decodedBodySize || e.transferSize;
      if (n > 0) { bytes = n; break; }
    }
    if (bytes === null) {
      try {
        const res = await fetch(img.src);
        const blob = await res.blob();
        bytes = blob.size;
      } catch (_) { /* leave null */ }
    }
    this.imageDimensions.update(m => ({
      ...m,
      [this.itemKey(item)]: {w: img.naturalWidth, h: img.naturalHeight, bytes}
    }));
  }

  formatBytes(n: number | null): string {
    if (n == null) return '';
    if (n < 1024) return `${n} B`;
    if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
    return `${(n / (1024 * 1024)).toFixed(2)} MB`;
  }

  onSizeChange(event: Event): void {
    const raw = parseInt((event.target as HTMLInputElement).value, 10);
    if (!Number.isFinite(raw)) return;
    const clamped = Math.max(MIN_THUMBNAIL_SIZE, Math.min(MAX_THUMBNAIL_SIZE, raw));
    this.thumbnailSize.set(clamped);
    localStorage.setItem(SIZE_STORAGE_KEY, String(clamped));
  }

  async reload(): Promise<void> {
    this.loading.set(true);
    try {
      await this.storage.loadAll();
    } catch (e) {
      console.warn('[Studio] reload failed', e);
    } finally {
      this.loading.set(false);
    }
  }

  async openPositioner(item: ThumbnailItem): Promise<void> {
    if (item.model3DId === null) return;
    this.positioning.set(item);
    this.currentDiplomacy.set((item.override?.diplomacy as Diplomacy) ?? Diplomacy.OWN);
    this.rendererStatus.set(this.rendererReady ? 'Loading model…' : 'Initialising renderer…');

    // Canvas is rendered behind [hidden] until positioning() is set; wait for
    // the next frame so it has real layout dimensions before Engine reads
    // them, otherwise WebGL viewport stays 0×0 and nothing draws.
    await new Promise(r => requestAnimationFrame(r));

    await this.ensureRendererReady();
    this.rendererStatus.set('Loading model…');
    this.babylonRenderServiceAccessImpl.getScene().getEngine().resize();
    this.loadAndFrameModel(item);
    this.startRenderLoop();

    // Give the GPU a few frames to compile shaders + stream textures so the
    // first visible frame already shows the unit instead of a black scene.
    for (let i = 0; i < WARMUP_FRAMES; i++) {
      await new Promise(r => requestAnimationFrame(r));
    }
    this.rendererStatus.set('');
  }

  cancel(): void {
    this.cleanupModel();
    this.babylonRenderServiceAccessImpl.getScene()?.getEngine()?.stopRenderLoop();
    this.camera?.detachControl();
    this.positioning.set(null);
  }

  async save(): Promise<void> {
    const item = this.positioning();
    if (!item || !this.camera) return;
    this.busy.set(true);
    try {
      const override: ThumbnailOverride = {
        alpha: this.camera.alpha,
        beta: this.camera.beta,
        radius: this.camera.radius,
        targetX: this.camera.target.x,
        targetY: this.camera.target.y,
        targetZ: this.camera.target.z,
        diplomacy: this.currentDiplomacy()
      };
      const dataUrl = await this.captureScreenshot();
      const png = await (await fetch(dataUrl)).blob();
      await this.storage.save(item, override, png);
      this.cancel();
    } catch (e) {
      console.error('[Studio] save failed', e);
    } finally {
      this.busy.set(false);
    }
  }

  /**
   * Diplomacy controls material/team color, baked at cloneModel3D time.
   * Changing it means re-cloning the model — camera state is preserved so the
   * framing stays identical.
   */
  onDiplomacyChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as Diplomacy;
    this.currentDiplomacy.set(value);
    const item = this.positioning();
    if (!item || !this.rendererReady) return;
    // Rebuild only the model; keep the existing camera so the user doesn't
    // lose their framing.
    this.cleanupModel();
    this.currentModel = this.babylonModelService.cloneModel3D(item.model3DId!, null, value);
  }

  async resetItem(): Promise<void> {
    const item = this.positioning();
    if (!item) return;
    this.busy.set(true);
    try {
      await this.storage.clear(item);
      this.cancel();
    } catch (e) {
      console.error('[Studio] reset failed', e);
    } finally {
      this.busy.set(false);
    }
  }

  // ===== Renderer lifecycle =====

  private ensureRendererReady(): Promise<void> {
    if (this.rendererReady) return Promise.resolve();
    if (this.rendererInitPromise) return this.rendererInitPromise;

    this.rendererInitPromise = (async () => {
      this.babylonRenderServiceAccessImpl.setup(this.canvasRef.nativeElement);
      const scene = this.babylonRenderServiceAccessImpl.getScene();
      // Fully transparent so the captured PNG carries an alpha channel; in
      // preview the positioner's dark background shows through the canvas,
      // visually identical to the previous opaque clear color.
      scene.clearColor = new Color4(0, 0, 0, 0);
      this.light = new HemisphericLight('thumb-fill', new Vector3(0.3, 1, 0.4), scene);
      this.light.intensity = 1.1;
      this.rendererStatus.set('Loading model library (one-time)…');
      await this.babylonModelService.init();
      this.rendererReady = true;
    })();
    return this.rendererInitPromise;
  }

  private loadAndFrameModel(item: ThumbnailItem): void {
    this.cleanupModel();
    this.currentModel = this.babylonModelService.cloneModel3D(item.model3DId!, null, this.currentDiplomacy());
    const scene = this.babylonRenderServiceAccessImpl.getScene();

    const node = this.currentModel.getModel3D();
    const {min, max} = node.getHierarchyBoundingVectors();
    const size = max.subtract(min);
    const maxDim = Math.max(size.x, size.y, size.z);
    const defaultRadius = Math.max(maxDim * RADIUS_MULTIPLIER, MIN_RADIUS);
    const defaultTarget = min.add(max).scale(0.5);

    if (!this.camera) {
      this.camera = new ArcRotateCamera('thumb-cam', DEFAULT_ALPHA, DEFAULT_BETA, defaultRadius, defaultTarget, scene);
    }
    if (item.override) {
      this.camera.alpha = item.override.alpha;
      this.camera.beta = item.override.beta;
      this.camera.radius = item.override.radius;
      this.camera.target = new Vector3(item.override.targetX, item.override.targetY, item.override.targetZ);
    } else {
      this.camera.alpha = DEFAULT_ALPHA;
      this.camera.beta = DEFAULT_BETA;
      this.camera.radius = defaultRadius;
      this.camera.target = defaultTarget;
    }
    // Smooth wheel zoom: percentage-based instead of fixed step (default
    // wheelPrecision=3 jumps in chunks of radius/3 per notch, which is
    // unusable for fine framing). 1% per notch + clamp to bounding box.
    this.camera.wheelDeltaPercentage = 0.01;
    this.camera.pinchDeltaPercentage = 0.01;
    this.camera.lowerRadiusLimit = Math.max(maxDim * 0.4, 0.5);
    this.camera.upperRadiusLimit = Math.max(maxDim * 8, 50);
    scene.activeCamera = this.camera;
    this.camera.attachControl(this.canvasRef.nativeElement, true);
  }

  private startRenderLoop(): void {
    const scene = this.babylonRenderServiceAccessImpl.getScene();
    const engine = scene.getEngine();
    engine.stopRenderLoop();
    engine.runRenderLoop(() => scene.render());
  }

  private cleanupModel(): void {
    if (this.currentModel) {
      this.currentModel.dispose();
      this.currentModel = null;
    }
  }

  private captureScreenshot(): Promise<string> {
    const scene = this.babylonRenderServiceAccessImpl.getScene();
    const engine = scene.getEngine();
    return new Promise<string>(async (resolve, reject) => {
      // Warm-up frames so shaders/textures finish for the off-screen render-target.
      for (let f = 0; f < WARMUP_FRAMES; f++) {
        scene.render();
        await new Promise(r => requestAnimationFrame(r));
      }
      Tools.CreateScreenshotUsingRenderTarget(
        engine as any,
        this.camera!,
        {width: this.thumbnailSize(), height: this.thumbnailSize(), precision: 1},
        data => data ? resolve(data) : reject(new Error('empty screenshot')),
        'image/png',
        4,
        true
      );
    });
  }
}
