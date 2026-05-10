import {
  Color3,
  Material,
  Mesh,
  MeshBuilder,
  Scene,
  StandardMaterial,
  Texture,
  Vector2,
  Vector3,
} from "@babylonjs/core";
import {BowWaveHalo} from "./bow-wave-halo";

// Ship wake renderer: per ship, two continuous ribbon meshes (port + starboard
// arm) drawn with foam-wave.png on a transparent unlit StandardMaterial. Each
// arm is a single merged mesh with shared vertices between segments — the
// ribbon stays smooth where adjacent segments meet. Plus a bow-wave foam halo
// under the hull (delegated to BowWaveHalo).

const LIFETIME_MS = 3500;
const SPAWN_INTERVAL_M = 0.6;
const RIBBON_WIDTH_M = 1.0;
const STERN_OFFSET_M = 1.6;
const Y_ABOVE_WATER = 0.04;
const INITIAL_HALF_SPREAD_M = 0.05;
const OUTWARD_DRIFT_MPS = 0.7;
// Drift saturates after this duration — old points stop moving sideways, so a
// ship that turns doesn't drag its old wake into skewed shapes.
const DRIFT_DURATION_MS = 1500;
// Texture tiles every N meters along ribbon length — controls wave-band density.
const TEXTURE_TILE_M = 5.0;
// Width taper at the tail end (1.0 = taper to zero, 0.0 = uniform width).
const WIDTH_TAPER_AT_TAIL = 0.75;
// Final-fade phase — over the last FINAL_FADE_MS of a point's lifetime its
// width shrinks linearly to 0. Short enough that the inward edge migration
// reads as "wake dissolving" rather than "pulled toward the ship".
const FINAL_FADE_MS = 800;
// Bow-wave halo dimensions for ships: elongated along the hull's forward
// axis to fit the rectangular ship shape.
const SHIP_BOW_HALO_LENGTH_M = 6.0;
const SHIP_BOW_HALO_WIDTH_M  = 3.0;

interface ArmPoint {
  birthTime: number;
  // Stern position at spawn time (frozen).
  baseX: number;
  baseZ: number;
  // Perpendicular unit direction at spawn (port direction; flipped per arm).
  perpX: number;
  perpZ: number;
  // Texture U coordinate, frozen at spawn time. Computed from cumulative ship
  // travel distance — independent of array position so pruning the head does
  // not shift remaining points' UVs (which would manifest as the foam texture
  // sliding toward the ship).
  u: number;
  // Recomputed each frame from baseX/Z + perp * arm.side * (initial + drift*age).
  worldX: number;
  worldZ: number;
}

class WakeArm {
  points: ArmPoint[] = [];
  mesh: Mesh | null = null;
  // Tracks vertex count of the live mesh so we can update in place via
  // CreateRibbon's `instance:` param when the point count is unchanged.
  private lastPointCount = 0;

  constructor(private scene: Scene, private side: number) {}

  push(point: ArmPoint): void {
    this.points.push(point);
  }

  // Drop expired points from the head of the array (oldest first).
  pruneOld(nowMs: number): void {
    while (this.points.length > 0 && nowMs - this.points[0].birthTime >= LIFETIME_MS) {
      this.points.shift();
    }
  }

  rebuild(nowMs: number, material: Material): void {
    const n = this.points.length;
    if (n < 2) {
      if (this.mesh) {
        this.mesh.dispose();
        this.mesh = null;
        this.lastPointCount = 0;
      }
      return;
    }

    // Recompute world positions with current outward drift. Drift is clamped
    // to DRIFT_DURATION_MS — beyond that, old points stay fixed in place.
    for (const p of this.points) {
      const age = nowMs - p.birthTime;
      const driftAgeMs = Math.min(age, DRIFT_DURATION_MS);
      const offset = INITIAL_HALF_SPREAD_M + OUTWARD_DRIFT_MPS * (driftAgeMs / 1000);
      p.worldX = p.baseX + p.perpX * this.side * offset;
      p.worldZ = p.baseZ + p.perpZ * this.side * offset;
    }

    // Build two parallel paths (left + right edge of ribbon) and per-vertex UVs.
    const leftPath: Vector3[] = new Array(n);
    const rightPath: Vector3[] = new Array(n);
    const uvs: Vector2[] = new Array(2 * n);

    const halfWFull = RIBBON_WIDTH_M / 2;
    const fadeStartMs = LIFETIME_MS - FINAL_FADE_MS;

    for (let i = 0; i < n; i++) {
      const p = this.points[i];

      // Perpendicular is FROZEN at spawn time (p.perpX / p.perpZ). We do NOT
      // derive it from neighbour positions — that would re-orient the ribbon
      // at old points whenever younger neighbours drift, making the trail
      // appear to be tugged toward the ship.
      const nx = p.perpX * this.side;
      const nz = p.perpZ * this.side;

      // U coord was frozen at spawn time. Stable across pruning of older points.
      const u = p.u;

      // Width taper saturates within DRIFT_DURATION_MS (same as drift) so
      // ribbon edges don't keep migrating after a point's drift is done.
      const taperT = Math.min(1, (nowMs - p.birthTime) / DRIFT_DURATION_MS);
      let halfW = halfWFull * (1 - taperT * WIDTH_TAPER_AT_TAIL);

      // Final fade-out: in the last FINAL_FADE_MS of life, shrink width to 0.
      // Short enough that the inward edge migration reads as natural dissipation.
      const age = nowMs - p.birthTime;
      if (age > fadeStartMs) {
        const fadeT = Math.min(1, (age - fadeStartMs) / FINAL_FADE_MS);
        halfW *= (1 - fadeT);
      }

      leftPath[i] = new Vector3(p.worldX + nx * halfW, Y_ABOVE_WATER, p.worldZ + nz * halfW);
      rightPath[i] = new Vector3(p.worldX - nx * halfW, Y_ABOVE_WATER, p.worldZ - nz * halfW);
      uvs[i] = new Vector2(u, 0);
      uvs[n + i] = new Vector2(u, 1);
    }

    if (this.mesh && this.lastPointCount === n) {
      // In-place update — positions change every frame (drift + final fade).
      MeshBuilder.CreateRibbon("ShipWakeArm", {
        pathArray: [leftPath, rightPath],
        instance: this.mesh,
      }, this.scene);
    } else {
      // Vertex count changed (spawn or prune) — full recreate.
      if (this.mesh) {
        this.mesh.dispose();
      }
      this.mesh = MeshBuilder.CreateRibbon("ShipWakeArm", {
        pathArray: [leftPath, rightPath],
        uvs,
        sideOrientation: Mesh.DOUBLESIDE,
        updatable: true,
      }, this.scene);
      this.mesh.material = material;
      this.mesh.isPickable = false;
      this.lastPointCount = n;
    }
  }

