// Height map for the Noob Island as a band along the south edge (layout in noob-band-layout.mjs, 2026-10-28).
//
// West of r1FillX and south of region 1 everything is rebuilt:
// - the strip x 0-1235, from the planet edge up to stripCoastY: low land 0.7-1.6 m, a sand band along the shore;
// - the channel up to r1CoastY, down to -5 m;
// - the sea that lay between the old island and region 1 becomes land up to the old shore, so region 1 starts at
//   r1CoastY. Ponds inside region 1 that were never connected to that sea stay as they are.
// North of the filled sea and east of r1FillX nothing changes.
//
// Land must stay passable: a 1 x 1 m cell is blocked once its corners differ by 0.5 m (TerrainAnalyzer). The new
// land is relaxed until it is smooth, and the run reports every blocked cell it left on land it made.
//
// Usage:
//   node noob-band-terrain.mjs                 # dry run: stats + preview PNG, writes a backup of the current map
//   node noob-band-terrain.mjs --out f.gz      # write the gzipped result
//   node noob-band-terrain.mjs --apply         # upload to RAZARION_BASE_URL (default localhost), then warm-restart
//                                              # and regenerate the minimap in the editor (a stored image)
//   node noob-band-terrain.mjs --input f.gz    # start from this map, not the server's: to run the script again on a
//                                              # map it already changed, give it the backup of the first run
//
// RAZARION_BASE_URL / RAZARION_ADMIN / RAZARION_PASSWORD as in raise-subregion1.mjs.

