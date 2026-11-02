// Moves the Phase-1 content onto the band (noob-band-layout.mjs) and makes every phase region a rectangle (2026-10-28).
//
// - (Bot1) bots: per cell one Garrison, one Water bot (in the channel) and three Extractors. The bots already in
//   the config are reused for the first cells (their ids are kept), the rest are added. No Phase-1 quest names a bot,
//   they all count item types, so a copy per cell needs no quest change.
// - Start regions: 121 = the strip with the spawn points of all cells in fill order, 122/134/135 = the rectangles of
//   Phase 2. Ids are kept (the update endpoint is id-preserving), so the reposition quests keep pointing at 122.
// - Resource regions: one Razarion field per cell, Phase-2 fields clipped to their region. Box regions likewise.
// - Quests with a place: 386 harbour zone, 486 crossing target, 392 unload, 488 clear the island. 379 destroys the
//   garrison's Tesla.
// Phase-2 bots and quest targets stay where they are; layout B was chosen because none of them has to move.
//
// Usage:
//   node noob-band-config.mjs            # dry run: writes the payloads to %TEMP%/noob-band-config/, posts nothing
//   node noob-band-config.mjs --apply    # posts them to RAZARION_BASE_URL (default localhost), then warm-restart
//
// RAZARION_BASE_URL / RAZARION_ADMIN / RAZARION_PASSWORD as in raise-subregion1.mjs.

import { mkdirSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";
import { BAND_X, BOT_PLATEAU, CELLS, P2_X, P2_Y, R1_Y, R2_Y, STRIP_END, cellLayout, r1CoastY, rect, stripCoastY, WATER_U_PLATES } from "./noob-band-layout.mjs";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const SGE_ID = 3;
const APPLY = process.argv.includes("--apply");
const OUT_DIR = join(tmpdir(), "noob-band-config");

async function auth() {
  const res = await fetch(`${BASE_URL}/rest/user/auth`, {
    method: "POST",
    headers: { Authorization: "Basic " + Buffer.from(`${ADMIN}:${PASSWORD}`).toString("base64") },
  });
  if (!res.ok) throw new Error(`auth ${res.status}`);
  return (await res.text()).trim();
}
const token = await auth();
const sgeRes = await fetch(`${BASE_URL}/rest/editor/server-game-engine/read/${SGE_ID}`, { headers: { Authorization: `Bearer ${token}` } });
if (!sgeRes.ok) throw new Error(`read ${sgeRes.status}`);
const sge = await sgeRes.json();
mkdirSync(OUT_DIR, { recursive: true });
writeFileSync(join(OUT_DIR, "before.json"), JSON.stringify(sge, null, 1));

const clone = (o) => JSON.parse(JSON.stringify(o));
const place = (corners) => ({ polygon2D: { corners }, position: null, radius: null });
// Polygons are stored by their corners only; the lines are derived again on read.
function stripLines(o) {
  if (o && typeof o === "object") {
    if (o.polygon2D && o.polygon2D.lines) delete o.polygon2D.lines;
    for (const k of Object.keys(o)) stripLines(o[k]);
  }
  return o;
}
/** Moves every position of a bot: (p - from) * scale + to. */
function transform(o, from, to, scale = 1) {
  if (o && typeof o === "object") {
    if (typeof o.x === "number" && typeof o.y === "number" && Object.keys(o).length <= 3) {
      o.x = to.x + (o.x - from.x) * scale;
      o.y = to.y + (o.y - from.y) * scale;
      return o;
    }
    for (const k of Object.keys(o)) transform(o[k], from, to, scale);
  }
  return o;
}
const realmCenter = (bot) => {
  if (bot.realm.position) return { ...bot.realm.position };
  const c = bot.realm.polygon2D.corners;
  return { x: c.reduce((a, p) => a + p.x, 0) / c.length, y: c.reduce((a, p) => a + p.y, 0) / c.length };
};

// --- Bots
const byName = (re) => sge.botConfigs.filter((b) => re.test(b.internalName));
const garrisons = byName(/^\(Bot1\) Garrison/);
const waters = byName(/^\(Bot1\) Water/);
const extractors = byName(/^\(Bot1\) Extractor/).sort((a, b) => a.id - b.id);
const others = sge.botConfigs.filter((b) => !b.internalName.startsWith("(Bot1)"));
if (!garrisons.length || !waters.length || !extractors.length) throw new Error("Phase-1 bot templates not found");
const T_GARRISON = stripLines(clone(garrisons[0])), T_WATER = stripLines(clone(waters[0])), T_EXTRACTOR = stripLines(clone(extractors[0]));
const reuse = (list, i) => (i < list.length ? list[i].id : null);

const bots = [];
let extractorNo = 0;
for (let c = 0; c < CELLS; c++) {
  const cell = cellLayout(c);
  const g = transform(clone(T_GARRISON), realmCenter(T_GARRISON), cell.garrison);
  g.id = reuse(garrisons, c); g.internalName = `(Bot1) Garrison ${c + 1}`;
  // Players always come from the north (the base row). Three refineries in a row in the middle of the bot ground, and
  // a Tesla in each north corner of it: quest 379 has the player's three vipers destroy a Tesla - the first target
  // that fights back. Three vipers win, together or one by one, with one or two of them hit. The two Teslas stand
  // 32 m apart, so a viper firing at one is 17.8 m from the other, out of its 15 m: each fights alone. Two Teslas
  // close together killed all three vipers when they came one by one, and a refinery fell to the first salvo like
  // an extractor (fight simulation, 2026-11-01). Two and not one: up to ten bases share a cell, and while the only
  // Tesla was down the tip sent the next player to the one of the neighbouring cell, 300 m away (PROD play test).
  // Corners and row stand on the plates (16-56 m around a centre 2 m south of the garrison point).
  const REFINERIES = [[-9, -6], [0, -6], [9, -6]], TESLAS = [[-16, 14], [16, 14]], TESLA_REPOP_MS = 20000;
  for (const state of g.botEnragementStateConfigs) {
    const teslas = state.botItems.filter((item) => item.baseItemTypeId === 5);
    if (teslas.length === 0) continue;
    state.botItems = state.botItems.filter((item) => item.baseItemTypeId !== 5);
    for (let i = 0; i < TESLAS.length; i++) state.botItems.push({ ...clone(teslas[Math.min(i, teslas.length - 1)]), rePopTime: TESLA_REPOP_MS });
  }
  let refinery = 0, tesla = 0;
  for (const state of g.botEnragementStateConfigs) for (const item of state.botItems) {
    const slot = item.baseItemTypeId === 24 ? REFINERIES[refinery++] : item.baseItemTypeId === 5 ? TESLAS[tesla++] : null;
    if (slot) item.place = { polygon2D: null, position: { x: cell.garrison.x + slot[0], y: cell.garrison.y + slot[1] }, radius: 1 };
    // Spawned, not built: the bot's builder appeared anywhere in the realm, and where it stood on a slot the building
    // was never placed - it was the builder that should have moved away (PROD, Garrison 3, "Can not find free place"
    // every 3 s, 2026-11-01). The builder now waits behind the row and never has to move.
    if (slot) item.createDirectly = true;
    if (item.baseItemTypeId === 13) item.place = { polygon2D: null, position: { x: cell.garrison.x, y: cell.garrison.y - 16 }, radius: 2 };
  }
  bots.push(g);

  // The water bot on its U of plates in the open channel (noob-band-layout.mjs). Its items are written here, not
  // moved from the template: the vipers and the factory are gone - no quest asks for them, and a viper on the plates
  // reached a passing transporter - and the dockyard is spawned on its spot. The builder has nothing to build any
  // more; it stays on the south row because the bot looks better with it.
  // Hydras: each on its own spot, a triangle south of the dockyard, symmetric to the U's axis. A column on the axis
  // kept them furthest from the transporters' ways north (in reach after a detour of 14-20 m), the triangle looks
  // better and is in reach after 6 m (play test, 2026-11-02). Built by the dockyard, so a player who sinks one for
  // quest 388 sees the next one come out of it.
  // Camping: a player's unit parked in front of the bot shot every new dockyard and hydra (PROD, 01.11.: one base 13
  // dockyards in 3 min, another 4; both already at quest 486), and quest 388 was blocked for the cell. Five kills by
  // one base inside the realm switch the bot to "Camping", which has the same items plus three marlins (range 17 >
  // hydra 15) and a badger on each side of the U, so a camper west, east or south of it is in reach too. During
  // "Camping" a transporter is in reach after a detour of 3-4 m (the badgers on the legs), in normal after 6 m. With the realm empty again the bot falls back to normal. A switch removes all
  // items and builds the new state from scratch (BotEnragementState), so both states hold the whole bot.
  // The realm reaches ~20 m beyond every item a camper shoots at - a kill only counts while the killer's base has a
  // unit inside it - and stays clear of the transporters' ways north (x0+107-115, x0+208-212) and of the harbour
  // zone (at most 188), else a passing transporter or a dockyard on the coast would keep the bot in "Camping".
  const wc = cell.water;
  const at = (dx, y, radius = 1) => ({ polygon2D: null, position: { x: wc.x + dx, y }, radius });
  const botItem = (baseItemTypeId, o) => ({ angle: 0, baseItemTypeId, count: 1, createDirectly: false, idleTtl: null, moveRealmIfIdle: false,
    noRebuild: false, noSpawn: false, place: null, placeNearCenter: false, rePopTime: 0, spreadPlace: null, ...o });
  const waterItems = [
    botItem(13, { createDirectly: true, place: at(0, 210, 2) }),                                // builder, south row
    botItem(9, { createDirectly: true, place: at(0, 230, 2), placeNearCenter: true, rePopTime: 30 }), // dockyard
    ...[[-7.5, 223], [7.5, 223], [0, 219]].map(([dx, y]) => botItem(10, { spreadPlace: at(dx, y), rePopTime: 5000 })), // hydras
  ];
  const marlins = [[-6, 233], [6, 233], [0, 236]].map(([dx, y]) => botItem(29, { createDirectly: true, place: at(dx, y), rePopTime: 5000 }));
  // Badgers, one per part of the U, each spawned anywhere on its part: west leg with the south-west corner, south row
  // between the legs (where the builder stands the spot is taken, the engine tries another), east leg with the
  // south-east corner. Inset by the badger's radius, so every spot is on the plates. One item of three on the whole U
  // could put all three on one side.
  const R = 1.2, U_W = wc.x - 20, U_E = wc.x + 20, LEG = 8, U_S = 206, U_N = 238, ROW_N = 214;
  const badgers = [
    rect(U_W + R, U_S + R, U_W + LEG - R, U_N - R),
    rect(U_W + LEG + R, U_S + R, U_E - LEG - R, ROW_N - R),
    rect(U_E - LEG + R, U_S + R, U_E - R, U_N - R),
  ].map((corners) => botItem(20, { createDirectly: true, place: place(corners), rePopTime: 20000 }));
  const w = clone(T_WATER);
  w.id = reuse(waters, c); w.internalName = `(Bot1) Water ${c + 1}`;
  w.realm = place(rect(wc.x - 22, 190, wc.x + 22, 252));
  w.autoAttack = false;
  w.groundBoxPositions = WATER_U_PLATES.map(([dx, y]) => ({ x: wc.x + dx, y }));
  w.botGroundSlopeBoxes = [];
  w.botEnragementStateConfigs = [
    { name: "Normal", enrageUpKills: 5, botItems: waterItems },
    { name: "Camping", enrageUpKills: null, botItems: [...clone(waterItems), ...marlins, ...badgers] },
  ];
  bots.push(w);

  for (const e of cell.extractors) {
    const x = transform(clone(T_EXTRACTOR), realmCenter(T_EXTRACTOR), e);
    x.id = reuse(extractors, extractorNo); x.internalName = `(Bot1) Extractor ${++extractorNo}`;
    bots.push(x);
  }
}
// The plates sit flush on the levelled land (noob-band-terrain.mjs), see BOT_PLATEAU.
for (const b of bots) b.groundBoxHeight = BOT_PLATEAU;
const allBots = [...bots, ...others.map(stripLines)];

// --- Start regions (ids kept)
const sr = (id) => { const r = sge.startRegionConfigs.find((s) => s.id === id); if (!r) throw new Error(`start region ${id}`); return stripLines(clone(r)); };
const spawns = []; for (let c = 0; c < CELLS; c++) spawns.push(...cellLayout(c).spawns);
const startRegions = [
  { ...sr(121), region: place(rect(0, 0, BAND_X, 200)), spawnPositions: spawns, findFreePosition: true, positionRadius: 12, positionMaxItems: 2, noBaseViewPosition: null },
  { ...sr(122), region: place(rect(0, R1_Y, BAND_X, R2_Y)) },
  { ...sr(134), region: place(rect(0, R2_Y, BAND_X, P2_Y)) },
  { ...sr(135), region: place(rect(BAND_X, 0, P2_X, P2_Y)) },
];

// --- Resource regions (not id-preserving, nothing refers to them)
const rr = (re) => { const r = sge.resourceRegionConfigs.find((x) => re.test(x.internalName)); if (!r) throw new Error(`resource region ${re}`); return stripLines(clone(r)); };
const noobField = rr(/Phase 1 (Noob|cell)/);   // second run: the cell fields
const resourceRegions = [];
for (let c = 0; c < CELLS; c++) {
  const f = cellLayout(c).resource;
  resourceRegions.push({ ...clone(noobField), id: null, internalName: `Phase 1 cell ${c + 1}`, count: 8, region: place(rect(f.x0, f.y0, f.x1, f.y1)) });
}
resourceRegions.push({ ...rr(/Phase 2 region 1/), id: null, region: place(rect(0, R1_Y, BAND_X, R2_Y)) });
resourceRegions.push({ ...rr(/Phase 2 region 2/), id: null, region: place(rect(0, R2_Y, BAND_X, P2_Y)) });
resourceRegions.push({ ...rr(/Phase 2 region 3/), id: null, region: place(rect(BAND_X, 0, P2_X, P2_Y)) });

// --- Box regions: region 1, and regions 2+3 together (the L16 box quest is played in region 2)
const br = (re) => { const r = sge.boxRegionConfigs.find((x) => re.test(x.internalName)); if (!r) throw new Error(`box region ${re}`); return stripLines(clone(r)); };
const boxRegions = [
  { ...br(/Phase 2 region 1/), id: null, region: place(rect(0, R1_Y, BAND_X, R2_Y)) },
  { ...br(/Phase 2 region (3|2\+3)/), id: null, internalName: "Phase 2 region 2+3",
    region: place([{ x: 0, y: R2_Y }, { x: BAND_X, y: R2_Y }, { x: BAND_X, y: 0 }, { x: P2_X, y: 0 }, { x: P2_X, y: P2_Y }, { x: 0, y: P2_Y }]) },
];

// --- Quests with a place
const quests = stripLines(clone(sge.serverLevelQuestConfigs));
const setPlace = (questId, corners) => {
  for (const g of quests) for (const q of g.questConfigs) if (q.id === questId) { q.conditionConfig.comparisonConfig.placeConfig = place(corners); return; }
  throw new Error(`quest ${questId}`);
};
// Harbour zone: a strip of water along the coast, 1-14 m off the shore. Water only, so the marker shows nothing a
// dockyard cannot stand on, and close enough for the builder, who builds at most 15 m out onto the water.
const harbour = [];
for (let x = 0; x <= STRIP_END - 50; x += 15) harbour.push({ x, y: Math.round((stripCoastY(x) + 1) * 10) / 10 });
for (let x = Math.floor((STRIP_END - 50) / 15) * 15; x >= 0; x -= 15) harbour.push({ x, y: Math.round((stripCoastY(x) + 14) * 10) / 10 });
setPlace(386, harbour);
// Crossing target: a strip of water 3-13 m off region 1's south coast. The old rectangle began 30-45 m out, and a
// transporter that stopped at its edge had the land out of its 20 m reach for unloading (quest 392, local play
// test, 2026-11-01).
const landing = [];
for (let x = 0; x <= BAND_X - 15; x += 15) landing.push({ x, y: Math.round((r1CoastY(x) - 13) * 10) / 10 });
for (let x = BAND_X - 15; x >= 0; x -= 15) landing.push({ x, y: Math.round((r1CoastY(x) - 3) * 10) / 10 });
setPlace(486, landing);
setPlace(392, rect(0, 280, BAND_X, R2_Y));         // unload the builder anywhere on region 1
setPlace(488, rect(0, 0, BAND_X, 280));            // clear the island: the strip and the channel
// Quest 379: destroy the garrison's Tesla instead of a refinery (see the garrison above)
for (const g of quests) for (const q of g.questConfigs) if (q.id === 379) q.conditionConfig.comparisonConfig.typeCount = { 5: 1 };

const payloads = { botConfig: allBots, startRegionConfig: startRegions, resourceRegionConfig: resourceRegions, boxRegionConfig: boxRegions, serverLevelQuestConfig: quests };
for (const [k, v] of Object.entries(payloads)) writeFileSync(join(OUT_DIR, `${k}.json`), JSON.stringify(v, null, 1));
console.log(`bots ${allBots.length} (Phase 1 ${bots.length}, new ${bots.filter((b) => b.id === null).length}), spawn points ${spawns.length}, resource regions ${resourceRegions.length}, box regions ${boxRegions.length}`);
console.log(`payloads and the config before: ${OUT_DIR}`);

if (APPLY) {
  for (const [k, v] of Object.entries(payloads)) {
    const r = await fetch(`${BASE_URL}/rest/editor/server-game-engine/update/${k}/${SGE_ID}`, {
      method: "POST", headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" }, body: JSON.stringify(v),
    });
    console.log(`${k}: ${r.status}${r.ok ? "" : " " + (await r.text()).slice(0, 300)}`);
    if (!r.ok) throw new Error(`update ${k} failed`);
  }
  console.log(`posted to ${BASE_URL} - now warm-restart the planet (POST /rest/planet-mgmt-controller/restartPlanetWarm)`);
}
