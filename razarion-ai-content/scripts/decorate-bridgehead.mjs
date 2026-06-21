// Decorate Phase-2 Sub-region 1 ("Bridgehead") with terrain objects (vegetation + rocks).
// Reads the PROD planet-117 heightmap (public) to scatter objects on dry land inside the Bridgehead
// polygon, biasing rocks to high/steep/coastal ground and vegetation to low–medium land.
//
//   node decorate-bridgehead.mjs            # DRY-RUN: preview PNG + payload JSON, no write
//   RAZARION_ADMIN=.. RAZARION_PASSWORD=.. node decorate-bridgehead.mjs --apply   # write to PROD
//
// On --apply it maps object types by internalName from the PROD list (IDs differ local<->prod).
// Determinism: seeded PRNG (Date.now/Math.random are fine here, but we seed for repeatable previews).

import { gunzipSync } from "node:zlib";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { writeFileSync } from "node:fs";
import { createRequire } from "node:module";
const require = createRequire(import.meta.url);
const sharp = require("sharp");

const BASE_URL = process.env.RAZARION_BASE_URL || "https://razarion.com";
const ADMIN = process.env.RAZARION_ADMIN_EMAIL || process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_ADMIN_PASSWORD || process.env.RAZARION_PASSWORD || "";
const PLANET_ID = 117;
const APPLY = process.argv.includes("--apply");
const DENSITY = Number(process.env.DENSITY || 1);   // multiplier on placement probability

const NODE = 160, TILE_NODE = NODE * NODE;
const toMeters = (u16) => u16 * 0.01 - 200;

// Seeded PRNG (mulberry32) for repeatable layout
let _s = 0x9e3779b9;
const rnd = () => { _s |= 0; _s = (_s + 0x6D2B79F5) | 0; let t = Math.imul(_s ^ (_s >>> 15), 1 | _s); t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t; return ((t ^ (t >>> 14)) >>> 0) / 4294967296; };

const BRIDGEHEAD = [[810,0],[850,0],[850,1150],[0,1150],[0,756],[117,740],[402,589],[630,350],[804,162]];

// Object palette by internalName (resolved to PROD ids at apply time).
const VEGETATION = ["Fern","Fern small","Palm plant","Tropical Plant","Palm bush","Banana plant","Banana plant small","Palm tree","Palm tree1","Palm tree2","Banana plant big"];
const ROCKS = ["Rock1A","Rock1B","Rock2","Rock3"];

function pointInPoly(x, y, poly) {
  let inside = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [xi, yi] = poly[i], [xj, yj] = poly[j];
    if ((yi > y) !== (yj > y) && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) inside = !inside;
  }
  return inside;
}

function heightToColor(h) {
  if (h < -5) return [10,30,100]; if (h < 0) return [30,80,170]; if (h < 1) return [160,200,100];
  if (h < 3) return [80,170,50]; if (h < 10) return [100,150,40]; if (h < 20) return [120,115,70];
  if (h < 30) return [115,110,85]; if (h < 50) return [125,120,105]; if (h < 80) return [150,148,142];
  return [210,210,215];
}

async function fetchPublic(path) {
  const r = await fetch(`${BASE_URL}${path}`);
  if (!r.ok) throw new Error(`GET ${path}: ${r.status} ${r.statusText}`);
  return r;
}
async function fetchHeightmap() {
  const r = await fetchPublic(`/rest/terrainHeightMap/${PLANET_ID}`);
  let buf = Buffer.from(await r.arrayBuffer());
  if (buf.length >= 2 && buf[0] === 0x1f && buf[1] === 0x8b) buf = gunzipSync(buf);
  return buf;
}

function makeSampler(hmap, side, tileX) {
  const idxOf = (nx, ny) => TILE_NODE * (Math.floor(ny / NODE) * tileX + Math.floor(nx / NODE)) + (ny % NODE) * NODE + (nx % NODE);
  return (x, y) => {
    const nx = Math.max(0, Math.min(side - 1, Math.round(x)));
    const ny = Math.max(0, Math.min(side - 1, Math.round(y)));
    return toMeters(hmap[idxOf(nx, ny)]);
  };
}

