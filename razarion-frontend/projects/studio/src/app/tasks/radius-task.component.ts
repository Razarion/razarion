import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {DecimalPipe} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {
  ArcRotateCamera,
  Color3,
  Color4,
  DirectionalLight,
  Mesh,
  MeshBuilder,
  ShadowGenerator,
  StandardMaterial,
  Tools,
  TransformNode,
  Vector3
} from '@babylonjs/core';
import {
  BaseItemTypeEditorControllerClient,
  BoxItemTypeEditorControllerClient,
  ResourceItemTypeEditorControllerClient,
  TerrainObjectEditorControllerClient
} from '../../../../../src/app/generated/razarion-share';
import {TypescriptGenerator} from '../../../../../src/app/backend/typescript-generator';
import {BabylonModelService} from '../../../../../src/app/game/renderer/babylon-model.service';
import {BabylonRenderServiceAccessImpl} from '../../../../../src/app/game/renderer/babylon-render-service-access-impl.service';
import {RenderObject} from '../../../../../src/app/game/renderer/render-object';
import {Diplomacy} from '../../../../../src/app/gwtangular/GwtAngularFacade';

/** Height of the radius disc above the ground; the model is seated just above it. */
const DISC_Y = 0.01;

type ItemKind = 'base' | 'box' | 'resource' | 'terrainObject';

/** Per-kind wiring: which editor client backs the list/save and where the radius lives
 *  (BaseItemType keeps it under physicalAreaConfig, Box/Resource have it top-level). */
interface KindConfig {
  label: string;
  client: { readAll(): Promise<any[]>; update(config: any): Promise<void> };
  getRadius(item: any): number;
  setRadius(item: any, radius: number): void;
}

/**
 * Radius tuner: pick a base item / box / resource type, see its model with a flat
 * disc whose radius equals the type's configured radius. Drag the slider to resize
 * the disc live, then Save to write the new radius straight back to the DB via the
 * same editor controller clients the production editor uses (admin JWT attached by
 * AuthInterceptor). No new backend endpoint, one source of truth per kind.
 */
