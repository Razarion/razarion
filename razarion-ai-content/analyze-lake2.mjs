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

  function getH(x, y) {
    const tileX = Math.floor(x / NODE_X_COUNT);
    const tileY = Math.floor(y / NODE_Y_COUNT);
    const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + (y % NODE_Y_COUNT) * NODE_X_COUNT + (x % NODE_X_COUNT);
    if (idx >= hmLen) return 0;
    return uint16ToHeight(hm[idx]);
  }

  // Print water presence per 100m row/column strips
  console.log("Water presence by Y row (0-2000m), checking X 0-2000:");
  for (let y = 0; y < 2000; y += 100) {
    let waterCount = 0, total = 0;
    for (let x = 0; x < 2000; x += 10) {
      total++;
      if (getH(x, y) < 0) waterCount++;
    }
    const bar = "#".repeat(Math.round(waterCount / total * 40));
    console.log(`  Y=${String(y).padStart(4)}: ${bar} (${waterCount}/${total})`);
  }

  console.log("\nWater presence by X column (0-2000m), checking Y 0-2000:");
  for (let x = 0; x < 2000; x += 100) {
    let waterCount = 0, total = 0;
    for (let y = 0; y < 2000; y += 10) {
      total++;
      if (getH(x, y) < 0) waterCount++;
    }
    const bar = "#".repeat(Math.round(waterCount / total * 40));
    console.log(`  X=${String(x).padStart(4)}: ${bar} (${waterCount}/${total})`);
  }
}

main().catch(e => { console.error(e); process.exit(1); });
