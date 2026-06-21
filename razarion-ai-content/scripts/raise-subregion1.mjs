// Raise Phase-2 Sub-region 1 ("Bridgehead", L10–L13) from water to playable land.
// Only the Sub-region 1 polygon is modified (rectangle X0–850 / Y0–1150 MINUS Noob Island).
// Rolling base relief 0.3–3.5 m; existing higher terrain is preserved (max); edges fade back to the
// original terrain over a blend band so the new land meets surrounding water / the island as a shore.
//
// Usage:
//   node raise-subregion1.mjs            # dry-run: stats only, no upload (writes a backup)
//   node raise-subregion1.mjs --apply    # apply + upload
//
// Endpoints/auth mirror razarion-ai-content/src/index.ts.

import { gunzipSync, gzipSync } from "node:zlib";
import { writeFileSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const PLANET_ID = 117;
const APPLY = process.argv.includes("--apply");

const NODE = 160;                 // nodes per tile axis
const TILE_NODE = NODE * NODE;    // 25600
const BLEND = 80;                 // m: edge fade band (land -> original terrain)
const H_PRECISION = 0.01, H_MIN = -200;  // height_m = u16*0.01 - 200  (0m water = u16 20000)
const toMeters = (u16) => u16 * H_PRECISION + H_MIN;
const toU16 = (m) => Math.max(0, Math.min(65535, Math.round((m - H_MIN) / H_PRECISION)));

// Sub-region 1 polygon (game coords, origin bottom-left, Y up), CCW. See progression.md §4.1.
const POLY = [
  [810, 0], [850, 0], [850, 1150], [0, 1150], [0, 756],
  [117, 740], [402, 589], [630, 350], [804, 162],
];

function pointInPoly(x, y, poly) {
  let inside = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [xi, yi] = poly[i], [xj, yj] = poly[j];
    if ((yi > y) !== (yj > y) && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) inside = !inside;
  }
  return inside;
}

function distToPolyEdge(x, y, poly) {
  let best = Infinity;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [x1, y1] = poly[j], [x2, y2] = poly[i];
    const dx = x2 - x1, dy = y2 - y1;
    const len2 = dx * dx + dy * dy || 1;
    let t = ((x - x1) * dx + (y - y1) * dy) / len2;
    t = Math.max(0, Math.min(1, t));
    const px = x1 + t * dx, py = y1 + t * dy;
    const d = Math.hypot(x - px, y - py);
    if (d < best) best = d;
  }
  return best;
}

const smoothstep = (e0, e1, v) => {
  const t = Math.max(0, Math.min(1, (v - e0) / (e1 - e0)));
  return t * t * (3 - 2 * t);
};

function noise(x, y, s) {
  return Math.sin(x * 0.0131 + s + 1.7) * Math.cos(y * 0.0173 + s * 1.3) * 0.5
    + Math.sin(x * 0.0293 + y * 0.0311 + s * 2.1) * 0.25
    + Math.cos(x * 0.0531 - y * 0.0471 + s * 3.7) * 0.125
    + Math.sin(x * 0.0971 + y * 0.0893 + s * 5.3) * 0.0625;
}
function baseRelief(x, y) {
  return Math.max(0.3, 1.5 + noise(x, y, 42) * 1.2 + noise(x, y, 123) * 0.6 + noise(x, y, 77) * 0.2);
}

async function authenticate() {
  const cred = Buffer.from(`${ADMIN}:${PASSWORD}`).toString("base64");
  const r = await fetch(`${BASE_URL}/rest/user/auth`, { method: "POST", headers: { Authorization: `Basic ${cred}` } });
  if (!r.ok) throw new Error(`auth failed: ${r.status} ${r.statusText}`);
  return r.text();
}
async function fetchHeightmap() {
  const r = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
  if (!r.ok) throw new Error(`download failed: ${r.status}`);
  let buf = Buffer.from(await r.arrayBuffer());
  if (buf.length >= 2 && buf[0] === 0x1f && buf[1] === 0x8b) buf = gunzipSync(buf);
  return buf;
}

