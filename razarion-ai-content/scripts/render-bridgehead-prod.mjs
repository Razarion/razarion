// Render the Bridgehead (Phase-2 Sub-region 1) reference images from the PROD planet-117 heightmap.
// The terrain heightmap endpoint is public, so no auth is needed. Mirrors region_map_image's terrain
// colouring + phase/sub-region outlines. Output: two PNGs (focus + full-planet context).
//
// Usage: node render-bridgehead-prod.mjs
// Override host: RAZARION_BASE_URL=https://razarion.com node render-bridgehead-prod.mjs

import { gunzipSync } from "node:zlib";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { createRequire } from "node:module";
const require = createRequire(import.meta.url);
const sharp = require("sharp");

const BASE_URL = process.env.RAZARION_BASE_URL || "https://razarion.com";
const PLANET_ID = 117;
const NODE = 160, TILE_NODE = NODE * NODE;
const H_PRECISION = 0.01, H_MIN = -200;
const toMeters = (u16) => u16 * H_PRECISION + H_MIN;

function heightToColor(h) {
  if (h < -5) return [10, 30, 100];
  if (h < 0)  return [30, 80, 170];
  if (h < 1)  return [160, 200, 100];
  if (h < 3)  return [80, 170, 50];
  if (h < 10) return [100, 150, 40];
  if (h < 20) return [120, 115, 70];
  if (h < 30) return [115, 110, 85];
  if (h < 50) return [125, 120, 105];
  if (h < 80) return [150, 148, 142];
  return [210, 210, 215];
}

const PHASES = [
  { color: [255, 70, 70],  poly: [[0,0],[810,0],[804,162],[630,350],[402,589],[117,740],[0,756]] },
  { color: [255, 235, 59], poly: [[0,0],[2000,0],[2000,2000],[0,2000]] },
  { color: [80, 220, 255], poly: [[2000,0],[5120,0],[5120,2500],[2000,2500]] },
  { color: [255, 120, 255],poly: [[0,2000],[2000,2000],[2000,2500],[5120,2500],[5120,5120],[0,5120]] },
];
const SUBREGION1 = { color: [120, 220, 90], poly: [[810,0],[850,0],[850,1150],[0,1150],[0,756],[117,740],[402,589],[630,350],[804,162]] };

async function fetchHeightmap() {
  const r = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
  if (!r.ok) throw new Error(`download failed: ${r.status} ${r.statusText}`);
  let buf = Buffer.from(await r.arrayBuffer());
  if (buf.length >= 2 && buf[0] === 0x1f && buf[1] === 0x8b) buf = gunzipSync(buf);
  return buf;
}

function drawLine(rgb, w, h, x0, y0, x1, y1, color, thick) {
  const dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
  const sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
  const half = Math.floor(thick / 2);
  let err = dx - dy, x = x0, y = y0;
  for (;;) {
    for (let oy = -half; oy <= half; oy++) for (let ox = -half; ox <= half; ox++) {
      const px = x + ox, py = y + oy;
      if (px >= 0 && px < w && py >= 0 && py < h) { const i = (py * w + px) * 3; rgb[i] = color[0]; rgb[i+1] = color[1]; rgb[i+2] = color[2]; }
    }
    if (x === x1 && y === y1) break;
    const e2 = 2 * err;
    if (e2 > -dy) { err -= dy; x += sx; }
    if (e2 < dx) { err += dx; y += sy; }
  }
}

async function render(hmap, side, tileX, view) {
  const { x1, y1, x2, y2, width, polys, outPath } = view;
  const spanX = x2 - x1, spanY = y2 - y1;
  const outW = width, outH = Math.round((width * spanY) / spanX);
  const rgb = new Uint8Array(outW * outH * 3);
  const idxOf = (nx, ny) => TILE_NODE * (Math.floor(ny / NODE) * tileX + Math.floor(nx / NODE)) + (ny % NODE) * NODE + (nx % NODE);

  for (let py = 0; py < outH; py++) {
    const gy = y2 - ((py + 0.5) / outH) * spanY; // top = north
    for (let px = 0; px < outW; px++) {
      const gx = x1 + ((px + 0.5) / outW) * spanX;
      let color = [16, 18, 22];
      const nx = Math.round(gx), ny = Math.round(gy);
      if (nx >= 0 && ny >= 0 && nx < side && ny < side) {
        const idx = idxOf(nx, ny);
        if (idx < hmap.length) color = heightToColor(toMeters(hmap[idx]));
      }
      const i = (py * outW + px) * 3; rgb[i] = color[0]; rgb[i+1] = color[1]; rgb[i+2] = color[2];
    }
  }

  const gx2px = (gx) => Math.round(((gx - x1) / spanX) * outW);
  const gy2px = (gy) => Math.round(((y2 - gy) / spanY) * outH);
  const thick = Math.max(2, Math.round(outW / 350));
  for (const p of polys)
    for (let i = 0; i < p.poly.length; i++) {
      const a = p.poly[i], c = p.poly[(i + 1) % p.poly.length];
      drawLine(rgb, outW, outH, gx2px(a[0]), gy2px(a[1]), gx2px(c[0]), gy2px(c[1]), p.color, thick);
    }

  await sharp(Buffer.from(rgb), { raw: { width: outW, height: outH, channels: 3 } }).png().toFile(outPath);
  console.log(`wrote ${outPath}  (${outW}x${outH}, bounds X ${x1}..${x2} Y ${y1}..${y2})`);
}

async function main() {
  console.log(`Fetching PROD heightmap from ${BASE_URL} ...`);
  const raw = await fetchHeightmap();
  const len = Math.floor(raw.length / 2);
  const side = Math.round(Math.sqrt(len));
  const tileX = Math.round(side / NODE);
  console.log(`heightmap: ${len} nodes => ${side}x${side} m, ${tileX}x${tileX} tiles`);
  const hmap = new Uint16Array(len);
  for (let i = 0; i < len; i++) hmap[i] = raw[i * 2] + (raw[i * 2 + 1] << 8);

  await render(hmap, side, tileX, {
    x1: -60, y1: -60, x2: 2060, y2: 2060, width: 1000,
    polys: [PHASES[0], PHASES[1], SUBREGION1],
    outPath: join(tmpdir(), "razarion-bridgehead-prod.png"),
  });
  await render(hmap, side, tileX, {
    x1: 0, y1: 0, x2: 5120, y2: 5120, width: 900,
    polys: [...PHASES, SUBREGION1],
    outPath: join(tmpdir(), "razarion-bridgehead-prod-context.png"),
  });
}

main().catch((e) => { console.error(e); process.exit(1); });
