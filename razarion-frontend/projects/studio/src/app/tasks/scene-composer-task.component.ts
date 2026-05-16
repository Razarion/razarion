import {AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {
  ArcRotateCamera,
  Color3,
  Color4,
  DirectionalLight,
  GizmoManager,
  Mesh,
  PointerEventTypes,
  Scene,
  ShadowGenerator,
  TransformNode,
  Vector3
} from '@babylonjs/core';
import {StudioSceneSummary} from '../../../../../src/app/generated/razarion-share';
import {BabylonModelService} from '../../../../../src/app/game/renderer/babylon-model.service';
import {BabylonRenderServiceAccessImpl} from '../../../../../src/app/game/renderer/babylon-render-service-access-impl.service';
import {RenderObject} from '../../../../../src/app/game/renderer/render-object';
import {Diplomacy} from '../../../../../src/app/gwtangular/GwtAngularFacade';
import {ObjectNameId} from '../../../../../src/app/generated/razarion-share';
import {SceneContent, SceneItem, SceneStorageService, SceneTerrain, emptyScene} from './scene-storage.service';
import {TerrainLoaderService} from './terrain-loader.service';
import {ThumbnailItem, ThumbnailStorageService} from './thumbnail-storage.service';

const DIPLOMACIES: Diplomacy[] = [Diplomacy.OWN, Diplomacy.ENEMY, Diplomacy.FRIEND, Diplomacy.RESOURCE, Diplomacy.BOX];

export type GizmoTool = 'move' | 'rotate' | 'scale';

/**
 * Three-pane scene composer: Scene list · Babylon viewport · Library/Properties.
 *
 * Items spawn at origin on library click; clicking a placed mesh selects it and
 * attaches a Babylon GizmoManager so the user can move/rotate/scale via gizmos
 * or via the numeric inputs in the Properties pane. Save flushes the scene-node
 * transforms back into content.items[] before POST.
 *
 * Renderer is on-demand — Babylon engine spins up the first time a scene is
 * opened and stays warm for subsequent opens within the session.
 */
@Component({
  selector: 'studio-scene-composer-task',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="composer">
      <!-- ============ LEFT: scene list ============ -->
      <aside class="left">
        <header>
          <h3>Scenes</h3>
          <button class="ghost sm" (click)="reload()" [disabled]="loading()" title="Re-fetch scene list">↻</button>
        </header>

        @if (!hasToken()) {
          <div class="status warn">Not logged in — use the header form.</div>
        }
        @if (storage.lastError(); as err) {
          <div class="status error">{{ err }}</div>
        }

        <ul class="scene-list">
          @for (s of storage.scenes(); track s.id) {
            <li [class.active]="current()?.id === s.id" (click)="openScene(s.id)">
              <span class="scene-name">{{ s.name }}</span>
              <button class="ghost xs" (click)="deleteScene(s, $event)" title="Delete">×</button>
            </li>
          } @empty {
            <li class="empty">No scenes yet.</li>
          }
        </ul>

        <button class="primary block" (click)="createScene()" [disabled]="busy()">+ New scene</button>
      </aside>

      <!-- ============ CENTER: viewport ============ -->
      <section class="viewport">
        @if (current()) {
          <div class="viewport-header">
            <input class="scene-rename" type="text" [(ngModel)]="currentName" [disabled]="busy()">
            <div class="tool-palette" role="group" aria-label="Transform tool">
              <button [class.active]="gizmoTool() === 'move'" (click)="setGizmoTool('move')" title="Move (G)">↔</button>
              <button [class.active]="gizmoTool() === 'rotate'" (click)="setGizmoTool('rotate')" title="Rotate (R)">↻</button>
              <button [class.active]="gizmoTool() === 'scale'" (click)="setGizmoTool('scale')" title="Scale (S)">⤡</button>
            </div>
            <div class="spacer"></div>
            <button class="primary" (click)="saveScene()" [disabled]="busy() || !!rendererStatus()">
              {{ busy() ? 'Saving…' : 'Save' }}
            </button>
          </div>
        } @else {
          <div class="placeholder">
            <p>Pick a scene from the left or click <strong>+ New scene</strong>.</p>
          </div>
        }
        <canvas #viewportCanvas [hidden]="!current()"></canvas>
        @if (current() && rendererStatus(); as msg) {
          <div class="loading-overlay">
            <div class="spinner"></div>
            <span>{{ msg }}</span>
          </div>
        }
      </section>

      <!-- ============ RIGHT: library + properties ============ -->
      <aside class="right">
        @if (current()) {
          <header><h3>Scene</h3></header>
          <div class="scene-settings">
            <div class="prop-row">
              <span class="prop-label">Planet</span>
              <select (change)="onPlanetChange($event)" [disabled]="busy()">
                <option value="" [selected]="content()?.terrain?.planetId == null">— none —</option>
                @for (p of planets(); track p.id) {
                  <option [value]="p.id" [selected]="p.id === content()?.terrain?.planetId">{{ p.internalName }} (#{{ p.id }})</option>
                }
              </select>
            </div>
          </div>

          <div class="divider"></div>

          <header>
            <h3>Library</h3>
            <input class="filter" type="search" placeholder="filter…" [(ngModel)]="libraryFilter">
          </header>
          <div class="library">
            @for (it of filteredLibrary(); track itemKey(it)) {
              <button class="lib-item"
                      (click)="spawnFromLibrary(it)"
                      [disabled]="it.model3DId === null"
                      [title]="it.model3DId === null ? 'No model3D' : 'Click to spawn at origin'">
                @if (it.thumbnailImageId !== null) {
                  <img [src]="'/rest/image/' + it.thumbnailImageId" [alt]="it.internalName">
                } @else {
                  <div class="no-img">{{ it.kind[0].toUpperCase() }}</div>
                }
                <span class="lib-name">{{ it.internalName }}</span>
              </button>
            } @empty {
              <div class="empty">No items match.</div>
            }
          </div>

          <div class="divider"></div>

          <header><h3>Properties</h3></header>
          @if (selectedItem(); as it) {
            <div class="props">
              <div class="prop-row">
                <span class="prop-label">Item</span>
                <span class="prop-value">{{ libraryLabel(it) }} <span class="muted">#{{ it.id }}</span></span>
              </div>
              <div class="prop-row">
                <span class="prop-label">Position</span>
                <input type="number" step="0.5" [ngModel]="it.position[0]" (ngModelChange)="updateProp('px', $event)">
                <input type="number" step="0.5" [ngModel]="it.position[1]" (ngModelChange)="updateProp('py', $event)">
                <input type="number" step="0.5" [ngModel]="it.position[2]" (ngModelChange)="updateProp('pz', $event)">
              </div>
              <div class="prop-row">
                <span class="prop-label">Rotation Y</span>
                <input type="number" step="0.1" [ngModel]="it.rotationY" (ngModelChange)="updateProp('ry', $event)">
                <span class="muted">rad</span>
              </div>
              <div class="prop-row">
                <span class="prop-label">Scale</span>
                <input type="number" step="0.1" min="0.1" [ngModel]="it.scale" (ngModelChange)="updateProp('s', $event)">
              </div>
              <div class="prop-row">
                <span class="prop-label">Diplomacy</span>
                <select [ngModel]="it.diplomacy" (ngModelChange)="updateProp('diplomacy', $event)">
                  @for (d of diplomacies; track d) {
                    <option [value]="d">{{ d }}</option>
                  }
                </select>
              </div>
              <div class="prop-row">
                <button class="ghost" (click)="deleteSelected()">Delete item</button>
              </div>
            </div>
          } @else {
            <div class="placeholder small">
              <p>Click a placed item in the viewport to edit it.</p>
              <p class="muted">items: {{ content()?.items?.length ?? 0 }}</p>
            </div>
          }
        }
      </aside>
    </div>
  `,
  styles: [`
    :host { position: absolute; inset: 0; display: block; }
    .composer {
      position: absolute;
      inset: 0;
      display: grid;
      grid-template-columns: 220px 1fr 280px;
      gap: 1px;
      background: #2a2e35;
    }
    aside, .viewport {
      background: #15171c;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
    aside header, .viewport-header {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      border-bottom: 1px solid #2a2e35;
      gap: 8px;
      flex-shrink: 0;
    }
    aside h3 {
      margin: 0;
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: #aaa;
    }
    .scene-list {
      list-style: none;
      margin: 0;
      padding: 6px 0;
      flex: 1;
      overflow-y: auto;
    }
    .scene-list li {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 7px 12px;
      font-size: 12px;
      color: #ddd;
      cursor: pointer;
      border-left: 3px solid transparent;
    }
    .scene-list li:hover { background: #1c1f25; }
    .scene-list li.active { background: #232830; border-left-color: #4a9eff; }
    .scene-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .scene-list li.empty { color: #666; font-style: italic; cursor: default; }
    .scene-list li.empty:hover { background: transparent; }
    button {
      padding: 6px 12px;
      border-radius: 4px;
      border: 1px solid #4a9eff;
      background: rgba(74, 158, 255, 0.18);
      color: #eee;
      cursor: pointer;
      font-size: 12px;
    }
    button:hover:not(:disabled) { background: rgba(74, 158, 255, 0.32); }
    button.primary { background: rgba(74, 158, 255, 0.4); }
    button.primary:hover:not(:disabled) { background: rgba(74, 158, 255, 0.55); }
    button.ghost { background: transparent; border-color: rgba(255, 255, 255, 0.2); color: #aaa; }
    button.sm { padding: 3px 8px; font-size: 11px; }
    button.xs { padding: 0 6px; font-size: 14px; line-height: 1; }
    button.block { margin: 8px 12px; }
    button:disabled { opacity: 0.5; cursor: default; }
    .status {
      margin: 8px 12px;
      padding: 8px 10px;
      border-radius: 4px;
      font-size: 11px;
    }
    .status.warn { background: rgba(255, 200, 80, 0.12); color: #ffd56b; border: 1px solid rgba(255, 200, 80, 0.3); }
    .status.error { background: rgba(255, 90, 90, 0.12); color: #ff8a8a; border: 1px solid rgba(255, 90, 90, 0.3); }

    .viewport { background: #0e1014; position: relative; }
    .viewport canvas { flex: 1; width: 100%; height: 100%; display: block; outline: none; }
    .viewport-header {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      z-index: 10;
      background: rgba(20, 22, 26, 0.92);
    }
    .scene-rename {
      padding: 5px 8px;
      font-size: 13px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-family: inherit;
      min-width: 200px;
    }
    .scene-rename:focus { outline: none; border-color: #4a9eff; }
    .viewport-header .spacer { flex: 1; }
    .tool-palette {
      display: inline-flex;
      border: 1px solid #4a9eff;
      border-radius: 4px;
      overflow: hidden;
      margin-left: 8px;
    }
    .tool-palette button {
      padding: 5px 10px;
      border: none;
      border-radius: 0;
      background: transparent;
      color: #aaa;
      font-size: 14px;
      line-height: 1;
      cursor: pointer;
      min-width: 28px;
    }
    .tool-palette button.active {
      background: rgba(74, 158, 255, 0.4);
      color: #fff;
    }
    .tool-palette button:not(.active):hover {
      background: rgba(74, 158, 255, 0.12);
    }
    .loading-overlay {
      position: absolute;
      inset: 0;
      z-index: 20;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 14px;
      background: rgba(14, 16, 20, 0.85);
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
    @keyframes spin { to { transform: rotate(360deg); } }
    .placeholder {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #777;
      font-size: 13px;
      padding: 30px;
      text-align: center;
    }
    .placeholder.small { padding: 12px; font-size: 11px; align-items: flex-start; }
    .placeholder p { margin: 4px 0; }
    .placeholder .muted, .muted { color: #555; font-size: 10px; }

    .filter {
      flex: 1;
      padding: 4px 8px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-family: inherit;
    }
    .filter:focus { outline: none; border-color: #4a9eff; }
    .library {
      flex: 1;
      overflow-y: auto;
      padding: 4px 8px;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 4px;
      min-height: 100px;
    }
    .library .empty { grid-column: 1 / -1; color: #666; font-style: italic; padding: 12px; font-size: 11px; }
    .lib-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2px;
      padding: 4px;
      border-radius: 3px;
      border: 1px solid rgba(255, 255, 255, 0.08);
      background: rgba(255, 255, 255, 0.03);
      cursor: pointer;
      font-size: 10px;
    }
    .lib-item:hover:not(:disabled) { border-color: #4a9eff; background: rgba(74, 158, 255, 0.12); }
    .lib-item img { width: 56px; height: 56px; object-fit: contain; image-rendering: auto; }
    .lib-item .no-img {
      width: 56px;
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #1a1d22;
      color: #555;
      font-weight: bold;
    }
    .lib-name {
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #ccc;
    }
    .divider { height: 1px; background: #2a2e35; margin: 4px 0; }
    .props {
      padding: 8px 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      font-size: 11px;
    }
    .prop-row {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
    }
    .prop-label {
      flex: 0 0 70px;
      color: #aaa;
      font-weight: 500;
    }
    .prop-value { color: #ddd; font-size: 11px; }
    .props input, .props select {
      flex: 1;
      min-width: 50px;
      padding: 3px 6px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-variant-numeric: tabular-nums;
      font-family: inherit;
    }
    .props input:focus, .props select:focus { outline: none; border-color: #4a9eff; }
    .scene-settings { padding: 8px 12px; display: flex; flex-direction: column; gap: 6px; font-size: 11px; }
    .scene-settings select {
      flex: 1;
      padding: 3px 6px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-family: inherit;
    }
    .scene-settings select:focus { outline: none; border-color: #4a9eff; }
  `]
})
export class SceneComposerTaskComponent implements OnInit, AfterViewInit {
  protected readonly storage = inject(SceneStorageService);
  protected readonly thumbStorage = inject(ThumbnailStorageService);
  private readonly babylonModel = inject(BabylonModelService);
  private readonly babylonRender = inject(BabylonRenderServiceAccessImpl);
  private readonly terrainLoader = inject(TerrainLoaderService);

  @ViewChild('viewportCanvas', {static: true}) private canvasRef!: ElementRef<HTMLCanvasElement>;

  readonly loading = signal(false);
  readonly busy = signal(false);
  readonly current = signal<StudioSceneSummary | null>(null);
  readonly content = signal<SceneContent | null>(null);
  readonly selectedItemId = signal<number | null>(null);
  readonly rendererStatus = signal<string>('');
  readonly gizmoTool = signal<GizmoTool>('move');
  readonly diplomacies = DIPLOMACIES;
  readonly planets = signal<ObjectNameId[]>([]);
  currentName = '';
  libraryFilter = '';

  private rendererReady = false;
  private camera: ArcRotateCamera | null = null;
  private gizmoManager: GizmoManager | null = null;
  private readonly nodes = new Map<number, TransformNode>();
  private readonly renderObjects = new Map<number, RenderObject>();
  private nextItemId = 1;

  async ngOnInit(): Promise<void> {
    if (!this.hasToken()) return;
    await this.reload();
    // Library content lives in ThumbnailStorageService — load once so the
    // library list is populated when the user opens any scene.
    if (this.thumbStorage.items().length === 0) {
      await this.thumbStorage.loadAll().catch(() => {});
    }
    // Planet list for the terrain dropdown.
    try {
      this.planets.set(await this.terrainLoader.listPlanets());
    } catch (e) {
      console.warn('[Studio] planet list failed', e);
    }
  }

  ngAfterViewInit(): void {
    // Canvas is in DOM (hidden via [hidden]); engine bootstraps on first
    // openScene so users who never visit /scenes pay no Babylon cost.
  }

  hasToken(): boolean { return !!localStorage.getItem('app.token'); }
  itemKey(it: ThumbnailItem): string { return `${it.kind}:${it.id}`; }

  setGizmoTool(tool: GizmoTool): void {
    this.gizmoTool.set(tool);
    this.applyGizmoTool();
  }

  private applyGizmoTool(): void {
    if (!this.gizmoManager) return;
    const tool = this.gizmoTool();
    this.gizmoManager.positionGizmoEnabled = tool === 'move';
    this.gizmoManager.rotationGizmoEnabled = tool === 'rotate';
    this.gizmoManager.scaleGizmoEnabled = tool === 'scale';
    // Re-attach to current selection so the newly-enabled gizmo appears.
    this.attachGizmoToSelection();
  }

  /** Blender-ish shortcuts; ignore when typing into an input. */
  @HostListener('document:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    if (!this.current() || !this.rendererReady) return;
    const target = event.target as HTMLElement | null;
    if (target && (target.tagName === 'INPUT' || target.tagName === 'SELECT' || target.tagName === 'TEXTAREA')) return;
    switch (event.key.toLowerCase()) {
      case 'g': this.setGizmoTool('move'); break;
      case 'r': this.setGizmoTool('rotate'); break;
      case 's': this.setGizmoTool('scale'); break;
      default: return;
    }
    event.preventDefault();
  }

  filteredLibrary(): ThumbnailItem[] {
    const f = this.libraryFilter.trim().toLowerCase();
    const all = this.thumbStorage.items();
    if (!f) return all;
    return all.filter(i => i.internalName.toLowerCase().includes(f));
  }

  selectedItem(): SceneItem | null {
    const sel = this.selectedItemId();
    if (sel == null) return null;
    return this.content()?.items.find(i => i.id === sel) ?? null;
  }

  libraryLabel(it: SceneItem): string {
    const lib = this.thumbStorage.items().find(l => l.kind === it.kind && l.id === it.itemTypeId);
    return lib?.internalName ?? `${it.kind}#${it.itemTypeId}`;
  }

  // ===== Scene CRUD =====

  async reload(): Promise<void> {
    this.loading.set(true);
    try { await this.storage.list(); } finally { this.loading.set(false); }
  }

  async createScene(): Promise<void> {
    this.busy.set(true);
    try {
      const name = `Scene ${this.storage.scenes().length + 1}`;
      const summary = await this.storage.create(name, emptyScene());
      await this.openScene(summary.id);
    } finally {
      this.busy.set(false);
    }
  }

  async openScene(id: number): Promise<void> {
    this.busy.set(true);
    this.rendererStatus.set(this.rendererReady ? 'Loading scene…' : 'Initialising renderer…');
    try {
      const {summary, content} = await this.storage.read(id);
      this.disposeAllItems();
      this.terrainLoader.disposeTerrain();
      this.current.set(summary);
      this.content.set(content);
      this.currentName = summary.name;
      this.selectedItemId.set(null);

      // Wait one frame so the canvas (just un-hidden via *if/hidden) has
      // its layout size before Babylon binds the WebGL viewport.
      await new Promise(r => requestAnimationFrame(r));
      await this.ensureRenderer();
      this.babylonRender.getScene().getEngine().resize();

      // Recompute next id from loaded content so re-saves don't clash.
      this.nextItemId = content.items.reduce((m, i) => Math.max(m, i.id), 0) + 1;
      for (const item of content.items) {
        this.spawnNode(item);
      }
      // Terrain — fired after items so terrain build progress isn't blocking
      // the item display. Errors are logged but don't abort scene open.
      if (content.terrain?.planetId) {
        this.rendererStatus.set('Loading terrain…');
        await this.terrainLoader.loadTerrain(content.terrain.planetId).catch(e => {
          console.warn('[Studio] terrain load failed', e);
        });
      }
      this.applyCamera(content);
      this.rendererStatus.set('');
    } catch (e) {
      console.warn('[Studio] scene open failed', e);
      this.rendererStatus.set('');
    } finally {
      this.busy.set(false);
    }
  }

  async saveScene(): Promise<void> {
    const cur = this.current();
    const cnt = this.content();
    if (!cur || !cnt) return;
    this.busy.set(true);
    try {
      // Flush scene-node transforms into content.items (gizmo edits live on
      // the nodes, not in the model until we save).
      for (const item of cnt.items) {
        const node = this.nodes.get(item.id);
        if (!node) continue;
        item.position = [node.position.x, node.position.y, node.position.z];
        item.rotationY = node.rotation.y;
        item.scale = node.scaling.x;
      }
      // Camera state.
      if (this.camera) {
        cnt.camera = {
          mode: 'arc',
          position: [this.camera.position.x, this.camera.position.y, this.camera.position.z],
          target: [this.camera.target.x, this.camera.target.y, this.camera.target.z],
          alpha: this.camera.alpha, beta: this.camera.beta, radius: this.camera.radius
        };
      }
      const updated = await this.storage.save(cur.id, this.currentName, cnt);
      this.current.set(updated);
    } catch (e) {
      console.warn('[Studio] scene save failed', e);
    } finally {
      this.busy.set(false);
    }
  }

  async deleteScene(s: StudioSceneSummary, event: Event): Promise<void> {
    event.stopPropagation();
    if (!confirm(`Delete scene "${s.name}"? This cannot be undone.`)) return;
    this.busy.set(true);
    try {
      await this.storage.delete(s.id);
      if (this.current()?.id === s.id) {
        this.disposeAllItems();
        this.terrainLoader.disposeTerrain();
        this.current.set(null);
        this.content.set(null);
        this.currentName = '';
        this.selectedItemId.set(null);
      }
    } finally {
      this.busy.set(false);
    }
  }

  // ===== Item placement =====

  spawnFromLibrary(libItem: ThumbnailItem): void {
    if (libItem.model3DId == null) return;
    // Spawn at what the camera is looking at, so the new item lands in the
    // viewport's centre instead of always at world origin.
    const t = this.camera?.target ?? Vector3.Zero();
    const item: SceneItem = {
      id: this.nextItemId++,
      kind: libItem.kind,
      itemTypeId: libItem.id,
      position: [t.x, t.y, t.z],
      rotationY: 0,
      scale: 1,
      diplomacy: 'OWN'
    };
    this.content.update(c => c ? {...c, items: [...c.items, item]} : c);
    this.spawnNode(item);
    this.selectedItemId.set(item.id);
    this.attachGizmoToSelection();
  }

  /** Persist + reload terrain when the user picks a different planet. */
  async onPlanetChange(event: Event): Promise<void> {
    const raw = (event.target as HTMLSelectElement).value;
    const planetId = raw ? parseInt(raw, 10) : null;
    this.content.update(c => {
      if (!c) return c;
      const terrain: SceneTerrain | null = planetId ? {planetId, region: null} : null;
      return {...c, terrain};
    });
    this.rendererStatus.set('Loading terrain…');
    try {
      await this.terrainLoader.loadTerrain(planetId);
    } catch (e) {
      console.warn('[Studio] terrain load failed', e);
    } finally {
      this.rendererStatus.set('');
    }
  }

  deleteSelected(): void {
    const id = this.selectedItemId();
    if (id == null) return;
    this.disposeItem(id);
    this.content.update(c => c ? {...c, items: c.items.filter(i => i.id !== id)} : c);
    this.selectedItemId.set(null);
    this.gizmoManager?.attachToMesh(null);
  }

  updateProp(field: 'px' | 'py' | 'pz' | 'ry' | 's' | 'diplomacy', value: number | string): void {
    const id = this.selectedItemId();
    if (id == null) return;
    this.content.update(c => {
      if (!c) return c;
      const items = c.items.map(i => {
        if (i.id !== id) return i;
        const next = {...i};
        switch (field) {
          case 'px': next.position = [+value, i.position[1], i.position[2]]; break;
          case 'py': next.position = [i.position[0], +value, i.position[2]]; break;
          case 'pz': next.position = [i.position[0], i.position[1], +value]; break;
          case 'ry': next.rotationY = +value; break;
          case 's':  next.scale = Math.max(0.01, +value); break;
          case 'diplomacy': next.diplomacy = String(value); break;
        }
        return next;
      });
      return {...c, items};
    });
    this.applyToNode(id);
  }

  // ===== Babylon plumbing =====

  private async ensureRenderer(): Promise<void> {
    if (this.rendererReady) return;
    this.babylonRender.setup(this.canvasRef.nativeElement);
    const scene = this.babylonRender.getScene();
    scene.clearColor = new Color4(0.05, 0.06, 0.08, 1);

    // Production lighting parity — env IBL (added by `setup()` via
    // scene.createDefaultEnvironment) plus a directional light. A hemispheric
    // fill would compound with both and wash out the terrain ground.
    // babylon-terrain-tile.impl also reads directionalLight.includedOnlyMeshes
    // and shadowGenerator off the renderer service directly, so they must
    // exist before terrain build runs.
    const dirLight = new DirectionalLight('SceneDirectional', new Vector3(-3, -10, 3), scene);
    dirLight.intensity = 0.8;
    dirLight.shadowMinZ = -100;
    dirLight.shadowMaxZ = 200;
    dirLight.autoUpdateExtends = false;
    dirLight.shadowFrustumSize = 150;
    dirLight.diffuse = new Color3(1, 1, 1);
    dirLight.specular = new Color3(1, 1, 1);
    dirLight.shadowEnabled = true;
    this.babylonRender.directionalLight = dirLight;
    const shadowGenerator = new ShadowGenerator(2048, dirLight);
    shadowGenerator.useExponentialShadowMap = true;
    shadowGenerator.darkness = 0.6;
    this.babylonRender.shadowGenerator = shadowGenerator;

    this.rendererStatus.set('Loading model library…');
    await this.babylonModel.init();

    this.camera = new ArcRotateCamera('scene-cam', -Math.PI / 4, Math.PI / 3, 20, Vector3.Zero(), scene);
    this.camera.wheelDeltaPercentage = 0.01;
    this.camera.pinchDeltaPercentage = 0.01;
    this.camera.lowerRadiusLimit = 0.5;
    this.camera.upperRadiusLimit = 5000;
    // Defaults are tuned for small models; a 5km terrain needs a far snappier
    // pan (lower = faster) and the inertia removed so dragging feels direct.
    this.camera.panningSensibility = 20;
    this.camera.panningInertia = 0;
    this.camera.inertia = 0.5;
    // WASD pans the camera target across the ground plane. Arrow keys keep
    // their default of rotating alpha/beta.
    this.camera.keysUp = [];
    this.camera.keysDown = [];
    this.camera.keysLeft = [];
    this.camera.keysRight = [];
    this.attachWasdPan(scene);
    this.camera.attachControl(this.canvasRef.nativeElement, true);
    scene.activeCamera = this.camera;

    this.gizmoManager = new GizmoManager(scene);
    this.gizmoManager.usePointerToAttachGizmos = false;
    this.applyGizmoTool();

    // Pointer-pick: walk up from the picked mesh to find its scene-item root.
    scene.onPointerObservable.add(info => {
      if (info.type !== PointerEventTypes.POINTERPICK) return;
      const picked = info.pickInfo?.pickedMesh;
      if (!picked) {
        this.selectedItemId.set(null);
        this.gizmoManager?.attachToMesh(null);
        return;
      }
      const sceneItemId = this.findOwningItemId(picked);
      if (sceneItemId != null) {
        this.selectedItemId.set(sceneItemId);
        this.attachGizmoToSelection();
      }
    });

    scene.getEngine().runRenderLoop(() => scene.render());
    this.rendererReady = true;
    this.rendererStatus.set('');
  }

  /**
   * Pan the camera target across the ground plane with WASD. Speed scales with
   * radius so it feels consistent whether you're zoomed close or looking at
   * the whole map. Q/E nudges the target up/down for terrain with vertical
   * features.
   */
  private attachWasdPan(scene: Scene): void {
    const held = new Set<string>();
    const canvas = this.canvasRef.nativeElement;
    canvas.tabIndex = 0;
    canvas.addEventListener('keydown', e => {
      const key = e.key.toLowerCase();
      if ('wasdqe'.includes(key)) {
        held.add(key);
        e.preventDefault();
      }
    });
    canvas.addEventListener('keyup', e => held.delete(e.key.toLowerCase()));
    canvas.addEventListener('blur', () => held.clear());
    scene.onBeforeRenderObservable.add(() => {
      if (!this.camera || held.size === 0) return;
      // dt in seconds, capped to avoid jumps when the tab was inactive.
      const dt = Math.min(0.05, scene.getEngine().getDeltaTime() / 1000);
      const speed = Math.max(2, this.camera.radius) * 1.5 * dt;
      // Forward = projection of camera→target onto the ground plane.
      const forward = this.camera.target.subtract(this.camera.position);
      forward.y = 0;
      forward.normalize();
      const right = new Vector3(forward.z, 0, -forward.x);
      const delta = new Vector3(0, 0, 0);
      if (held.has('w')) delta.addInPlace(forward.scale(speed));
      if (held.has('s')) delta.addInPlace(forward.scale(-speed));
      if (held.has('d')) delta.addInPlace(right.scale(speed));
      if (held.has('a')) delta.addInPlace(right.scale(-speed));
      if (held.has('e')) delta.y += speed;
      if (held.has('q')) delta.y -= speed;
      this.camera.target.addInPlace(delta);
    });
  }

  private spawnNode(item: SceneItem): void {
    const lib = this.thumbStorage.items().find(l => l.kind === item.kind && l.id === item.itemTypeId);
    if (!lib || lib.model3DId == null) {
      console.warn('[Studio] no library entry for', item);
      return;
    }
    const renderObject = this.babylonModel.cloneModel3D(lib.model3DId, null, item.diplomacy as Diplomacy);
    const node = renderObject.getModel3D();
    node.position = new Vector3(...item.position);
    node.rotation = new Vector3(0, item.rotationY, 0);
    node.scaling = new Vector3(item.scale, item.scale, item.scale);
    // Tag every descendant mesh so pointer-pick can map back to the item id.
    this.tagNodeTree(node, item.id);
    this.nodes.set(item.id, node);
    this.renderObjects.set(item.id, renderObject);
  }

  private tagNodeTree(node: TransformNode, sceneItemId: number): void {
    (node as any).sceneItemId = sceneItemId;
    for (const child of node.getChildren()) {
      if (child instanceof TransformNode) {
        this.tagNodeTree(child, sceneItemId);
      } else {
        (child as any).sceneItemId = sceneItemId;
      }
    }
  }

  private findOwningItemId(mesh: any): number | null {
    let node: any = mesh;
    while (node) {
      if (typeof node.sceneItemId === 'number') return node.sceneItemId;
      node = node.parent;
    }
    return null;
  }

  private applyToNode(id: number): void {
    const node = this.nodes.get(id);
    const item = this.content()?.items.find(i => i.id === id);
    if (!node || !item) return;
    node.position = new Vector3(...item.position);
    node.rotation = new Vector3(0, item.rotationY, 0);
    node.scaling = new Vector3(item.scale, item.scale, item.scale);
    // Diplomacy means re-cloning since material is baked at clone time —
    // detect and re-spawn if changed.
    const renderObject = this.renderObjects.get(id);
    if (renderObject) {
      const lib = this.thumbStorage.items().find(l => l.kind === item.kind && l.id === item.itemTypeId);
      const wasDiplomacy = (node as any).bakedDiplomacy ?? 'OWN';
      if (lib && lib.model3DId != null && wasDiplomacy !== item.diplomacy) {
        this.disposeItem(id);
        this.spawnNode(item);
        if (this.selectedItemId() === id) this.attachGizmoToSelection();
      } else {
        (node as any).bakedDiplomacy = item.diplomacy;
      }
    }
  }

  private attachGizmoToSelection(): void {
    const id = this.selectedItemId();
    if (id == null || !this.gizmoManager) return;
    const node = this.nodes.get(id);
    if (!node) return;
    this.gizmoManager.attachToMesh(node as unknown as Mesh);
  }

  private applyCamera(content: SceneContent): void {
    if (!this.camera) return;
    const c = content.camera;
    if (c.alpha != null) this.camera.alpha = c.alpha;
    if (c.beta != null) this.camera.beta = c.beta;
    if (c.radius != null) this.camera.radius = c.radius;
    this.camera.target = new Vector3(...c.target);
  }

  private disposeAllItems(): void {
    for (const id of [...this.nodes.keys()]) this.disposeItem(id);
    this.gizmoManager?.attachToMesh(null);
  }

  private disposeItem(id: number): void {
    const ro = this.renderObjects.get(id);
    if (ro) ro.dispose();
    this.nodes.delete(id);
    this.renderObjects.delete(id);
  }
}