import { gunzipSync, gzipSync, deflateSync } from "node:zlib";
import { readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";
import {
  BAND_X, BOT_GROUND_LAND, CELLS, STRIP_END, botPlateaus, r1CoastY, r1FillX, stripCoastY,
} from "./noob-band-layout.mjs";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const PLANET_ID = 117;
const APPLY = process.argv.includes("--apply");
const OUT = process.argv.includes("--out") ? process.argv[process.argv.indexOf("--out") + 1] : null;
const INPUT = process.argv.includes("--input") ? process.argv[process.argv.indexOf("--input") + 1] : null;
const PREVIEW = process.argv.includes("--preview") ? process.argv[process.argv.indexOf("--preview") + 1] : join(tmpdir(), "noob-band-preview.png");

const H_PRECISION = 0.01, H_MIN = -200;
const toMeters = (u16) => u16 * H_PRECISION + H_MIN;
const toU16 = (m) => Math.max(0, Math.min(65535, Math.round((m - H_MIN) / H_PRECISION)));
const index = (x, y) => 25600 * (Math.floor(y / 160) * 32 + Math.floor(x / 160)) + (y % 160) * 160 + (x % 160);

const W = 1360, H = 840;            // working window, covers the band, the channel and the sea that gets filled
const WALL = 0.5;

// Seeded Perlin noise (same generator as noob-island-hills.mjs, other seed)
const perm = new Uint8Array(512);
{
  const p = new Uint8Array(256);
  for (let i = 0; i < 256; i++) p[i] = i;
  let s = 2810;
  for (let i = 255; i > 0; i--) { s = (s * 16807) % 2147483647; const j = s % (i + 1); [p[i], p[j]] = [p[j], p[i]]; }
  for (let i = 0; i < 512; i++) perm[i] = p[i & 255];
}
const GRAD = [[1, 1], [-1, 1], [1, -1], [-1, -1], [1, 0], [-1, 0], [1, 0], [-1, 0], [0, 1], [0, -1], [0, 1], [0, -1]];
const fade = (t) => t * t * t * (t * (t * 6 - 15) + 10), lerp = (a, b, t) => a + t * (b - a);
function perlin(x, y) {
  const X = Math.floor(x), Y = Math.floor(y), xf = x - X, yf = y - Y, X0 = X & 255, Y0 = Y & 255;
  const g = (a, b, dx, dy) => { const gr = GRAD[perm[(perm[a] + b) & 255] % 12]; return gr[0] * dx + gr[1] * dy; };
  const u = fade(xf), v = fade(yf);
  return lerp(lerp(g(X0, Y0, xf, yf), g(X0 + 1, Y0, xf - 1, yf), u), lerp(g(X0, Y0 + 1, xf, yf - 1), g(X0 + 1, Y0 + 1, xf - 1, yf - 1), u), v);
}
function fbm(x, y, octaves) {
  let v = 0, a = 1, f = 1, m = 0;
  for (let i = 0; i < octaves; i++) { v += perlin(x * f, y * f) * a; m += a; a *= 0.5; f *= 2; }
  return v / m;
}

async function auth() {
  const res = await fetch(`${BASE_URL}/rest/user/auth`, {
    method: "POST",
    headers: { Authorization: "Basic " + Buffer.from(`${ADMIN}:${PASSWORD}`).toString("base64") },
  });
  if (!res.ok) throw new Error(`auth ${res.status}`);
  return (await res.text()).trim();
}

let body;
if (INPUT) {
  body = readFileSync(INPUT);
} else {
  const res = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
  if (!res.ok) throw new Error(`download ${res.status}`);
  body = Buffer.from(await res.arrayBuffer());
}
const heightmap = body.length === 5120 * 5120 * 2 ? body : Buffer.from(gunzipSync(body));
const backup = join(tmpdir(), `heightmap-${PLANET_ID}-before-noob-band-${Date.now()}.gz`);
writeFileSync(backup, gzipSync(heightmap));

const old = new Float64Array(W * H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) old[y * W + x] = toMeters(heightmap.readUInt16LE(index(x, y) * 2));

// --- What each node becomes: 0 untouched, 1 strip land, 2 channel water, 3 filled land, 4 region-1 old land
const UNTOUCHED = 0, STRIP = 1, CHANNEL = 2, FILLED = 3;
const kind = new Uint8Array(W * H);
const stripLand = (x, y) => {
  if (x > STRIP_END) return false;
  let top = stripCoastY(x);
  const round = 45; // the east end of the strip rounds off
  if (x > STRIP_END - round) top *= Math.sqrt(Math.max(0, 1 - ((x - (STRIP_END - round)) / round) ** 2));
  return y <= top;
};
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  if (x >= r1FillX(y)) continue;
  if (y < r1CoastY(x)) kind[y * W + x] = stripLand(x, y) ? STRIP : CHANNEL;
}
// The sea south of region 1: water north of r1CoastY that is connected to the channel. Flood fill from the coast line.
{
  const stack = [];
  for (let x = 0; x < W; x++) {
    const y = Math.ceil(r1CoastY(x));
    if (x < r1FillX(y) && y < H && old[y * W + x] < 0.3) stack.push(y * W + x);
  }
  while (stack.length) {
    const k = stack.pop();
    if (kind[k] !== UNTOUCHED) continue;
    const x = k % W, y = (k / W) | 0;
    if (x >= r1FillX(y) || y < r1CoastY(x) || old[k] >= 0.3) continue;
    kind[k] = FILLED;
    if (x > 0) stack.push(k - 1);
    if (x < W - 1) stack.push(k + 1);
    if (y > 0) stack.push(k - W);
    if (y < H - 1) stack.push(k + W);
  }
}

