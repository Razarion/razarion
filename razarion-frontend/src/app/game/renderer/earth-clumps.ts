import {RawTexture} from '@babylonjs/core/Materials/Textures/rawTexture';
import {PBRMaterial} from '@babylonjs/core/Materials/PBR/pbrMaterial';
import {Color3} from '@babylonjs/core/Maths/math.color';
import {Matrix, Quaternion, Vector3} from '@babylonjs/core/Maths/math.vector';
import {Mesh} from '@babylonjs/core/Meshes/mesh';
import '@babylonjs/core/Meshes/thinInstanceMesh';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {BabylonTerrainTileImpl} from './babylon-terrain-tile.impl';
import {GroundZone} from './ground-zone';
import {initPerm, scatterNoise, SEED} from './procedural-textures';
import {cardAtlasMaterial, drawBlade, fillClumpMeshes, GrassClumps, keepClumps, warmClumpMeshes} from './grass-clumps';
import {ClumpFadePlugin} from './clump-fade-plugin';
import {ClumpCloudPlugin} from './clump-cloud-plugin';
import {AddClump, CLUMP_RADIUS, ClumpField} from './clump-field';

/** Bare ground clump settings - the terrain lab's sliders. */
export interface EarthSettings {
  on: boolean;
  /** Clumps per m² of bare ground, most at its edge to the lawn */
  density: number;
  /** Clump height (m) */
  height: number;
  /** Clump width (m) */
  width: number;
  /** Radius (m) around the camera's ground point */
  radius: number;
  /** How far the clump's cards lean out (GrassClumps): flat fans show a bald middle from above */
  splay: number;
  /** Brightness */
  tone: number;
  castShadows: boolean;
}

// The tile's TerrainZone.UNDER: earth, stony ground and the gravel at the foot of rock
const ZONE_UNDER = 1;
// bare() on a path through the lawn
const PATH = 0.4;
// sRGB. The feet in the earth's own browns, so a clump grows out of the bare ground as a grass clump out
// of the lawn; the tips dry straw, withered grey-brown or a weed's olive
const EARTH = [[124, 102, 78], [112, 92, 70], [134, 112, 86]];
const STRAW = [[150, 130, 88], [136, 118, 80], [158, 140, 98]];
const WITHERED = [[132, 118, 96], [118, 106, 86]];
const WEED = [[128, 136, 64], [112, 124, 56], [140, 140, 74]];
// The atlas cells (grass-clumps.ts, 2 x 2): stubble, withered stalks, a low weed, dry grass
const STUBBLE = 0, STALKS = 1, WEEDS = 2, DRY_GRASS = 3;
const VARIANTS = 4;

/**
 * Detail for the bare earth patches and paths, made as the lawn's grass clumps (GrassClumps): fanned cards
 * whose foot is the earth's colour - dry stubble, withered stalks, a low weed, dry grass - instead of 3D
 * stones and clods, which read as confetti from the game's camera. Most along the earth's frayed edge
 * to the lawn, sparse in its middle and on the paths, in clusters. Thin instances, only around the camera.
 */
export class EarthClumps {
  readonly settings: EarthSettings = {on: true, density: 8, height: 0.2, width: 0.5, radius: CLUMP_RADIUS, splay: 0.6, tone: 3.4, castShadows: false};
  private texture: RawTexture | null = null;
  private material: PBRMaterial | null = null;
  private meshes: (Mesh | undefined)[] = [];
  private shown: { x: number, z: number, ends: Int32Array[] } | null = null;
  private keep = -1;
  count = 0;

  constructor(private readonly babylonRender: BabylonRenderServiceAccessImpl, private readonly grass: GrassClumps) {
  }

  /** Where the clumps stand, per chunk; GroundClumps works it out a few chunks per frame. */
  readonly field = new ClumpField(VARIANTS, 11, () => this.settings.density, (tile, x, z, rnd, add) => this.place(tile, x, z, rnd, add));

