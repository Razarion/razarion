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

  // For each Y row, find the rightmost water pixel (max X where water)
  console.log("Rightmost water (X) per Y row:");
  for (let y = 0; y < 1200; y += 50) {
    let maxWaterX = -1;
    for (let x = 0; x < 2000; x += 5) {
      if (getH(x, y) < 0) maxWaterX = x;
    }
    const bar = maxWaterX > 0 ? "~".repeat(Math.round(maxWaterX / 30)) : "";
    console.log(`  Y=${String(y).padStart(4)}: ${bar} (maxX=${maxWaterX})`);
  }

  // For each X column, find the topmost water pixel (max Y where water)
  console.log("\nTopmost water (Y) per X column:");
  for (let x = 0; x < 1500; x += 50) {
    let maxWaterY = -1;
    for (let y = 0; y < 1500; y += 5) {
      if (getH(x, y) < 0) maxWaterY = y;
    }
    const bar = maxWaterY > 0 ? "~".repeat(Math.round(maxWaterY / 30)) : "";
    console.log(`  X=${String(x).padStart(4)}: ${bar} (maxY=${maxWaterY})`);
  }
}

main().catch(e => { console.error(e); process.exit(1); });
