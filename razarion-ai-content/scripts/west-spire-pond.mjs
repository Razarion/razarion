// Table hills and a pond around the west Spire outpost ((Bot2) Spire 1, 287-343 / 442-498), like the east one at
// the pond near 733/533 (2026-11-13, table hills 2026-11-15).
//
// The west outpost stood on a flat meadow at 1 m. Now it is a landscape of mesas: flat tops at a few heights with
// rock faces all round (impassable, drawn as rock by the ground shader) and ramps as the only way up:
// - the plate sits on a mesa at 5 m with a ramp to the south (the landing beach) and one to the north-east;
// - north-west a mesa in two steps, 4.5 and 8 m; west one at 3.5 m, north one at 6 m, east of the pond one at 3 m;
// - a pond in the meadow east of the base, about 44 m across and 3 m deep;
// - calm flat meadow in the valleys between.
// Edges and ramps wander with a fixed noise, so the mesas are no boxes. It stays clear of the landing beach
// (everything above y 405) and at least 20 m from the player bases north-east of it on PROD (394-441 / 553-601,
// 2026-11-13).
//
// A second run sees the plateau and stops; --base <backup> redoes it from the heightmap of before. Goes together
// with razarion-server/docker/spire-outpost.sql (plate height 5.2, the plants that stood where the pond is now,
// rocks).
//
// Usage:
//   node west-spire-pond.mjs                              # dry-run: stats only, writes a backup of the heightmap
//   node west-spire-pond.mjs --apply                      # apply + upload, then warm-restart the planet
//   node west-spire-pond.mjs --apply --base <backup.gz>   # redo from the heightmap before an earlier run
//   --dump <file> writes the result raw (for checks)
//
// RAZARION_BASE_URL / RAZARION_ADMIN / RAZARION_PASSWORD as in raise-subregion1.mjs.

