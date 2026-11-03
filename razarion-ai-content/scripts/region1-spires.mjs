// Two Spire bots in Phase 2 region 1 instead of one (2026-11-03).
//
// Region 1 is 1260 m wide. Its only Spire bot (quest 403: destroy a Spire) stood at x 528, y 584. Bases are built just
// behind where the builder lands on the south coast (y ~300, tip-region.ts pointInPolygon), anywhere along it. One
// Spire in the middle was 500 m away from a base at either end; two, each in the middle of its half and 170 m inland,
// are at most ~220 m away. The bot of id 1607 moves to the west one, the east one is a copy of it.
//
// The plates sit on levelled land, as on the Noob Island band (noob-band-layout.mjs, BOT_PLATEAU): the old Spire stood
// on uneven ground with ramps, and a plate edge above the land is a wall. Terrain objects on the plates are removed.
//
// Usage:
//   node region1-spires.mjs            # dry run: prints what it would do, writes the bots to %TEMP%/region1-spires/
//   node region1-spires.mjs --apply    # height map, terrain objects and bots to RAZARION_BASE_URL (default localhost),
//                                      # then warm-restart the planet and regenerate the minimap in the editor
//
// RAZARION_BASE_URL / RAZARION_ADMIN / RAZARION_PASSWORD as in raise-subregion1.mjs.

