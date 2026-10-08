import {simplexPerlin3D} from "./cloud-shadow";

/**
 * Where the ground shader draws bare earth and stony ground, shared by the shader (ground-material.ts, on the GPU)
 * and the sprite placement (BabylonTerrainTileImpl, on the CPU): a grass tuft must not stand on
 * earth, nor a stone on a lawn. Same pattern as cloud-shadow.ts - the values live here once, and the
 * CPU uses the same simplex noise the shader's SimplexPerlin3DBlock computes.
 *
 * The shader also blends by the height of the earth texture; that moves the edge by a few
 * centimetres and is left out here.
 */
export const GROUND = {
  MACRO_SCALE: 0.025,    // ~40 m: the stony stretches
  MACRO_Z: 3.1,
  PATCH_SCALE: 0.1,      // world metres -> noise units: ~10 m patches
  PATCH_Z: 7.3,          // picks an independent noise field
  FINE_SCALE: 0.4,       // ~2.5 m, frays the patch edges
  FINE_Z: 11.7,
  FINE_WEIGHT: 0.5,
  SLOPE_EDGE0: 0.02,     // 1 - normal.y: from here a slope starts to show earth
  SLOPE_EDGE1: 0.08,
  // Both bare ground kinds mix large and small noise, so there are big stretches and, between them,
  // spots of a few metres - stones in the grass, grass in the stones.
  EARTH_SLOPE: 0.65,     // earth weight = slope * EARTH_SLOPE + patch * EARTH_PATCH + fine * EARTH_FINE + EARTH_BIAS
  EARTH_PATCH: 1.2,      // about a tenth of the flat land
  EARTH_FINE: 0.8,
  EARTH_BIAS: -0.15,
  EARTH_EDGE0: 0.2,      // earth fades into grass over a wider band than the other overlays
  EARTH_EDGE1: 0.8,
  STONY_MACRO: 1.6,      // stony weight = macro * STONY_MACRO + patch * STONY_PATCH + fine * STONY_FINE
  STONY_PATCH: 0.7,      //   + slope * STONY_SLOPE + STONY_BIAS: about a fifth of the land,
  STONY_FINE: 0.45,      //   a little more on slopes
  STONY_SLOPE: 0.3,
  STONY_BIAS: -0.05,
  SHARE_EDGE0: 0.35,     // weight -> share of the overlay (overlayByHeight)
  SHARE_EDGE1: 0.65,
};

const smoothstep = (e0: number, e1: number, v: number) => {
  const t = Math.max(0, Math.min(1, (v - e0) / (e1 - e0)));
  return t * t * (3 - 2 * t);
};

/**
 * 0..1, how much of the ground at this spot is bare earth or stones - what a grass tuft must not
 * stand on. `slope` = 1 - normal.y.
 */
export function bareShare(worldX: number, worldZ: number, slope: number): number {
  const fine = simplexPerlin3D(worldX * GROUND.FINE_SCALE, worldZ * GROUND.FINE_SCALE, GROUND.FINE_Z);
  const patch = simplexPerlin3D(worldX * GROUND.PATCH_SCALE, worldZ * GROUND.PATCH_SCALE, GROUND.PATCH_Z);
  const macro = simplexPerlin3D(worldX * GROUND.MACRO_SCALE, worldZ * GROUND.MACRO_SCALE, GROUND.MACRO_Z);
  const slopeStep = smoothstep(GROUND.SLOPE_EDGE0, GROUND.SLOPE_EDGE1, slope);
  const earth = smoothstep(GROUND.EARTH_EDGE0, GROUND.EARTH_EDGE1,
    slopeStep * GROUND.EARTH_SLOPE + patch * GROUND.EARTH_PATCH + fine * GROUND.EARTH_FINE + GROUND.EARTH_BIAS);
  const stony = smoothstep(GROUND.SHARE_EDGE0, GROUND.SHARE_EDGE1, macro * GROUND.STONY_MACRO
    + patch * GROUND.STONY_PATCH + fine * GROUND.STONY_FINE + slopeStep * GROUND.STONY_SLOPE + GROUND.STONY_BIAS);
  // Stones are laid over earth, earth over grass
  return 1 - (1 - earth) * (1 - stony);
}
