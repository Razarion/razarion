import type {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import type {BabylonTerrainTileImpl} from './babylon-terrain-tile.impl';
import {ClumpField} from './clump-field';
import {EarthClumps} from './earth-clumps';
import {GrassClumps} from './grass-clumps';
import {GroundZone} from './ground-zone';
import {initPerm, SEED} from './procedural-textures';
import {Showcase} from './showcase';

/**
 * The 3D grass clumps on the lawn and the dry ones on bare ground (grass-clumps.ts, earth-clumps.ts) in
 * the game, around the screen's ground centre. Where they stand is worked out per 16 m chunk
 * (ClumpField), a few milliseconds per frame, ahead of the camera; showing them only copies the
 * chunks' lists into the meshes, when the camera has moved or new chunks are ready; zooming only
 * changes how many are drawn (the camera fade is in the shader, ClumpFadePlugin). Before the first
 * show the textures, meshes and shaders are made ahead, one kind per frame (warm).
 * Worked out in one go, the island around the camera took 90 - 130 ms: a jolt every time the camera
 * stopped (render telemetry, 2026-11-02).
 */
export class GroundClumps {
  /** Milliseconds of a frame for working out chunks */
  private static readonly BUDGET_MS = 3;
  /** Milliseconds between two looks at the camera, and between two shows */
  private static readonly CHECK_MS = 200;
  /** Chunks worked out this far (m) beyond the radius, ahead of the scrolling camera, and kept this far */
  private static readonly AHEAD = 24;
  private static readonly KEEP = 64;
  /** How far (m) the camera moves before the clumps are shown anew around it */
  private static readonly MOVE = 6;
  readonly grass: GrassClumps;
  readonly earth: EarthClumps;
  private zone: "none" | "loading" | "ready" = "none";
  // The kinds made ahead so far (warm): 0 none, 1 grass, 2 both - one per frame, the atlases are drawn on the CPU
  private warmed = 0;
  private lastCheck = 0;
  private complete = false;
  private shown: { x: number, z: number, chunks: number } | null = null;
  // Since the last takeStats (RenderTelemetry)
  private builds = 0;
  private buildMaxMs = 0;

  constructor(private readonly renderer: BabylonRenderServiceAccessImpl, private readonly tiles: () => Iterable<BabylonTerrainTileImpl>) {
    this.grass = new GrassClumps(renderer);
    this.earth = new EarthClumps(renderer, this.grass);
  }

  /** Every frame: works out chunks while some are missing, looks at the camera every CHECK_MS. */
  update(now: number): void {
    if (!Showcase.isOn("clumps")) {
      if (this.shown) {
        this.clear();
      }
      return;
    }
    if (this.zone !== "ready") {
      // The tile's sand/land decision reads the splatter texture
      if (this.zone === "none") {
        this.zone = "loading";
        GroundZone.load().then(() => {
          initPerm(SEED);
          this.zone = "ready";
        });
      }
      return;
    }
    if (this.warmed < 2) {
      this.warm();
      return;
    }
    this.grass.refade();
    this.earth.refade();
    const check = now - this.lastCheck >= GroundClumps.CHECK_MS;
    if (this.complete && !check) {
      return;
    }
    const centre = this.renderer.setupCenterGroundPosition();
    if (!isFinite(centre.x) || !isFinite(centre.z)) {
      return;
    }
    const start = performance.now();
    const tiles = ClumpField.tilesByIndex(this.readyTiles());
    const grassRadius = this.grass.settings.radius, earthRadius = this.earth.settings.radius;
    const grassDone = this.grass.field.prepare(tiles, centre.x, centre.z, grassRadius + GroundClumps.AHEAD, grassRadius + GroundClumps.KEEP, GroundClumps.BUDGET_MS);
    const earthDone = grassDone && this.earth.field.prepare(tiles, centre.x, centre.z, earthRadius + GroundClumps.AHEAD, earthRadius + GroundClumps.KEEP,
      GroundClumps.BUDGET_MS - (performance.now() - start));
    this.complete = grassDone && earthDone;
    if (!check) {
      return;
    }
    this.lastCheck = now;
    const chunks = this.grass.field.version() + this.earth.field.version(), shown = this.shown;
    if (shown && Math.hypot(centre.x - shown.x, centre.z - shown.z) < GroundClumps.MOVE && chunks === shown.chunks) {
      return;
    }
    const showStart = performance.now();
    this.grass.show(centre.x, centre.z);
    this.earth.show(centre.x, centre.z);
    this.shown = {x: centre.x, z: centre.z, chunks};
    this.builds++;
    this.buildMaxMs = Math.max(this.buildMaxMs, performance.now() - showStart);
  }

  /** How many clumps stand, and the shows since the last call - for RenderTelemetry, once per period. */
  takeStats(): { clumps: number, clumpBuilds: number, clumpBuildMaxMs: number } {
    const stats = {clumps: this.grass.count + this.earth.count, clumpBuilds: this.builds, clumpBuildMaxMs: this.buildMaxMs};
    this.builds = 0;
    this.buildMaxMs = 0;
    return stats;
  }

  dispose(): void {
    this.grass.dispose();
    this.earth.dispose();
    this.shown = null;
    this.warmed = 0;
  }

  /** One kind's texture, meshes and shader per frame; logged once, the phone took 300 ms for its first show. */
  private warm(): void {
    const kind = this.warmed++ === 0 ? this.grass : this.earth, start = performance.now();
    const compiled = kind.warm(), made = performance.now();
    compiled.then(() => console.info(`[GroundClumps] ${kind === this.grass ? "grass" : "earth"} warm: ${Math.round(made - start)} ms on the CPU, shader ready after ${Math.round(performance.now() - start)} ms`));
  }

  private clear(): void {
    this.grass.hide();
    this.earth.hide();
    this.shown = null;
  }

  /** The loaded tiles that have their heights and the masks the clumps read (vegetation, paths). */
  private readyTiles(): BabylonTerrainTileImpl[] {
    const ready: BabylonTerrainTileImpl[] = [];
    for (const tile of this.tiles()) {
      const t = tile as any;
      if (t.groundHeights?.length && t.vegetationMask) {
        ready.push(tile);
      }
    }
    return ready;
  }
}