  /** Detail anew around (centerX, centerY) on the given tiles, all at once - after a setting has changed (the lab). */
  async build(tiles: Iterable<BabylonTerrainTileImpl>, centerX: number, centerY: number): Promise<number> {
    await GroundZone.load();
    initPerm(SEED);
    this.disposeMeshes();
    this.field.reset();
    this.field.prepare(ClumpField.tilesByIndex(tiles), centerX, centerY, this.settings.radius, this.settings.radius, Infinity);
    return this.show(centerX, centerY);
  }

  /** As GrassClumps.show, with the grass's camera fade. */
  show(centerX: number, centerY: number): number {
    const s = this.settings;
    const clumps = this.field.assemble(centerX, centerY, s.on ? s.radius : 0);
    fillClumpMeshes(this.meshes, clumps, v => this.mesh(v));
    this.shown = {x: centerX, z: centerY, ends: clumps.ends};
    this.refade(true);
    this.applyShadows();
    return this.count;
  }

  /** As GrassClumps.refade. */
  refade(force = false): void {
    if (!this.shown) {
      return;
    }
    const keep = this.grass.nearestKeep(this.shown.x, this.shown.z, this.settings.radius);
    if (force || keep !== this.keep) {
      this.keep = keep;
      this.count = keepClumps(this.meshes, this.shown.ends, keep);
    }
  }

  hide(): void {
    this.meshes.forEach(mesh => mesh?.setEnabled(false));
    this.shown = null;
    this.count = 0;
  }

  /** As GrassClumps.warm. */
  warm(): Promise<void> {
    return warmClumpMeshes(this.meshes, VARIANTS, v => this.mesh(v));
  }

  /** A clump at (x, z), if the ground is bare there: its size, turn, motif and colour. */
  private place(tile: any, x: number, z: number, rnd: () => number, add: AddClump): void {
    const s = this.settings;
    // Full along the frayed edge (bare between 0 and 1), half in the middle, less on the paths; in
    // clusters of a few metres
    const bare = this.bare(tile, x, z);
    const share = bare <= 0 ? 0 : bare === PATH ? 0.3 : bare < 1 ? 1 : 0.5;
    if (rnd() >= share * smoothstep(-0.45, 0.2, scatterNoise(x / 6 + 3.3, z / 6 + 8.1))) {
      return;
    }
    // One in ten a small shrub, twice the size: no even carpet
    const big = rnd() < 0.1 ? 2 : 1;
    const w = s.width * big * (0.6 + 0.6 * rnd()), h = s.height * big * (0.5 + rnd());
    const q = Quaternion.RotationYawPitchRoll(rnd() * Math.PI * 2, (rnd() - 0.5) * 0.35, (rnd() - 0.5) * 0.35);
    const m = Matrix.Compose(new Vector3(rnd() < 0.5 ? -w : w, h, w), q, new Vector3(x, tile.getHeightSmooth(x, z) - 0.02, z));
    // Mostly stubble and dry grass, now and then withered stalks or a weed; at the lawn's edge half
    // weeds, so the lawn runs out into the earth instead of ending
    const edge = bare > 0 && bare < 0.5, pick = rnd();
    const variant = edge && pick < 0.5 ? WEEDS : pick < 0.3 ? STUBBLE : pick < 0.6 ? DRY_GRASS : pick < 0.75 ? STALKS : WEEDS;
    // Greener towards the lawn: the dry blades take a little of its green at the edge
    const k = 0.9 + 0.2 * rnd(), green = bare > 0 && bare < 1 ? 1 - bare : 0;
    add(variant, m, k * (1 - 0.15 * green), k, k * (1 - 0.35 * green));
  }

  /** One motif per clump */
  private mesh(variant: number): Mesh {
    return this.grass.clump(variant, `Earth clumps ${variant}`, this.earthMaterial(), this.settings.splay, true);
  }

