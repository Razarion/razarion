import {DynamicTexture} from "@babylonjs/core/Materials/Textures/dynamicTexture";
import {StandardMaterial} from "@babylonjs/core/Materials/standardMaterial";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {Matrix, Quaternion, Vector3} from "@babylonjs/core/Maths/math.vector";
import {Mesh} from "@babylonjs/core/Meshes/mesh";
import {MeshBuilder} from "@babylonjs/core/Meshes/meshBuilder";
import "@babylonjs/core/Meshes/thinInstanceMesh";
import type {TransformNode} from "@babylonjs/core/Meshes/transformNode";
import type {Scene} from "@babylonjs/core/scene";

/**
 * Props on a bot ground plate: shipping containers, tanks, crate stacks and an antenna mast in the
 * plate's outer corners, barrel groups, crates and lamp posts along its edges, the middle left free for
 * the bot's buildings. Without them the plates read as empty grey squares from the game's camera.
 *
 * Purely visual (no collision, no pathing, not pickable) and worked out on the client from the plate's
 * 8 m boxes alone, deterministic like the markings (bot-ground-markings-compute.ts): every client, every
 * build, the same props. Simple primitives, no model files - the database has no such models.
 * Placeable bot terrain objects with their own height come later (memory: BotTerrainObject).
 */

const BOX = 8;
const HALF = BOX / 2;
// Clear of the hazard stripes along the outline (STRIPE_WIDTH 0.8 in the markings)
const EDGE_INSET = 1.1;
// A ramp hangs on a plate edge when its centre lies about one box beyond it
const RAMP_REACH = 6.5;

export enum PropKind { CONTAINER, BARREL, CRATE, TANK, LAMP, MAST }

const KIND_COUNT = 6;

export interface BotGroundProp {
  kind: PropKind;
  x: number;
  z: number;
  /** Above the plate's top (stacked crates) */
  lift: number;
  /** Around the vertical */
  yaw: number;
  /** sRGB 0..1 */
  color: [number, number, number];
}

// sRGB colours
const CONTAINER_COLORS: [number, number, number][] = [[0.55, 0.20, 0.14], [0.22, 0.32, 0.45], [0.34, 0.38, 0.24], [0.70, 0.42, 0.12], [0.45, 0.45, 0.46]];
const BARREL_COLORS: [number, number, number][] = [[0.80, 0.62, 0.10], [0.62, 0.14, 0.09], [0.20, 0.30, 0.42], [0.25, 0.32, 0.20]];
const CRATE_COLORS: [number, number, number][] = [[0.52, 0.40, 0.26], [0.36, 0.38, 0.24], [0.46, 0.36, 0.24]];
const TANK_COLORS: [number, number, number][] = [[0.80, 0.80, 0.78], [0.66, 0.68, 0.66]];
const STEEL: [number, number, number] = [0.30, 0.31, 0.32];

interface Cell {
  x: number;
  z: number;
}

/**
 * Where the props of one plate stand, in game coordinates (x east, z = game y north) on its top face.
 * flats: the centres of the plate's 8 m boxes, slopes: the centres of its ramps.
 */