// Land/water mask of the result, then the distance of every node to the other side (two-pass chamfer)
const isLand = new Uint8Array(W * H);
for (let k = 0; k < W * H; k++) isLand[k] = kind[k] === STRIP || kind[k] === FILLED || (kind[k] === UNTOUCHED && old[k] >= 0) ? 1 : 0;
function distanceTo(target) {
  const d = new Float64Array(W * H).fill(1e9);
  for (let k = 0; k < W * H; k++) if (target(k)) d[k] = 0;
  const D2 = Math.SQRT2;
  for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
    const k = y * W + x; let v = d[k];
    if (x > 0) v = Math.min(v, d[k - 1] + 1);
    if (y > 0) { v = Math.min(v, d[k - W] + 1); if (x > 0) v = Math.min(v, d[k - W - 1] + D2); if (x < W - 1) v = Math.min(v, d[k - W + 1] + D2); }
    d[k] = v;
  }
  for (let y = H - 1; y >= 0; y--) for (let x = W - 1; x >= 0; x--) {
    const k = y * W + x; let v = d[k];
    if (x < W - 1) v = Math.min(v, d[k + 1] + 1);
    if (y < H - 1) { v = Math.min(v, d[k + W] + 1); if (x < W - 1) v = Math.min(v, d[k + W + 1] + D2); if (x > 0) v = Math.min(v, d[k + W - 1] + D2); }
    d[k] = v;
  }
  return d;
}
const toWater = distanceTo((k) => !isLand[k]);
const toLand = distanceTo((k) => isLand[k]);

// New heights
const out = Float64Array.from(old);
const changed = new Uint8Array(W * H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  const k = y * W + x;
  if (kind[k] === UNTOUCHED) continue;
  let h;
  if (isLand[k]) {
    const land = 1.15 + 0.45 * fbm(x / 70, y / 70, 3);          // low, gently rolling
    h = Math.min(land, 0.06 + 0.07 * toWater[k]);                 // sand band along the shore
    if (kind[k] === FILLED) h = Math.min(h, 0.06 + 0.05 * toWater[k]);
  } else {
    h = -(0.06 + Math.min(4.9, 0.14 * toLand[k]));   // shallow at the shore, else the shore cells block
  }
  out[k] = h;
  changed[k] = 1;
}
// Where the channel cuts into old land, that land would end in a cliff: give it the same shore slope. Measured to
// the channel only - ponds further inland keep their banks.
const toChannel = distanceTo((k) => kind[k] === CHANNEL && !isLand[k]);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  const k = y * W + x;
  if (kind[k] !== UNTOUCHED || !isLand[k] || x >= r1FillX(y) || toChannel[k] > 60) continue;
  const shore = 0.06 + 0.07 * toChannel[k];
  if (old[k] > shore) { out[k] = shore; changed[k] = 1; }
}
// Smooth the new land into the old one: a ring of 6 m of old land around what was filled may move too.
const ring = distanceTo((k) => changed[k] === 1 && isLand[k] === 1);
const movable = new Uint8Array(W * H);
for (let k = 0; k < W * H; k++) movable[k] = isLand[k] && (changed[k] || (ring[k] <= 6 && old[k] < 3)) ? 1 : 0;
for (let it = 0; it < 60; it++) {
  for (let y = 1; y < H - 1; y++) for (let x = 1; x < W - 1; x++) {
    const k = y * W + x;
    if (!movable[k] || toWater[k] < 3) continue;  // the shore keeps its sand band
    out[k] = 0.5 * out[k] + 0.125 * (out[k - 1] + out[k + 1] + out[k - W] + out[k + W]);
  }
}
for (let k = 0; k < W * H; k++) if (movable[k]) changed[k] = 1;
// Bot grounds: the land under them is levelled to BOT_GROUND_LAND, with a ramp of 12 m around (see noob-band-layout.mjs).
for (let c = 0; c < CELLS; c++) for (const [px, py, half] of botPlateaus(c)) {
  for (let y = Math.max(0, py - half - 12); y <= py + half + 12; y++) for (let x = Math.max(0, px - half - 12); x <= px + half + 12; x++) {
    const k = y * W + x; if (!isLand[k] || kind[k] !== STRIP) continue;
    const outside = Math.max(Math.abs(x - px) - half, Math.abs(y - py) - half, 0);
    const w = Math.max(0, 1 - outside / 12);
    out[k] = out[k] + (BOT_GROUND_LAND - out[k]) * w * Math.min(1, toWater[k] / 10);
    changed[k] = 1;
  }
}

