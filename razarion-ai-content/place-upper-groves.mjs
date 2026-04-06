import { gunzipSync } from "node:zlib";
import { writeFileSync } from "node:fs";

const BASE_URL = "http://localhost:8080";
const PLANET_ID = 117;
const NODE_X_COUNT = 160, NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.01, HEIGHT_MIN = -200;
const WALL_HEIGHT_DIFF = 0.5;
const SPLATTER_UV_SCALE = 0.006;

// Phase 1 area
const P1_MIN_X = 0, P1_MAX_X = 820;
const P1_MIN_Y = 0, P1_MAX_Y = 800;

// Plant models for groves (smaller plants, no big palms)
const GROVE_MODELS = [
  { modelId: 9,  name: "Banana plant", radius: 1 },
  { modelId: 10, name: "Banana plant small", radius: 0.5 },
  { modelId: 16, name: "Tropical Plant", radius: 1 },
  { modelId: 13, name: "Palm bush", radius: 0.8 },
  { modelId: 11, name: "Fern", radius: 0.5 },
  { modelId: 12, name: "Fern small", radius: 0.3 },
  { modelId: 32, name: "Palm plant", radius: 0.5 },
];

// ===== Tileable Perlin noise (matching procedural-textures.ts) =====
const perm = new Uint8Array(512);
const grad3 = [
  [1,1,0],[-1,1,0],[1,-1,0],[-1,-1,0],
  [1,0,1],[-1,0,1],[1,0,-1],[-1,0,-1],
  [0,1,1],[0,-1,1],[0,1,-1],[0,-1,-1],
];

function initPerm(seed) {
  const p = new Uint8Array(256);
  for (let i = 0; i < 256; i++) p[i] = i;
  let s = seed;
  for (let i = 255; i > 0; i--) {
    s = (s * 16807 + 0) % 2147483647;
    const j = s % (i + 1);
    [p[i], p[j]] = [p[j], p[i]];
  }
  for (let i = 0; i < 512; i++) perm[i] = p[i & 255];
}

function fade(t) { return t * t * t * (t * (t * 6 - 15) + 10); }
function lerp(a, b, t) { return a + t * (b - a); }
function clamp01(v) { return Math.max(0, Math.min(1, v)); }
function mod(n, m) { return ((n % m) + m) % m; }

function perlin2dTile(x, y, px, py) {
  const X = Math.floor(x), Y = Math.floor(y);
  const xf = x - X, yf = y - Y;
  const X0 = mod(X, px), Y0 = mod(Y, py);
  const X1 = (X0 + 1) % px, Y1 = (Y0 + 1) % py;
  const u = fade(xf), v = fade(yf);
  const aa = perm[(perm[X0 & 255] + Y0) & 255], ab = perm[(perm[X0 & 255] + Y1) & 255];
  const ba = perm[(perm[X1 & 255] + Y0) & 255], bb = perm[(perm[X1 & 255] + Y1) & 255];
  const dot = (g, dx, dy) => { const gr = grad3[g % 12]; return gr[0] * dx + gr[1] * dy; };
  return lerp(
    lerp(dot(aa, xf, yf), dot(ba, xf - 1, yf), u),
    lerp(dot(ab, xf, yf - 1), dot(bb, xf - 1, yf - 1), u), v
  );
}

function fbmTile(x, y, octaves, lac, pers, px, py) {
  let value = 0, amp = 1, freq = 1, max = 0;
  for (let i = 0; i < octaves; i++) {
    value += perlin2dTile(x * freq, y * freq, px * freq, py * freq) * amp;
    max += amp; amp *= pers; freq *= lac;
  }
  return value / max;
}

function warpedFbmTile(x, y, octaves, warpStr, warpSeed, px, py) {
  const wx = perlin2dTile(x + warpSeed, y + warpSeed, px, py) * warpStr;
  const wy = perlin2dTile(x + warpSeed + 50, y + warpSeed + 50, px, py) * warpStr;
  return fbmTile(x + wx, y + wy, octaves, 2.0, 0.5, px, py);
}

const SEED = 77, SCALE = 4, WARP_STRENGTH = 1.5;

function splatterValue(nx, ny) {
  const large = warpedFbmTile(nx * SCALE, ny * SCALE, 3, WARP_STRENGTH, SEED, SCALE, SCALE);
  const mid = fbmTile(nx * SCALE * 2, ny * SCALE * 2, 2, 2.0, 0.5, SCALE * 2, SCALE * 2) * 0.2;
  const raw = (large + mid) * 0.5 + 0.5;
  const detail = fbmTile(nx * SCALE * 12, ny * SCALE * 12, 3, 2.0, 0.5, SCALE * 12, SCALE * 12) * 0.1;
  const v = raw + detail;
  const t = clamp01((v - 0.48) / 0.04);
  return t * t * (3 - 2 * t);
}

function uint16ToHeight(v) { return v * HEIGHT_PRECISION + HEIGHT_MIN; }

function seededRandom(seed) {
  let s = seed;
  return () => { s = (s * 16807 + 0) % 2147483647; return (s - 1) / 2147483646; };
}

