// Paths across the beginners' island start area (2026-10-04): trodden earth from the start pads past the
// Garrison to the reachable east coast (where the Dockyard quest builds), with branches from the west pads and
// through the hilly north-west. Look only - the paths change nothing for pathing or building.
//
// Routed by A* over the current height map: water, blocked cells and the surroundings of pads and bot grounds are
// out, slopes cost, beach costs a little. Then smoothed. Writes the network as JSON for the renderer
// (razarion-frontend/public/renderer/paths/planet-117.json, see ground-paths.ts) and removes the terrain objects
// standing on it - mostly bushes and ferns, they would grow out of the middle of the path.
//
// Usage:
//   node noob-island-paths.mjs                    # dry-run: writes the JSON, lists the objects on the paths
//   node noob-island-paths.mjs --apply            # also deletes those objects (updateTerrain), then warm-restart
//   node noob-island-paths.mjs --ids-out f.txt    # write the object ids instead of deleting (local db whose admin
//                                                 # login differs: DELETE FROM TERRAIN_OBJECT_POSITION WHERE id IN ...)
//
// RAZARION_BASE_URL / RAZARION_ADMIN / RAZARION_PASSWORD as in raise-subregion1.mjs.

import { gunzipSync } from "node:zlib";
import { writeFileSync, mkdirSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN = process.env.RAZARION_ADMIN || "admin@admin.com";
const PASSWORD = process.env.RAZARION_PASSWORD || "1234";
const PLANET_ID = 117;
const APPLY = process.argv.includes("--apply");
const IDS_OUT = process.argv.includes("--ids-out") ? process.argv[process.argv.indexOf("--ids-out") + 1] : null;
const PATHS_OUT = join(dirname(fileURLToPath(import.meta.url)), "../../razarion-frontend/public/renderer/paths/planet-117.json");

const toMeters = (u16) => u16 * 0.01 - 200;
const index = (x, y) => 25600 * (Math.floor(y / 160) * 32 + Math.floor(x / 160)) + (y % 160) * 160 + (x % 160);
const W = 320, H = 320;   // the start area and its coast

// Where the paths go (game coords)
const SOUTH_PADS = [135, 42];   // north edge of the southern pad row
const WEST_PADS = [42, 135];    // east edge of the western pad row
const NORTH_WEST = [42, 215];   // upper end of the western pad row
const NORTH_WEST_VIA = [110, 200];
const GARRISON = [160, 100];    // south gate of the (Bot1) Garrison
const EAST_COAST = [242, 180];  // the reachable coast of the Dockyard quest (place 1797)
const WIDTH_MAIN = 4, WIDTH_BRANCH = 3;

// Keep-out: start pads (ServerTerrainShapeService.generateDecals) and bot grounds, as in noob-island-hills.mjs
const RECTS = [];
for (let i = 0; i < 6; i++) RECTS.push([25 + i * 35, 2, 25, 25], [7, 35 + i * 35, 25, 25]);
RECTS.push([179, 54, 8, 8], [140, 106, 40, 40], [378, 256, 56, 56], [87, 54, 8, 8], [129, 49, 8, 8],
  [75, 221, 8, 8], [67, 183, 8, 8], [78, 111, 8, 8], [492, 556, 64, 56]);
const rectDistance = (x, y) => {
  let d = Infinity;
  for (const [rx, ry, w, h] of RECTS) d = Math.min(d, Math.hypot(Math.max(rx - x, 0, x - rx - w), Math.max(ry - y, 0, y - ry - h)));
  return d;
};

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
const heightmap = body.length === 5120 * 5120 * 2 ? body : Buffer.from(gunzipSync(body));
const h = new Float64Array(W * H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) h[y * W + x] = toMeters(heightmap.readUInt16LE(index(x, y) * 2));
const at = (x, y) => h[y * W + x];
const cornerRange = (x, y) => {
  const v = [at(x, y), at(x + 1, y), at(x, y + 1), at(x + 1, y + 1)];
  return Math.max(...v) - Math.min(...v);
};

const cost = new Float64Array(W * H).fill(Infinity);
for (let y = 0; y < H - 1; y++) for (let x = 0; x < W - 1; x++) {
  const v = at(x, y), slope = cornerRange(x, y), rd = rectDistance(x, y);
  if (v <= 0.05 || slope >= 0.5 || rd < 2) continue;
  cost[y * W + x] = 1 + slope * 25 + (v < 0.3 ? 1.5 : 0) + (rd < 6 ? (6 - rd) * 0.8 : 0);
}

function astar([sx, sy], isGoal, heuristic) {
  const g = new Float64Array(W * H).fill(Infinity), from = new Int32Array(W * H).fill(-1), done = new Uint8Array(W * H);
  const heap = [];
  const push = (f, i) => {
    heap.push([f, i]);
    for (let k = heap.length - 1; k > 0;) {
      const p = (k - 1) >> 1;
      if (heap[p][0] <= heap[k][0]) break;
      [heap[p], heap[k]] = [heap[k], heap[p]];
      k = p;
    }
  };
  const pop = () => {
    const top = heap[0], last = heap.pop();
    if (heap.length) {
      heap[0] = last;
      for (let k = 0; ;) {
        const l = 2 * k + 1, r = l + 1;
        let m = k;
        if (l < heap.length && heap[l][0] < heap[m][0]) m = l;
        if (r < heap.length && heap[r][0] < heap[m][0]) m = r;
        if (m === k) break;
        [heap[m], heap[k]] = [heap[k], heap[m]];
        k = m;
      }
    }
    return top;
  };
  const start = sy * W + sx;
  g[start] = 0;
  push(heuristic(sx, sy), start);
  const STEPS = [[1, 0, 1], [-1, 0, 1], [0, 1, 1], [0, -1, 1], [1, 1, Math.SQRT2], [1, -1, Math.SQRT2], [-1, 1, Math.SQRT2], [-1, -1, Math.SQRT2]];
  while (heap.length) {
    const [, i] = pop();
    if (done[i]) continue;
    done[i] = 1;
    const x = i % W, y = (i / W) | 0;
    if (isGoal(x, y)) {
      const route = [];
      for (let k = i; k >= 0; k = from[k]) route.push([k % W, (k / W) | 0]);
      return route.reverse();
    }
    for (const [dx, dy, length] of STEPS) {
      const nx = x + dx, ny = y + dy;
      if (nx < 0 || ny < 0 || nx >= W - 1 || ny >= H - 1) continue;
      const j = ny * W + nx;
      const next = g[i] + cost[j] * length;
      if (next < g[j]) {
        g[j] = next;
        from[j] = i;
        push(next + heuristic(nx, ny), j);
      }
    }
  }
  throw new Error(`no route from (${sx}, ${sy})`);
}

const nearestFree = ([x0, y0]) => {
  let best = null, bestDistance = Infinity;
  for (let y = 0; y < H - 1; y++) for (let x = 0; x < W - 1; x++) {
    const d = Math.hypot(x - x0, y - y0);
    if (isFinite(cost[y * W + x]) && d < bestDistance) {
      bestDistance = d;
      best = [x, y];
    }
  }
  return best;
};
const toPoint = ([tx, ty]) => [(x, y) => Math.abs(x - tx) <= 1 && Math.abs(y - ty) <= 1, (x, y) => Math.hypot(x - tx, y - ty)];
const onRoutes = (...routes) => {
  const cells = new Set(routes.flat().map(([x, y]) => y * W + x));
  return [(x, y) => cells.has(y * W + x), () => 0];
};

const main = astar(nearestFree(SOUTH_PADS), ...toPoint(nearestFree(EAST_COAST)));
const west = astar(nearestFree(WEST_PADS), ...onRoutes(main));
const garrison = astar(nearestFree(GARRISON), ...onRoutes(main));
const via = nearestFree(NORTH_WEST_VIA);
const northWest = [...astar(nearestFree(NORTH_WEST), ...toPoint(via)), ...astar(via, ...onRoutes(main, west)).slice(1)];

// Every fourth node, then Chaikin three times: no more staircase from the grid
function smooth(route) {
  let p = route.filter((_, i) => i % 4 === 0 || i === route.length - 1).map(([x, y]) => [x + 0.5, y + 0.5]);
  for (let pass = 0; pass < 3; pass++) {
    const q = [p[0]];
    for (let i = 0; i < p.length - 1; i++) {
      const [a, b] = [p[i], p[i + 1]];
      q.push([a[0] * 0.75 + b[0] * 0.25, a[1] * 0.75 + b[1] * 0.25], [a[0] * 0.25 + b[0] * 0.75, a[1] * 0.25 + b[1] * 0.75]);
    }
    q.push(p[p.length - 1]);
    p = q;
  }
  return p.map(([x, y]) => [Math.round(x * 10) / 10, Math.round(y * 10) / 10]);
}
const paths = [
  {name: "main", width: WIDTH_MAIN, points: smooth(main)},
  {name: "west", width: WIDTH_BRANCH, points: smooth(west)},
  {name: "garrison", width: WIDTH_BRANCH, points: smooth(garrison)},
  {name: "north-west", width: WIDTH_BRANCH, points: smooth(northWest)},
];
mkdirSync(dirname(PATHS_OUT), {recursive: true});
writeFileSync(PATHS_OUT, JSON.stringify({planetId: PLANET_ID, paths}) + "\n");

const distanceToPaths = (x, y) => {
  let best = Infinity;
  for (const {width, points} of paths) for (let i = 1; i < points.length; i++) {
    const [ax, ay] = points[i - 1], [bx, by] = points[i], dx = bx - ax, dy = by - ay, l = dx * dx + dy * dy;
    const t = l ? Math.max(0, Math.min(1, ((x - ax) * dx + (y - ay) * dy) / l)) : 0;
    best = Math.min(best, Math.hypot(x - ax - t * dx, y - ay - t * dy) - width / 2);
  }
  return best;   // < 0 inside a path
};

// Terrain objects on the paths, from the terrain shape (public, has ids). Half a metre for the object itself.
const shapeRes = await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`);
if (!shapeRes.ok) throw new Error(`terrainshape ${shapeRes.status}`);
const shape = await shapeRes.json();
const onPaths = [];
for (const tile of shape.nativeTerrainShapeTiles.flat()) {
  for (const list of tile?.nativeTerrainShapeObjectLists ?? []) {
    for (const p of list.terrainShapeObjectPositions) {
      if (p.x >= W || p.y >= H) continue;
      const scale = p.scale ? p.scale.x : 1;
      if (distanceToPaths(p.x, p.y) < 0.5 * scale) onPaths.push(p.terrainObjectId);
    }
  }
}
const length = paths.reduce((sum, {points}) => sum + points.slice(1).reduce((s, p, i) => s + Math.hypot(p[0] - points[i][0], p[1] - points[i][1]), 0), 0);
console.log(`${Math.round(length)} m of path written to ${PATHS_OUT}`);
console.log(`${onPaths.length} terrain objects on the paths`);

if (IDS_OUT) {
  writeFileSync(IDS_OUT, onPaths.join(",") + "\n");
  console.log(`ids written to ${IDS_OUT} - nothing deleted`);
} else if (!APPLY) {
  console.log("dry-run - nothing deleted (use --apply)");
} else if (onPaths.length) {
  const token = await auth();
  const up = await fetch(`${BASE_URL}/rest/editor/planeteditor/updateTerrain/${PLANET_ID}`, {
    method: "PUT",
    headers: {Authorization: `Bearer ${token}`, "Content-Type": "application/json"},
    body: JSON.stringify({createdTerrainObjects: [], updatedTerrainObjects: [], deletedTerrainObjectsIds: onPaths}),
  });
  if (!up.ok) throw new Error(`updateTerrain ${up.status}`);
  console.log("deleted - now warm-restart the planet (POST /rest/planet-mgmt-controller/restartPlanetWarm)");
}
