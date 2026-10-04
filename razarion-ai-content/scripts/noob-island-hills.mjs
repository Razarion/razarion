// Gentle hills on the beginners' island (Phase 1), so its land is no longer one flat sheet (2026-10-04).
//
// Raise only, by up to ~2.1 m in rounded hills about 85 m across (Perlin fBm), under these rules:
// - nothing at or below 0.3 m changes: water and beach stay exactly as they are, and the raise only reaches full
//   strength 30 m inland, so the shore keeps its flat sand band;
// - the start-pad strip along the west and south planet edges, the start pads themselves and the bot grounds stay
//   flat (fade over 6-22 m), and so does a 25 m band along the island's border to Phase 2;
// - no 1x1 m cell changes between passable and blocked (corner range >= 0.5 m, TerrainAnalyzer), and a passable
//   cell stays under 0.45 m (or its old range, if that was higher). Where a cell would break that, the raise is
//   taken back around it until none does.
//
// Not idempotent by nature - a second run would raise the hills again. So --apply only runs on the height map it
// was made for (INPUT_DIGEST, planet 117 as of the 2026-10-04 backup); --force overrides that.
//
// Usage:
//   node noob-island-hills.mjs            # dry-run: stats only, writes a backup of the current heightmap
//   node noob-island-hills.mjs --apply    # apply + upload, then warm-restart the planet
//   node noob-island-hills.mjs --out f.gz # write the gzipped result to a file instead (e.g. to load it into a local
//                                         # database whose admin login differs)
//
// RAZARION_BASE_URL / RAZARION_ADMIN / RAZARION_PASSWORD as in raise-subregion1.mjs.

import { gunzipSync, gzipSync } from "node:zlib";
import { createHash } from "node:crypto";
import { writeFileSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const PLANET_ID = 117;
const APPLY = process.argv.includes("--apply");
const FORCE = process.argv.includes("--force");
const OUT = process.argv.includes("--out") ? process.argv[process.argv.indexOf("--out") + 1] : null;
// SHA-256 of the unpacked height map this was made for (planet 117, backup db_2026_10_04_2)
const INPUT_DIGEST = "992095570ed5b26b9462c9b27759d9f27b9d495017d79b3df4d002f12fef3342";

const H_PRECISION = 0.01, H_MIN = -200;
const toMeters = (u16) => u16 * H_PRECISION + H_MIN;
const toU16 = (m) => Math.max(0, Math.min(65535, Math.round((m - H_MIN) / H_PRECISION)));
const index = (x, y) => 25600 * (Math.floor(y / 160) * 32 + Math.floor(x / 160)) + (y % 160) * 160 + (x % 160);

// Phase 1 boundary (docs/game-design/progression.md)
const ISLAND = [[0, 0], [810, 0], [804, 162], [630, 350], [402, 589], [117, 740], [0, 756]];
// Start pads: ServerTerrainShapeService.generateDecals() - 25 x 25 m from (25 + i*35, 2) and (7, 35 + i*35)
const PADS = [];
for (let i = 0; i < 6; i++) PADS.push([25 + i * 35, 2, 25, 25], [7, 35 + i * 35, 25, 25]);
// Bot grounds on the island (BOT_CONFIG_GROUND_BOX_POSITIONS / _SLOPES as of 2026-10-04, 8 m boxes), [x, y, w, h]
const BOT_GROUNDS = [
  [179, 54, 8, 8], [140, 106, 40, 40], [378, 256, 56, 56], [87, 54, 8, 8], [129, 49, 8, 8],
  [75, 221, 8, 8], [67, 183, 8, 8], [78, 111, 8, 8], [492, 556, 64, 56],
];
const PROTECTED = [...PADS, ...BOT_GROUNDS];

const LOW = 0.3;                         // at or below: untouched
const AMP = 2.5, WAVE = 85;              // hill height (m) and size (m)
const SHORE = 30, PROTECT_IN = 6, PROTECT_OUT = 22, EDGE = 25;
const W = 840, H = 800;                  // working window, covers the island

const ss = (e0, e1, v) => { const t = Math.max(0, Math.min(1, (v - e0) / (e1 - e0))); return t * t * (3 - 2 * t); };

function inside(x, y, poly) {
  let r = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [xi, yi] = poly[i], [xj, yj] = poly[j];
    if ((yi > y) !== (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) r = !r;
  }
  return r;
}

// Distance to the island's border with Phase 2 (the planet edges x = 0 and y = 0 do not count)
function borderDistance(x, y) {
  let best = Infinity;
  for (let i = 1; i < ISLAND.length - 1; i++) {
    const [ax, ay] = ISLAND[i], [bx, by] = ISLAND[i + 1], dx = bx - ax, dy = by - ay, l = dx * dx + dy * dy;
    const t = Math.max(0, Math.min(1, ((x - ax) * dx + (y - ay) * dy) / l));
    best = Math.min(best, Math.hypot(x - ax - t * dx, y - ay - t * dy));
  }
  return best;
}

function protectedDistance(x, y) {
  let best = Infinity;
  for (const [rx, ry, w, h] of PROTECTED) {
    best = Math.min(best, Math.hypot(Math.max(rx - x, 0, x - rx - w), Math.max(ry - y, 0, y - ry - h)));
  }
  return best;
}

// Seeded Perlin noise, fixed seed so every run makes the same hills
const perm = new Uint8Array(512);
{
  const p = new Uint8Array(256);
  for (let i = 0; i < 256; i++) p[i] = i;
  let s = 4711;
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
    headers: {Authorization: "Basic " + Buffer.from(`${ADMIN}:${PASSWORD}`).toString("base64")},
  });
  if (!res.ok) throw new Error(`auth ${res.status}`);
  return (await res.text()).trim();
}