export function planBotGroundProps(flats: Cell[], slopes: Cell[]): BotGroundProp[] {
  if (flats.length === 0) {
    return [];
  }
  const key = (x: number, z: number) => `${Math.round(x)},${Math.round(z)}`;
  const cells = new Set(flats.map(flat => key(flat.x, flat.z)));
  const has = (x: number, z: number) => cells.has(key(x, z));
  // A plate edge towards (dx, dz) leads onto a ramp: keep it clear
  const ramp = (cell: Cell, dx: number, dz: number) =>
    slopes.some(slope => Math.abs(slope.x - (cell.x + dx * BOX)) < RAMP_REACH && Math.abs(slope.z - (cell.z + dz * BOX)) < RAMP_REACH);
  // The same plate always gets the same props, wherever it is drawn from
  const sorted = [...flats].sort((a, b) => a.x - b.x || a.z - b.z);
  const rnd = mulberry32(hash(sorted.map(cell => key(cell.x, cell.z)).join(";")));
  const pick = <T>(list: T[]) => list[Math.floor(rnd() * list.length)];

  const props: BotGroundProp[] = [];
  // Edge segments already taken by a corner piece: `${cell} ${dx},${dz}`
  const used = new Set<string>();
  let mast = false;
  // A plate of one or two boxes is taken by its building: no room for containers, tanks or the mast
  const small = flats.length < 3;
  const DIRS: [number, number][] = [[1, 0], [-1, 0], [0, 1], [0, -1]];

  // Outer corners: two open sides that meet, neither onto a ramp
  for (const cell of sorted) {
    for (const [sx, sz] of [[1, 1], [1, -1], [-1, 1], [-1, -1]]) {
      if (has(cell.x + sx * BOX, cell.z) || has(cell.x, cell.z + sz * BOX) || ramp(cell, sx, 0) || ramp(cell, 0, sz)) {
        continue;
      }
      const choice = rnd();
      // Corner point inside the stripes
      const cx = cell.x + sx * (HALF - EDGE_INSET), cz = cell.z + sz * (HALF - EDGE_INSET);
      if (small) {
        if (choice < 0.4) {
          crateStack(props, cx - sx * 1.2, cz - sz * 1.2, rnd, pick);
        }
        continue;
      }
      if (choice < 0.42) {
        // Container along one of the two edges, its back against that edge
        const alongX = rnd() < 0.5;
        const length = 6.06, depth = 2.44;
        const x = alongX ? cx - sx * length / 2 : cx - sx * depth / 2;
        const z = alongX ? cz - sz * depth / 2 : cz - sz * length / 2;
        props.push({kind: PropKind.CONTAINER, x, z, lift: 0, yaw: alongX ? 0 : Math.PI / 2, color: pick(CONTAINER_COLORS)});
        // Now and then a second one on top
        if (rnd() < 0.25) {
          props.push({kind: PropKind.CONTAINER, x, z, lift: 2.6, yaw: (alongX ? 0 : Math.PI / 2) + (rnd() - 0.5) * 0.06, color: pick(CONTAINER_COLORS)});
        }
        used.add(`${key(cell.x, cell.z)} ${alongX ? 0 : sx},${alongX ? sz : 0}`);
      } else if (choice < 0.6) {
        // One or two tanks
        props.push({kind: PropKind.TANK, x: cx - sx * 1.3, z: cz - sz * 1.3, lift: 0, yaw: rnd() * Math.PI, color: pick(TANK_COLORS)});
        if (rnd() < 0.5) {
          const alongX = rnd() < 0.5;
          props.push({kind: PropKind.TANK, x: cx - sx * (alongX ? 4.1 : 1.3), z: cz - sz * (alongX ? 1.3 : 4.1), lift: 0, yaw: rnd() * Math.PI, color: pick(TANK_COLORS)});
        }
      } else if (choice < 0.8) {
        crateStack(props, cx - sx * 1.2, cz - sz * 1.2, rnd, pick);
      } else if (!mast) {
        mast = true;
        props.push({kind: PropKind.MAST, x: cx - sx * 0.6, z: cz - sz * 0.6, lift: 0, yaw: rnd() * Math.PI, color: STEEL});
      }
    }
  }

  // Edges: barrel groups, crates, lamp posts
  let lampTurn = 0;
  for (const cell of sorted) {
    for (const [dx, dz] of DIRS) {
      if (has(cell.x + dx * BOX, cell.z + dz * BOX) || ramp(cell, dx, dz)) {
        continue;
      }
      // Point on the edge line, inset, and the direction along it
      const ex = cell.x + dx * (HALF - EDGE_INSET), ez = cell.z + dz * (HALF - EDGE_INSET);
      const ax = dz !== 0 ? 1 : 0, az = dx !== 0 ? 1 : 0;
      // Every third edge segment a lamp post at its middle, facing inwards: more read as a fence
      if (lampTurn++ % 3 === 0) {
        props.push({kind: PropKind.LAMP, x: ex + dx * 0.4, z: ez + dz * 0.4, lift: 0, yaw: Math.atan2(dz, -dx), color: STEEL});
      }
      if (used.has(`${key(cell.x, cell.z)} ${dx},${dz}`)) {
        continue;
      }
      const choice = rnd();
      // Off the middle (the lamp) towards one end
      const offset = (rnd() < 0.5 ? -1 : 1) * (1.6 + rnd() * 0.8);
      const px = ex + ax * offset - dx * 0.9, pz = ez + az * offset - dz * 0.9;
      if (choice < 0.4) {
        barrelGroup(props, px, pz, rnd, pick);
      } else if (choice < 0.6) {
        crateStack(props, px, pz, rnd, pick);
      }
    }
  }
  return props;
}

