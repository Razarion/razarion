
// ========== Tileable Perlin noise ==========

const perm = new Uint8Array(512);
const grad3 = [
  [1,1,0],[-1,1,0],[1,-1,0],[-1,-1,0],
  [1,0,1],[-1,0,1],[1,0,-1],[-1,0,-1],
  [0,1,1],[0,-1,1],[0,1,-1],[0,-1,-1],
];

export function initPerm(seed: number): void {
  const p = new Uint8Array(256);
  for (let i = 0; i < 256; i++) p[i] = i;
  let s = seed;
  for (let i = 255; i > 0; i--) {
    s = (s * 16807 + 0) % 2147483647;
    const j = s % (i + 1);
    [p[i], p[j]] = [p[j], p[i]];
  }
  for (let i = 0; i < 512; i++) perm[i] = p[i & 255];
}

function fade(t: number): number { return t * t * t * (t * (t * 6 - 15) + 10); }
function lerp(a: number, b: number, t: number): number { return a + t * (b - a); }

/** Wrap-safe modulo (always positive) */
function mod(n: number, m: number): number { return ((n % m) + m) % m; }

/** Perlin noise that tiles seamlessly with period px/py */
function perlin2dTile(x: number, y: number, px: number, py: number): number {
  const X = Math.floor(x), Y = Math.floor(y);
  const xf = x - X, yf = y - Y;
  const X0 = mod(X, px), Y0 = mod(Y, py);
  const X1 = (X0 + 1) % px, Y1 = (Y0 + 1) % py;
  const u = fade(xf), v = fade(yf);
  const aa = perm[(perm[X0 & 255] + Y0) & 255], ab = perm[(perm[X0 & 255] + Y1) & 255];
  const ba = perm[(perm[X1 & 255] + Y0) & 255], bb = perm[(perm[X1 & 255] + Y1) & 255];
  const dot = (g: number, dx: number, dy: number) => {
    const gr = grad3[g % 12]; return gr[0] * dx + gr[1] * dy;
  };
  return lerp(
    lerp(dot(aa, xf, yf), dot(ba, xf - 1, yf), u),
    lerp(dot(ab, xf, yf - 1), dot(bb, xf - 1, yf - 1), u), v
  );
}

/** fBm with tileable noise — each octave's period scales with frequency */
function fbmTile(x: number, y: number, octaves: number, lac: number, pers: number, px: number, py: number): number {
  let value = 0, amp = 1, freq = 1, max = 0;
  for (let i = 0; i < octaves; i++) {
    value += perlin2dTile(x * freq, y * freq, px * freq, py * freq) * amp;
    max += amp; amp *= pers; freq *= lac;
  }
  return value / max;
}

/**
 * World-space fBm for scattering (sprite density, size, tint). Roughly in [-0.4, 0.4], std ~0.17.
 * Callers pass world metres divided by the feature size. The 256-cell period is far beyond a planet
 * at those scales, so no repetition is visible. Needs initPerm() first.
 */
export function scatterNoise(x: number, y: number): number {
  return fbmTile(x, y, 3, 2.0, 0.5, 256, 256);
}

// Seed of the scatter noise (initPerm)
export const SEED = 77;