import { gunzipSync, gzipSync } from "node:zlib";
import { readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const PLANET_ID = 117;
const APPLY = process.argv.includes("--apply");
const arg = (name) => process.argv.includes(name) ? process.argv[process.argv.indexOf(name) + 1] : null;

const H_PRECISION = 0.01, H_MIN = -200;
const toMeters = (u16) => u16 * H_PRECISION + H_MIN;
const toU16 = (m) => Math.max(0, Math.min(65535, Math.round((m - H_MIN) / H_PRECISION)));
const index = (x, y) => 25600 * (Math.floor(y / 160) * 32 + Math.floor(x / 160)) + (y % 160) * 160 + (x % 160);
const smooth = (t) => t * t * (3 - 2 * t);
const clamp01 = (t) => Math.min(1, Math.max(0, t));

const AREA = {x0: 0, x1: 490, y0: 405, y1: 660};

// Mesas: a rounded box (x0-x1 / y0-y1, corner r) with a flat top at TOP metres (absolute), the edge moved by the noise.
// The face is EDGE metres wide (steeper than a unit can drive), with a low scree apron of APRON metres at its foot.
// Ramps start at a point of the edge and run out DIR (degrees, 0 = +x, 90 = +y) over LENGTH metres, WIDTH wide,
// down to the ground - or, with TO, to that height (a step from an upper to a lower top).
// Wild mesas: instead of the box, a few oval LOBES (rot in degrees) melted together, a stronger WARP and ROUGH edge
// (times the notches and gullies), and instead of a ramp a natural SLOPE: towards DIR the face widens to WIDTH metres.
const EDGE = 2.5, APRON = 5, WARP = 6;
const MESAS = [
  // 15-30 m of free top around the plate (287-343 / 442-498); east only up to the pond's bank
  {name: "base", x0: 256, x1: 362, y0: 426, y1: 530, r: 16, top: 5.0, flat: {x0: 280, x1: 350, y0: 435, y1: 505}, ramps: [
    {x: 300, y: 426, dir: -90, width: 14, length: 20},
    {x: 357, y: 525, dir: 45, width: 14, length: 20},
  ]},
  // the two west ones 25 m further west than first built: a wide valley between them and the base (2026-11-15)
  {name: "north-west", x0: 155, x1: 243, y0: 527, y1: 605, r: 15, top: 4.5, ramps: [
    {x: 197, y: 527, dir: -90, width: 14, length: 16},
    {x: 243, y: 575, dir: 0, width: 13, length: 16},
    {x: 155, y: 560, dir: 180, width: 13, length: 16},
  ]},
  {name: "north-west top", x0: 170, x1: 217, y0: 547, y1: 592, r: 12, top: 8.0, ramps: [
    {x: 217, y: 562, dir: 0, width: 10, length: 14, to: 4.5},
    {x: 195, y: 547, dir: -90, width: 10, length: 12, to: 4.5},
  ]},
  {name: "west", x0: 140, x1: 211, y0: 435, y1: 492, r: 14, top: 3.5, ramps: [
    {x: 180, y: 435, dir: -90, width: 12, length: 14},
    {x: 211, y: 465, dir: 0, width: 12, length: 14},
    {x: 175, y: 492, dir: 90, width: 12, length: 14},
  ]},
  {name: "north", x0: 295, x1: 365, y0: 552, y1: 606, r: 14, top: 6.0, ramps: [
    {x: 298, y: 556, dir: 225, width: 14, length: 20},
  ]},
  {name: "east", x0: 428, x1: 474, y0: 478, y1: 528, r: 12, top: 3.0, ramps: [
    {x: 428, y: 503, dir: 180, width: 12, length: 12},
  ]},
  // Far west, wilder (2026-11-15): a high one with a lower shelf at its south-west foot, and one further north. All
  // more than 80 m from the PROD player base at 171/540 (2026-11-15).
  {name: "far west", top: 6.5, warp: 11, rough: 1.6, slope: {dir: 200, width: 28}, ramps: [
    {x: 95, y: 498, dir: 80, width: 13, length: 22},
    {x: 85, y: 442, dir: -100, width: 13, length: 22},
  ], lobes: [
    {x: 80, y: 470, rx: 45, ry: 30, rot: 20}, {x: 52, y: 505, rx: 30, ry: 24, rot: -10}, {x: 100, y: 445, rx: 22, ry: 16, rot: -35},
  ]},
  {name: "far west shelf", top: 3.5, warp: 9, rough: 1.4, slope: {dir: 270, width: 16}, ramps: [], lobes: [
    {x: 40, y: 452, rx: 26, ry: 20, rot: 0}, {x: 28, y: 478, rx: 18, ry: 22, rot: 15},
  ]},
  {name: "far north-west", top: 5.0, warp: 11, rough: 1.6, slope: {dir: 0, width: 26}, ramps: [
    {x: 60, y: 590, dir: -90, width: 13, length: 18},
    {x: 62, y: 635, dir: 90, width: 13, length: 16},
  ], lobes: [
    {x: 62, y: 612, rx: 40, ry: 26, rot: -15}, {x: 38, y: 597, rx: 24, ry: 20, rot: -20}, {x: 95, y: 630, rx: 22, ry: 16, rot: 20},
  ]},
];
// The pond: shore wavy with the angle and with noise, 3 m deep in the middle, a shore BANK metres wide.
const POND = {x: 397, y: 444, r: 22, depth: 3.0, bank: 9};

// Smooth value noise, about -1..1, fixed seed: every run gives the same terrain.
function hash(ix, iy) {
  let h = (Math.imul(ix, 374761393) + Math.imul(iy, 668265263)) | 0;
  h = Math.imul(h ^ (h >>> 13), 1274126177);
  return ((h ^ (h >>> 16)) >>> 0) / 4294967295 * 2 - 1;
}
function noise(x, y) {
  const ix = Math.floor(x), iy = Math.floor(y), fx = smooth(x - ix), fy = smooth(y - iy);
  const a = hash(ix, iy), b = hash(ix + 1, iy), c = hash(ix, iy + 1), d = hash(ix + 1, iy + 1);
  return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy;
}
const fbm = (x, y, scale) => 0.65 * noise(x / scale, y / scale) + 0.35 * noise(x / scale * 2.1 + 17, y / scale * 2.1 + 5);

/** Signed distance to the mesa's rounded box, negative inside; the outline wanders with the noise. */
function mesaDistance(m, x, y) {
  let sd;
  if (m.lobes) {
    // The lobes melted together (smooth minimum over 10 m).
    sd = null;
    for (const l of m.lobes) {
      const a = l.rot * Math.PI / 180, dx = x - l.x, dy = y - l.y;
      const u = (dx * Math.cos(a) + dy * Math.sin(a)) / l.rx, v = (-dx * Math.sin(a) + dy * Math.cos(a)) / l.ry;
      const d = (Math.hypot(u, v) - 1) * Math.min(l.rx, l.ry);
      if (sd === null) sd = d;
      else { const k = 10, hm = clamp01(0.5 + 0.5 * (d - sd) / k); sd = d + (sd - d) * hm - k * hm * (1 - hm); }
    }
  } else {
    const cx = (m.x0 + m.x1) / 2, cy = (m.y0 + m.y1) / 2;
    const qx = Math.abs(x - cx) - ((m.x1 - m.x0) / 2 - m.r), qy = Math.abs(y - cy) - ((m.y1 - m.y0) / 2 - m.r);
    sd = Math.hypot(Math.max(qx, 0), Math.max(qy, 0)) + Math.min(Math.max(qx, qy), 0) - m.r;
  }
  const rough = m.rough ?? 1;
  // Big bays and spurs (30 m), smaller notches (9 m), gullies and buttresses (sharp-edged, 7 m) and a rough edge
  // (3.5 m): no line looks drawn with a ruler.
  const gully = 2.2 * (Math.abs(noise(x / 7 + 3, y / 7 - 9)) - 0.35);
  return sd + (m.warp ?? WARP) * fbm(x, y, m.lobes ? 40 : 30) + rough * (3.5 * fbm(x + 11, y - 7, 9) + gully) + 0.8 * noise(x / 3.5, y / 3.5);
}

/** 0..1 up the rock face (t from its foot to its top edge): mostly one steep drop, in places with a ledge halfway. */
function faceProfile(t, ledge) {
  const plain = smooth(t);
  const stepped = t < 0.4 ? 0.5 * smooth(t / 0.4) : t < 0.6 ? 0.5 : 0.5 + 0.5 * smooth((t - 0.6) / 0.4);
  return plain + (stepped - plain) * ledge;
}

/** The height the mesa itself (top, face, scree; no ramps) wants here (absolute), or null. */
function mesaBody(m, x, y, old) {
  let best = null;
  const sd = mesaDistance(m, x, y);
  // The face is 1.5-5 m wide (steep rock to broken rock), twice as wide where it has a ledge.
  let ledge = smooth(clamp01((fbm(x - 130, y + 60, 18) - 0.15) * 3));
  let edge = EDGE * (1 + 0.6 * fbm(x + 200, y, 14)) * (1 + ledge);
  if (m.slope) {
    // Towards the slope's direction (seen from the lobes' middle) the face widens into a drivable slope.
    const cx = m.lobes.reduce((t, l) => t + l.x, 0) / m.lobes.length, cy = m.lobes.reduce((t, l) => t + l.y, 0) / m.lobes.length;
    const toward = Math.cos(Math.atan2(y - cy, x - cx) - m.slope.dir * Math.PI / 180);
    const f = Math.max(0, toward) ** 3;
    edge = edge + (m.slope.width - edge) * f;
    ledge *= 1 - f;
  }
  const apron = APRON * (1 + 0.6 * fbm(x - 70, y + 20, 12));
  // The rim is no level line: up to +-0.5 m wavy in the 8 m behind the face, and it rounds off into the face. Never on
  // the flat part a mesa keeps (the plate).
  const keep = m.flat && x >= m.flat.x0 && x <= m.flat.x1 && y >= m.flat.y0 && y <= m.flat.y1;
  const rimZone = keep ? 0 : 1 - smooth(clamp01((-sd - edge) / 8));
  const top = m.top + rimZone * (0.5 * fbm(x + 500, y - 40, 7) - 0.25);
  if (sd < 0) best = old + (top - old) * faceProfile(clamp01(-sd / Math.max(1.5, edge)), ledge);
  // Scree cones at the foot, up to a fifth of the face high in places, none in others.
  else if (sd < apron) best = old + (top - old) * 0.2 * clamp01(fbm(x, y + 300, 5) + 0.45) * (1 - smooth(sd / apron));
  return best;
}

/**
 * Where along the ramp's line the mesa's flat top really ends (the noisy edge can lie metres before or after the
 * ramp's nominal start). The ramp starts there, so no rock step stands across its top.
 */
function rampStart(m, r) {
  if (r.start !== undefined) return r.start;
  const a = r.dir * Math.PI / 180;
  let start = -30;
  for (let along = -30; along <= 25; along += 0.5) {
    const x = Math.round(r.x + along * Math.cos(a)), y = Math.round(r.y + along * Math.sin(a));
    const body = mesaBody(m, x, y, baseAt(x, y));
    if (body === null || body < m.top - 0.3) break;
    start = along;
  }
  return (r.start = start);
}

/** The height this mesa and its ramps want here (absolute), or null. */
function mesaTarget(m, x, y, old) {
  let best = mesaBody(m, x, y, old);
  for (const r of m.ramps) {
    const a = r.dir * Math.PI / 180, dx = x - r.x, dy = y - r.y;
    const along = dx * Math.cos(a) + dy * Math.sin(a) - rampStart(m, r);
    const f = clamp01(along / r.length);
    // A slight bend, wider towards the foot, uneven sides.
    const across = Math.abs(-dx * Math.sin(a) + dy * Math.cos(a) - 2.5 * Math.sin(Math.PI * f) * (r.dir % 90 ? -1 : 1));
    const half = r.width / 2 * (1 + 0.35 * smooth(clamp01(f * 2 - 1))) * (1 + 0.12 * fbm(x, y, 8));
    const soft = 1.5 + 1.5 * clamp01(0.5 + fbm(x + 40, y, 10));
    if (along < -8 || along > r.length || across > half + soft) continue;
    const bottom = r.to ?? old;
    const h = m.top - (m.top - bottom) * f;
    const side = 1 - smooth(clamp01((across - half) / soft));
    const z = old + Math.max(0, h - old) * side;
    // The ramp cuts through the face: on its middle its own height, towards its sides the higher of both (walls).
    const outer = best === null ? z : Math.max(best, z);
    best = side * (old + Math.max(0, h - old)) + (1 - side) * outer;
  }
  return best;
}

function pondRadius(x, y) {
  const angle = Math.atan2(y - POND.y, x - POND.x);
  return POND.r * (1 + 0.14 * Math.sin(2 * angle + 1.0) + 0.08 * Math.sin(3 * angle + 2.5) + 0.12 * fbm(x, y, 9));
}

/** The pond carved into the ground around it (surface): the bank rises from the waterline to that ground. */
function pondTarget(x, y, surface) {
  const r = Math.hypot(x - POND.x, y - POND.y), R = pondRadius(x, y);
  if (r < R) return -POND.depth * (1 - (r / R) ** 2);       // 0 at the waterline
  const bank = Math.max(POND.bank, 4.5 * surface);
  if (r < R + bank) return Math.min(surface, surface * smooth((r - R) / bank) - 0.01);
  return null;
}

async function auth() {
  const res = await fetch(`${BASE_URL}/rest/user/auth`, {
    method: "POST",
    headers: {Authorization: "Basic " + Buffer.from(`${ADMIN}:${PASSWORD}`).toString("base64")},
  });
  if (!res.ok) throw new Error(`auth ${res.status}`);
  return (await res.text()).trim();
}

const unpack = (buf) => buf.length === 5120 * 5120 * 2 ? buf : Buffer.from(gunzipSync(buf));
const res = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
if (!res.ok) throw new Error(`download ${res.status}`);
// fetch unpacks a Content-Encoding: gzip by itself; the file is the gzipped heightmap either way.
const heightmap = unpack(Buffer.from(await res.arrayBuffer()));
const backup = join(tmpdir(), `heightmap-${PLANET_ID}-before-west-spire-pond-${Date.now()}.gz`);
writeFileSync(backup, gzipSync(heightmap));
// The ground the shapes go onto: the server's, or --base (the heightmap from before an earlier run).
const BASE = arg("--base") ? unpack(readFileSync(arg("--base"))) : heightmap;
const baseAt = (x, y) => toMeters(BASE.readUInt16LE(index(x, y) * 2));
if (baseAt(315, 470) > MESAS[0].top - 0.5) {
  console.log(`the plateau is already there (${baseAt(315, 470).toFixed(2)} m) - nothing to do; --base <backup> redoes it`);
  process.exit(0);
}

let raised = 0, lowered = 0, water = 0;
for (let y = AREA.y0; y <= AREA.y1; y++) {
  for (let x = AREA.x0; x <= AREA.x1; x++) {
    const old = baseAt(x, y);
    let surface = old;
    for (const m of MESAS) {
      const z = mesaTarget(m, x, y, old);
      if (z !== null) surface = Math.max(surface, z);
    }
    const pond = pondTarget(x, y, surface);
    const target = pond !== null ? Math.min(surface, pond) : surface;
    const u16 = toU16(target);
    const before = heightmap.readUInt16LE(index(x, y) * 2);
    if (u16 !== before) {
      heightmap.writeUInt16LE(u16, index(x, y) * 2);
      if (u16 > before) raised++; else lowered++;
    }
    if (target < 0) water++;
  }
}
console.log(`${BASE_URL}: ${raised} nodes raised, ${lowered} lowered, ${water} under water; backup: ${backup}`);
if (arg("--dump")) writeFileSync(arg("--dump"), heightmap);
// Every ramp driven along its middle and a third of its width to each side, top to foot: no node a unit cannot pass
// (the engine blocks a node whose corners differ by 0.5 m or more).
const nowAt = (x, y) => toMeters(heightmap.readUInt16LE(index(x, y) * 2));
const blocked = (x, y) => {
  const c = [nowAt(x, y), nowAt(x + 1, y), nowAt(x + 1, y + 1), nowAt(x, y + 1)];
  return Math.max(...c) - Math.min(...c) >= 0.5;
};
let badRamps = 0;
for (const m of MESAS) {
  for (const r of m.ramps) {
    const a = r.dir * Math.PI / 180, start = rampStart(m, r), bad = [];
    for (let along = -4; along <= r.length + 2; along += 0.5) {
      const f = clamp01(along / r.length), bend = 2.5 * Math.sin(Math.PI * f) * (r.dir % 90 ? -1 : 1);
      for (const off of [-r.width / 3, 0, r.width / 3]) {
        const c = off + bend;
        const x = Math.floor(r.x + (start + along) * Math.cos(a) - c * Math.sin(a));
        const y = Math.floor(r.y + (start + along) * Math.sin(a) + c * Math.cos(a));
        if (blocked(x, y)) bad.push(x + "/" + y);
      }
    }
    if (bad.length) badRamps++;
    console.log(`ramp ${m.name} ${r.x}/${r.y} dir ${r.dir}: starts ${start.toFixed(1)} m along, ${bad.length ? "BLOCKED at " + [...new Set(bad)].slice(0, 6).join(" ") : "clear"}`);
  }
}
// --ramps <file>: the ramps with their real start, for checking terrain objects on them.
if (arg("--ramps")) writeFileSync(arg("--ramps"), JSON.stringify(MESAS.flatMap(m => m.ramps.map(r => ({mesa: m.name, ...r})))));
if (badRamps && APPLY) throw new Error(`${badRamps} ramps blocked - not uploaded`);
if (!APPLY) {
  console.log("dry-run - nothing uploaded (use --apply)");
} else if (raised + lowered === 0) {
  console.log("already there - nothing to upload");
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