@Component({
  selector: 'studio-radius-task',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  template: `
    <div class="radius-task">
      <!-- ============ LEFT: kind switch + type list ============ -->
      <aside class="left">
        <div class="kind-tabs" role="group" aria-label="Item kind">
          <button [class.active]="kind() === 'base'" (click)="selectKind('base')">Base</button>
          <button [class.active]="kind() === 'box'" (click)="selectKind('box')">Box</button>
          <button [class.active]="kind() === 'resource'" (click)="selectKind('resource')">Resource</button>
          <button [class.active]="kind() === 'terrainObject'" (click)="selectKind('terrainObject')">Terrain</button>
        </div>
        <header>
          <h3>{{ kinds[kind()].label }}</h3>
          <button class="ghost sm" (click)="loadAll()" [disabled]="loading()" title="Re-fetch list">↻</button>
        </header>

        @if (!hasToken()) {
          <div class="status warn">Not logged in — use the header form.</div>
        }
        @if (lastError(); as err) {
          <div class="status error">{{ err }}</div>
        }

        <ul class="unit-list">
          @for (it of items(); track it.id) {
            <li [class.active]="selected()?.id === it.id" (click)="select(it)">
              <span class="unit-name">{{ it.internalName || ('#' + it.id) }}</span>
              <span class="unit-radius">r {{ radiusOf(it) | number: '1.2-2' }}</span>
            </li>
          } @empty {
            <li class="empty">{{ loading() ? 'Loading…' : 'None.' }}</li>
          }
        </ul>
      </aside>

      <!-- ============ CENTER: viewport ============ -->
      <section class="viewport">
        @if (selected()) {
          <div class="viewport-header">
            <span class="title">{{ selected()!.internalName || ('#' + selected()!.id) }}</span>
            <div class="spacer"></div>
            <button class="primary" (click)="save()" [disabled]="busy() || !dirty() || !!rendererStatus()">
              {{ busy() ? 'Saving…' : (dirty() ? 'Save' : 'Saved') }}
            </button>
          </div>
        } @else {
          <div class="placeholder"><p>Pick a {{ kind() }} type from the left.</p></div>
        }
        <canvas #viewportCanvas [hidden]="!selected()"></canvas>
        @if (selected() && rendererStatus(); as msg) {
          <div class="loading-overlay"><span>{{ msg }}</span></div>
        }
      </section>

      <!-- ============ RIGHT: radius control ============ -->
      <aside class="right">
        @if (selected()) {
          <header><h3>Radius</h3></header>
          <div class="control">
            <input type="range" min="0.1" max="10" step="0.05"
                   [ngModel]="radius()" (ngModelChange)="onRadiusChange($event)" [disabled]="busy()">
            <div class="value-row">
              <input type="number" min="0" step="0.05"
                     [ngModel]="radius()" (ngModelChange)="onRadiusChange($event)" [disabled]="busy()">
              <span class="unit">m</span>
            </div>
            @if (!selected()!.model3DId) {
              <div class="status warn">No model3DId — disc only.</div>
            }
          </div>
        }
      </aside>
    </div>
  `,
  styles: [`
    :host { display: block; width: 100%; height: 100%; }
    .radius-task { display: grid; grid-template-columns: 240px 1fr 220px; width: 100%; height: 100%; }
    aside { background: #1a1d22; border-right: 1px solid #2a2e35; display: flex; flex-direction: column; overflow: hidden; }
    aside.right { border-right: none; border-left: 1px solid #2a2e35; }
    aside header { display: flex; align-items: center; gap: 8px; padding: 10px 12px; border-bottom: 1px solid #2a2e35; }
    aside h3 { margin: 0; font-size: 12px; text-transform: uppercase; letter-spacing: 0.06em; color: #aaa; }
    .kind-tabs { display: flex; gap: 2px; padding: 8px 8px 0; }
    .kind-tabs button { flex: 1; padding: 5px 0; font-size: 11px; border-radius: 4px 4px 0 0; border: 1px solid #2a2e35; border-bottom: none; background: transparent; color: #aaa; cursor: pointer; }
    .kind-tabs button:hover { background: rgba(74,158,255,0.10); color: #ddd; }
    .kind-tabs button.active { background: rgba(74,158,255,0.22); color: #fff; border-color: rgba(74,158,255,0.5); }
    .unit-list { list-style: none; margin: 0; padding: 4px; overflow-y: auto; flex: 1; }
    .unit-list li { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 6px 8px; border-radius: 4px; cursor: pointer; font-size: 12px; color: #ccc; }
    .unit-list li:hover { background: rgba(74, 158, 255, 0.10); }
    .unit-list li.active { background: rgba(74, 158, 255, 0.22); color: #fff; }
    .unit-list li.empty { color: #777; cursor: default; }
    .unit-radius { color: #7fc7ff; font-variant-numeric: tabular-nums; font-size: 11px; }
    .viewport { position: relative; display: flex; flex-direction: column; background: #0d0f12; }
    .viewport-header { display: flex; align-items: center; gap: 10px; padding: 8px 12px; background: #1a1d22; border-bottom: 1px solid #2a2e35; }
    .viewport-header .title { font-size: 13px; color: #eee; }
    .spacer { flex: 1; }
    canvas { flex: 1; width: 100%; height: 100%; outline: none; display: block; }
    .placeholder { flex: 1; display: flex; align-items: center; justify-content: center; color: #777; }
    .loading-overlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.5); color: #ddd; font-size: 13px; }
    .control { padding: 14px 12px; display: flex; flex-direction: column; gap: 12px; }
    .control input[type=range] { width: 100%; }
    .value-row { display: flex; align-items: center; gap: 6px; }
    .value-row input { width: 90px; padding: 5px 8px; background: rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.18); border-radius: 3px; color: #eee; font-family: inherit; }
    .value-row .unit { color: #888; font-size: 12px; }
    .status { padding: 6px 8px; font-size: 11px; border-radius: 4px; margin: 4px; }
    .status.warn { color: #ffd479; background: rgba(255, 200, 80, 0.08); }
    .status.error { color: #ff8a8a; background: rgba(255, 80, 80, 0.08); }
    button { padding: 5px 12px; border-radius: 4px; border: 1px solid #4a9eff; background: rgba(74,158,255,0.18); color: #eee; cursor: pointer; font-size: 11px; }
    button:hover:not(:disabled) { background: rgba(74,158,255,0.32); }
    button:disabled { opacity: 0.5; cursor: default; }
    button.ghost { background: transparent; border-color: rgba(255,255,255,0.2); color: #aaa; }
    button.sm { padding: 2px 8px; }
  `]
})
export class RadiusTaskComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('viewportCanvas') private canvasRef!: ElementRef<HTMLCanvasElement>;

  private readonly httpClient = inject(HttpClient);
  private readonly babylonModel = inject(BabylonModelService);
  private readonly babylonRender = inject(BabylonRenderServiceAccessImpl);
  private readonly adapter = TypescriptGenerator.generateHttpClientAdapter(this.httpClient);
  private readonly baseClient = new BaseItemTypeEditorControllerClient(this.adapter);
  private readonly boxClient = new BoxItemTypeEditorControllerClient(this.adapter);
  private readonly resourceClient = new ResourceItemTypeEditorControllerClient(this.adapter);
  private readonly terrainObjectClient = new TerrainObjectEditorControllerClient(this.adapter);

  readonly kinds: Record<ItemKind, KindConfig> = {
    base: {
      label: 'Base items', client: this.baseClient,
      getRadius: it => it.physicalAreaConfig.radius,
      setRadius: (it, r) => { it.physicalAreaConfig.radius = r; }
    },
    box: {
      label: 'Boxes', client: this.boxClient,
      getRadius: it => it.radius,
      setRadius: (it, r) => { it.radius = r; }
    },
    resource: {
      label: 'Resources', client: this.resourceClient,
      getRadius: it => it.radius,
      setRadius: (it, r) => { it.radius = r; }
    },
    terrainObject: {
      label: 'Terrain objects', client: this.terrainObjectClient,
      getRadius: it => it.radius,
      setRadius: (it, r) => { it.radius = r; }
    }
  };

  readonly kind = signal<ItemKind>('base');
  readonly items = signal<any[]>([]);
  readonly selected = signal<any | null>(null);
  readonly radius = signal(1);
  readonly loading = signal(false);
  readonly busy = signal(false);
  readonly dirty = signal(false);
  readonly rendererStatus = signal<string | null>('');
  readonly lastError = signal<string | null>(null);

  private rendererReady = false;
  private renderObject: RenderObject | null = null;
  private holder: TransformNode | null = null;
  private disc: Mesh | null = null;
  private camera!: ArcRotateCamera;
  private viewInit = false;
  private pendingSelect: any | null = null;

  private cfg(): KindConfig {
    return this.kinds[this.kind()];
  }

  radiusOf(item: any): number {
    return this.cfg().getRadius(item);
  }

  ngOnInit(): void {
    this.rendererStatus.set(null);
    this.loadAll();
  }

  ngAfterViewInit(): void {
    this.viewInit = true;
    if (this.pendingSelect) {
      const it = this.pendingSelect;
      this.pendingSelect = null;
      this.select(it);
    }
  }

  ngOnDestroy(): void {
    this.renderObject?.dispose();
    this.holder?.dispose();
    this.disc?.dispose();
  }

  hasToken(): boolean {
    return !!localStorage.getItem('app.token');
  }

  selectKind(kind: ItemKind): void {
    if (this.kind() === kind) return;
    this.kind.set(kind);
    this.selected.set(null);
    this.dirty.set(false);
    this.renderObject?.dispose();
    this.renderObject = null;
    this.holder?.dispose();
    this.holder = null;
    this.loadAll();
  }

  async loadAll(): Promise<void> {
    this.loading.set(true);
    this.lastError.set(null);
    try {
      const all = await this.cfg().client.readAll();
      this.items.set([...all].sort((a, b) => a.id - b.id));
    } catch (e: any) {
      this.items.set([]);
      this.lastError.set(this.formatError(e));
    } finally {
      this.loading.set(false);
    }
  }

  async select(item: any): Promise<void> {
    this.selected.set(item);
    this.radius.set(this.cfg().getRadius(item));
    this.dirty.set(false);
    // The canvas is *ngIf-gated on selected(); on the very first pick it doesn't
    // exist until the next change-detection pass, so defer to ngAfterViewInit.
    if (!this.viewInit || !this.canvasRef) {
      this.pendingSelect = item;
      return;
    }
    await this.ensureRenderer();
    this.renderUnit();
  }

  onRadiusChange(value: number): void {
    if (value == null || isNaN(value)) return;
    this.radius.set(value);
    this.dirty.set(true);
    this.applyDiscRadius();
  }

  async save(): Promise<void> {
    const sel = this.selected();
    if (!sel) return;
    this.busy.set(true);
    this.lastError.set(null);
    try {
      this.cfg().setRadius(sel, this.radius());
      await this.cfg().client.update(sel);
      // Reflect the new value in the list without a full reload.
      this.items.update(arr => arr.map(it => it.id === sel.id ? sel : it));
      this.dirty.set(false);
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
    } finally {
      this.busy.set(false);
    }
  }

  // ===== Babylon plumbing =====

  private async ensureRenderer(): Promise<void> {
    if (this.rendererReady) return;
    this.rendererStatus.set('Starting renderer…');
    this.babylonRender.setup(this.canvasRef.nativeElement);
    const scene = this.babylonRender.getScene();
    scene.clearColor = new Color4(0.05, 0.06, 0.08, 1);

    // Production lighting parity (see scene-composer): env IBL from setup() plus a
    // directional light. The terrain-tile impl reads directionalLight/shadowGenerator
    // off the renderer service, so they must exist even though we render no terrain.
    const dirLight = new DirectionalLight('RadiusDirectional', new Vector3(-3, -10, 3), scene);
    dirLight.intensity = 0.8;
    dirLight.diffuse = new Color3(1, 1, 1);
    dirLight.specular = new Color3(1, 1, 1);
    this.babylonRender.directionalLight = dirLight;
    this.babylonRender.shadowGenerator = new ShadowGenerator(2048, dirLight);

    this.rendererStatus.set('Loading model library…');
    await this.babylonModel.init();

    this.camera = new ArcRotateCamera('radius-cam', -Math.PI / 4, Math.PI / 3, 20, Vector3.Zero(), scene);
    this.camera.wheelDeltaPercentage = 0.01;
    this.camera.lowerRadiusLimit = 0.5;
    this.camera.upperRadiusLimit = 500;
    this.camera.attachControl(this.canvasRef.nativeElement, true);
    scene.activeCamera = this.camera;

    scene.getEngine().runRenderLoop(() => scene.render());
    this.rendererReady = true;
    this.rendererStatus.set(null);
  }

  /** (Re)build the model + radius disc for the current selection. */
  private renderUnit(): void {
    const sel = this.selected();
    if (!sel) return;

    this.renderObject?.dispose();
    this.renderObject = null;
    this.holder?.dispose();
    this.holder = null;

    if (sel.model3DId != null) {
      try {
        this.renderObject = this.babylonModel.cloneModel3D(sel.model3DId, null, Diplomacy.OWN);
        this.renderObject.setPosition(Vector3.Zero());
        // Parent the model under a fresh holder and centre by moving the HOLDER. A parent
        // translation cleanly moves the child's world position even when the mesh carries a
        // baked pivot/transform (the Box mesh sits at world ~-62 while its own position is 0,
        // so shifting the mesh position alone doesn't move it onto the disc).
        this.holder = new TransformNode('radius-holder', this.babylonRender.getScene());
        this.renderObject.setParent(this.holder);
        this.centerOnDisc(this.renderObject, this.holder);
      } catch (e: any) {
        this.lastError.set(this.formatError(e));
      }
    }

    if (!this.disc) {
      const scene = this.babylonRender.getScene();
      // Unit disc (radius 1); scaled live by applyDiscRadius(). Flat on the ground,
      // double-sided and unlit so it reads as a clean footprint ring from any angle.
      this.disc = MeshBuilder.CreateDisc('radius-disc', {radius: 1, tessellation: 96}, scene);
      this.disc.rotation.x = Tools.ToRadians(90);
      this.disc.position.y = DISC_Y;
      this.disc.isPickable = false;
      const mat = new StandardMaterial('radius-disc-mat', scene);
      mat.diffuseColor = new Color3(0.2, 0.6, 1);
      mat.emissiveColor = new Color3(0.15, 0.45, 0.85);
      mat.alpha = 0.30;
      mat.backFaceCulling = false;
      mat.disableLighting = true;
      this.disc.material = mat;
    }
    this.applyDiscRadius();

    // Frame the camera to the disc so big and small items both fill the view.
    this.camera.setTarget(Vector3.Zero());
    this.camera.radius = Math.max(6, this.radius() * 5);
  }

  /**
   * Centres the model's footprint on the disc (X/Z) and seats it just above the disc plane (Y) by
   * moving the parent HOLDER, not the model itself. Models can carry a baked pivot/transform where
   * the rendered geometry lives far from the mesh's own position (the Box mesh sits at world ~-62
   * while its position is 0). Shifting the mesh position then fails to move it; translating the
   * holder is a clean world-space move that always works, for single-mesh and multi-mesh models.
   */
  private centerOnDisc(renderObject: RenderObject, holder: TransformNode): void {
    const root = renderObject.getModel3D();
    const candidates = [root, ...root.getChildMeshes(false)];

    holder.position.set(0, 0, 0);
    holder.computeWorldMatrix(true);
    root.computeWorldMatrix(true);

    // Accumulate the world AABB over REAL geometry only. Babylon's getHierarchyBoundingVectors
    // also counts marker/helper meshes that carry zero vertices — their bounding box collapses to
    // a point at the node origin (0,0,0), which pins minY to 0. A rock whose geometry sits above
    // its origin (e.g. "Rock1 grup2", ~0.5m up) then never gets seated and floats. Filtering to
    // getTotalVertices() > 0 — the same test the static-model path uses — yields the true visible
    // footprint, so the rock's actual bottom lands on the disc.
    let minX = Infinity, minY = Infinity, minZ = Infinity;
    let maxX = -Infinity, maxY = -Infinity, maxZ = -Infinity;
    candidates.forEach(n => {
      if (!(n instanceof Mesh) || n.getTotalVertices() === 0) {
        return;
      }
      n.computeWorldMatrix(true);
      n.refreshBoundingInfo({});
      const bb = n.getBoundingInfo().boundingBox;
      minX = Math.min(minX, bb.minimumWorld.x); maxX = Math.max(maxX, bb.maximumWorld.x);
      minY = Math.min(minY, bb.minimumWorld.y); maxY = Math.max(maxY, bb.maximumWorld.y);
      minZ = Math.min(minZ, bb.minimumWorld.z); maxZ = Math.max(maxZ, bb.maximumWorld.z);
    });
    if (!isFinite(minX) || !isFinite(minY)) {
      return;
    }
    holder.position.x = -(minX + maxX) / 2;
    holder.position.z = -(minZ + maxZ) / 2;
    holder.position.y = DISC_Y + 0.01 - minY;
  }

  private applyDiscRadius(): void {
    if (!this.disc) return;
    const r = this.radius();
    // Disc lives in local XY then rotates 90° about X (Y→Z), so scaling X and Y
    // gives a circle of radius r in the world XZ plane.
    this.disc.scaling.x = r;
    this.disc.scaling.y = r;
  }

  private formatError(e: any): string {
    const status = e?.status ?? e?.response?.status;
    const msg = e?.error?.message ?? e?.message ?? 'request failed';
    return status ? `HTTP ${status}: ${msg}` : msg;
  }
}