function barrelGroup(props: BotGroundProp[], x: number, z: number, rnd: () => number, pick: <T>(list: T[]) => T) {
  const color = pick(BARREL_COLORS);
  const count = 3 + Math.floor(rnd() * 4);
  for (let i = 0; i < count; i++) {
    // Packed around the spot, the odd one in another colour
    const a = i * 2.4 + rnd() * 0.4, r = i === 0 ? 0 : i > 4 ? 1.15 : 0.62;
    props.push({kind: PropKind.BARREL, x: x + Math.cos(a) * r, z: z + Math.sin(a) * r, lift: 0, yaw: rnd() * Math.PI,
      color: rnd() < 0.15 ? pick(BARREL_COLORS) : color});
  }
}

function crateStack(props: BotGroundProp[], x: number, z: number, rnd: () => number, pick: <T>(list: T[]) => T) {
  const color = pick(CRATE_COLORS);
  const yaw = (rnd() - 0.5) * 0.3;
  const spots: [number, number][] = [[0, 0], [1.15, 0], [0, 1.15], [1.15, 1.15]];
  const count = 2 + Math.floor(rnd() * 3);
  for (let i = 0; i < count; i++) {
    const [ox, oz] = spots[i];
    props.push({kind: PropKind.CRATE, x: x + ox - 0.57, z: z + oz - 0.57, lift: 0, yaw: yaw + (rnd() - 0.5) * 0.15, color});
  }
  if (rnd() < 0.6) {
    props.push({kind: PropKind.CRATE, x: x - 0.3, z: z - 0.2, lift: 1.1, yaw: yaw + (rnd() - 0.5) * 0.4, color});
  }
}

/**
 * The prop templates of a scene, one mesh per kind; a tile draws its props as thin instances of
 * clones of them (createPropMeshes).
 */
class PropTemplates {
  private static scene: Scene | null = null;
  private static templates: Mesh[] = [];

  static get(scene: Scene): Mesh[] {
    if (PropTemplates.scene !== scene || PropTemplates.templates.length === 0) {
      PropTemplates.scene = scene;
      PropTemplates.templates = PropTemplates.build(scene);
    }
    return PropTemplates.templates;
  }