  dispose(): void {
    if (this.mesh) {
      this.mesh.dispose();
      this.mesh = null;
    }
    this.lastPointCount = 0;
    this.points.length = 0;
  }
}

export class ShipWakeRenderer {
  // Single shared material for all ship wakes — foam-wave.png on a transparent,
  // unlit StandardMaterial.
  private static sharedMaterial: Material | null = null;

  private portArm: WakeArm;
  private starboardArm: WakeArm;
  private bowHalo: BowWaveHalo | null = null;
  private lastSpawnPos: Vector3 | null = null;
  // Cumulative ship distance (in texture-tile units) used to assign each
  // spawned point a stable U coordinate. Monotonically increasing.
  private spawnU = 0;

  constructor(private scene: Scene) {
    this.portArm = new WakeArm(scene, +1);
    this.starboardArm = new WakeArm(scene, -1);
  }

  update(nowMs: number, shipPos: Vector3, yaw: number): void {
    if (this.lastSpawnPos === null) {
      this.lastSpawnPos = shipPos.clone();
    } else {
      const dist = Vector3.Distance(this.lastSpawnPos, shipPos);
      if (dist >= SPAWN_INTERVAL_M) {
        this.spawnU += dist / TEXTURE_TILE_M;
        this.spawn(nowMs, shipPos, yaw, this.spawnU);
        this.lastSpawnPos.copyFrom(shipPos);
      }
    }

    this.portArm.pruneOld(nowMs);
    this.starboardArm.pruneOld(nowMs);

    const mat = ShipWakeRenderer.getMaterial(this.scene);
    this.portArm.rebuild(nowMs, mat);
    this.starboardArm.rebuild(nowMs, mat);

    // Bow-wave halo follows the ship every frame. The foam-flow animation
    // happens in the shader (UV displacement); the mesh itself just tracks
    // position and yaw so the elongated halo aligns with hull direction.
    if (!this.bowHalo) {
      this.bowHalo = new BowWaveHalo(this.scene, SHIP_BOW_HALO_LENGTH_M, SHIP_BOW_HALO_WIDTH_M);
    }
    this.bowHalo.setPose(shipPos.x, shipPos.z, yaw);
  }

  dispose(): void {
    this.portArm.dispose();
    this.starboardArm.dispose();
    if (this.bowHalo) {
      this.bowHalo.dispose();
      this.bowHalo = null;
    }
    this.lastSpawnPos = null;
  }

  private spawn(nowMs: number, shipPos: Vector3, yaw: number, u: number): void {
    // Ship facing direction in world: (sin(yaw), 0, cos(yaw)). Stern is behind.
    // Perpendicular ("port") direction: (cos(yaw), 0, -sin(yaw)).
    const fwdX = Math.sin(yaw);
    const fwdZ = Math.cos(yaw);
    const perpX = Math.cos(yaw);
    const perpZ = -Math.sin(yaw);
    const sternX = shipPos.x - fwdX * STERN_OFFSET_M;
    const sternZ = shipPos.z - fwdZ * STERN_OFFSET_M;

    this.portArm.push({
      birthTime: nowMs,
      baseX: sternX, baseZ: sternZ,
      perpX, perpZ,
      u,
      worldX: 0, worldZ: 0,
    });
    this.starboardArm.push({
      birthTime: nowMs,
      baseX: sternX, baseZ: sternZ,
      perpX, perpZ,
      u,
      worldX: 0, worldZ: 0,
    });
  }

  private static getMaterial(scene: Scene): Material {
    if (!ShipWakeRenderer.sharedMaterial) {
      const mat = new StandardMaterial("ShipWakeMat", scene);
      const tex = new Texture("renderer/textures/foam-wave.png", scene);
      tex.hasAlpha = true;
      // Prevent shared cache from being collected when one wake quad is disposed.
      tex.dispose = () => {};
      mat.diffuseTexture = tex;
      mat.useAlphaFromDiffuseTexture = true;
      mat.emissiveColor = new Color3(1, 1, 1);
      mat.diffuseColor = new Color3(0, 0, 0);
      mat.specularColor = new Color3(0, 0, 0);
      mat.disableLighting = true;
      mat.backFaceCulling = false;
      ShipWakeRenderer.sharedMaterial = mat;
    }
    return ShipWakeRenderer.sharedMaterial;
  }
}
