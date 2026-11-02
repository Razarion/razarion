// Terrain objects for the Noob Island band (noob-band-layout.mjs), run after noob-band-terrain.mjs (2026-10-28).
//
// Deletes every object on the strip (they were placed for the old island and would stand on the new
// spawn points and bot grounds) and every object that the new height map put under water. Then scatters vegetation
// in clumps on the strip and on the land that filled the sea south of region 1 - but nothing on a spawn plot, a bot
// ground, the Razarion field or the shore a builder drives to.
//
// Usage:
//   node noob-band-objects.mjs --before heightmap-before.gz            # dry run: counts only
//   node noob-band-objects.mjs --before heightmap-before.gz --apply    # PUT updateTerrain, then warm-restart
//
// --before is the height map as it was before noob-band-terrain.mjs (that script writes it as a backup). Region 1
// only gets plants where that map had water - the land around it was modelled by hand and stays as it is - and a
// second run knows its own plants there by the same test.
//
// Object types are resolved by internalName on the target server (ids differ between local and PROD).

import { gunzipSync } from "node:zlib";
import { readFileSync } from "node:fs";
import { BAND_X, CELLS, STRIP_END, cellLayout, r1CoastY, r1FillX, stripCoastY } from "./noob-band-layout.mjs";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const PLANET_ID = 117;
const APPLY = process.argv.includes("--apply");
const BEFORE = process.argv.includes("--before") ? process.argv[process.argv.indexOf("--before") + 1] : null;
const W = 1360, H = 840;

const index = (x, y) => 25600 * (Math.floor(y / 160) * 32 + Math.floor(x / 160)) + (y % 160) * 160 + (x % 160);
let rnd = 20261028;
const random = () => { rnd = (rnd * 16807) % 2147483647; return (rnd - 1) / 2147483646; };

async function auth() {
  const res = await fetch(`${BASE_URL}/rest/user/auth`, {
    method: "POST",
    headers: { Authorization: "Basic " + Buffer.from(`${ADMIN}:${PASSWORD}`).toString("base64") },
  });
  if (!res.ok) throw new Error(`auth ${res.status}`);
  return (await res.text()).trim();
}
const token = await auth();

const hmRes = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
let hm = Buffer.from(await hmRes.arrayBuffer());
if (hm.length !== 5120 * 5120 * 2) hm = Buffer.from(gunzipSync(hm));
const height = (x, y) => hm.readUInt16LE(index(Math.floor(x), Math.floor(y)) * 2) * 0.01 - 200;
if (!BEFORE) throw new Error("--before <height map before noob-band-terrain.mjs> is required");
const before = gunzipSync(readFileSync(BEFORE));
const wasWater = (x, y) => before.readUInt16LE(index(Math.floor(x), Math.floor(y)) * 2) * 0.01 - 200 < 0;
const slope = (x, y) => Math.max(Math.abs(height(x + 1, y) - height(x, y)), Math.abs(height(x, y + 1) - height(x, y)));

// Where nothing may stand
const keepOut = [];   // [x, y, r]
for (let c = 0; c < CELLS; c++) {
  const cell = cellLayout(c);
  for (const s of cell.spawns) keepOut.push([s.x, s.y, 16]);
  keepOut.push([cell.garrison.x, cell.garrison.y, 32]);
  for (const e of cell.extractors) keepOut.push([e.x, e.y, 12]);
}
const inField = (x, y) => { for (let c = 0; c < CELLS; c++) { const f = cellLayout(c).resource; if (x > f.x0 - 6 && x < f.x1 + 6 && y > f.y0 - 6 && y < f.y1 + 6) return true; } return false; };
const blockedSpot = (x, y) => inField(x, y) || keepOut.some(([kx, ky, r]) => Math.hypot(x - kx, y - ky) < r);
const onStrip = (x, y) => x <= STRIP_END + 5 && y < stripCoastY(x) + 5;
const rebuilt = (x, y) => x < r1FillX(y) && y < 840;

