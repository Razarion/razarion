import {RawTexture} from '@babylonjs/core/Materials/Textures/rawTexture';
import {Texture} from '@babylonjs/core/Materials/Textures/texture';
import {Material} from '@babylonjs/core/Materials/material';
import {PBRMaterial} from '@babylonjs/core/Materials/PBR/pbrMaterial';
import {Color3} from '@babylonjs/core/Maths/math.color';
import {Matrix, Quaternion, Vector3} from '@babylonjs/core/Maths/math.vector';
import {Mesh} from '@babylonjs/core/Meshes/mesh';
import type {Scene} from '@babylonjs/core/scene';
import {VertexData} from '@babylonjs/core/Meshes/mesh.vertexData';
import '@babylonjs/core/Meshes/thinInstanceMesh';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {BabylonTerrainTileImpl} from './babylon-terrain-tile.impl';
import {GroundZone} from './ground-zone';
import {initPerm, scatterNoise, SEED} from './procedural-textures';
import {AddClump, ClumpField, Clumps} from './clump-field';
import {CLUMP_FADE, ClumpFadePlugin} from './clump-fade-plugin';

/** Grass clump settings - the terrain lab's sliders. */
export interface GrassSettings {
  on: boolean;
  /** Clumps per m² where grass grows */
  density: number;
  /** Blade height (m) */
  height: number;
  /** Clump width (m) */
  width: number;
  /** Radius (m) around the camera's ground point that gets grass */
  radius: number;
  /** Brightness of the blades */
  tone: number;
  /** 0 = the cliff lab's greens, 1 = yellowed towards the game's olive ground */
  dry: number;
  /** Brightness spread between neighbouring patches (0 = every clump alike) */
  patches: number;
  /** How far the clump's cards lean out (0 = upright, 1 = about 40 degrees) */
  splay: number;
  /** Camera distance (m) from which clumps thin out - grass and the bare ground's detail alike */
  fadeNear: number;
  /** Camera distance (m) from which only one in ten is left (0 = no thinning) */
  fadeFar: number;
  castShadows: boolean;
}

// Blade feet in the lawn's own olive (the ground shader's grass), tips lit and drier
const FEET = [[96, 98, 40], [86, 92, 34], [104, 102, 44]];
const TIPS = [[150, 166, 70], [134, 156, 60], [164, 172, 84], [124, 146, 54], [156, 160, 76]];
// The blade texture: a 2 x 2 atlas of different blade sets, so neighbouring clumps differ
const ATLAS = 2;
const VARIANTS = 4;
// The tile's TerrainZone.UPPER: the grass sprite sheet, where the ground shader draws grass
const ZONE_UPPER = 0;

/**
 * 3D grass from the cliff lab (tools/cliff-lab): clumps of fanned blade cards, as thin
 * instances on the terrain tiles around the camera (GroundClumps). They grow where the tiles put their grass sprites (the tile's own
 * zone decision: not on sand, rock, paths, decals or bot grounds), thinned in patches by the same
 * scatter noise. Only around the camera - the whole island would be millions of clumps.
 */
export class GrassClumps {
  // The cliff lab's values: small and dense, a fuzz over the ground rather than single tufts
  readonly settings: GrassSettings = {on: true, density: 14, height: 0.28, width: 0.47, radius: 60, tone: 1.6, dry: 0, patches: 0.5, splay: 0.9, fadeNear: 25, fadeFar: 60, castShadows: false};
  private texture: RawTexture | null = null;
  private material: PBRMaterial | null = null;
  // Per variant, made when first needed and refilled by show
  private meshes: (Mesh | undefined)[] = [];
  // Around where the meshes were last filled, and the buckets of their clumps (ClumpField.visible)
  private shown: { x: number, z: number, ends: Int32Array[] } | null = null;
  private keep = -1;
  count = 0;

  constructor(private readonly babylonRender: BabylonRenderServiceAccessImpl) {
  }

  /** Where the grass stands, per chunk; GroundClumps works it out a few chunks per frame. */
  readonly field = new ClumpField(VARIANTS, 3, () => this.settings.density, (tile, x, z, rnd, add) => this.place(tile, x, z, rnd, add));

