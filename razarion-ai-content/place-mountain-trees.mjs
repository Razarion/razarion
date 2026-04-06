import { gunzipSync } from "node:zlib";

const BASE_URL = "http://localhost:8080";
const PLANET_ID = 117;
const NODE_X_COUNT = 160, NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.01, HEIGHT_MIN = -200;
const WALL_HEIGHT_DIFF = 0.5;

// Phase 1 area: (0,0) to (820,800) in game coordinates (from generate-phase-map.mjs)
const P1_MIN_X = 0, P1_MAX_X = 820;
const P1_MIN_Y = 0, P1_MAX_Y = 800;

// 3D model IDs for palm trees/vegetation
const TREE_MODELS = [
  { modelId: 14, name: "Palm tree1", radius: 2 },
  { modelId: 15, name: "Palm tree", radius: 2 },
  { modelId: 21, name: "Palm tree2", radius: 2 },
  { modelId: 6,  name: "Banana plant big", radius: 1.5 },
  { modelId: 9,  name: "Banana plant", radius: 1 },
  { modelId: 16, name: "Tropical Plant", radius: 1 },
  { modelId: 13, name: "Palm bush", radius: 0.8 },
  { modelId: 11, name: "Fern", radius: 0.5 },
];

function uint16ToHeight(v) { return v * HEIGHT_PRECISION + HEIGHT_MIN; }

function seededRandom(seed) {
  let s = seed;
  return () => { s = (s * 16807 + 0) % 2147483647; return (s - 1) / 2147483646; };
}