function generate(sampleH) {
  const objects = [];   // {x, y, cat, name, scale, yaw}
  const SPACING = 21;   // base grid spacing in m
  const xs = BRIDGEHEAD.map(p => p[0]), ys = BRIDGEHEAD.map(p => p[1]);
  const minX = Math.min(...xs), maxX = Math.max(...xs), minY = Math.min(...ys), maxY = Math.max(...ys);

  for (let gy = minY; gy <= maxY; gy += SPACING) {
    for (let gx = minX; gx <= maxX; gx += SPACING) {
      const x = gx + (rnd() - 0.5) * SPACING * 0.9;
      const y = gy + (rnd() - 0.5) * SPACING * 0.9;
      if (!pointInPoly(x, y, BRIDGEHEAD)) continue;
      const h = sampleH(x, y);
      if (h < 0.5) continue;                       // keep off water / wet shore

      // local roughness (max abs height diff to 4 neighbours at 6 m)
      const slope = Math.max(
        Math.abs(h - sampleH(x + 6, y)), Math.abs(h - sampleH(x - 6, y)),
        Math.abs(h - sampleH(x, y + 6)), Math.abs(h - sampleH(x, y - 6)));

      // placement probability: sparser overall, denser near rough/high ground & coast
      let p = 0.13;
      if (h < 1.6) p = 0.06;                        // open low land = build area: keep airy
      if (h > 6) p += 0.08;
      if (slope > 1.2) p += 0.18;                   // rocky/steep gets busier
      p *= DENSITY;
      if (rnd() > p) continue;

      // category: mostly vegetation; rocks only on steep/high/coastal ground
      const rockBias = 0.06 + (slope > 1.5 ? 0.42 : 0) + (h > 10 ? 0.22 : 0);
      const isRock = rnd() < rockBias;
      const name = isRock ? ROCKS[(rnd() * ROCKS.length) | 0] : VEGETATION[(rnd() * VEGETATION.length) | 0];
      const scale = isRock ? 0.7 + rnd() * 1.1 : 0.8 + rnd() * 0.6;
      objects.push({ x: +x.toFixed(2), y: +y.toFixed(2), cat: isRock ? "rock" : "veg", name, scale: +scale.toFixed(3), yaw: +(rnd() * Math.PI * 2).toFixed(4) });
    }
  }
  return objects;
}

function toPositions(objects, nameToId) {
  return objects.map(o => ({
    id: -987654321,
    terrainObjectConfigId: nameToId ? nameToId[o.name] : o.name,
    position: { x: o.x, y: o.y },
    scale: { x: o.scale, y: o.scale, z: o.scale },
    rotation: { x: 0, y: 0, z: o.yaw },
    offset: null,
  }));
}

async function renderPreview(hmap, side, tileX, objects, outPath) {
  const x1 = -50, y1 = -50, x2 = 1350, y2 = 1350, outW = 1000, outH = 1000;
  const spanX = x2 - x1, spanY = y2 - y1;
  const rgb = new Uint8Array(outW * outH * 3);
  const idxOf = (nx, ny) => TILE_NODE * (Math.floor(ny / NODE) * tileX + Math.floor(nx / NODE)) + (ny % NODE) * NODE + (nx % NODE);
  for (let py = 0; py < outH; py++) {
    const gy = y2 - ((py + 0.5) / outH) * spanY;
    for (let px = 0; px < outW; px++) {
      const gx = x1 + ((px + 0.5) / outW) * spanX;
      let c = [16,18,22];
      const nx = Math.round(gx), ny = Math.round(gy);
      if (nx >= 0 && ny >= 0 && nx < side && ny < side) c = heightToColor(toMeters(hmap[idxOf(nx, ny)]));
      const i = (py * outW + px) * 3; rgb[i] = c[0]; rgb[i+1] = c[1]; rgb[i+2] = c[2];
    }
  }
  // Bridgehead outline
  const gx2 = (gx) => Math.round(((gx - x1) / spanX) * outW), gy2 = (gy) => Math.round(((y2 - gy) / spanY) * outH);
  const put = (px, py, col) => { if (px>=0&&px<outW&&py>=0&&py<outH){const i=(py*outW+px)*3;rgb[i]=col[0];rgb[i+1]=col[1];rgb[i+2]=col[2];} };
  for (let i = 0; i < BRIDGEHEAD.length; i++) { const a = BRIDGEHEAD[i], b = BRIDGEHEAD[(i+1)%BRIDGEHEAD.length];
    const steps = 400; for (let s=0;s<=steps;s++){const t=s/steps; put(gx2(a[0]+(b[0]-a[0])*t), gy2(a[1]+(b[1]-a[1])*t),[120,220,90]);} }
  // object dots
  for (const o of objects) { const cx = gx2(o.x), cy = gy2(o.y); const col = o.cat === "rock" ? [70,70,78] : [25,110,25];
    for (let dy=-1; dy<=1; dy++) for (let dx=-1; dx<=1; dx++) put(cx+dx, cy+dy, col); }
  await sharp(Buffer.from(rgb), { raw: { width: outW, height: outH, channels: 3 } }).png().toFile(outPath);
}