// Check: blocked cells on land this script made or moved
let blocked = 0, landCells = 0; const blockedAt = [];
for (let y = 0; y < H - 1; y++) for (let x = 0; x < W - 1; x++) {
  const k = y * W + x; if (!changed[k]) continue;
  const c = [out[k], out[k + 1], out[k + W], out[k + W + 1]];
  if ((c[0] + c[1] + c[2] + c[3]) / 4 < 0) continue;
  landCells++;
  if (Math.max(...c) - Math.min(...c) >= WALL) { blocked++; if (blockedAt.length < 12) blockedAt.push(`${x}/${y}`); }
}
let filled = 0, strip = 0; for (let k = 0; k < W * H; k++) { if (kind[k] === FILLED) filled++; if (kind[k] === STRIP) strip++; }
console.log(`strip ${strip} m², filled sea ${filled} m², changed nodes ${changed.reduce((a, b) => a + b, 0)}`);
console.log(`land cells made or moved ${landCells}, blocked ${blocked}${blockedAt.length ? " e.g. " + blockedAt.join(" ") : ""}`);
console.log(`backup of the input: ${backup}`);

// Preview: old | new, side by side
{
  const S = 2, PW = W / S, PH = H / S, img = Buffer.alloc((PW * 2 + 10) * PH * 3);
  const color = (h) => h < 0 ? [30, 90 - Math.min(60, -h * 10), 170 - Math.min(80, -h * 12)] : [150 + Math.min(80, h * 6), 165 + Math.min(40, h * 3), 95 + Math.min(80, h * 4)];
  for (let py = 0; py < PH; py++) for (let px = 0; px < PW; px++) {
    const k = ((PH - 1 - py) * S) * W + px * S;
    for (const [src, off] of [[old, 0], [out, PW + 10]]) {
      const c = color(src[k]); const o = (py * (PW * 2 + 10) + px + off) * 3; img[o] = c[0]; img[o + 1] = c[1]; img[o + 2] = c[2];
    }
  }
  const crcT = []; for (let i = 0; i < 256; i++) { let c = i; for (let j = 0; j < 8; j++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1; crcT[i] = c >>> 0; }
  const crc = (b) => { let c = 0xffffffff; for (const v of b) c = crcT[(c ^ v) & 255] ^ (c >>> 8); return (c ^ 0xffffffff) >>> 0; };
  const chunk = (t, d) => { const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const td = Buffer.concat([Buffer.from(t), d]); const c = Buffer.alloc(4); c.writeUInt32BE(crc(td)); return Buffer.concat([l, td, c]); };
  const RW = PW * 2 + 10, raw = Buffer.alloc((RW * 3 + 1) * PH);
  for (let y = 0; y < PH; y++) img.copy(raw, y * (RW * 3 + 1) + 1, y * RW * 3, (y + 1) * RW * 3);
  const ih = Buffer.alloc(13); ih.writeUInt32BE(RW, 0); ih.writeUInt32BE(PH, 4); ih[8] = 8; ih[9] = 2;
  writeFileSync(PREVIEW, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk("IHDR", ih), chunk("IDAT", deflateSync(raw)), chunk("IEND", Buffer.alloc(0))]));
  console.log(`preview (old | new): ${PREVIEW}`);
}

for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) if (changed[y * W + x]) heightmap.writeUInt16LE(toU16(out[y * W + x]), index(x, y) * 2);
if (OUT) { writeFileSync(OUT, gzipSync(heightmap)); console.log(`written ${OUT}`); }
if (APPLY) {
  const token = await auth();
  const up = await fetch(`${BASE_URL}/rest/editor/planeteditor/updateCompressedHeightMap/${PLANET_ID}`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/octet-stream" },
    body: gzipSync(heightmap),
  });
  if (!up.ok) throw new Error(`upload ${up.status}`);
  console.log(`uploaded to ${BASE_URL} - now warm-restart the planet (POST /rest/planet-mgmt-controller/restartPlanetWarm) and regenerate the minimap in the editor`);
}
