/**
 * Cloud shadows, shared by the ground shader (ground-material.ts, on the GPU) and the sprites
 * (SpriteShading, on the CPU). Both must see the same cloud at the same place and time, so the
 * parameters live here and the noise below is a line-by-line port of Babylon's SimplexPerlin3DBlock.
 */
export const CLOUD = {
  SCALE: 0.022,          // world metres -> noise units: clouds 30-60 m across
  SPEED: 0.045,          // noise units per second (~2 m/s)
  EVOLVE_SPEED: 0.02,    // how fast the shapes change
  FINE_SCALE: 2.7,       // second octave
  FINE_WEIGHT: 0.35,
  EDGE0: 0.1,            // noise -> cloud cover
  EDGE1: 0.4,
  SHADOW_STRENGTH: 0.6,  // share of the sun a cloud takes away
};

/** Seconds, the same clock the ground shader gets (performance.now based). */
export function cloudTime(): number {
  return performance.now() / 1000;
}

/** 1 = full sun, 1 - SHADOW_STRENGTH = under a cloud. */
export function cloudSunShade(worldX: number, worldZ: number, time: number): number {
  const drift = time * CLOUD.SPEED;
  const sx = worldX * CLOUD.SCALE + drift, sy = worldZ * CLOUD.SCALE + drift, sz = time * CLOUD.EVOLVE_SPEED;
  const n = simplexPerlin3D(sx, sy, sz)
    + CLOUD.FINE_WEIGHT * simplexPerlin3D(sx * CLOUD.FINE_SCALE, sy * CLOUD.FINE_SCALE, sz * CLOUD.FINE_SCALE);
  const t = Math.max(0, Math.min(1, (n - CLOUD.EDGE0) / (CLOUD.EDGE1 - CLOUD.EDGE0)));
  return 1 - t * t * (3 - 2 * t) * CLOUD.SHADOW_STRENGTH;
}

const fract = (v: number) => v - Math.floor(v);
const L0 = 635.298681, L1 = 682.357502, L2 = 668.926525;
const Z0 = 48.500388, Z1 = 65.294118, Z2 = 63.934599;

/** One corner's contribution: hashed gradient dotted with the offset, times the falloff kernel. */
function corner(pt: number, m0: number, m1: number, m2: number, vx: number, vy: number, vz: number): number {
  const kernel = Math.max(0.5 - (vx * vx + vy * vy + vz * vz), 0);
  if (kernel === 0) {
    return 0;
  }
  const h0 = fract(pt * m0) - 0.49999, h1 = fract(pt * m1) - 0.49999, h2 = fract(pt * m2) - 0.49999;
  const gradient = (h0 * vx + h1 * vy + h2 * vz) / Math.sqrt(h0 * h0 + h1 * h1 + h2 * h2);
  return kernel * kernel * kernel * gradient;
}

/**
 * Port of the GLSL in @babylonjs/core SimplexPerlin3DBlock (roughly -1..1). Scalars only: it runs
 * for every sprite candidate of a tile (ground-rules.ts) and for the cloud shade of the sprites, and
 * the first port's dozen small arrays per call made it twice the cost of the noise it replaced.
 */
export function simplexPerlin3D(x: number, y: number, z: number): number {
  const H = 0.7071067811865476, S = 1 / 3, U = 1 / 6;
  if (x === 0 && y === 0 && z === 0) {
    x = 0.00001;
  }
  const px = x * H, py = y * H, pz = z * H;
  const d = (px + py + pz) * S;
  let pix = Math.floor(px + d), piy = Math.floor(py + d), piz = Math.floor(pz + d);
  const du = (pix + piy + piz) * U;
  const x0x = px - pix + du, x0y = py - piy + du, x0z = pz - piz + du;
  // step(edge, v) = v < edge ? 0 : 1
  const g0 = x0x < x0y ? 0 : 1, g1 = x0y < x0z ? 0 : 1, g2 = x0z < x0x ? 0 : 1;
  const l0 = 1 - g0, l1 = 1 - g1, l2 = 1 - g2;
  const p1x = Math.min(g0, l2), p1y = Math.min(g1, l0), p1z = Math.min(g2, l1);
  const p2x = Math.max(g0, l2), p2y = Math.max(g1, l0), p2z = Math.max(g2, l1);
  pix -= Math.floor(pix / 69) * 69;
  piy -= Math.floor(piy / 69) * 69;
  piz -= Math.floor(piz / 69) * 69;
  const incx = 67.5 < pix ? 0 : pix + 1, incy = 67.5 < piy ? 0 : piy + 1, incz = 67.5 < piz ? 0 : piz + 1;
  const pt0 = (pix + 50) ** 2, pt1 = (piy + 161) ** 2, pt2 = (incx + 50) ** 2, pt3 = (incy + 161) ** 2;
  const c1 = (pt0 + (pt2 - pt0) * p1x) * (pt1 + (pt3 - pt1) * p1y);
  const c2 = (pt0 + (pt2 - pt0) * p2x) * (pt1 + (pt3 - pt1) * p2y);
  const lo0 = 1 / (L0 + piz * Z0), lo1 = 1 / (L1 + piz * Z1), lo2 = 1 / (L2 + piz * Z2);
  const hi0 = 1 / (L0 + incz * Z0), hi1 = 1 / (L1 + incz * Z1), hi2 = 1 / (L2 + incz * Z2);
  const firstHigh = p1z >= 0.5, secondHigh = p2z >= 0.5;
  const result = corner(pt0 * pt1, lo0, lo1, lo2, x0x, x0y, x0z)
    + corner(c1, firstHigh ? hi0 : lo0, firstHigh ? hi1 : lo1, firstHigh ? hi2 : lo2, x0x - p1x + U, x0y - p1y + U, x0z - p1z + U)
    + corner(c2, secondHigh ? hi0 : lo0, secondHigh ? hi1 : lo1, secondHigh ? hi2 : lo2, x0x - p2x + S, x0y - p2y + S, x0z - p2z + S)
    + corner(pt2 * pt3, hi0, hi1, hi2, x0x - 0.5, x0y - 0.5, x0z - 0.5);
  return result * 37.837227241611314;
}