import { gunzipSync, gzipSync } from "node:zlib";
import { mkdirSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const SGE_ID = 3;
const PLANET_ID = 117;
const TEMPLATE_ID = 1607;
const APPLY = process.argv.includes("--apply");
const OUT_DIR = join(tmpdir(), "region1-spires");

/** Middles of the two Spire bots' realms: the middles of the two halves of region 1, ~170 m inland. */
const SPIRES = [{ x: 315, y: 470 }, { x: 945, y: 470 }];
const RAMP = 12;        // the levelled land runs out into the old one over this many metres
const PLATE_ABOVE = 0.2; // plate top above the land under it: at the same height the two flicker (noob-band-layout.mjs)
const WALL = 0.5;

async function auth() {
  const res = await fetch(`${BASE_URL}/rest/user/auth`, {
    method: "POST",
    headers: { Authorization: "Basic " + Buffer.from(`${ADMIN}:${PASSWORD}`).toString("base64") },
  });
  if (!res.ok) throw new Error(`auth ${res.status}`);
  return (await res.text()).trim();
}
const token = await auth();
const get = async (path, auth = true) => {
  const r = await fetch(`${BASE_URL}${path}`, auth ? { headers: { Authorization: `Bearer ${token}` } } : {});
  if (!r.ok) throw new Error(`${path} ${r.status}`);
  return r;
};
mkdirSync(OUT_DIR, { recursive: true });

// --- Height map
const H_PRECISION = 0.01, H_MIN = -200;
const toMeters = (u16) => u16 * H_PRECISION + H_MIN;
const toU16 = (m) => Math.max(0, Math.min(65535, Math.round((m - H_MIN) / H_PRECISION)));
const index = (x, y) => 25600 * (Math.floor(y / 160) * 32 + Math.floor(x / 160)) + (y % 160) * 160 + (x % 160);
const body = Buffer.from(await (await get(`/rest/terrainHeightMap/${PLANET_ID}`, false)).arrayBuffer());
const heightmap = body.length === 5120 * 5120 * 2 ? body : Buffer.from(gunzipSync(body));
const backup = join(OUT_DIR, `heightmap-${PLANET_ID}-before-${Date.now()}.gz`);
writeFileSync(backup, gzipSync(heightmap));
const height = (x, y) => toMeters(heightmap.readUInt16LE(index(x, y) * 2));

// --- Bots
const sge = await (await get(`/rest/editor/server-game-engine/read/${SGE_ID}`)).json();
writeFileSync(join(OUT_DIR, "before.json"), JSON.stringify(sge, null, 1));
const clone = (o) => JSON.parse(JSON.stringify(o));
function stripLines(o) {
  if (o && typeof o === "object") {
    if (o.polygon2D && o.polygon2D.lines) delete o.polygon2D.lines;
    for (const k of Object.keys(o)) stripLines(o[k]);
  }
  return o;
}
function translate(o, dx, dy) {
  if (o && typeof o === "object") {
    if (typeof o.x === "number" && typeof o.y === "number" && Object.keys(o).length <= 3) { o.x += dx; o.y += dy; return o; }
    for (const k of Object.keys(o)) translate(o[k], dx, dy);
  }
  return o;
}
const template = sge.botConfigs.find((b) => b.id === TEMPLATE_ID);
if (!template) throw new Error(`bot ${TEMPLATE_ID} missing`);
const corners = template.realm.polygon2D.corners;
const bbox = (cs) => ({ x0: Math.min(...cs.map((c) => c.x)), y0: Math.min(...cs.map((c) => c.y)), x1: Math.max(...cs.map((c) => c.x)), y1: Math.max(...cs.map((c) => c.y)) });
const tb = bbox(corners);
const centre = { x: (tb.x0 + tb.x1) / 2, y: (tb.y0 + tb.y1) / 2 };
// A second run finds the copy by its name and keeps its id.
const copies = sge.botConfigs.filter((b) => b.id !== TEMPLATE_ID && /^\(Bot2\) Spire/.test(b.internalName));
const others = sge.botConfigs.filter((b) => b.id !== TEMPLATE_ID && !copies.includes(b)).map(stripLines);

const changed = new Set();
const out = new Map();     // node index -> new height
const bots = SPIRES.map((spot, i) => {
  const bot = translate(stripLines(clone(template)), spot.x - centre.x, spot.y - centre.y);
  bot.id = i === 0 ? TEMPLATE_ID : copies[i - 1]?.id ?? null;
  bot.internalName = `(Bot2) Spire ${i + 1}`;
  bot.botGroundSlopeBoxes = [];
  const b = bbox(bot.realm.polygon2D.corners);
  // The land under the realm becomes as high as most of it already is
  const under = [];
  for (let y = b.y0; y <= b.y1; y++) for (let x = b.x0; x <= b.x1; x++) under.push(height(x, y));
  under.sort((p, q) => p - q);
  const land = Math.round(under[under.length >> 1] * 10) / 10;
  if (under[0] < 0.1) throw new Error(`Spire ${i + 1}: water under the realm (${under[0].toFixed(2)} m)`);
  bot.groundBoxHeight = Math.round((land + PLATE_ABOVE) * 10) / 10;
  let moved = 0;
  for (let y = b.y0 - RAMP; y <= b.y1 + RAMP; y++) for (let x = b.x0 - RAMP; x <= b.x1 + RAMP; x++) {
    const outside = Math.max(b.x0 - x, x - b.x1, b.y0 - y, y - b.y1, 0);
    const w = 1 - outside / RAMP;
    const old = height(x, y), h = old + (land - old) * w;
    if (Math.abs(h - old) < 0.005) continue;
    out.set(index(x, y), h); changed.add(`${x},${y}`); moved++;
  }
  console.log(`Spire ${i + 1} (id ${bot.id ?? "new"}): realm ${b.x0}-${b.x1} x ${b.y0}-${b.y1}, land ${under[0].toFixed(2)}-${under[under.length - 1].toFixed(2)} m -> ${land} m, plates ${bot.groundBoxHeight} m, ${moved} nodes moved`);
  return bot;
});
for (const b of copies.slice(SPIRES.length - 1)) console.log(`bot ${b.id} ${b.internalName} is dropped`);

// The old site: a pocket in front of a 7 m cliff, its floor under the plates left with steps of 0.7 m that block (no
// unit ever drove there). The floor is relaxed until it is smooth; the cliff, everything from 1.6 m up, stays.
const after = (x, y) => out.get(index(x, y)) ?? height(x, y);
if (Math.hypot(centre.x - SPIRES[0].x, centre.y - SPIRES[0].y) > 1) {
  const floor = [];
  for (let y = tb.y0 - 12; y <= tb.y1 + 8; y++) for (let x = tb.x0 - 12; x <= tb.x1; x++) if (height(x, y) < 1.6) floor.push([x, y]);
  for (let it = 0; it < 80; it++) for (const [x, y] of floor) {
    out.set(index(x, y), 0.5 * after(x, y) + 0.125 * (after(x - 1, y) + after(x + 1, y) + after(x, y - 1) + after(x, y + 1)));
  }
  for (const [x, y] of floor) changed.add(`${x},${y}`);
  console.log(`old site ${tb.x0}-${tb.x1} x ${tb.y0}-${tb.y1}: floor of ${floor.length} nodes smoothed`);
}

// Check: blocked cells where the land was moved
let blocked = 0;
for (const key of changed) {
  const [x, y] = key.split(",").map(Number);
  const c = [after(x, y), after(x + 1, y), after(x, y + 1), after(x + 1, y + 1)];
  if (Math.max(...c) - Math.min(...c) >= WALL) { blocked++; if (blocked <= 15) process.stdout.write(`${x}/${y}(${Math.min(...c).toFixed(1)}-${Math.max(...c).toFixed(1)}) `); }
}
console.log(`land nodes moved ${changed.size}, blocked cells ${blocked}`);

// --- Terrain objects on the plates (or within 3 m of them)
const shape = await (await get(`/rest/terrainshape/${PLANET_ID}`, false)).json();
const deleted = [];
for (const tile of shape.nativeTerrainShapeTiles.flat()) {
  for (const list of tile?.nativeTerrainShapeObjectLists ?? []) {
    for (const p of list.terrainShapeObjectPositions) {
      if (bots.some((bot) => { const b = bbox(bot.realm.polygon2D.corners); return p.x > b.x0 - 3 && p.x < b.x1 + 3 && p.y > b.y0 - 3 && p.y < b.y1 + 3; })) deleted.push(p.terrainObjectId);
    }
  }
}
console.log(`terrain objects to delete: ${deleted.length}`);

const allBots = [...bots, ...others];
writeFileSync(join(OUT_DIR, "botConfig.json"), JSON.stringify(allBots, null, 1));
console.log(`bots ${allBots.length}, payload and the config before: ${OUT_DIR}, height map backup: ${backup}`);

if (APPLY) {
  for (const [k, h] of out) heightmap.writeUInt16LE(toU16(h), k * 2);
  const up = await fetch(`${BASE_URL}/rest/editor/planeteditor/updateCompressedHeightMap/${PLANET_ID}`, {
    method: "POST", headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/octet-stream" }, body: gzipSync(heightmap),
  });
  if (!up.ok) throw new Error(`height map ${up.status}`);
  if (deleted.length) {
    const t = await fetch(`${BASE_URL}/rest/editor/planeteditor/updateTerrain/${PLANET_ID}`, {
      method: "PUT", headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      body: JSON.stringify({ createdTerrainObjects: [], updatedTerrainObjects: [], deletedTerrainObjectsIds: deleted }),
    });
    if (!t.ok) throw new Error(`updateTerrain ${t.status} ${(await t.text()).slice(0, 300)}`);
  }
  const r = await fetch(`${BASE_URL}/rest/editor/server-game-engine/update/botConfig/${SGE_ID}`, {
    method: "POST", headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" }, body: JSON.stringify(allBots),
  });
  if (!r.ok) throw new Error(`botConfig ${r.status} ${(await r.text()).slice(0, 300)}`);
  console.log(`done on ${BASE_URL} - now warm-restart the planet (POST /rest/planet-mgmt-controller/restartPlanetWarm) and regenerate the minimap in the editor`);
}