  private static build(scene: Scene): Mesh[] {
    const templates: Mesh[] = new Array(KIND_COUNT);
    const bottom = (mesh: Mesh, height: number) => {
      mesh.position.y = height / 2;
      mesh.bakeCurrentTransformIntoVertices();
      return mesh;
    };
    const merge = (name: string, parts: Mesh[]) => {
      const mesh = Mesh.MergeMeshes(parts, true, true)!;
      mesh.name = name;
      return mesh;
    };

    templates[PropKind.CONTAINER] = bottom(MeshBuilder.CreateBox("BotProp container", {width: 6.06, height: 2.6, depth: 2.44}, scene), 2.6);
    templates[PropKind.CONTAINER].material = material(scene, "BotProp container", ribs);

    templates[PropKind.BARREL] = bottom(MeshBuilder.CreateCylinder("BotProp barrel", {diameter: 0.6, height: 0.9, tessellation: 12}, scene), 0.9);
    templates[PropKind.BARREL].material = material(scene, "BotProp barrel", rings);

    templates[PropKind.CRATE] = bottom(MeshBuilder.CreateBox("BotProp crate", {size: 1.1}, scene), 1.1);
    templates[PropKind.CRATE].material = material(scene, "BotProp crate", planks);

    const tankBody = bottom(MeshBuilder.CreateCylinder("tank body", {diameter: 2.4, height: 2.6, tessellation: 18}, scene), 2.6);
    const tankRoof = MeshBuilder.CreateSphere("tank roof", {diameter: 2.4, segments: 8}, scene);
    tankRoof.scaling.y = 0.35;
    tankRoof.position.y = 2.6;
    tankRoof.bakeCurrentTransformIntoVertices();
    const tankLadder = MeshBuilder.CreateBox("tank ladder", {width: 0.4, height: 2.9, depth: 0.08}, scene);
    tankLadder.position.set(0, 1.45, 1.24);
    tankLadder.bakeCurrentTransformIntoVertices();
    templates[PropKind.TANK] = merge("BotProp tank", [tankBody, tankRoof, tankLadder]);
    templates[PropKind.TANK].material = material(scene, "BotProp tank", band);

    const pole = bottom(MeshBuilder.CreateCylinder("lamp pole", {diameter: 0.14, height: 5, tessellation: 8}, scene), 5);
    const arm = MeshBuilder.CreateBox("lamp arm", {width: 1.0, height: 0.1, depth: 0.1}, scene);
    arm.position.set(0.45, 4.95, 0);
    arm.bakeCurrentTransformIntoVertices();
    const head = MeshBuilder.CreateBox("lamp head", {width: 0.55, height: 0.14, depth: 0.32}, scene);
    head.position.set(0.95, 4.88, 0);
    head.bakeCurrentTransformIntoVertices();
    const foot = bottom(MeshBuilder.CreateBox("lamp foot", {width: 0.4, height: 0.3, depth: 0.4}, scene), 0.3);
    templates[PropKind.LAMP] = merge("BotProp lamp", [pole, arm, head, foot]);
    templates[PropKind.LAMP].material = material(scene, "BotProp lamp", null);

    const mastParts: Mesh[] = [];
    // Three legs leaning in, rungs between them, a dish and a blinking-light box on top
    for (let i = 0; i < 3; i++) {
      const a = i * Math.PI * 2 / 3;
      const leg = MeshBuilder.CreateCylinder("mast leg", {diameter: 0.12, height: 9.2, tessellation: 6}, scene);
      leg.rotationQuaternion = Quaternion.RotationAxis(new Vector3(-Math.sin(a), 0, Math.cos(a)), 0.045);
      leg.position.set(Math.cos(a) * 0.35, 4.6, Math.sin(a) * 0.35);
      leg.bakeCurrentTransformIntoVertices();
      mastParts.push(leg);
    }
    for (let h = 1; h < 9; h += 1.5) {
      const ring = MeshBuilder.CreateTorus("mast rung", {diameter: 0.75 - h * 0.045, thickness: 0.05, tessellation: 6}, scene);
      ring.position.y = h;
      ring.bakeCurrentTransformIntoVertices();
      mastParts.push(ring);
    }
    const dish = MeshBuilder.CreateCylinder("mast dish", {diameterTop: 1.3, diameterBottom: 0.3, height: 0.35, tessellation: 12}, scene);
    dish.rotationQuaternion = Quaternion.RotationAxis(Vector3.Right(), Math.PI / 2 - 0.3);
    dish.position.set(0, 7.4, 0.55);
    dish.bakeCurrentTransformIntoVertices();
    mastParts.push(dish);
    const tip = MeshBuilder.CreateBox("mast tip", {size: 0.22}, scene);
    tip.position.y = 9.3;
    tip.bakeCurrentTransformIntoVertices();
    mastParts.push(tip);
    templates[PropKind.MAST] = merge("BotProp mast", mastParts);
    templates[PropKind.MAST].material = material(scene, "BotProp mast", null);

    templates.forEach(template => {
      template.isPickable = false;
      template.setEnabled(false);
    });
    return templates;
  }
}

/**
 * Draws props as thin instances under `parent`: one mesh per kind present, colours per instance.
 * The caller registers the meshes with the lights and the shadows and disposes them with its tile.
 */
