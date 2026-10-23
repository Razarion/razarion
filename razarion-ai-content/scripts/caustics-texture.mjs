#!/usr/bin/env node
// Generates the caustics texture: the net of light the waves focus onto a shallow sea floor.
// Deterministic: run it again after changing a value.
//
//   node scripts/caustics-texture.mjs            # writes razarion-frontend/public/renderer/textures/water-caustics.webp
//   node scripts/caustics-texture.mjs --out DIR  # somewhere else, e.g. for a preview
//
// Worley noise, F2 - F1: zero on the borders between the cells, so a thin bright line along every
// border is a net of light veins. Warped a little, so the cells are not convex polygons but rounded
// blobs like real caustics. Tiles seamlessly (the cell lattice repeats every CELLS cells).
// The whitecap overlay (whitecap-material.ts) samples it twice at different scales and drifts, and
// takes the minimum: where two nets cross the light flickers, the way caustics do.

import sharp from 'sharp';
import { mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const SIZE = 512;
const CELLS = 8;            // cells per repeat
const LINE = 0.10;          // width of a vein in F2 - F1 units
const WARP = 0.22;          // in cells

function hash(x, y, seed) {
  let h = Math.imul(x, 0x27d4eb2d) ^ Math.imul(y + 0x3c6ef372, 0x165667b1) ^ Math.imul(seed + 0x5bd1e995, 0x9e3779b1);
  h = Math.imul(h ^ (h >>> 15), 0x85ebca6b);
  h = Math.imul(h ^ (h >>> 13), 0xc2b2ae35);
  h ^= h >>> 16;
  return (h >>> 0) / 4294967296;
}
const wrap = (v, p) => ((v % p) + p) % p;
const smoothstep = (e0, e1, v) => {
  const t = Math.max(0, Math.min(1, (v - e0) / (e1 - e0)));
  return t * t * (3 - 2 * t);
};

/** Smooth value noise repeating every `period` cells, -1..1. */
function valueNoise(x, y, period, seed) {
  const x0 = Math.floor(x), y0 = Math.floor(y);
  const fx = x - x0, fy = y - y0;
  const sx = fx * fx * (3 - 2 * fx), sy = fy * fy * (3 - 2 * fy);
  const v = (i, j) => hash(wrap(x0 + i, period), wrap(y0 + j, period), seed) * 2 - 1;
  const a = v(0, 0) + (v(1, 0) - v(0, 0)) * sx;
  const b = v(0, 1) + (v(1, 1) - v(0, 1)) * sx;
  return a + (b - a) * sy;
}

/** F2 - F1 of the distances to the cell points, on a lattice repeating every CELLS cells. */
function worleyEdge(x, y) {
  const cx = Math.floor(x), cy = Math.floor(y);
  let f1 = Infinity, f2 = Infinity;
  for (let j = -1; j <= 1; j++) {
    for (let i = -1; i <= 1; i++) {
      const gx = cx + i, gy = cy + j;
      const px = gx + 0.15 + 0.7 * hash(wrap(gx, CELLS), wrap(gy, CELLS), 1);
      const py = gy + 0.15 + 0.7 * hash(wrap(gx, CELLS), wrap(gy, CELLS), 2);
      const d = Math.hypot(x - px, y - py);
      if (d < f1) {
        f2 = f1;
        f1 = d;
      } else if (d < f2) {
        f2 = d;
      }
    }
  }
  return f2 - f1;
}

const data = Buffer.alloc(SIZE * SIZE);
for (let py = 0; py < SIZE; py++) {
  for (let px = 0; px < SIZE; px++) {
    const x = (px / SIZE) * CELLS, y = (py / SIZE) * CELLS;
    // Warp with noise of the same repeat, so the texture still tiles
    const wx = x + WARP * valueNoise(x * 2, y * 2, CELLS * 2, 3);
    const wy = y + WARP * valueNoise(x * 2, y * 2, CELLS * 2, 4);
    const edge = worleyEdge(wx, wy);
    // A bright core and a soft glow either side
    const core = 1 - smoothstep(0, LINE, edge);
    const glow = 1 - smoothstep(0, LINE * 3.5, edge);
    data[py * SIZE + px] = Math.round(255 * Math.min(1, core * 0.75 + glow * 0.35));
  }
}

const here = dirname(fileURLToPath(import.meta.url));
const outArg = process.argv.indexOf('--out');
const outDir = outArg > 0 ? process.argv[outArg + 1] : join(here, '..', '..', 'razarion-frontend', 'public', 'renderer', 'textures');
mkdirSync(outDir, { recursive: true });
await sharp(data, { raw: { width: SIZE, height: SIZE, channels: 1 } })
  .webp({ quality: 90 })
  .toFile(join(outDir, 'water-caustics.webp'));
console.log(`water-caustics.webp -> ${outDir}`);
