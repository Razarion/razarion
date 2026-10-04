// Raise a U-shaped wall of land around the RazCore Platform bot ((Bot1) Water), opening to the south-west toward
// the beginners' island (2026-10-04).
//
// Transporters on quest 486 steer for the nearest point of the target strip; from the east coast that way ran
// through the bot's realm and its Hydra patrol, and all 12 transporters lost in the week before 2026-09-30 sank
// there. Behind a wall that is closed toward the crossing, the way goes round the outside - path search never
// leads into a dead end - while the Hydras of quest 388 stay reachable through the opening. Goes together with
// razarion-server/docker/platform-bot-u-wall.sql (realm = inside of the U, Hydra patrol deeper inside).
//
// Raise only: inside the band the ground becomes 1 m at the edges and 2 m along the middle of the wall, around it
// it fades back into the old ground over 6 m; nothing is lowered, so running it twice changes nothing.
//
// Usage:
//   node platform-u-wall.mjs            # dry-run: stats only, writes a backup of the current heightmap
//   node platform-u-wall.mjs --apply    # apply + upload, then warm-restart the planet
//
// RAZARION_BASE_URL / RAZARION_ADMIN / RAZARION_PASSWORD as in raise-subregion1.mjs.

import { gunzipSync, gzipSync } from "node:zlib";
import { writeFileSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const PLANET_ID = 117;
const APPLY = process.argv.includes("--apply");

const H_PRECISION = 0.01, H_MIN = -200;
const toMeters = (u16) => u16 * H_PRECISION + H_MIN;
const toU16 = (m) => Math.max(0, Math.min(65535, Math.round((m - H_MIN) / H_PRECISION)));
const index = (x, y) => 25600 * (Math.floor(y / 160) * 32 + Math.floor(x / 160)) + (y % 160) * 160 + (x % 160);

// The wall: an arc band around the bot's island, with a 90° opening toward the beginners' island.
const CENTER = [400, 282];
const R_IN = 48, R_OUT = 70, BLEND = 6;
const TOWARD_NOOB = Math.atan2(150 - CENTER[1], 180 - CENTER[0]);   // ≈ -149°
const HALF_OPENING = Math.PI / 4;

function wallPolygon() {
  const from = TOWARD_NOOB + HALF_OPENING, to = TOWARD_NOOB + 2 * Math.PI - HALF_OPENING;
  const arc = (r, a, b, n) => Array.from({length: n + 1}, (_, i) => {
    const t = a + (b - a) * i / n;
    return [CENTER[0] + r * Math.cos(t), CENTER[1] + r * Math.sin(t)];
  });
  return [...arc(R_OUT, from, to, 40), ...arc(R_IN, to, from, 40)];
}

function inside(p, poly) {
  let r = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [xi, yi] = poly[i], [xj, yj] = poly[j];
    if ((yi > p[1]) !== (yj > p[1]) && p[0] < (xj - xi) * (p[1] - yi) / (yj - yi) + xi) r = !r;
  }
  return r;
}

function edgeDistance(p, poly) {
  let best = Infinity;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const a = poly[j], b = poly[i], dx = b[0] - a[0], dy = b[1] - a[1], l = dx * dx + dy * dy;
    const t = l ? Math.max(0, Math.min(1, ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / l)) : 0;
    best = Math.min(best, Math.hypot(p[0] - a[0] - t * dx, p[1] - a[1] - t * dy));
  }
  return best;
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
const backup = join(tmpdir(), `heightmap-${PLANET_ID}-before-platform-u-wall-${Date.now()}.gz`);
writeFileSync(backup, gzipSync(heightmap));

const poly = wallPolygon();
let raised = 0;
for (let y = CENTER[1] - R_OUT - BLEND - 2; y <= CENTER[1] + R_OUT + BLEND + 2; y++) {
  for (let x = CENTER[0] - R_OUT - BLEND - 2; x <= CENTER[0] + R_OUT + BLEND + 2; x++) {
    const p = [x + 0.5, y + 0.5];
    const old = toMeters(heightmap.readUInt16LE(index(x, y) * 2));
    let target;
    if (inside(p, poly)) {
      const r = Math.hypot(p[0] - CENTER[0], p[1] - CENTER[1]);
      const mid = (R_IN + R_OUT) / 2, half = (R_OUT - R_IN) / 2;
      target = 1.0 + 1.0 * (1 - Math.min(1, Math.abs(r - mid) / half));
    } else {
      const d = edgeDistance(p, poly);
      if (d >= BLEND) continue;
      // From the distance alone, never from the ground as it is: a second run must find nothing to raise.
      const t = d / BLEND, s = t * t * (3 - 2 * t);
      target = 1.0 - 4.0 * s;                                   // 1 m at the wall, -3 m after BLEND
    }
    if (target <= old) {
      continue;
    }
    const u16 = toU16(target);
    if (u16 > heightmap.readUInt16LE(index(x, y) * 2)) {
      heightmap.writeUInt16LE(u16, index(x, y) * 2);
      raised++;
    }
  }
}
console.log(`${BASE_URL}: ${raised} nodes to raise; backup of the current heightmap: ${backup}`);
if (!APPLY) {
  console.log("dry-run - nothing uploaded (use --apply)");
} else if (raised === 0) {
  console.log("the wall is already there - nothing to upload");
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