export function createPropMeshes(scene: Scene, parent: TransformNode, props: BotGroundProp[], plateTop: number): Mesh[] {
  const templates = PropTemplates.get(scene);
  const meshes: Mesh[] = [];
  for (let kind = 0; kind < KIND_COUNT; kind++) {
    const own = props.filter(prop => prop.kind === kind);
    if (own.length === 0) {
      continue;
    }
    const mesh = templates[kind].clone(`${templates[kind].name} ${Math.round(own[0].x)},${Math.round(own[0].z)}`, null, true)!;
    // Own geometry: thin-instance buffers live on the geometry, a shared one would draw every tile's
    // props at the positions of whichever tile set them last
    mesh.makeGeometryUnique();
    mesh.parent = parent;
    mesh.isPickable = false;
    mesh.receiveShadows = true;
    const matrices = new Float32Array(own.length * 16), colors = new Float32Array(own.length * 4);
    own.forEach((prop, i) => {
      Matrix.Compose(Vector3.One(), Quaternion.RotationYawPitchRoll(prop.yaw, 0, 0), new Vector3(prop.x, plateTop + prop.lift, prop.z))
        .copyToArray(matrices, i * 16);
      // Multiplied with the diffuse texture
      colors.set([lift(prop.color[0]), lift(prop.color[1]), lift(prop.color[2]), 1], i * 4);
    });
    // Not static: PropClearance rewrites it when a building comes or goes
    mesh.thinInstanceSetBuffer("matrix", matrices, 16, false);
    mesh.thinInstanceSetBuffer("color", colors, 4, true);
    mesh.thinInstanceRefreshBoundingInfo(false);
    mesh.setEnabled(true);
    PropClearance.register(mesh, own, matrices);
    meshes.push(mesh);
  }
  return meshes;
}

/** How far a prop reaches around its spot (m), for clearing the buildings. */
const REACH: Record<PropKind, number> = {
  [PropKind.CONTAINER]: 3.2,
  [PropKind.BARREL]: 0.35,
  [PropKind.CRATE]: 0.8,
  [PropKind.TANK]: 1.3,
  [PropKind.LAMP]: 0.6,
  [PropKind.MAST]: 0.5,
};
// Room left between a prop and a building, a building's radius being its footprint, not its outline
const CLEARANCE_GAP = 0.5;
const CLEARANCE_INTERVAL_MS = 1000;

interface ClearedMesh {
  mesh: Mesh;
  props: BotGroundProp[];
  /** The matrices as planned */
  planned: Float32Array;
  /** Per prop: hidden now */
  hidden: boolean[];
}

/**
 * Hides the props a building stands on: the plan knows only the plate, not where the bot puts its
 * Teslas and factories (and puts them again after they were destroyed). Once a second the meshes are
 * held against the buildings in the scene; a prop under one is drawn at scale zero, and comes back
 * when the building is gone. Units are left out - they drive about, the props would flicker.
 */
export class PropClearance {
  private static meshes: ClearedMesh[] = [];
  private static scene: Scene | null = null;
  private static footprints: () => { x: number, z: number, radius: number }[] = () => [];
  private static last = 0;

  /**
   * Once per scene, with where the buildings are and a hook for new ones: the client learns of a bot's
   * buildings only when they come near the camera, often after the tile was built - checked in the
   * next frame then, not up to a second later with a container standing in the building meanwhile.
   */
  static install(scene: Scene, footprints: () => { x: number, z: number, radius: number }[], onBuilding: (listener: () => void) => void): void {
    PropClearance.footprints = footprints;
    if (PropClearance.scene === scene) {
      return;
    }
    PropClearance.scene = scene;
    PropClearance.meshes = [];
    onBuilding(() => PropClearance.last = 0);
    scene.onBeforeRenderObservable.add(() => {
      const now = performance.now();
      if (now - PropClearance.last >= CLEARANCE_INTERVAL_MS) {
        PropClearance.last = now;
        PropClearance.update();
      }
    });
  }

  static register(mesh: Mesh, props: BotGroundProp[], planned: Float32Array): void {
    const entry: ClearedMesh = {mesh, props, planned: planned.slice(), hidden: props.map(() => false)};
    PropClearance.meshes.push(entry);
    mesh.onDisposeObservable.addOnce(() => PropClearance.meshes = PropClearance.meshes.filter(e => e !== entry));
    // Right away, not up to a second late: a tile built under a standing base
    PropClearance.clear(entry, PropClearance.footprints());
  }

