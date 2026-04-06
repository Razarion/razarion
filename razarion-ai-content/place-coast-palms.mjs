import { gunzipSync } from "node:zlib";
import { writeFileSync } from "node:fs";

const BASE_URL = "http://localhost:8080";
const PLANET_ID = 117;
const NODE_X_COUNT = 160, NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.01, HEIGHT_MIN = -200;

const P1_MIN_X = 0, P1_MAX_X = 820;
const P1_MIN_Y = 0, P1_MAX_Y = 800;

// Palm models for coastline
const PALM_MODELS = [
  { modelId: 14, name: "Palm tree1", radius: 2 },
  { modelId: 15, name: "Palm tree", radius: 2 },
  { modelId: 21, name: "Palm tree2", radius: 2 },
  { modelId: 13, name: "Palm bush", radius: 0.8 },
];

function uint16ToHeight(v) { return v * HEIGHT_PRECISION + HEIGHT_MIN; }
function seededRandom(seed) { let s = seed; return () => { s = (s * 16807) % 2147483647; return (s - 1) / 2147483646; }; }

async function main() {
  const shape = await (await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`)).json();
  const tileXCount = shape.nativeTerrainShapeTiles.length;

  let buf = Buffer.from(await (await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`)).arrayBuffer());
  if (buf[0] === 0x1f && buf[1] === 0x8b) buf = gunzipSync(buf);
  const bytes = new Uint8Array(buf), hmLen = Math.floor(bytes.length / 2), hm = new Uint16Array(hmLen);
  for (let i = 0; i < hmLen; i++) hm[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);

  function getHeight(x, y) {
    const tx = Math.floor(x / NODE_X_COUNT), ty = Math.floor(y / NODE_Y_COUNT);
    const idx = TILE_NODE_SIZE * (ty * tileXCount + tx) + (y % NODE_Y_COUNT) * NODE_X_COUNT + (x % NODE_X_COUNT);
    return idx >= hmLen || idx < 0 ? 0 : uint16ToHeight(hm[idx]);
  }

  function maxSlope(x, y) {
    const h = getHeight(x, y);
    let m = 0;
    for (let dy = -1; dy <= 1; dy++)
      for (let dx = -1; dx <= 1; dx++) {
        if (!dx && !dy) continue;
        m = Math.max(m, Math.abs(h - getHeight(x + dx, y + dy)));
      }
    return m;
  }

  // Find coastline positions: land (h > 0.15) near water (h <= 0) within 5m
  const SAMPLE_STEP = 3;
  const WATER_SEARCH = 5;
  const coastPoints = [];

  for (let y = P1_MIN_Y + 2; y < P1_MAX_Y - 2; y += SAMPLE_STEP) {
    for (let x = P1_MIN_X + 2; x < P1_MAX_X - 2; x += SAMPLE_STEP) {
      const h = getHeight(x, y);
      // Must be just above waterline — beach zone
      if (h < 0.15 || h > 0.8) continue;
      // Must be flat
      if (maxSlope(x, y) > 0.15) continue;

      // Check if water is nearby
      let nearWater = false;
      for (let dy = -WATER_SEARCH; dy <= WATER_SEARCH && !nearWater; dy++) {
        for (let dx = -WATER_SEARCH; dx <= WATER_SEARCH && !nearWater; dx++) {
          if (dx * dx + dy * dy > WATER_SEARCH * WATER_SEARCH) continue;
          if (getHeight(x + dx, y + dy) <= 0) nearWater = true;
        }
      }
      if (nearWater) {
        coastPoints.push({ x, y, h });
      }
    }
  }

  console.log(`Found ${coastPoints.length} coastline points`);

  // Sparse selection — few palms, deko only
  const SPACING = 15;
  const rand = seededRandom(321);

  // Shuffle
  for (let i = coastPoints.length - 1; i > 0; i--) {
    const j = Math.floor(rand() * (i + 1));
    [coastPoints[i], coastPoints[j]] = [coastPoints[j], coastPoints[i]];
  }

  const selected = [];
  for (const pos of coastPoints) {
    if (selected.some(p => Math.abs(p.x - pos.x) < SPACING && Math.abs(p.y - pos.y) < SPACING)) continue;
    selected.push(pos);
  }

  console.log(`Selected ${selected.length} palm positions`);

  const positions = selected.map(pos => {
    const model = PALM_MODELS[Math.floor(rand() * PALM_MODELS.length)];
    const rotZ = rand() * Math.PI * 2;
    const scale = 0.7 + rand() * 0.6; // 0.7-1.3
    return { x: pos.x, y: pos.y, model, rotZ, scale };
  });

  const configSql = PALM_MODELS.map(m =>
    `INSERT IGNORE INTO TERRAIN_OBJECT (internalName, radius, model3DId_id) VALUES ('${m.name}', ${m.radius}, ${m.modelId});`
  );
  const posSql = positions.map(({ x, y, model, rotZ, scale }) =>
    `INSERT INTO TERRAIN_OBJECT_POSITION (internalName, x, y, terrainObjectEntity_id, planet, rotationX, rotationY, rotationZ, scaleX, scaleY, scaleZ) VALUES ('${model.name}@${x},${y}', ${x}, ${y}, (SELECT id FROM TERRAIN_OBJECT WHERE internalName='${model.name}' LIMIT 1), 117, 0, 0, ${rotZ.toFixed(3)}, ${scale.toFixed(2)}, ${scale.toFixed(2)}, ${scale.toFixed(2)});`
  );

  writeFileSync("place-coast-palms.sql", [...configSql, "", ...posSql].join("\n"));
  console.log(`SQL written: ${positions.length} coast palms`);
}

main().catch(e => { console.error(e); process.exit(1); });
