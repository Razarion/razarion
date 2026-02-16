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
  const tileYCount = shape.nativeTerrainShapeTiles[0].length;
  const totalX = tileXCount * NODE_X_COUNT;
  const totalY = tileYCount * NODE_Y_COUNT;

  let buf = Buffer.from(await (await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`)).arrayBuffer());
  if (buf[0] === 0x1f && buf[1] === 0x8b) buf = gunzipSync(buf);
  const bytes = new Uint8Array(buf);
  const hmLen = Math.floor(bytes.length / 2);
  const hm = new Uint16Array(hmLen);
  for (let i = 0; i < hmLen; i++) hm[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);

  // Find bounding box of "interesting" terrain (water < 0 or hills > 5m)
  let minX = totalX, maxX = 0, minY = totalY, maxY = 0;
  const step = 4; // sample every 4m for speed
  for (let y = 0; y < totalY; y += step) {
    for (let x = 0; x < totalX; x += step) {
      const tileX = Math.floor(x / NODE_X_COUNT);
      const tileY = Math.floor(y / NODE_Y_COUNT);
      const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + (y % NODE_Y_COUNT) * NODE_X_COUNT + (x % NODE_X_COUNT);
      if (idx >= hmLen) continue;
      const h = uint16ToHeight(hm[idx]);
      if (h < 0 || h > 5) {
        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;
      }
    }
  }
  console.log(`Interesting terrain bounding box (water + hills > 5m):`);
  console.log(`  X: ${minX} - ${maxX} m`);
  console.log(`  Y: ${minY} - ${maxY} m`);
  console.log(`  Size: ${maxX - minX} x ${maxY - minY} m`);
}

main().catch(e => { console.error(e); process.exit(1); });