  static update(): void {
    if (PropClearance.meshes.length === 0) {
      return;
    }
    const footprints = PropClearance.footprints();
    PropClearance.meshes.forEach(entry => {
      if (entry.mesh.isEnabled(false)) {
        PropClearance.clear(entry, footprints);
      }
    });
  }

  private static clear(entry: ClearedMesh, footprints: { x: number, z: number, radius: number }[]): void {
    let changed = false;
    entry.props.forEach((prop, i) => {
      const reach = REACH[prop.kind] + CLEARANCE_GAP;
      const hide = footprints.some(f => {
        const r = f.radius + reach, dx = f.x - prop.x, dz = f.z - prop.z;
        return dx * dx + dz * dz < r * r;
      });
      if (hide !== entry.hidden[i]) {
        entry.hidden[i] = hide;
        changed = true;
      }
    });
    if (!changed) {
      return;
    }
    const matrices = entry.planned.slice();
    entry.hidden.forEach((hide, i) => {
      if (hide) {
        // Scale zero, the translation kept so the bounding box stays where it was
        const o = i * 16;
        for (let k = 0; k < 12; k++) {
          matrices[o + k] = 0;
        }
      }
    });
    entry.mesh.thinInstanceSetBuffer("matrix", matrices, 16, false);
  }
}

type Painter = (ctx: CanvasRenderingContext2D, size: number) => void;

function material(scene: Scene, name: string, painter: Painter | null): StandardMaterial {
  const mat = new StandardMaterial(name, scene);
  mat.diffuseColor = Color3.White();
  mat.specularColor = new Color3(0.08, 0.08, 0.08);
  if (painter) {
    const size = 128;
    const texture = new DynamicTexture(`${name} texture`, size, scene, true);
    const ctx = texture.getContext() as unknown as CanvasRenderingContext2D;
    ctx.fillStyle = "#ffffff";
    ctx.fillRect(0, 0, size, size);
    painter(ctx, size);
    texture.update(false);
    mat.diffuseTexture = texture;
  }
  return mat;
}

// Corrugated container walls: vertical ribs, a darker frame
const ribs: Painter = (ctx, size) => {
  for (let x = 0; x < size; x += 8) {
    ctx.fillStyle = "#c4c4c4";
    ctx.fillRect(x, 0, 3, size);
  }
  ctx.strokeStyle = "#6a6a6a";
  ctx.lineWidth = 6;
  ctx.strokeRect(0, 0, size, size);
};

// Barrel hoops
const rings: Painter = (ctx, size) => {
  ctx.fillStyle = "#8a8a8a";
  for (const y of [0.3, 0.7]) {
    ctx.fillRect(0, size * y - 3, size, 6);
  }
  ctx.fillStyle = "#b0b0b0";
  ctx.fillRect(0, 0, size, 4);
  ctx.fillRect(0, size - 4, size, 4);
};

// Crate: frame, diagonal brace, plank gaps
const planks: Painter = (ctx, size) => {
  ctx.fillStyle = "#b8b8b8";
  for (let y = 0; y < size; y += 26) {
    ctx.fillRect(0, y, size, 2);
  }
  ctx.strokeStyle = "#8c8c8c";
  ctx.lineWidth = 14;
  ctx.strokeRect(7, 7, size - 14, size - 14);
  ctx.beginPath();
  ctx.moveTo(10, size - 10);
  ctx.lineTo(size - 10, 10);
  ctx.stroke();
};

// Tank: a coloured band and weld seams
const band: Painter = (ctx, size) => {
  ctx.fillStyle = "#d24a1a";
  ctx.fillRect(0, size * 0.62, size, size * 0.1);
  ctx.fillStyle = "#c8c8c8";
  for (const y of [0.25, 0.45, 0.85]) {
    ctx.fillRect(0, size * y, size, 2);
  }
};

function hash(text: string): number {
  let h = 2166136261;
  for (let i = 0; i < text.length; i++) {
    h ^= text.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  return h >>> 0;
}

function mulberry32(seed: number): () => number {
  let a = seed;
  return () => {
    a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/** The palette is picked in sRGB, but the plates' sun and exposure draw it about half as bright. */
function lift(c: number): number {
  return Math.min(1, c * 1.6 + 0.05);
}