async function main() {
  console.log(`Base ${BASE_URL}, planet ${PLANET_ID}, mode=${APPLY ? "APPLY" : "dry-run"}`);
  const raw = await fetchHeightmap();
  const len = Math.floor(raw.length / 2);
  const side = Math.round(Math.sqrt(len));
  const tileXCount = Math.round(side / NODE);
  console.log(`heightmap: ${len} nodes => ${side}x${side} m, ${tileXCount}x${tileXCount} tiles`);

  // backup original
  const backup = join(tmpdir(), `razarion-heightmap-${PLANET_ID}-backup.bin`);
  writeFileSync(backup, raw);
  console.log(`backup written: ${backup}`);

  const hmap = new Uint16Array(len);
  for (let i = 0; i < len; i++) hmap[i] = raw[i * 2] + (raw[i * 2 + 1] << 8);
  const idxOf = (nx, ny) => TILE_NODE * (Math.floor(ny / NODE) * tileXCount + Math.floor(nx / NODE)) + (ny % NODE) * NODE + (nx % NODE);

  // bounding box of the polygon
  const xs = POLY.map((p) => p[0]), ys = POLY.map((p) => p[1]);
  const bx1 = Math.max(0, Math.floor(Math.min(...xs))), bx2 = Math.min(side - 1, Math.ceil(Math.max(...xs)));
  const by1 = Math.max(0, Math.floor(Math.min(...ys))), by2 = Math.min(side - 1, Math.ceil(Math.max(...ys)));

  let touched = 0, wasWater = 0, nowWater = 0, sum = 0, hmin = Infinity, hmax = -Infinity;
  for (let ny = by1; ny <= by2; ny++) {
    for (let nx = bx1; nx <= bx2; nx++) {
      if (!pointInPoly(nx + 0.5, ny + 0.5, POLY)) continue;
      const idx = idxOf(nx, ny);
      if (idx >= len) continue;
      const orig = toMeters(hmap[idx]);
      const taper = smoothstep(0, BLEND, distToPolyEdge(nx + 0.5, ny + 0.5, POLY));
      const land = Math.max(orig, baseRelief(nx, ny));   // fill water, keep existing hills
      const next = orig * (1 - taper) + land * taper;     // fade back to original at edges
      if (orig <= 0) wasWater++;
      if (next <= 0) nowWater++;
      hmap[idx] = toU16(next);
      touched++; sum += next;
      if (next < hmin) hmin = next; if (next > hmax) hmax = next;
    }
  }

  console.log(`\nSub-region 1 nodes modified: ${touched}`);
  console.log(`  water nodes:  before ${wasWater}  ->  after ${nowWater}  (${(100 * nowWater / touched).toFixed(1)}% still water)`);
  console.log(`  height range: ${hmin.toFixed(2)}m .. ${hmax.toFixed(2)}m  (avg ${(sum / touched).toFixed(2)}m)`);

  if (!APPLY) { console.log(`\nDRY-RUN — nothing uploaded. Re-run with --apply to upload.`); return; }

  const out = Buffer.alloc(len * 2);
  for (let i = 0; i < len; i++) { out[i * 2] = hmap[i] & 0xff; out[i * 2 + 1] = (hmap[i] >> 8) & 0xff; }
  const token = await authenticate();
  const r = await fetch(`${BASE_URL}/rest/editor/planeteditor/updateCompressedHeightMap/${PLANET_ID}`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/octet-stream" },
    body: gzipSync(out),
  });
  if (!r.ok) throw new Error(`upload failed: ${r.status} ${await r.text()}`);
  console.log(`\nUPLOADED. Verify with terrain_minimap_image, then restart_planet_warm.`);
}

main().catch((e) => { console.error(e); process.exit(1); });