  /** Up to `max` spots (and their ground height) of all bare ground within `radius` around (centerX, centerY), nearest first - where to look. */
  bareSpots(tiles: BabylonTerrainTileImpl[], centerX: number, centerY: number, radius: number, max = 10): { x: number, y: number, h: number }[] {
    const spots: { x: number, y: number, h: number, d: number }[] = [];
    for (const tile of tiles as any[]) {
      if (!tile.groundHeights?.length) {
        continue;
      }
      for (let z = Math.max(tile.tileYOffset, centerY - radius); z < Math.min(tile.tileYOffset + BabylonTerrainTileImpl.NODE_Y_COUNT, centerY + radius); z += 2) {
        for (let x = Math.max(tile.tileXOffset, centerX - radius); x < Math.min(tile.tileXOffset + BabylonTerrainTileImpl.NODE_X_COUNT, centerX + radius); x += 2) {
          if (this.bare(tile, x + 0.5, z + 0.5) >= 1 && this.bare(tile, x + 2.5, z + 0.5) >= 1 && this.bare(tile, x + 0.5, z + 2.5) >= 1) {
            spots.push({x: x + 1, y: z + 1, h: tile.getHeightAt(x + 1, z + 1), d: Math.hypot(x - centerX, z - centerY)});
          }
        }
      }
    }
    return spots.sort((a, b) => a.d - b.d).slice(0, max).map(({x, y, h}) => ({x, y, h}));
  }

  applyTint(): void {
    const tone = this.settings.tone;
    this.material?.albedoColor.set(tone, tone, tone);
  }

  setCastShadows(on: boolean): void {
    this.settings.castShadows = on;
    this.applyShadows();
  }

  dispose(): void {
    this.disposeMeshes();
    this.material?.dispose();
    this.texture?.dispose();
    this.material = null;
    this.texture = null;
  }

  /**
   * How much the spot is bare ground, 0 = lawn (or decal, bot ground, sand, water), 1 = all earth or
   * stones, PATH on a path through the lawn. In the earth's frayed edge the tile's zone decision is a
   * chance, as often earth as the shader draws it there: asked at three chances, it says how far into
   * the edge the spot is - the same answer in every build.
   */
  private bare(tile: any, x: number, z: number): number {
    if (tile.insideAnyDecal(x, z) || tile.insideBotGround(x, z)) {
      return 0;
    }
    const cell = Math.floor(z - tile.tileYOffset) * BabylonTerrainTileImpl.NODE_X_COUNT + Math.floor(x - tile.tileXOffset);
    const growth = tile.vegetationMask ? tile.vegetationMask[cell] : 0, height = tile.getHeightAt(x, z);
    let under = 0;
    for (const chance of [0.17, 0.5, 0.83]) {
      if (tile.getTerrainZone(x, z, height, growth, chance) === ZONE_UNDER) {
        under++;
      }
    }
    if (under > 0) {
      return under / 3;
    }
    // The path's bare middle (the zone says lawn there)
    const path = tile.pathMask ? tile.pathMask[cell] : 0;
    return path > 0.4 && tile.getTerrainZone(x, z, height, growth, 1) === 0 ? PATH : 0;
  }