// Existing objects, from the terrain shape (public, carries the ids)
const shape = await (await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`)).json();
const deleted = [];
for (const tile of shape.nativeTerrainShapeTiles.flat()) {
  for (const list of tile?.nativeTerrainShapeObjectLists ?? []) {
    for (const p of list.terrainShapeObjectPositions) {
      if (p.x >= W || p.y >= H || !rebuilt(p.x, p.y)) continue;
      if (onStrip(p.x, p.y) || height(p.x, p.y) < 0.1 || wasWater(p.x, p.y)) deleted.push(p.terrainObjectId);
    }
  }
}

// Object types by name
const types = await (await fetch(`${BASE_URL}/rest/editor/terrain-object/read`, { headers: { Authorization: `Bearer ${token}` } })).json();
const typeId = Object.fromEntries(types.map((t) => [t.internalName, t.id]));
const PLANTS = ["Fern", "Fern small", "Palm bush", "Tropical Plant", "Banana plant", "Banana plant small", "Palm plant", "Banana plant big"];
const TREES = ["Palm tree", "Palm tree1", "Palm tree2"];
for (const n of [...PLANTS, ...TREES]) if (!typeId[n]) throw new Error(`terrain object "${n}" missing on ${BASE_URL}`);

// Clumps: a few centres, objects scattered around them
const created = [];
const place = (x, y, name) => created.push({
  id: -987654321, terrainObjectConfigId: typeId[name], position: { x, y },
  scale: { x: 0.8 + 0.5 * random(), y: 0.8 + 0.5 * random(), z: 0.8 + 0.5 * random() }, rotation: { x: 0, y: 0, z: random() * Math.PI * 2 }, offset: null,
});
const okSpot = (x, y) => x > 2 && y > 2 && x < W - 2 && y < H - 2 && rebuilt(x, y) && height(x, y) > 0.25 && slope(x, y) < 0.3 && !blockedSpot(x, y);
function scatter(area, clumpEvery, region) {
  const clumps = Math.round(area / clumpEvery);
  let tries = 0, made = 0;
  while (made < clumps && tries++ < clumps * 40) {
    const cx = random() * W, cy = random() * H;
    if (!region(cx, cy) || !okSpot(cx, cy)) continue;
    made++;
    const n = 4 + Math.floor(random() * 9);
    for (let i = 0; i < n; i++) {
      const a = random() * Math.PI * 2, d = random() * 9;
      const x = cx + Math.cos(a) * d, y = cy + Math.sin(a) * d;
      if (region(x, y) && okSpot(x, y)) place(x, y, random() < 0.12 ? TREES[Math.floor(random() * TREES.length)] : PLANTS[Math.floor(random() * PLANTS.length)]);
    }
  }
}
// The whole strip, but not the last 10 m before the shore, where the builders drive along to build the harbour. A viper
// that jumped back on its way to the extractors was first put down to the clumps; it was the bot-ground plates
// (noob-band-layout.mjs, BOT_PLATEAU), and the clumps come back (local play test, 2026-11-01).
scatter(200000, 700, (x, y) => onStrip(x, y) && y < stripCoastY(x) - 10);
scatter(290000, 1400, (x, y) => !onStrip(x, y) && y >= r1CoastY(x) && x < BAND_X && wasWater(x, y));
console.log(`delete ${deleted.length} objects, create ${created.length}`);

if (APPLY) {
  const up = await fetch(`${BASE_URL}/rest/editor/planeteditor/updateTerrain/${PLANET_ID}`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify({ createdTerrainObjects: created, updatedTerrainObjects: [], deletedTerrainObjectsIds: deleted }),
  });
  if (!up.ok) throw new Error(`updateTerrain ${up.status} ${(await up.text()).slice(0, 300)}`);
  console.log(`done on ${BASE_URL} - now warm-restart the planet (POST /rest/planet-mgmt-controller/restartPlanetWarm)`);
}
