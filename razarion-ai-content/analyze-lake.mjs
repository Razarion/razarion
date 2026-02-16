import { gunzipSync } from "node:zlib";

const BASE_URL = "http://localhost:8080";
const PLANET_ID = 117;
const NODE_X_COUNT = 160, NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.1, HEIGHT_MIN = -200;

function uint16ToHeight(v) { return v * HEIGHT_PRECISION + HEIGHT_MIN; }

async function main() {
  const shape = await (await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`)).json();
  const tileXCount = shape.nativeTerrainShapeTiles.length;

  let buf = Buffer.from(await (await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`)).arrayBuffer());
  if (buf[0] === 0x1f && buf[1] === 0x8b) buf = gunzipSync(buf);
  const bytes = new Uint8Array(buf);
  const hmLen = Math.floor(bytes.length / 2);
  const hm = new Uint16Array(hmLen);
  for (let i = 0; i < hmLen; i++) hm[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);

  // Only look at bottom-left quadrant (0-1500m), find water extent
  let waterMaxX = 0, waterMaxY = 0;
  for (let y = 0; y < 1500; y += 2) {
    for (let x = 0; x < 1500; x += 2) {
      const tileX = Math.floor(x / NODE_X_COUNT);
      const tileY = Math.floor(y / NODE_Y_COUNT);
      const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + (y % NODE_Y_COUNT) * NODE_X_COUNT + (x % NODE_X_COUNT);
      if (idx >= hmLen) continue;
      const h = uint16ToHeight(hm[idx]);
      if (h < 0) {
        if (x > waterMaxX) waterMaxX = x;
        if (y > waterMaxY) waterMaxY = y;
      }
    }
  }
  console.log(`Bottom-left lake (water < 0m):`);
  console.log(`  Water extends to X=${waterMaxX}m, Y=${waterMaxY}m`);

  // Also find hills (>5m) only in 0-1500 range
  let hillMaxX = 0, hillMaxY = 0;
  for (let y = 0; y < 1500; y += 2) {
    for (let x = 0; x < 1500; x += 2) {
      const tileX = Math.floor(x / NODE_X_COUNT);
      const tileY = Math.floor(y / NODE_Y_COUNT);
      const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + (y % NODE_Y_COUNT) * NODE_X_COUNT + (x % NODE_X_COUNT);
      if (idx >= hmLen) continue;
      const h = uint16ToHeight(hm[idx]);
      if (h > 5) {
        if (x > hillMaxX) hillMaxX = x;
        if (y > hillMaxY) hillMaxY = y;
      }
    }
  }
  console.log(`  Hills (>5m) extend to X=${hillMaxX}m, Y=${hillMaxY}m`);
}

main().catch(e => { console.error(e); process.exit(1); });