async function main() {
  initPerm(SEED);

  // Load heightmap
  const shape = await (await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`)).json();
  const tileXCount = shape.nativeTerrainShapeTiles.length;

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

  function maxSlope(x, y) {
    const h = getHeight(x, y);
    let maxDiff = 0;
    for (let dy = -1; dy <= 1; dy++) {
      for (let dx = -1; dx <= 1; dx++) {
        if (dx === 0 && dy === 0) continue;
        maxDiff = Math.max(maxDiff, Math.abs(h - getHeight(x + dx, y + dy)));
      }
    }
    return maxDiff;
  }

  // Find patch centers by grid: divide P1 into cells, find the point with
  // highest splatter value in each cell = center of each grass patch
  const CELL_SIZE = 20; // one grove per 20x20m cell
  const rand = seededRandom(123);
  const groveCenters = [];

  function getSv(x, y) {
    const nx = (x * SPLATTER_UV_SCALE) % 1.0;
    const ny = (y * SPLATTER_UV_SCALE) % 1.0;
    return splatterValue(nx < 0 ? nx + 1 : nx, ny < 0 ? ny + 1 : ny);
  }

  for (let cellY = P1_MIN_Y; cellY < P1_MAX_Y; cellY += CELL_SIZE) {
    for (let cellX = P1_MIN_X; cellX < P1_MAX_X; cellX += CELL_SIZE) {
      let bestX = 0, bestY = 0, bestSv = 0;
      // Scan cell to find local maximum of splatter value
      for (let y = cellY + 2; y < Math.min(cellY + CELL_SIZE, P1_MAX_Y) - 2; y += 2) {
        for (let x = cellX + 2; x < Math.min(cellX + CELL_SIZE, P1_MAX_X) - 2; x += 2) {
          const h = getHeight(x, y);
          if (h < 0.3 || h > 2.5) continue;
          if (maxSlope(x, y) > 0.15) continue;
          const sv = getSv(x, y);
          if (sv > bestSv) {
            bestSv = sv;
            bestX = x;
            bestY = y;
          }
        }
      }
      // Only place a grove if the cell has a solid grass area
      if (bestSv > 0.8) {
        groveCenters.push({ x: bestX, y: bestY, h: getHeight(bestX, bestY), sv: bestSv });
      }
    }
  }

  console.log(`Found ${groveCenters.length} patch centers in Phase 1`);

  // Generate grove plants: 6-12 plants per grove in a tight cluster
  const positions = [];
  for (const center of groveCenters) {
    const plantCount = 6 + Math.floor(rand() * 7); // 6-12 plants
    for (let i = 0; i < plantCount; i++) {
      // Random offset from center (radius 1-3m) — visible cluster
      const angle = rand() * Math.PI * 2;
      const dist = 0.5 + rand() * 2.5;
      const px = Math.round(center.x + Math.cos(angle) * dist);
      const py = Math.round(center.y + Math.sin(angle) * dist);

      // Validate position
      if (px < P1_MIN_X + 1 || px >= P1_MAX_X - 1 || py < P1_MIN_Y + 1 || py >= P1_MAX_Y - 1) continue;
      const ph = getHeight(px, py);
      if (ph < 0.3 || maxSlope(px, py) > 0.2) continue;

      const model = GROVE_MODELS[Math.floor(rand() * GROVE_MODELS.length)];
      const rotZ = rand() * Math.PI * 2;
      const scale = 0.6 + rand() * 0.8; // 0.6-1.4
      positions.push({ x: px, y: py, model, rotZ, scale });
    }
  }

  console.log(`Generated ${positions.length} grove plants total`);

  // Ensure grove models exist as TerrainObject configs (check which already exist)
  // Build SQL: first check/create configs, then positions
  const configSql = [];
  const existingNames = new Set();

  // Check which configs already exist — use INSERT IGNORE pattern
  for (const m of GROVE_MODELS) {
    configSql.push(`INSERT IGNORE INTO TERRAIN_OBJECT (internalName, radius, model3DId_id) VALUES ('${m.name}', ${m.radius}, ${m.modelId});`);
  }

  const positionSql = positions.map(({ x, y, model, rotZ, scale }) =>
    `INSERT INTO TERRAIN_OBJECT_POSITION (internalName, x, y, terrainObjectEntity_id, planet, rotationX, rotationY, rotationZ, scaleX, scaleY, scaleZ) VALUES ('${model.name}@${x},${y}', ${x}, ${y}, (SELECT id FROM TERRAIN_OBJECT WHERE internalName='${model.name}' LIMIT 1), 117, 0, 0, ${rotZ.toFixed(3)}, ${scale.toFixed(2)}, ${scale.toFixed(2)}, ${scale.toFixed(2)});`
  );

  const allSql = [...configSql, "", ...positionSql].join("\n");
  writeFileSync("place-upper-groves.sql", allSql);
  console.log(`\nSQL written to place-upper-groves.sql (${configSql.length} configs, ${positionSql.length} positions)`);
}

main().catch(e => { console.error(e); process.exit(1); });
