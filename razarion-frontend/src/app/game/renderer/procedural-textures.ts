import {RawTexture, Texture} from "@babylonjs/core";
import type {Scene} from "@babylonjs/core/scene";

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
function clamp01(v: number): number { return Math.max(0, Math.min(1, v)); }

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

/** Domain-warped fBm, fully tileable */
function warpedFbmTile(x: number, y: number, octaves: number, warpStr: number, warpSeed: number, px: number, py: number): number {
  const wx = perlin2dTile(x + warpSeed, y + warpSeed, px, py) * warpStr;
  const wy = perlin2dTile(x + warpSeed + 50, y + warpSeed + 50, px, py) * warpStr;
  return fbmTile(x + wx, y + wy, octaves, 2.0, 0.5, px, py);
}

// ========== Splatter generation ==========

export const SEED = 77;
const SIZE = 512;
const SCALE = 4;
const WARP_STRENGTH = 1.5;
const SMOOTHSTEP_EDGE0 = 0.495;
const SMOOTHSTEP_EDGE1 = 0.505;

export function splatterValue(nx: number, ny: number): number {
  const large = warpedFbmTile(nx * SCALE, ny * SCALE, 3, WARP_STRENGTH, SEED, SCALE, SCALE);
  const mid = fbmTile(nx * SCALE * 2, ny * SCALE * 2, 2, 2.0, 0.5, SCALE * 2, SCALE * 2) * 0.2;
  const raw = (large + mid) * 0.5 + 0.5;
  // High-frequency noise adds scattered dots along the edge (like natural grass/dirt border)
  const detail = fbmTile(nx * SCALE * 12, ny * SCALE * 12, 3, 2.0, 0.5, SCALE * 12, SCALE * 12) * 0.1;
  // Smooth transition instead of hard binary cutoff
  const v = raw + detail;
  const t = clamp01((v - 0.48) / 0.04); // sharply ramp from 0.48 to 0.52
  return t * t * (3 - 2 * t); // smoothstep
}

/** Procedural splatter mask: white = upper (grass), black = under (sand) */
export function loadSplatterTexture(scene: Scene): Texture {
  initPerm(SEED);

  const data = new Uint8Array(SIZE * SIZE * 4);
  for (let y = 0; y < SIZE; y++) {
    const ny = y / SIZE;
    for (let x = 0; x < SIZE; x++) {
      const v = Math.round(splatterValue(x / SIZE, ny) * 255);
      const i = (y * SIZE + x) * 4;
      data[i] = v;
      data[i + 1] = v;
      data[i + 2] = v;
      data[i + 3] = 255;
    }
  }

  const tex = RawTexture.CreateRGBATexture(data, SIZE, SIZE, scene, true, false);
  tex.wrapU = Texture.WRAP_ADDRESSMODE;
  tex.wrapV = Texture.WRAP_ADDRESSMODE;
  return tex;
}