const res = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
if (!res.ok) throw new Error(`download ${res.status}`);
const body = Buffer.from(await res.arrayBuffer());
// fetch unpacks a Content-Encoding: gzip by itself; the file is the gzipped heightmap either way.
const heightmap = body.length === 5120 * 5120 * 2 ? body : Buffer.from(gunzipSync(body));
const inputDigest = createHash("sha256").update(heightmap).digest("hex");
const backup = join(tmpdir(), `heightmap-${PLANET_ID}-before-noob-island-hills-${Date.now()}.gz`);
writeFileSync(backup, gzipSync(heightmap));

const before = new Float64Array(W * H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) before[y * W + x] = toMeters(heightmap.readUInt16LE(index(x, y) * 2));

// Distance to the nearest node at or below LOW (two-pass chamfer)
const shore = new Float64Array(W * H).fill(1e9);
for (let i = 0; i < W * H; i++) if (before[i] <= LOW) shore[i] = 0;
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  const i = y * W + x;
  if (x > 0) shore[i] = Math.min(shore[i], shore[i - 1] + 1);
  if (y > 0) shore[i] = Math.min(shore[i], shore[i - W] + 1);
  if (x > 0 && y > 0) shore[i] = Math.min(shore[i], shore[i - W - 1] + Math.SQRT2);
  if (x < W - 1 && y > 0) shore[i] = Math.min(shore[i], shore[i - W + 1] + Math.SQRT2);
}
for (let y = H - 1; y >= 0; y--) for (let x = W - 1; x >= 0; x--) {
  const i = y * W + x;
  if (x < W - 1) shore[i] = Math.min(shore[i], shore[i + 1] + 1);
  if (y < H - 1) shore[i] = Math.min(shore[i], shore[i + W] + 1);
  if (x < W - 1 && y < H - 1) shore[i] = Math.min(shore[i], shore[i + W + 1] + Math.SQRT2);
  if (x > 0 && y < H - 1) shore[i] = Math.min(shore[i], shore[i + W - 1] + Math.SQRT2);
}

