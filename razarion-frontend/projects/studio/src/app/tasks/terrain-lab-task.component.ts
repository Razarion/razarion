import {AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {DecimalPipe} from '@angular/common';
import {Vector3} from '@babylonjs/core/Maths/math.vector';
import {BabylonRenderServiceAccessImpl} from '../../../../../src/app/game/renderer/babylon-render-service-access-impl.service';
import {BabylonModelService} from '../../../../../src/app/game/renderer/babylon-model.service';
import {SHOWCASE_FEATURES, Showcase, ShowcaseFeature} from '../../../../../src/app/game/renderer/showcase';
import {TerrainLoaderService} from './terrain-loader.service';
import {LabTuning, TuningValue} from './lab-tuning';
import {GrassSettings} from '../../../../../src/app/game/renderer/grass-clumps';
import {EarthSettings} from '../../../../../src/app/game/renderer/earth-clumps';

/** A spot to look at: game coordinates (x, y = the map's y, Babylon's z). */
interface LabView {
  name: string;
  x: number;
  y: number;
}

/**
 * The terrain lab: the game's own renderer - tiles, materials, sun, shadows, sky light, tone mapping,
 * plants, water - over a piece of the real planet, without the game engine. What looks right here
 * looks the same in the game, so terrain ideas are tried here first.
 *
 * window.razLab is the handle for scripts (razarion-social recordings): views, wire model, showcase
 * switches. window.razShowcase is installed as in the director client.
 */
@Component({
  selector: 'studio-terrain-lab-task',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  template: `
    <div class="layout">
      <canvas #canvas class="view"></canvas>
      <aside class="panel">
        <section>
          <h3>Terrain Lab</h3>
          <div class="status">{{ status() }}</div>
          <div class="row">
            <label>Planet <input type="number" [(ngModel)]="planetId" class="num"></label>
          </div>
          <div class="row">
            <label>Tiles x <input type="number" [(ngModel)]="tilesX" class="num"></label>
            <label>y <input type="number" [(ngModel)]="tilesY" class="num"></label>
          </div>
          <button (click)="reload()" [disabled]="busy()">Reload tiles</button>
        </section>

        <section>
          <h4>Views</h4>
          @for (view of views; track view.name) {
            <button class="view-btn" (click)="goTo(view)">{{ view.name }}</button>
          }
          <div class="where">Camera: {{ where() }}</div>
        </section>

        <section>
          <h4>Display</h4>
          <label class="check"><input type="checkbox" [ngModel]="wireframe()" (ngModelChange)="setWireframe($event)"> Wireframe</label>
          <label class="check"><input type="checkbox" [ngModel]="shadows()" (ngModelChange)="setShadows($event)"> Shadows</label>
          <div class="row slider">
            <span>Sun azimuth</span>
            <input type="range" min="0" max="360" step="1" [ngModel]="sunAzimuth()" (ngModelChange)="setSun($event, sunElevation())">
            <span class="val">{{ sunAzimuth() }}°</span>
          </div>
          <div class="row slider">
            <span>Sun elevation</span>
            <input type="range" min="5" max="85" step="1" [ngModel]="sunElevation()" (ngModelChange)="setSun(sunAzimuth(), $event)">
            <span class="val">{{ sunElevation() }}°</span>
          </div>
        </section>

        <section>
          <h4>Grass</h4>
          <label class="check"><input type="checkbox" [ngModel]="grassOn()" (ngModelChange)="setGrass('on', $event)"> 3D grass clumps</label>
          @for (slider of grassSliders; track slider.key) {
            <div class="row slider">
              <span>{{ slider.label }}</span>
              <input type="range" [min]="slider.min" [max]="slider.max" [step]="slider.step" [ngModel]="grassValue(slider.key)" (ngModelChange)="setGrass(slider.key, $event)">
              <span class="val">{{ grassValue(slider.key) | number:'1.0-2' }}</span>
            </div>
          }
          <label class="check"><input type="checkbox" [ngModel]="grassShadows()" (ngModelChange)="setGrassShadows($event)"> Clumps cast shadows</label>
          <div class="row">
            <button (click)="buildGrass()">Grass here</button>
            <span>{{ grassCount() | number:'1.0-0' }} clumps</span>
          </div>
        </section>

        <section>
          <h4>Bare ground</h4>
          <label class="check"><input type="checkbox" [ngModel]="earthOn()" (ngModelChange)="setEarth('on', $event)"> Stubble, dry grass, weeds</label>
          @for (slider of earthSliders; track slider.key) {
            <div class="row slider">
              <span>{{ slider.label }}</span>
              <input type="range" [min]="slider.min" [max]="slider.max" [step]="slider.step" [ngModel]="earthValue(slider.key)" (ngModelChange)="setEarth(slider.key, $event)">
              <span class="val">{{ earthValue(slider.key) | number:'1.0-2' }}</span>
            </div>
          }
          <label class="check"><input type="checkbox" [ngModel]="earthShadows()" (ngModelChange)="setEarthShadows($event)"> Clumps cast shadows</label>
          <div class="row"><span>{{ earthCount() | number:'1.0-0' }} clumps</span></div>
        </section>

        <section>
          <h4>Light</h4>
          @for (value of lightValues(); track value.name) {
            <div class="row slider">
              <span [class.changed]="value.value !== value.original">{{ value.name }}</span>
              <input type="range" [min]="value.min" [max]="value.max" [step]="value.step" [ngModel]="value.value" (ngModelChange)="setLight(value, $event)">
              <span class="val">{{ value.value | number:'1.0-2' }}</span>
            </div>
          }
        </section>

        <section>
          <h4>Material</h4>
          <div class="row">
            <select [ngModel]="material()" (ngModelChange)="selectMaterial($event)" class="grow">
              <option value="">– select –</option>
              @for (entry of materialNames(); track entry.name) {
                <option [value]="entry.name">{{ entry.name }} ({{ entry.count }})</option>
              }
            </select>
            <button (click)="readMaterials()" title="Read materials again">↻</button>
          </div>
          @if (material()) {
            <div class="row">
              <input placeholder="Filter inputs" [(ngModel)]="inputFilter" class="grow">
              <button (click)="resetMaterial()">Reset</button>
            </div>
            @for (value of materialInputs(); track value.name) {
              @if (!inputFilter || value.name.toLowerCase().includes(inputFilter.toLowerCase())) {
                <div class="input-row">
                  <div class="input-name" [class.changed]="value.value !== value.original" [title]="value.name">{{ value.name }}</div>
                  <div class="row">
                    <input type="range" [min]="value.min" [max]="value.max" [step]="value.step" [ngModel]="value.value" (ngModelChange)="setInput(value, $event)" class="grow">
                    <input type="number" [step]="value.step" [ngModel]="value.value" (ngModelChange)="setInput(value, $event)" class="num">
                  </div>
                </div>
              }
            }
          }
          <button (click)="exportValues()">Export</button>
          @if (exportText()) {
            <textarea readonly class="export" rows="6">{{ exportText() }}</textarea>
          }
        </section>

        <section>
          <h4>Showcase</h4>
          @for (feature of features; track feature) {
            <label class="check"><input type="checkbox" [ngModel]="featureOn(feature)" (ngModelChange)="setFeature(feature, $event)"> {{ feature }}</label>
          }
        </section>

        <section>
          <h4>Performance</h4>
          <div>{{ fps() | number:'1.0-0' }} fps · {{ frameMs() | number:'1.1-1' }} ms</div>
          <div>{{ meshes() }} Meshes · {{ vertices() | number:'1.0-0' }} Vertices</div>
        </section>
      </aside>
    </div>
  `,
  styles: [`
    :host { display: block; width: 100%; height: 100%; }
    .layout { display: flex; width: 100%; height: 100%; }
    .view { flex: 1; min-width: 0; height: 100%; display: block; outline: none; }
    .panel { width: 280px; flex-shrink: 0; overflow-y: auto; padding: 10px 12px; background: #1a1d22;
      border-left: 1px solid #2a2e35; font-size: 12px; }
    section { margin-bottom: 14px; }
    h3, h4 { margin: 0 0 6px; color: #aaa; font-weight: 600; }
    h3 { font-size: 13px; text-transform: uppercase; letter-spacing: 0.08em; }
    .status { color: #8a8; margin-bottom: 6px; min-height: 16px; }
    .row { display: flex; gap: 8px; align-items: center; margin-bottom: 4px; }
    .num { width: 56px; }
    .slider span:first-child { width: 92px; }
    .slider input { flex: 1; }
    .val { width: 34px; text-align: right; }
    .check { display: block; margin: 2px 0; }
    .view-btn { margin: 0 4px 4px 0; }
    .where { color: #888; margin-top: 4px; }
    .grow { flex: 1; min-width: 0; }
    .input-row { margin: 4px 0 6px; }
    .input-name { color: #bbb; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .changed { color: #e8b04a; }
    .export { width: 100%; box-sizing: border-box; margin-top: 6px; font-family: monospace; font-size: 11px;
      background: #111; color: #ddd; border: 1px solid #333; }
  `]
})
export class TerrainLabTaskComponent implements AfterViewInit, OnDestroy {
  @ViewChild('canvas') private canvasRef!: ElementRef<HTMLCanvasElement>;

  private readonly babylonRender = inject(BabylonRenderServiceAccessImpl);
  private readonly babylonModel = inject(BabylonModelService);
  private readonly terrainLoader = inject(TerrainLoaderService);

  // The noob island (planet 117, docs/game-design/progression.md) and a little around it
  planetId = 117;
  tilesX = 7;
  tilesY = 6;

  readonly views: LabView[] = [
    {name: 'Beach', x: 66, y: 275},
    {name: 'Building site', x: 51, y: 173},
    {name: 'Ridge', x: 100, y: 805},
    {name: 'Lake', x: 752, y: 565},
    {name: 'Pond', x: 690, y: 562},
  ];
  readonly features: ShowcaseFeature[] = SHOWCASE_FEATURES;

  readonly status = signal('');
  readonly busy = signal(false);
  readonly where = signal('');
  readonly wireframe = signal(false);
  readonly shadows = signal(true);
  // The game's sun: direction (-1, -0.85, 0.3) (BabylonRenderServiceAccessImpl.internalSetup)
  readonly sunAzimuth = signal(Math.round(this.toDegrees(Math.atan2(0.3, -1))));
  readonly sunElevation = signal(Math.round(this.toDegrees(Math.atan2(0.85, Math.hypot(1, 0.3)))));
  readonly fps = signal(0);
  readonly frameMs = signal(0);
  readonly meshes = signal(0);
  readonly vertices = signal(0);
  private readonly featureState = signal<Set<ShowcaseFeature>>(new Set(SHOWCASE_FEATURES));
  private readonly tuning = new LabTuning(this.babylonRender);
  // The game's own clumps (the renderer rebuilds them as the camera moves): the sliders change what the game shows
  private readonly grass = this.babylonRender.groundClumps.grass;
  readonly grassSliders: { key: keyof GrassSettings, label: string, min: number, max: number, step: number }[] = [
    {key: 'density', label: 'Clumps/m²', min: 0, max: 30, step: 0.5},
    {key: 'height', label: 'Blade height', min: 0.05, max: 2, step: 0.01},
    {key: 'width', label: 'Clump width', min: 0.1, max: 2, step: 0.01},
    {key: 'radius', label: 'Radius', min: 10, max: 200, step: 5},
    {key: 'tone', label: 'Brightness', min: 0.5, max: 2, step: 0.05},
    {key: 'dry', label: 'Olive', min: 0, max: 1.5, step: 0.05},
    {key: 'patches', label: 'Patches', min: 0, max: 1, step: 0.05},
    {key: 'splay', label: 'Lean out', min: 0, max: 1.5, step: 0.05},
    {key: 'fadeNear', label: 'Fade from (m)', min: 0, max: 150, step: 5},
    {key: 'fadeFar', label: 'Fade to (m)', min: 0, max: 300, step: 5},
  ];
  readonly grassOn = signal(this.grass.settings.on);
  readonly grassShadows = signal(this.grass.settings.castShadows);
  readonly grassCount = signal(0);
  private readonly grassVersion = signal(0);
  private grassTimer: ReturnType<typeof setTimeout> | null = null;
  private readonly earth = this.babylonRender.groundClumps.earth;
  readonly earthSliders: { key: keyof EarthSettings, label: string, min: number, max: number, step: number }[] = [
    {key: 'density', label: 'Clumps/m²', min: 0, max: 15, step: 0.5},
    {key: 'height', label: 'Height', min: 0.05, max: 0.6, step: 0.01},
    {key: 'width', label: 'Width', min: 0.1, max: 1.2, step: 0.01},
    {key: 'splay', label: 'Lean out', min: 0, max: 1.5, step: 0.05},
    {key: 'tone', label: 'Brightness', min: 0.5, max: 3, step: 0.05},
  ];
  readonly earthOn = signal(this.earth.settings.on);
  readonly earthCount = signal(0);
  readonly earthShadows = signal(this.earth.settings.castShadows);
  private readonly earthVersion = signal(0);
  readonly materialNames = signal<{ name: string, count: number }[]>([]);
  readonly material = signal('');
  readonly materialInputs = signal<TuningValue[]>([]);
  readonly lightValues = signal<TuningValue[]>([]);
  readonly exportText = signal('');
  inputFilter = '';
  private statsTimer: ReturnType<typeof setInterval> | null = null;

  async ngAfterViewInit(): Promise<void> {
    try {
      this.status.set('Starting renderer…');
      this.babylonRender.setup(this.canvasRef.nativeElement);
      // Sun, sky light, shadow generator, tone mapping, camera and render loop - as in the game
      this.babylonRender.runRenderer();
      Showcase.install(this.babylonRender);
      Showcase.keepShadows = true;
      this.status.set('Loading models…');
      await this.babylonModel.init();
      this.installApi();
      await this.waitForLogin();
      await this.reload();
      this.goTo(this.views[0]);
      this.statsTimer = setInterval(() => this.updateStats(), 500);
    } catch (e: any) {
      console.error('[TerrainLab]', e);
      this.status.set(`Error: ${e?.message ?? e}`);
    }
  }

  ngOnDestroy(): void {
    if (this.statsTimer) {
      clearInterval(this.statsTimer);
    }
    if (this.grassTimer) {
      clearTimeout(this.grassTimer);
    }
    this.babylonRender.groundClumps.dispose();
    this.terrainLoader.disposeTerrain();
    delete (window as any).razLab;
  }

  /** The terrain comes from the editor REST endpoints, which want the studio's login (header). */
  private async waitForLogin(): Promise<void> {
    while (!localStorage.getItem('app.token')) {
      this.status.set('Please log in (top right)');
      await new Promise(resolve => setTimeout(resolve, 500));
    }
  }

  async reload(): Promise<void> {
    this.busy.set(true);
    try {
      const size = 160;
      const region = {x: 0, y: 0, w: this.tilesX * size - 1, h: this.tilesY * size - 1};
      this.status.set(`Loading ${this.tilesX * this.tilesY} tiles…`);
      const start = performance.now();
      await this.terrainLoader.loadTerrain(this.planetId, region);
      await this.terrainLoader.whenTerrainReady();
      this.status.set(`${this.tilesX * this.tilesY} tiles in ${((performance.now() - start) / 1000).toFixed(1)} s`);
      // The tiles' materials are new: the lab's values go over them again
      this.tuning.reapply();
      this.readMaterials();
      this.lightValues.set(this.tuning.lightValues());
      await this.buildGrass();
    } finally {
      this.busy.set(false);
    }
  }

  goTo(view: LabView): void {
    this.babylonRender.setViewFieldCenter(view.x, view.y);
    this.buildGrass(view.x, view.y);
  }

  /** Grass and the bare ground's detail around (x, y), the camera's ground point by default. */
  async buildGrass(x?: number, y?: number): Promise<number> {
    if (x === undefined || y === undefined) {
      const target = this.babylonRender.setupCenterGroundPosition();
      x = target.x;
      y = target.z;
    }
    const count = await this.grass.build(this.terrainLoader.tiles(), x, y);
    this.grassCount.set(count);
    this.earthCount.set(await this.earth.build(this.terrainLoader.tiles(), x, y));
    return count;
  }

  setEarthShadows(on: boolean): void {
    this.earthShadows.set(on);
    this.earth.setCastShadows(on);
  }

  earthValue(key: keyof EarthSettings): number {
    this.earthVersion();
    return this.earth.settings[key] as number;
  }

  setEarth(key: keyof EarthSettings, value: number | boolean): void {
    (this.earth.settings as any)[key] = typeof value === 'boolean' ? value : +value;
    this.earthOn.set(this.earth.settings.on);
    this.earthVersion.update(v => v + 1);
    if (key === 'tone') {
      this.earth.applyTint();
      return;
    }
    if (this.grassTimer) {
      clearTimeout(this.grassTimer);
    }
    this.grassTimer = setTimeout(() => this.buildGrass(), 150);
  }

  grassValue(key: keyof GrassSettings): number {
    this.grassVersion();
    return this.grass.settings[key] as number;
  }

  setGrass(key: keyof GrassSettings, value: number | boolean): void {
    (this.grass.settings as any)[key] = typeof value === 'boolean' ? value : +value;
    this.grassOn.set(this.grass.settings.on);
    this.grassVersion.update(v => v + 1);
    if (key === 'tone' || key === 'dry') {
      this.grass.applyTint();
      return;
    }
    // Sliders fire on every step: build once they rest
    if (this.grassTimer) {
      clearTimeout(this.grassTimer);
    }
    this.grassTimer = setTimeout(() => this.buildGrass(), 150);
  }

  setGrassShadows(on: boolean): void {
    this.grassShadows.set(on);
    this.grass.setCastShadows(on);
  }

  setWireframe(on: boolean): void {
    this.wireframe.set(on);
    this.babylonRender.getScene().forceWireframe = on;
  }

  setShadows(on: boolean): void {
    this.shadows.set(on);
    this.babylonRender.directionalLight.shadowEnabled = on;
  }

  /** Azimuth: degrees in the ground plane from +x towards +z; elevation: degrees over the horizon. */
  setSun(azimuth: number, elevation: number): void {
    this.sunAzimuth.set(+azimuth);
    this.sunElevation.set(+elevation);
    const a = this.toRadians(+azimuth), e = this.toRadians(+elevation);
    // The light shines downwards, towards the sun's opposite side
    this.babylonRender.directionalLight.direction = new Vector3(Math.cos(a) * Math.cos(e), -Math.sin(e), Math.sin(a) * Math.cos(e));
  }

  readMaterials(): void {
    this.materialNames.set(this.tuning.materialNames());
    if (this.material()) {
      this.materialInputs.set(this.tuning.inputs(this.material()));
    }
  }

  selectMaterial(name: string): void {
    this.material.set(name);
    this.materialInputs.set(name ? this.tuning.inputs(name) : []);
  }

  setInput(value: TuningValue, newValue: number): void {
    if (newValue === null || isNaN(+newValue)) {
      return;
    }
    this.tuning.setInput(this.material(), value.name, +newValue);
    this.materialInputs.update(list => list.map(v => v.name === value.name ? {...v, value: +newValue} : v));
  }

  resetMaterial(): void {
    this.tuning.resetMaterial(this.material());
    this.materialInputs.set(this.tuning.inputs(this.material()));
  }

  setLight(value: TuningValue, newValue: number): void {
    this.tuning.setLight(value.name, +newValue);
    this.lightValues.update(list => list.map(v => v.name === value.name ? {...v, value: +newValue} : v));
  }

  exportValues(): void {
    this.exportText.set(this.tuning.export());
  }

  featureOn(feature: ShowcaseFeature): boolean {
    return this.featureState().has(feature);
  }

  setFeature(feature: ShowcaseFeature, on: boolean): void {
    Showcase.set(feature, on);
    const next = new Set(this.featureState());
    if (on) {
      next.add(feature);
    } else {
      next.delete(feature);
    }
    this.featureState.set(next);
  }

  private updateStats(): void {
    const engine = this.babylonRender.getEngine();
    const scene = this.babylonRender.getScene();
    const fps = engine.getFps();
    this.fps.set(fps);
    this.frameMs.set(fps > 0 ? 1000 / fps : 0);
    const active = scene.getActiveMeshes();
    let vertices = 0;
    for (let i = 0; i < active.length; i++) {
      vertices += active.data[i].getTotalVertices();
    }
    this.meshes.set(active.length);
    this.vertices.set(vertices);
    const target = this.babylonRender.setupCenterGroundPosition();
    this.where.set(`x ${Math.round(target.x)}, y ${Math.round(target.z)}`);
  }

  /** The handle for scripts: the same switches as the panel. */
  private installApi(): void {
    (window as any).razLab = {
      scene: () => this.babylonRender.getScene(),
      views: this.views,
      goTo: (x: number, y: number) => {
        this.babylonRender.directorActive = false;
        this.babylonRender.setViewFieldCenter(x, y);
      },
      /**
       * The game's camera put where the director's orbit keys put it, so a still from the game and one from the lab show the same picture. The game
       * camera stays (its tone mapping hangs on it); the lab just stops steering it. goTo() hands it back.
       */
      orbit: (x: number, height: number, y: number, alpha: number, beta: number, radius: number) => {
        const camera = this.babylonRender.getCamera();
        this.babylonRender.directorActive = true;
        this.babylonRender.directorCameraTick = () => undefined;
        // As the director's orbitPose (director.service.ts): beta is the height over the horizon
        camera.position = new Vector3(x + radius * Math.cos(beta) * Math.sin(alpha), height + radius * Math.sin(beta),
          y + radius * Math.cos(beta) * Math.cos(alpha));
        camera.setTarget(new Vector3(x, height, y));
      },
      wireframe: (on: boolean) => this.setWireframe(on),
      shadows: (on: boolean) => this.setShadows(on),
      sun: (azimuth: number, elevation: number) => this.setSun(azimuth, elevation),
      feature: (feature: ShowcaseFeature, on: boolean) => this.setFeature(feature, on),
      /** A material input by name, on every material of that name (as the panel's sliders). */
      input: (material: string, input: string, value: number) => this.tuning.setInput(material, input, value),
      light: (name: string, value: number) => this.tuning.setLight(name, value),
      export: () => this.tuning.export(),
      /** Grass settings (on, density, height, width, radius, tone, dry, patches, splay, fadeNear, fadeFar, castShadows), then grass around (x, y) or the camera. */
      grass: (settings: Partial<GrassSettings> = {}, x?: number, y?: number) => {
        Object.assign(this.grass.settings, settings);
        this.grassOn.set(this.grass.settings.on);
        this.setGrassShadows(this.grass.settings.castShadows);
        this.grass.applyTint();
        this.grassVersion.update(v => v + 1);
        return this.buildGrass(x, y);
      },
      /** Bare ground settings (on, density, height, width, radius, splay, tone, castShadows), then grass and detail around (x, y) or the camera. */
      earth: (settings: Partial<EarthSettings> = {}, x?: number, y?: number) => {
        Object.assign(this.earth.settings, settings);
        this.earth.applyTint();
        this.earthShadows.set(this.earth.settings.castShadows);
        this.earthOn.set(this.earth.settings.on);
        this.earthVersion.update(v => v + 1);
        return this.buildGrass(x, y).then(() => this.earth.count);
      },
      /** Spots of all bare ground (earth, stones) near (x, y), nearest first. */
      bareSpots: (x: number, y: number, radius = 40) => this.earth.bareSpots(this.terrainLoader.tiles(), x, y, radius),
      reload: () => this.reload(),
      ready: () => this.terrainLoader.whenTerrainReady(),
    };
  }

  private toDegrees(radians: number): number {
    return radians * 180 / Math.PI;
  }

  private toRadians(degrees: number): number {
    return degrees * Math.PI / 180;
  }
}