async function main() {
  // Load terrain shape
  const shape = await (await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`)).json();
  const tileXCount = shape.nativeTerrainShapeTiles.length;

  // Load heightmap
  let buf = Buffer.from(await (await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`)).arrayBuffer());
  if (buf[0] === 0x1f && buf[1] === 0x8b) buf = gunzipSync(buf);
  const bytes = new Uint8Array(buf);
  const hmLen = Math.floor(bytes.length / 2);
  const hm = new Uint16Array(hmLen);
  for (let i = 0; i < hmLen; i++) hm[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);

  function getHeight(x, y) {
    const tileX = Math.floor(x / NODE_X_COUNT);
    const tileY = Math.floor(y / NODE_Y_COUNT);
    const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + (y % NODE_Y_COUNT) * NODE_X_COUNT + (x % NODE_X_COUNT);
    if (idx >= hmLen || idx < 0) return 0;
    return uint16ToHeight(hm[idx]);
  }

  // Helper: max slope at a position (height diff to immediate neighbors)
  function maxSlope(x, y) {
    const h = getHeight(x, y);
    let maxDiff = 0;
    for (let dy = -1; dy <= 1; dy++) {
      for (let dx = -1; dx <= 1; dx++) {
        if (dx === 0 && dy === 0) continue;
        const nh = getHeight(x + dx, y + dy);
        maxDiff = Math.max(maxDiff, Math.abs(h - nh));
      }
    }
    return maxDiff;
  }

  // Classify terrain: find placement zones
  // Two zones: (1) foot of mountain = flat land near steep slopes, (2) mountain top = flat areas on top
  const placementPositions = [];
  const NEAR_MOUNTAIN_RADIUS = 5; // close to mountain edge

  for (let y = P1_MIN_Y + 2; y < P1_MAX_Y - 2; y++) {
    for (let x = P1_MIN_X + 2; x < P1_MAX_X - 2; x++) {
      const h = getHeight(x, y);
      if (h < 0.3) continue; // skip water/beach

      const slope = maxSlope(x, y);

      // Skip steep slopes — no trees on cliff faces
      if (slope > 0.25) continue;

      // Check distance to nearest steep terrain (mountain edge)
      let minMountainDist = Infinity;
      for (let dy = -NEAR_MOUNTAIN_RADIUS; dy <= NEAR_MOUNTAIN_RADIUS; dy++) {
        for (let dx = -NEAR_MOUNTAIN_RADIUS; dx <= NEAR_MOUNTAIN_RADIUS; dx++) {
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (dist > NEAR_MOUNTAIN_RADIUS) continue;
          const nx = x + dx, ny = y + dy;
          if (nx < P1_MIN_X + 1 || nx >= P1_MAX_X - 1 || ny < P1_MIN_Y + 1 || ny >= P1_MAX_Y - 1) continue;
          if (maxSlope(nx, ny) >= WALL_HEIGHT_DIFF && dist < minMountainDist) {
            minMountainDist = dist;
          }
        }
      }

      if (minMountainDist <= NEAR_MOUNTAIN_RADIUS) {
        // Zone tag: foot (low, h < 3m) or top (high, h >= 3m)
        const zone = h < 3 ? "foot" : "top";
        placementPositions.push({ x, y, h, zone, distToMountain: minMountainDist });
      }
    }
  }

  // Sort by distance to mountain edge (closest first) for denser placement near edges
  placementPositions.sort((a, b) => a.distToMountain - b.distToMountain);

  const footCount = placementPositions.filter(p => p.zone === "foot").length;
  const topCount = placementPositions.filter(p => p.zone === "top").length;
  console.log(`Found ${placementPositions.length} placement nodes (${footCount} foot, ${topCount} top) in P1`);

  // Print height distribution for debugging
  const heightHist = {};
  for (let y = P1_MIN_Y; y < P1_MAX_Y; y++) {
    for (let x = P1_MIN_X; x < P1_MAX_X; x++) {
      const h = getHeight(x, y);
      const bucket = Math.floor(h);
      heightHist[bucket] = (heightHist[bucket] || 0) + 1;
    }
  }
  console.log("\nP1 height distribution:");
  for (const [h, count] of Object.entries(heightHist).sort((a, b) => Number(a[0]) - Number(b[0]))) {
    const bar = "#".repeat(Math.min(Math.round(count / 50), 60));
    console.log(`  ${h.padStart(4)}m: ${String(count).padStart(5)} ${bar}`);
  }

  // Sample positions with minimum spacing (closer near mountain edge)
  const rand = seededRandom(42);
  const placed = [];

  // Already sorted by distance to mountain — pick greedily with spacing check
  for (const pos of placementPositions) {
    const minSpacing = pos.distToMountain <= 2 ? 3 : 4;
    const tooClose = placed.some(p => {
      const dx = Math.abs(p.x - pos.x), dy = Math.abs(p.y - pos.y);
      return dx < minSpacing && dy < minSpacing;
    });
    if (tooClose) continue;
    placed.push(pos);
  }

  console.log(`\nSelected ${placed.length} positions for tree placement`);

  if (placed.length === 0) {
    console.log("No mountain base positions found! Exiting.");
    process.exit(1);
  }

  // Output SQL file for DB import
  const sqlLines = [];

  // Create terrain object configs
  for (const m of TREE_MODELS) {
    sqlLines.push(`INSERT INTO TERRAIN_OBJECT (internalName, radius, model3DId_id) VALUES ('${m.name}', ${m.radius}, ${m.modelId});`);
  }

  // Query back the IDs
  sqlLines.push(`SELECT id, internalName FROM TERRAIN_OBJECT;`);

  // We need to build position inserts referencing config IDs
  // First pass: collect what we need
  const positionInserts = [];
  for (const pos of placed) {
    const model = TREE_MODELS[Math.floor(rand() * TREE_MODELS.length)];
    const rotZ = rand() * Math.PI * 2; // rotationZ = vertical axis (Y/Z swapped in Babylon)
    const scale = 0.8 + rand() * 0.6;
    positionInserts.push({ pos, model, rotZ, scale });
  }

  // Output config SQL
  const configSql = TREE_MODELS.map(m =>
    `INSERT INTO TERRAIN_OBJECT (internalName, radius, model3DId_id) VALUES ('${m.name}', ${m.radius}, ${m.modelId});`
  ).join("\n");

  console.log("\n=== Step 1: Create TerrainObject configs ===");
  console.log(configSql);

  // Output position SQL with subquery for ID lookup
  console.log("\n=== Step 2: Place positions (run after step 1) ===");
  for (const { pos, model, rotZ, scale } of positionInserts) {
    console.log(`INSERT INTO TERRAIN_OBJECT_POSITION (internalName, x, y, terrainObjectEntity_id, planet, rotationX, rotationY, rotationZ, scaleX, scaleY, scaleZ) VALUES ('${model.name}@${pos.x},${pos.y}', ${pos.x}, ${pos.y}, (SELECT id FROM TERRAIN_OBJECT WHERE internalName='${model.name}' LIMIT 1), 117, 0, 0, ${rotZ.toFixed(3)}, ${scale.toFixed(2)}, ${scale.toFixed(2)}, ${scale.toFixed(2)});`);
  }

  console.log(`\nTotal: ${positionInserts.length} positions`);

  // Write combined SQL file
  const allSql = [
    configSql,
    "",
    ...positionInserts.map(({ pos, model, rotZ, scale }) =>
      `INSERT INTO TERRAIN_OBJECT_POSITION (internalName, x, y, terrainObjectEntity_id, planet, rotationX, rotationY, rotationZ, scaleX, scaleY, scaleZ) VALUES ('${model.name}@${pos.x},${pos.y}', ${pos.x}, ${pos.y}, (SELECT id FROM TERRAIN_OBJECT WHERE internalName='${model.name}' LIMIT 1), 117, 0, 0, ${rotZ.toFixed(3)}, ${scale.toFixed(2)}, ${scale.toFixed(2)}, ${scale.toFixed(2)});`
    )
  ].join("\n");

  const fs = await import("node:fs");
  fs.writeFileSync("place-mountain-trees.sql", allSql);
  console.log("\nSQL written to place-mountain-trees.sql");
}

main().catch(e => { console.error(e); process.exit(1); });