  /** Grass anew around (centerX, centerY) on the given tiles, all at once - after a setting has changed (the lab). */
  async build(tiles: Iterable<BabylonTerrainTileImpl>, centerX: number, centerY: number): Promise<number> {
    // The tile's sand/land decision reads the splatter texture
    await GroundZone.load();
    initPerm(SEED);
    this.disposeMesh();
    this.field.reset();
    this.field.prepare(ClumpField.tilesByIndex(tiles), centerX, centerY, this.settings.radius, this.settings.radius, Infinity);
    return this.show(centerX, centerY);
  }

  /** The worked out clumps within the radius around (centerX, centerY) into the meshes; the camera fade thins them (refade). */
  show(centerX: number, centerY: number): number {
    const s = this.settings;
    const clumps = this.field.assemble(centerX, centerY, s.on ? s.radius : 0);
    fillClumpMeshes(this.meshes, clumps, v => this.clump(v, `Grass clumps ${v}`));
    this.shown = {x: centerX, z: centerY, ends: clumps.ends};
    this.refade(true);
    this.applyShadows();
    return this.count;
  }

  /**
   * Every frame: draws only as many clumps as the camera fade can keep at the nearest of them - the
   * shader thins the rest (ClumpFadePlugin). Zooming changes only these counts, the lists stay.
   */
  refade(force = false): void {
    if (!this.shown) {
      return;
    }
    const keep = this.nearestKeep(this.shown.x, this.shown.z, this.settings.radius);
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

  /** Texture, material, meshes and shader made ahead (GroundClumps), so the first show does not stall a frame. */
  warm(): Promise<void> {
    return warmClumpMeshes(this.meshes, VARIANTS, v => this.clump(v, `Grass clumps ${v}`));
  }

  /** A clump at (x, z), if grass grows there: its size, turn, blade set and colour. */
  private place(tile: any, x: number, z: number, rnd: () => number, add: AddClump): void {
    const s = this.settings;
    const grow = this.grows(tile, x, z, rnd);
    if (grow <= 0) {
      return;
    }
    // Lower towards paths and bald patches: the lawn thins out instead of ending in a wall
    const w = s.width * (0.6 + 0.6 * rnd()), h = s.height * (0.5 + 0.9 * rnd() * rnd()) * (0.35 + 0.65 * grow);
    const q = Quaternion.RotationYawPitchRoll(rnd() * Math.PI * 2, (rnd() - 0.5) * 0.35, (rnd() - 0.5) * 0.35);
    // Mirrored at random: four blade sets read as eight
    const m = Matrix.Compose(new Vector3(rnd() < 0.5 ? -w : w, h, w), q, new Vector3(x, tile.getHeightSmooth(x, z) - 0.02, z));
    const variant = (rnd() * VARIANTS) | 0;
    // Patches a few metres across, lighter and drier or darker and lusher, and a little per clump
    const patch = scatterNoise(x / 7 + 13.1, z / 7 + 5.7) * s.patches, own = (rnd() - 0.5) * 0.2 * s.patches;
    const light = 1 + patch + own;
    add(variant, m, light * (1 + 0.3 * patch), light, light * (1 - 0.3 * patch));
  }

  /**
   * How many clumps (0 - 1) the camera fade keeps at the nearest point of the disc of radius around
   * (x, z): all nearby, fewer from fadeNear, one in ten from fadeFar - from the game's camera the lawn
   * stays calm, close up it gets its blades. As ClumpFadePlugin's shader, which thins clump by clump.
   */
  nearestKeep(x: number, z: number, radius: number): number {
    const {fadeNear, fadeFar} = this.settings, camera = this.babylonRender.getCamera()?.position;
    if (fadeFar <= 0 || !camera) {
      return 1;
    }
    // The ground a little below the camera's height over the island's base
    const dy = Math.max(0, camera.y - 2), d = Math.hypot(Math.max(0, Math.hypot(x - camera.x, z - camera.z) - radius), dy);
    return 1 - 0.9 * smoothstep(fadeNear, Math.max(fadeNear + 1, fadeFar), d);
  }

  setCastShadows(on: boolean): void {
    this.settings.castShadows = on;
    this.applyShadows();
  }

  dispose(): void {
    this.disposeMesh();
    this.material?.dispose();
    this.texture?.dispose();
    this.material = null;
    this.texture = null;
  }

  /**
   * As the tile's addSprite: inside the tile, not on decals, bot grounds or paths, and the grass zone.
   * 0 = no grass, else how fully it grows (1 = full height, less at the edge of paths and patches).
   */
  private grows(tile: any, x: number, z: number, rnd: () => number): number {
    if (tile.insideAnyDecal(x, z) || tile.insideBotGround(x, z)) {
      return 0;
    }
    const cell = Math.floor(z - tile.tileYOffset) * BabylonTerrainTileImpl.NODE_X_COUNT + Math.floor(x - tile.tileXOffset);
    const path = tile.pathMask ? tile.pathMask[cell] : 0;
    if (rnd() < path * 1.5) {
      return 0;
    }
    // Thinned in patches, with the sprites' scatter noise (a little wider: a lawn has gaps, no bald spots)
    const density = smoothstep(-0.25, 0.15, scatterNoise(x / 48, z / 48));
    if (rnd() >= density) {
      return 0;
    }
    const height = tile.getHeightAt(x, z);
    if (tile.getTerrainZone(x, z, height, tile.vegetationMask ? tile.vegetationMask[cell] : 0, rnd()) !== ZONE_UPPER) {
      return 0;
    }
    return Math.max(0.05, density * (1 - path));
  }

  /**
   * One clump mesh (thin instances go on it): six cards, normals straight up - lit like the lawn under them,
   * no dark backs. Each card a cell of the atlas (cardAtlasMaterial) - the blades', or EarthClumps's with its own
   * material; oneCell puts the variant's cell on all six cards (one motif per clump). Lit by the sun only, as the ground.
   */
  clump(variant: number, name: string, material: PBRMaterial = this.bladeMaterial(), splay = this.settings.splay, oneCell = false): Mesh {
    const scene = this.babylonRender.getScene();
    const positions: number[] = [], normals: number[] = [], uvs: number[] = [], indices: number[] = [];
    // Five cards fanned out from the foot: leaning outwards they still show blades to the game's camera
    // above, where upright cards shrink to lines (splay 0 = upright, 1 = about 40 degrees). A sixth
    // stands upright in the middle, else the fan leaves a bald ring.
    const cards = 5;
    for (let k = 0; k <= cards; k++) {
      const lean = k < cards ? splay : 0;
      const a = (k + 0.15 * Math.sin(k * 7.3 + variant)) * 2 * Math.PI / cards, rx = Math.cos(a), rz = Math.sin(a), px = -rz * 0.3, pz = rx * 0.3;
      const tx = rx * lean * 0.8, tz = rz * lean * 0.8, ty = 1 / Math.sqrt(1 + lean * lean * 0.64), base = positions.length / 3;
      positions.push(-px + rx * 0.05, 0, -pz + rz * 0.05, px + rx * 0.05, 0, pz + rz * 0.05, px * 1.2 + tx, ty, pz * 1.2 + tz, -px * 1.2 + tx, ty, -pz * 1.2 + tz);
      for (let i = 0; i < 4; i++) {
        normals.push(0, 1, 0);
      }
      const cell = oneCell ? variant : (variant + k) % (ATLAS * ATLAS), u0 = (cell % ATLAS) / ATLAS, v0 = Math.floor(cell / ATLAS) / ATLAS, d = 1 / ATLAS;
      uvs.push(u0, v0, u0 + d, v0, u0 + d, v0 + d, u0, v0 + d);
      indices.push(base, base + 1, base + 2, base, base + 2, base + 3);
    }
    const vertexData = new VertexData();
    vertexData.positions = positions;
    vertexData.normals = normals;
    vertexData.uvs = uvs;
    vertexData.indices = indices;
    const mesh = new Mesh(name, scene);
    vertexData.applyToMesh(mesh);
    mesh.material = material;
    mesh.receiveShadows = true;
    mesh.isPickable = false;
    // The clumps lie around the screen's centre anyway; their bounds over tens of thousands of
    // instance matrices took longer than drawing the few that a culled mesh would save
    mesh.alwaysSelectAsActiveMesh = true;
    // The game's sun lights only the meshes listed with it. The sky light stays off, as on the ground
    // (its shader brings its own ambient): with it the blades were nearly twice as bright as the lawn.
    this.babylonRender.directionalLight.includedOnlyMeshes.push(mesh);
    scene.getLightByName(BabylonRenderServiceAccessImpl.SKY_LIGHT_NAME)?.excludedMeshes.push(mesh);
    return mesh;
  }

  private bladeMaterial(): PBRMaterial {
    if (this.material) {
      return this.material;
    }
    // Per atlas cell 34 thin bent blades: the foot in the lawn's olive, so a clump grows out of the
    // ground instead of standing on it as a dark stamp, the tip lighter where the sun catches it
    const {texture, material} = cardAtlasMaterial(this.babylonRender.getScene(), 'Grass clumps', 7, (ctx, ox, bottom, C, rnd) => {
      for (let i = 0; i < 34; i++) {
        drawBlade(ctx, bottom, ox + C * (0.12 + 0.76 * rnd()), C * (0.35 + 0.6 * rnd()), (rnd() - 0.5) * C * 0.45, 1.6 + rnd() * 2.6,
          FEET[(rnd() * FEET.length) | 0], TIPS[(rnd() * TIPS.length) | 0], 0.85 + rnd() * 0.3);
      }
    });
    new ClumpFadePlugin(material, () => this.settings);
    this.texture = texture;
    this.material = material;
    this.applyTint();
    return material;
  }

  /** Brightness and dryness: 0 = the blades' own colours, 1 = yellowed further. */
  applyTint(): void {
    if (!this.material) {
      return;
    }
    const {tone, dry} = this.settings;
    this.material.albedoColor = new Color3(tone * (1 + 0.45 * dry), tone * (1 + 0.1 * dry), tone * (1 - 0.35 * dry));
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

  private disposeMesh(): void {
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

/**
 * A texture atlas of ATLAS x ATLAS cells, each drawn by `draw` on a 2D canvas (the cell's left edge ox, its
 * ground line bottom, cell size C), and the alpha-tested PBR material for clump cards (GrassClumps.clump).
 * Transparent pixels take the drawn pixels' mean colour: the mip levels then fade the shapes out instead
 * of drawing a black rim around them (clearRect's transparent black would bleed in).
 */
export function cardAtlasMaterial(scene: Scene, name: string, seed: number,
                                  draw: (ctx: CanvasRenderingContext2D, ox: number, bottom: number, C: number, rnd: () => number) => void): { texture: RawTexture, material: PBRMaterial } {
  const C = 256, N = C * ATLAS;
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = N;
  // Drawn in memory: read back from a GPU canvas, the first atlas took 170 ms (getImageData)
  const ctx = canvas.getContext('2d', {willReadFrequently: true})!;
  ctx.clearRect(0, 0, N, N);
  const rnd = () => (seed = (seed * 16807) % 2147483647) / 2147483647;
  for (let cell = 0; cell < ATLAS * ATLAS; cell++) {
    // Canvas rows run down, the texture's v up (flipped below): cell row 0 is the bottom one
    draw(ctx, (cell % ATLAS) * C, N - Math.floor(cell / ATLAS) * C, C, rnd);
  }
  const pixels = ctx.getImageData(0, 0, N, N).data;
  let r = 0, g = 0, b = 0, solid = 0;
  for (let i = 0; i < pixels.length; i += 4) {
    if (pixels[i + 3] > 128) {
      r += pixels[i];
      g += pixels[i + 1];
      b += pixels[i + 2];
      solid++;
    }
  }
  const data = new Uint8Array(pixels.length);
  for (let i = 0; i < pixels.length; i += 4) {
    const opaque = pixels[i + 3] > 128;
    data[i] = opaque ? pixels[i] : r / solid;
    data[i + 1] = opaque ? pixels[i + 1] : g / solid;
    data[i + 2] = opaque ? pixels[i + 2] : b / solid;
    data[i + 3] = pixels[i + 3];
  }
  // The canvas has y down, the texture's v goes up: rows flipped, the feet at v = 0
  const flipped = new Uint8Array(data.length);
  for (let y = 0; y < N; y++) {
    flipped.set(data.subarray(y * N * 4, (y + 1) * N * 4), (N - 1 - y) * N * 4);
  }
  const texture = RawTexture.CreateRGBATexture(flipped, N, N, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
  texture.hasAlpha = true;
  texture.wrapU = texture.wrapV = Texture.CLAMP_ADDRESSMODE;
  texture.anisotropicFilteringLevel = 4;
  // PBR as the models: lit by the same sun and environment, so the cards sit in the scene as the
  // plants do (a StandardMaterial came out lime green against the olive ground)
  const material = new PBRMaterial(name, scene);
  material.albedoTexture = texture;
  material.useAlphaFromAlbedoTexture = true;
  material.transparencyMode = Material.MATERIAL_ALPHATEST;
  material.alphaCutOff = 0.4;
  material.backFaceCulling = false;
  material.metallic = 0;
  material.roughness = 1;
  return {texture, material};
}

/** A bent blade from the ground line up, foot colour to tip colour (sRGB), times k. */
export function drawBlade(ctx: CanvasRenderingContext2D, bottom: number, x: number, h: number, bend: number, w: number,
                          foot: number[], tip: number[], k: number): void {
  const grad = ctx.createLinearGradient(0, bottom, 0, bottom - h);
  grad.addColorStop(0, `rgb(${Math.min(255, foot[0] * k) | 0},${Math.min(255, foot[1] * k) | 0},${Math.min(255, foot[2] * k) | 0})`);
  grad.addColorStop(1, `rgb(${Math.min(255, tip[0] * k) | 0},${Math.min(255, tip[1] * k) | 0},${Math.min(255, tip[2] * k) | 0})`);
  ctx.fillStyle = grad;
  ctx.beginPath();
  ctx.moveTo(x - w, bottom);
  ctx.quadraticCurveTo(x - w * 0.4 + bend * 0.3, bottom - h * 0.5, x + bend, bottom - h);
  ctx.quadraticCurveTo(x + w * 0.4 + bend * 0.3, bottom - h * 0.5, x + w, bottom);
  ctx.fill();
}

/**
 * Fills the per-variant meshes with the clumps (thin instances), making a variant's mesh the first
 * time it has any. Which are drawn says keepClumps.
 */
export function fillClumpMeshes(meshes: (Mesh | undefined)[], clumps: Clumps, create: (variant: number) => Mesh): void {
  for (let v = 0; v < clumps.fades.length; v++) {
    if (!clumps.fades[v].length) {
      continue;
    }
    const mesh = meshes[v] ??= create(v);
    mesh.thinInstanceSetBuffer('matrix', clumps.matrices[v], 16, true);
    mesh.thinInstanceSetBuffer('color', clumps.colors[v], 4, true);
    mesh.thinInstanceSetBuffer(CLUMP_FADE, clumps.fades[v], 1, true);
  }
}

/**
 * Draws of each variant only the clumps (a prefix: they are in order of their fade draws) the camera
 * fade can keep, at most `keep`; a mesh without any is switched off. Returns how many are drawn.
 */
export function keepClumps(meshes: (Mesh | undefined)[], ends: Int32Array[], keep: number): number {
  let count = 0;
  for (let v = 0; v < ends.length; v++) {
    const mesh = meshes[v], n = ClumpField.visible(ends[v], keep);
    if (!mesh) {
      continue;
    }
    if (n) {
      mesh.thinInstanceCount = n;
    }
    mesh.setEnabled(n > 0);
    count += n;
  }
  return count;
}

/**
 * Makes the variants' meshes and compiles their shader ahead, on one stand-in clump each (a zero
 * matrix: it draws nothing) - the shader's defines need thin instances with colour and fade, as a
 * show gives them. The meshes stay off until a show fills them.
 */
export async function warmClumpMeshes(meshes: (Mesh | undefined)[], variants: number, create: (variant: number) => Mesh): Promise<void> {
  const compiling: Promise<void>[] = [];
  for (let v = 0; v < variants; v++) {
    if (meshes[v]) {
      continue;
    }
    const mesh = meshes[v] = create(v);
    mesh.thinInstanceSetBuffer('matrix', new Float32Array(16), 16, true);
    mesh.thinInstanceSetBuffer('color', new Float32Array(4), 4, true);
    mesh.thinInstanceSetBuffer(CLUMP_FADE, new Float32Array(1), 1, true);
    mesh.setEnabled(false);
    compiling.push(mesh.material!.forceCompilationAsync(mesh, {useInstances: true}));
  }
  await Promise.all(compiling);
}

function smoothstep(edge0: number, edge1: number, x: number): number {
  const t = Math.min(1, Math.max(0, (x - edge0) / (edge1 - edge0)));
  return t * t * (3 - 2 * t);
}