const delta = new Float64Array(W * H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  const i = y * W + x;
  if (before[i] <= LOW || !inside(x, y, ISLAND)) continue;
  const mask = ss(40, 55, x) * ss(32, 47, y)                     // start-pad strip
    * ss(0, SHORE, shore[i])
    * ss(PROTECT_IN, PROTECT_OUT, protectedDistance(x, y))
    * ss(0, EDGE, borderDistance(x, y));
  const hills = ss(-0.05, 0.35, fbm(x / WAVE, y / WAVE, 3));     // rounded hills, flat in between
  const ripple = 0.5 + 0.5 * fbm(x / 22 + 17, y / 22 + 5, 2);   // small undulation on top
  delta[i] = mask * (AMP * hills + 0.35 * ripple);
}

// Same uint16 round trip as the engine, so untouched nodes stay bit-identical
const after = new Float64Array(W * H);
const range = (a, x, y) => {
  const v = [a[y * W + x], a[y * W + x + 1], a[(y + 1) * W + x], a[(y + 1) * W + x + 1]];
  return Math.max(...v) - Math.min(...v);
};
let violations, passes = 0;
do {
  for (let i = 0; i < W * H; i++) after[i] = delta[i] === 0 ? before[i] : toMeters(toU16(before[i] + delta[i]));
  violations = [];
  for (let y = 0; y < H - 1; y++) for (let x = 0; x < W - 1; x++) {
    const r0 = range(before, x, y), r1 = range(after, x, y);
    if ((r0 >= 0.5) !== (r1 >= 0.5) || (r0 < 0.5 && r1 > Math.max(r0, 0.45))) violations.push([x, y]);
  }
  for (const [x, y] of violations) for (let dy = -3; dy <= 4; dy++) for (let dx = -3; dx <= 4; dx++) {
    const xx = x + dx, yy = y + dy;
    if (xx < 0 || yy < 0 || xx >= W || yy >= H) continue;
    const k = yy * W + xx;
    delta[k] *= 0.7;
    if (delta[k] < 0.015) delta[k] = 0;
  }
  passes++;
} while (violations.length && passes < 60);
if (violations.length) throw new Error(`${violations.length} cells still change passability - nothing written`);

let raised = 0, maxRaise = 0;
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  const u16 = toU16(after[y * W + x]);
  if (u16 !== heightmap.readUInt16LE(index(x, y) * 2)) {
    if (before[y * W + x] <= LOW) throw new Error(`node (${x}, ${y}) at or below ${LOW} m would change`);
    heightmap.writeUInt16LE(u16, index(x, y) * 2);
    raised++;
    maxRaise = Math.max(maxRaise, after[y * W + x] - before[y * W + x]);
  }
}
console.log(`${BASE_URL}: input ${inputDigest}`);
console.log(`${raised} nodes raised (max ${maxRaise.toFixed(2)} m) after ${passes} passes; backup: ${backup}`);
if (OUT) {
  writeFileSync(OUT, gzipSync(heightmap));
  console.log(`written to ${OUT} - nothing uploaded`);
} else if (!APPLY) {
  console.log("dry-run - nothing uploaded (use --apply)");
} else if (inputDigest !== INPUT_DIGEST && !FORCE) {
  console.log(`not the height map this was made for (${INPUT_DIGEST}) - already applied? Nothing uploaded (--force overrides)`);
} else {
  const token = await auth();
  const up = await fetch(`${BASE_URL}/rest/editor/planeteditor/updateCompressedHeightMap/${PLANET_ID}`, {
    method: "POST",
    headers: {Authorization: `Bearer ${token}`, "Content-Type": "application/octet-stream"},
    body: gzipSync(heightmap),
  });
  if (!up.ok) throw new Error(`upload ${up.status}`);
  console.log("uploaded - now warm-restart the planet (POST /rest/planet-mgmt-controller/restartPlanetWarm)");
}