async function authenticate() {
  if (!PASSWORD) throw new Error("RAZARION_PASSWORD not set — required for --apply");
  const cred = Buffer.from(`${ADMIN}:${PASSWORD}`).toString("base64");
  const r = await fetch(`${BASE_URL}/rest/user/auth`, { method: "POST", headers: { Authorization: `Basic ${cred}` } });
  if (!r.ok) throw new Error(`auth failed: ${r.status} ${r.statusText}`);
  return r.text();
}
async function fetchProdNameToId(token) {
  const r = await fetch(`${BASE_URL}/rest/editor/terrain-object/read`, { headers: { Authorization: `Bearer ${token}`, Accept: "application/json" } });
  if (!r.ok) throw new Error(`read terrain objects: ${r.status}`);
  const list = await r.json();
  const map = {};
  for (const c of list) map[c.internalName] = c.id;
  return map;
}

async function main() {
  console.log(`Source PROD heightmap ${BASE_URL}, mode=${APPLY ? "APPLY" : "dry-run"}, density=${DENSITY}`);
  const raw = await fetchHeightmap();
  const len = Math.floor(raw.length / 2), side = Math.round(Math.sqrt(len)), tileX = Math.round(side / NODE);
  const hmap = new Uint16Array(len);
  for (let i = 0; i < len; i++) hmap[i] = raw[i*2] + (raw[i*2+1] << 8);
  const sampleH = makeSampler(hmap, side, tileX);

  const objects = generate(sampleH);
  const veg = objects.filter(o => o.cat === "veg").length, rock = objects.length - veg;
  console.log(`generated ${objects.length} objects: ${veg} vegetation, ${rock} rocks`);

  const previewPath = join(tmpdir(), "razarion-bridgehead-decorate-preview.png");
  await renderPreview(hmap, side, tileX, objects, previewPath);
  console.log(`preview: ${previewPath}`);

  const payloadPath = join(tmpdir(), "razarion-bridgehead-decorate-payload.json");
  writeFileSync(payloadPath, JSON.stringify(objects, null, 0));
  console.log(`objects json: ${payloadPath}`);

  if (!APPLY) { console.log(`\nDRY-RUN — nothing written. Review the preview, then re-run with --apply (+ prod creds).`); return; }

  const token = await authenticate();
  const nameToId = await fetchProdNameToId(token);
  const missing = [...new Set(objects.map(o => o.name))].filter(n => nameToId[n] === undefined);
  if (missing.length) throw new Error(`PROD has no terrain object named: ${missing.join(", ")}`);
  const payload = { createdTerrainObjects: toPositions(objects, nameToId), updatedTerrainObjects: [], deletedTerrainObjectsIds: [] };
  const r = await fetch(`${BASE_URL}/rest/editor/planeteditor/updateTerrain/${PLANET_ID}`, {
    method: "PUT", headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" }, body: JSON.stringify(payload),
  });
  if (!r.ok) throw new Error(`updateTerrain failed: ${r.status} ${await r.text()}`);
  console.log(`\nAPPLIED ${objects.length} terrain objects to PROD. Restart planet warm to see them in-game.`);
}

main().catch((e) => { console.error(e); process.exit(1); });
