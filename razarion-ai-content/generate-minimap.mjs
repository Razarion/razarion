import { gunzipSync, deflateSync } from "node:zlib";
import { writeFileSync } from "node:fs";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const PLANET_ID = parseInt(process.argv[2] || "117", 10);
const OUT_WIDTH = parseInt(process.argv[3] || "800", 10);
const OUT_FILE = process.argv[4] || `minimap-planet-${PLANET_ID}.png`;

const NODE_X_COUNT = 160;
const NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.1;
const HEIGHT_MIN = -200;

function uint16ToHeight(uint16) {
  return uint16 * HEIGHT_PRECISION + HEIGHT_MIN;
}

function heightToColor(h) {
  if (h < -5)  return [10, 30, 100];      // Deep water
  if (h < 0)   return [30, 80, 170];      // Shallow water
  if (h < 1)   return [160, 200, 100];    // Beach/shore
  if (h < 3)   return [80, 170, 50];      // Low land
  if (h < 10)  return [100, 150, 40];     // Medium land
  if (h < 20)  return [120, 115, 70];     // Hills - earthy olive
  if (h < 30)  return [115, 110, 85];     // Mountain - gray-brown
  if (h < 50)  return [125, 120, 105];    // High mountain - rocky gray
  if (h < 80)  return [150, 148, 142];    // Very high mountain - light gray
  return [210, 210, 215];                 // Peak/snow
}

// --- CRC32 for PNG ---
const crcTable = [];
for (let n = 0; n < 256; n++) {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  crcTable[n] = c;
}
function crc32(buf) {
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) crc = crcTable[(crc ^ buf[i]) & 0xff] ^ (crc >>> 8);
  return (crc ^ 0xffffffff) >>> 0;
}
function pngChunk(type, data) {
  const typeBytes = new Uint8Array([type.charCodeAt(0), type.charCodeAt(1), type.charCodeAt(2), type.charCodeAt(3)]);
  const lenBuf = new Uint8Array(4);
  new DataView(lenBuf.buffer).setUint32(0, data.length, false);
  const crcInput = new Uint8Array(4 + data.length);
  crcInput.set(typeBytes, 0);
  crcInput.set(data, 4);
  const crcVal = crc32(crcInput);
  const crcBuf = new Uint8Array(4);
  new DataView(crcBuf.buffer).setUint32(0, crcVal, false);
  const chunk = new Uint8Array(4 + 4 + data.length + 4);
  chunk.set(lenBuf, 0);
  chunk.set(typeBytes, 4);
  chunk.set(data, 8);
  chunk.set(crcBuf, 8 + data.length);
  return chunk;
}
function encodePNG(width, height, rgb) {
  const ihdr = new Uint8Array(13);
  const ihdrView = new DataView(ihdr.buffer);
  ihdrView.setUint32(0, width, false);
  ihdrView.setUint32(4, height, false);
  ihdr[8] = 8; ihdr[9] = 2; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
  const rowSize = 1 + width * 3;
  const raw = new Uint8Array(height * rowSize);
  for (let y = 0; y < height; y++) {
    raw[y * rowSize] = 0;
    raw.set(rgb.subarray(y * width * 3, (y + 1) * width * 3), y * rowSize + 1);
  }
  const compressed = deflateSync(raw, { level: 6 });
  const signature = new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10]);
  const ihdrChunk = pngChunk("IHDR", ihdr);
  const idatChunk = pngChunk("IDAT", compressed);
  const iendChunk = pngChunk("IEND", new Uint8Array(0));
  const png = new Uint8Array(signature.length + ihdrChunk.length + idatChunk.length + iendChunk.length);
  let offset = 0;
  png.set(signature, offset); offset += signature.length;
  png.set(ihdrChunk, offset); offset += ihdrChunk.length;
  png.set(idatChunk, offset); offset += idatChunk.length;
  png.set(iendChunk, offset);
  return png;
}

// --- Main ---
async function main() {
  console.log(`Fetching terrain shape for planet ${PLANET_ID}...`);
  const shapeRes = await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`);
  if (!shapeRes.ok) throw new Error(`GET terrainshape failed: ${shapeRes.status}`);
  const terrainShape = await shapeRes.json();

  const tileXCount = terrainShape.nativeTerrainShapeTiles.length;
  const tileYCount = terrainShape.nativeTerrainShapeTiles[0].length;
  const totalXNodes = tileXCount * NODE_X_COUNT;
  const totalYNodes = tileYCount * NODE_Y_COUNT;
  console.log(`Terrain: ${totalXNodes} x ${totalYNodes} m (${tileXCount} x ${tileYCount} tiles)`);

  console.log("Fetching heightmap...");
  const hmRes = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
  if (!hmRes.ok) throw new Error(`GET terrainHeightMap failed: ${hmRes.status}`);
  let buffer = Buffer.from(await hmRes.arrayBuffer());
  if (buffer.length >= 2 && buffer[0] === 0x1f && buffer[1] === 0x8b) {
    buffer = gunzipSync(buffer);
  }
  const bytes = new Uint8Array(buffer);
  const heightMapLength = Math.floor(bytes.length / 2);
  const heightMap = new Uint16Array(heightMapLength);
  for (let i = 0; i < heightMapLength; i++) {
    heightMap[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);
  }

  const outWidth = Math.min(Math.max(OUT_WIDTH, 32), 2048);
  const step = Math.max(1, Math.ceil(totalXNodes / outWidth));
  const cols = Math.ceil(totalXNodes / step);
  const rows = Math.ceil(totalYNodes / step);
  console.log(`Rendering ${cols} x ${rows} px (1 px = ${step} m)...`);

  const rgb = new Uint8Array(cols * rows * 3);
  for (let oy = 0; oy < rows; oy++) {
    const srcY = (rows - 1 - oy) * step;
    for (let ox = 0; ox < cols; ox++) {
      const srcX = ox * step;
      const tileX = Math.floor(srcX / NODE_X_COUNT);
      const tileY = Math.floor(srcY / NODE_Y_COUNT);
      const localX = srcX % NODE_X_COUNT;
      const localY = srcY % NODE_Y_COUNT;
      const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + localY * NODE_X_COUNT + localX;
      let r = 0, g = 0, b = 0;
      if (idx < heightMapLength) {
        [r, g, b] = heightToColor(uint16ToHeight(heightMap[idx]));
      }
      const px = (oy * cols + ox) * 3;
      rgb[px] = r; rgb[px + 1] = g; rgb[px + 2] = b;
    }
  }

  const png = encodePNG(cols, rows, rgb);
  writeFileSync(OUT_FILE, png);
  console.log(`Saved: ${OUT_FILE} (${png.length} bytes)`);
}

main().catch(e => { console.error(e.message); process.exit(1); });
