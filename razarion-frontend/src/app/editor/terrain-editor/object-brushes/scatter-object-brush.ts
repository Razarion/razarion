import {Vector3} from "@babylonjs/core";
import {AbstractObjectBrush, ObjectBrushContext} from "./abstract-object-brush";

/**
 * Randomly scatters the selected terrain object inside the brush disc while dragging, with per-object
 * scale and yaw jitter. `minDistance` (Poisson-style rejection against already-placed objects) throttles
 * the density so repeated dabs over the same spot don't pile objects on top of each other.
 */
export class ScatterObjectBrush extends AbstractObjectBrush {
  /** Placement attempts per dab (each may be rejected by ground / slope / min-distance). */
  countPerDab = 5;
  /** Minimum spacing between objects in meters; 0 disables the spacing check (free overlap). */
  minDistance = 3;
  minScale = 0.8;
  maxScale = 1.2;
  /** Max random yaw in degrees (±). 0 = no rotation, 180 = full random heading. */
  yawJitterDeg = 180;
  /** When on, a candidate is only placed if its local slope passes the threshold test (see slopeAbove). */
  slopeFilterEnabled = false;
  /** Slope threshold in m/m used by the slope filter. */
  slopeThreshold = 0.3;
  /** true → only place where slope ≥ threshold (steep ground); false → only where slope ≤ threshold (flat). */
  slopeAbove = false;
  /** When on, a candidate is only placed if its ground height passes the water-level test (see waterAbove). */
  waterFilterEnabled = false;
  /** Water level (height in m) used by the water filter; default 0 = the map's water line. */
  waterLevel = 0;
  /** true → only place where height ≥ waterLevel (above water / on land); false → only where height ≤ waterLevel (underwater). */
  waterAbove = true;

  /** Sampling half-step in meters for the local finite-difference slope. */
  private static readonly SLOPE_SAMPLE = 1;

  constructor(ctx: ObjectBrushContext) {
    super(ctx);
  }

  apply(center: Vector3): void {
    // The brush scatters a mix: each placement picks one of the selected object types at random, so a
    // single stroke can drop e.g. several different rocks and bushes.
    const configIds = this.ctx.scatterConfigIds();
    if (configIds.length === 0) {
      return;
    }
    // Existing neighbours within reach seed the spacing check, so dabs respect already-placed objects.
    const placed: { x: number, z: number }[] = this.minDistance > 0 ? this.nearbyPositions(center) : [];

    for (let i = 0; i < this.countPerDab; i++) {
      const p = this.randomPointInDisc(center);
      const placement = this.evaluate(p.x, p.z);
      if (!placement) {
        continue;
      }
      if (this.minDistance > 0 && this.tooClose(p.x, p.z, placed)) {
        continue;
      }
      const configId = configIds[Math.floor(Math.random() * configIds.length)];
      const scale = this.minScale + Math.random() * (this.maxScale - this.minScale);
      this.ctx.createObject(configId, p.x, p.z, placement.height, this.randomYaw(), scale, placement.tiltNormal);
      placed.push(p);
    }
  }

  /**
   * Decides whether a candidate point is valid and returns its placement data, or null to skip it.
   * Places on any ground, optionally gated by the (toggleable) slope and water filters.
   */
  protected evaluate(x: number, z: number): { height: number, tiltNormal?: Vector3 } | null {
    const height = this.ctx.groundHeightAt(x, z);
    if (height === null) {
      return null;
    }
    if (this.slopeFilterEnabled) {
      const slope = this.localSlope(x, z);
      if (slope === null) {
        return null;
      }
      // slopeAbove: keep steep ground (slope ≥ threshold); otherwise keep flat ground (slope ≤ threshold).
      if (this.slopeAbove ? slope < this.slopeThreshold : slope > this.slopeThreshold) {
        return null;
      }
    }
    if (this.waterFilterEnabled) {
      // waterAbove: keep land (height ≥ waterLevel); otherwise keep underwater ground (height ≤ waterLevel).
      if (this.waterAbove ? height < this.waterLevel : height > this.waterLevel) {
        return null;
      }
    }
    return {height};
  }

  /** Local terrain slope (m/m) via a finite-difference gradient, or null if any sample is off-ground. */
  protected localSlope(x: number, z: number): number | null {
    const e = ScatterObjectBrush.SLOPE_SAMPLE;
    const hxPlus = this.ctx.groundHeightAt(x + e, z);
    const hxMinus = this.ctx.groundHeightAt(x - e, z);
    const hzPlus = this.ctx.groundHeightAt(x, z + e);
    const hzMinus = this.ctx.groundHeightAt(x, z - e);
    if (hxPlus === null || hxMinus === null || hzPlus === null || hzMinus === null) {
      return null;
    }
    const dhdx = (hxPlus - hxMinus) / (2 * e);
    const dhdz = (hzPlus - hzMinus) / (2 * e);
    return Math.sqrt(dhdx * dhdx + dhdz * dhdz);
  }

  protected randomYaw(): number {
    if (this.yawJitterDeg <= 0) {
      return 0;
    }
    const max = this.yawJitterDeg * Math.PI / 180;
    return (Math.random() * 2 - 1) * max;
  }

  private nearbyPositions(center: Vector3): { x: number, z: number }[] {
    const reach = this.radius + this.minDistance;
    const reach2 = reach * reach;
    return this.ctx.terrainObjectNodes()
      .filter(node => {
        const dx = node.position.x - center.x;
        const dz = node.position.z - center.z;
        return dx * dx + dz * dz <= reach2;
      })
      .map(node => ({x: node.position.x, z: node.position.z}));
  }

  private tooClose(x: number, z: number, placed: { x: number, z: number }[]): boolean {
    const min2 = this.minDistance * this.minDistance;
    return placed.some(p => {
      const dx = p.x - x;
      const dz = p.z - z;
      return dx * dx + dz * dz < min2;
    });
  }
}