  /** The four motifs of the atlas, each one's feet in the earth's browns. */
  private earthMaterial(): PBRMaterial {
    if (this.material) {
      return this.material;
    }
    const pick = (list: number[][], rnd: () => number) => list[(rnd() * list.length) | 0];
    const {texture, material} = cardAtlasMaterial(this.babylonRender.getScene(), 'Earth clumps', 13, (ctx, ox, bottom, C, rnd) => {
      // Cell = column + 2 * row, row 0 at the canvas' bottom
      const cell = Math.round(ox / C) + (bottom < 2 * C ? 2 : 0);
      if (cell === STUBBLE) {
        // Many short stiff stalks
        for (let i = 0; i < 44; i++) {
          drawBlade(ctx, bottom, ox + C * (0.12 + 0.76 * rnd()), C * (0.15 + 0.35 * rnd()), (rnd() - 0.5) * C * 0.12, 1.5 + rnd() * 1.8,
            pick(EARTH, rnd), pick(STRAW, rnd), 0.85 + rnd() * 0.3);
        }
      } else if (cell === STALKS) {
        // A few thin tall ones, each with two or three side twigs
        for (let i = 0; i < 7; i++) {
          const x = ox + C * (0.2 + 0.6 * rnd()), h = C * (0.45 + 0.45 * rnd()), bend = (rnd() - 0.5) * C * 0.2;
          const tip = pick(WITHERED, rnd), k = 0.85 + rnd() * 0.3;
          drawBlade(ctx, bottom, x, h, bend, 1 + rnd() * 0.6, pick(EARTH, rnd), tip, k);
          ctx.strokeStyle = `rgb(${tip[0] * k | 0},${tip[1] * k | 0},${tip[2] * k | 0})`;
          ctx.lineWidth = 1.2;
          for (let t = 0; t < 3; t++) {
            const f = 0.4 + 0.5 * rnd(), sx = x + bend * f * f, sy = bottom - h * f, side = rnd() < 0.5 ? -1 : 1;
            ctx.beginPath();
            ctx.moveTo(sx, sy);
            ctx.lineTo(sx + side * C * (0.05 + 0.08 * rnd()), sy - C * (0.04 + 0.08 * rnd()));
            ctx.stroke();
          }
        }
      } else if (cell === WEEDS) {
        // Short broad leaves from one foot
        const cx = ox + C * 0.5;
        for (let i = 0; i < 20; i++) {
          drawBlade(ctx, bottom, cx + (rnd() - 0.5) * C * 0.3, C * (0.15 + 0.25 * rnd()), (rnd() - 0.5) * C * 0.7, 3 + rnd() * 4,
            pick(EARTH, rnd), pick(WEED, rnd), 0.85 + rnd() * 0.3);
        }
      } else {
        // Dry grass: longer bent blades, pale straw
        for (let i = 0; i < 32; i++) {
          drawBlade(ctx, bottom, ox + C * (0.12 + 0.76 * rnd()), C * (0.3 + 0.55 * rnd()), (rnd() - 0.5) * C * 0.5, 1.6 + rnd() * 2.4,
            pick(EARTH, rnd), pick(STRAW, rnd), 0.85 + rnd() * 0.3);
        }
      }
    });
    const tone = this.settings.tone;
    material.albedoColor = new Color3(tone, tone, tone);
    // The grass's camera fade
    new ClumpFadePlugin(material, () => this.grass.settings);
    new ClumpCloudPlugin(material);
    this.texture = texture;
    this.material = material;
    return material;
  }

  private applyShadows(): void {
    const generator = this.babylonRender.shadowGenerator;
    if (!generator) {
      return;
    }
    for (const mesh of this.meshes) {
      if (!mesh) {
        continue;
      }
      if (this.settings.castShadows) {
        generator.addShadowCaster(mesh, false);
      } else {
        generator.removeShadowCaster(mesh, false);
      }
    }
  }

  private disposeMeshes(): void {
    const included = this.babylonRender.directionalLight.includedOnlyMeshes;
    for (const mesh of this.meshes) {
      if (!mesh) {
        continue;
      }
      const index = included.indexOf(mesh);
      if (index >= 0) {
        included.splice(index, 1);
      }
      this.babylonRender.shadowGenerator?.removeShadowCaster(mesh, false);
      mesh.dispose();
    }
    this.meshes = [];
    this.shown = null;
    this.count = 0;
  }
}

function smoothstep(edge0: number, edge1: number, x: number): number {
  const t = Math.min(1, Math.max(0, (x - edge0) / (edge1 - edge0)));
  return t * t * (3 - 2 * t);
}
