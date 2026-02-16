import { gunzipSync, deflateSync } from "node:zlib";
import { writeFileSync } from "node:fs";

const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const PLANET_ID = 117;
const OUT_WIDTH = 800;
const OUT_FILE = "phase-map.png";

const NODE_X_COUNT = 160;
const NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.1;
const HEIGHT_MIN = -200;

function uint16ToHeight(uint16) { return uint16 * HEIGHT_PRECISION + HEIGHT_MIN; }

function heightToColor(h) {
  if (h < -5)  return [10, 30, 100];
  if (h < 0)   return [30, 80, 170];
  if (h < 1)   return [160, 200, 100];
  if (h < 3)   return [80, 170, 50];
  if (h < 10)  return [100, 150, 40];
  if (h < 20)  return [170, 150, 60];
  if (h < 30)  return [150, 120, 60];
  if (h < 50)  return [130, 110, 90];
  if (h < 80)  return [160, 155, 150];
  return [230, 230, 235];
}

// --- Phase definitions (in game meters) ---
// The map is 5120x5120m. Bottom-left = (0,0). Y increases upward.
// Phase boundaries in game meters
const P1_X = 820;   // Bottom-left lake extends to ~X=810m
const P1_Y = 800;   // Bottom-left lake extends to ~Y=760m
const P2_MAX = 2000; // P2 wraps around P1, up to 2000m
const P3_MAX = 2500; // P3 wraps around P2, up to 2500m

const PHASES = [
  { name: "P1: Noob Island", color: [0, 150, 255] },
  { name: "P2: Frontier",    color: [0, 220, 80] },
  { name: "P3: The Siege",   color: [255, 180, 0] },
  { name: "P4: Warzone",     color: [220, 40, 40] },
];

function getPhase(gameX, gameY) {
  // P1: bottom-left = lake area
  if (gameX < P1_X && gameY < P1_Y) return 0;
  // P2: wraps around P1, up to P2_MAX
  if (gameX < P2_MAX && gameY < P2_MAX) return 1;
  // P3: right of P2, up to P3_MAX height
  if (gameX >= P2_MAX && gameY < P3_MAX) return 2;
  // P4: everything else (above P2 + above P3)
  return 3;
}

// --- PNG encoding (same as before) ---
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
  const t = new Uint8Array([type.charCodeAt(0), type.charCodeAt(1), type.charCodeAt(2), type.charCodeAt(3)]);
  const l = new Uint8Array(4); new DataView(l.buffer).setUint32(0, data.length, false);
  const ci = new Uint8Array(4 + data.length); ci.set(t, 0); ci.set(data, 4);
  const cv = crc32(ci);
  const cb = new Uint8Array(4); new DataView(cb.buffer).setUint32(0, cv, false);
  const ch = new Uint8Array(4 + 4 + data.length + 4);
  ch.set(l, 0); ch.set(t, 4); ch.set(data, 8); ch.set(cb, 8 + data.length);
  return ch;
}
function encodePNG(width, height, rgb) {
  const ihdr = new Uint8Array(13);
  const v = new DataView(ihdr.buffer);
  v.setUint32(0, width, false); v.setUint32(4, height, false);
  ihdr[8] = 8; ihdr[9] = 2;
  const rowSize = 1 + width * 3;
  const raw = new Uint8Array(height * rowSize);
  for (let y = 0; y < height; y++) {
    raw[y * rowSize] = 0;
    raw.set(rgb.subarray(y * width * 3, (y + 1) * width * 3), y * rowSize + 1);
  }
  const compressed = deflateSync(raw, { level: 6 });
  const sig = new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10]);
  const ic = pngChunk("IHDR", ihdr);
  const dc = pngChunk("IDAT", compressed);
  const ec = pngChunk("IEND", new Uint8Array(0));
  const png = new Uint8Array(sig.length + ic.length + dc.length + ec.length);
  let o = 0;
  png.set(sig, o); o += sig.length;
  png.set(ic, o); o += ic.length;
  png.set(dc, o); o += dc.length;
  png.set(ec, o);
  return png;
}

// --- Draw text labels (simple 3x5 bitmap font) ---
const FONT = {
  '0': [0b111,0b101,0b101,0b101,0b111],
  '1': [0b010,0b110,0b010,0b010,0b111],
  '2': [0b111,0b001,0b111,0b100,0b111],
  '3': [0b111,0b001,0b111,0b001,0b111],
  '4': [0b101,0b101,0b111,0b001,0b001],
  '5': [0b111,0b100,0b111,0b001,0b111],
  'P': [0b111,0b101,0b111,0b100,0b100],
  ':': [0b000,0b010,0b000,0b010,0b000],
  ' ': [0b000,0b000,0b000,0b000,0b000],
};

function drawChar(rgb, cols, rows, cx, cy, ch, color, scale) {
  const bitmap = FONT[ch];
  if (!bitmap) return;
  for (let row = 0; row < 5; row++) {
    for (let col = 0; col < 3; col++) {
      if (bitmap[row] & (1 << (2 - col))) {
        for (let sy = 0; sy < scale; sy++) {
          for (let sx = 0; sx < scale; sx++) {
            const px = cx + col * scale + sx;
            const py = cy + row * scale + sy;
            if (px >= 0 && px < cols && py >= 0 && py < rows) {
              const idx = (py * cols + px) * 3;
              rgb[idx] = color[0]; rgb[idx + 1] = color[1]; rgb[idx + 2] = color[2];
            }
          }
        }
      }
    }
  }
}

function drawText(rgb, cols, rows, x, y, text, color, scale) {
  for (let i = 0; i < text.length; i++) {
    drawChar(rgb, cols, rows, x + i * (3 * scale + scale), y, text[i], color, scale);
  }
}

// --- Main ---
async function main() {
  console.log("Fetching terrain...");
  const shapeRes = await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`);
  const terrainShape = await shapeRes.json();
  const tileXCount = terrainShape.nativeTerrainShapeTiles.length;
  const tileYCount = terrainShape.nativeTerrainShapeTiles[0].length;
  const totalXNodes = tileXCount * NODE_X_COUNT;
  const totalYNodes = tileYCount * NODE_Y_COUNT;

  const hmRes = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
  let buffer = Buffer.from(await hmRes.arrayBuffer());
  if (buffer[0] === 0x1f && buffer[1] === 0x8b) buffer = gunzipSync(buffer);
  const bytes = new Uint8Array(buffer);
  const hmLen = Math.floor(bytes.length / 2);
  const hm = new Uint16Array(hmLen);
  for (let i = 0; i < hmLen; i++) hm[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);

  const step = Math.max(1, Math.ceil(totalXNodes / OUT_WIDTH));
  const cols = Math.ceil(totalXNodes / step);
  const rows = Math.ceil(totalYNodes / step);
  console.log(`Rendering ${cols}x${rows} px...`);

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
      if (idx < hmLen) [r, g, b] = heightToColor(uint16ToHeight(hm[idx]));

      // Game coordinates
      const gameX = srcX; // 1 node = 1 meter
      const gameY = srcY;

      // Phase overlay (semi-transparent tint)
      const phase = getPhase(gameX, gameY);
      const pc = PHASES[phase].color;
      const blend = 0.25; // 25% phase color tint
      r = Math.round(r * (1 - blend) + pc[0] * blend);
      g = Math.round(g * (1 - blend) + pc[1] * blend);
      b = Math.round(b * (1 - blend) + pc[2] * blend);

      // Draw phase boundary lines (thicker)
      const bw = 2;
      const onP1Top    = Math.abs(gameY - P1_Y) < step * bw && gameX < P1_X;
      const onP1Right  = Math.abs(gameX - P1_X) < step * bw && gameY < P1_Y;
      const onP2Top    = Math.abs(gameY - P2_MAX) < step * bw && gameX < P2_MAX;
      const onP2Right  = Math.abs(gameX - P2_MAX) < step * bw && gameY < P2_MAX;
      const onP2Top2   = Math.abs(gameY - P2_MAX) < step * bw && gameX < P2_MAX; // P2 top = P4 start
      const onP3Top    = Math.abs(gameY - P3_MAX) < step * bw && gameX >= P2_MAX;
      const onP3Left   = Math.abs(gameX - P2_MAX) < step * bw && gameY >= P2_MAX && gameY < P3_MAX;

      if (onP1Top || onP1Right || onP2Top || onP2Right || onP2Top2 || onP3Top || onP3Left) {
        r = 255; g = 255; b = 255;
      }

      const px = (oy * cols + ox) * 3;
      rgb[px] = r; rgb[px + 1] = g; rgb[px + 2] = b;
    }
  }

  // Draw phase labels
  const scale = 4;
  const mToP = (m) => Math.round(m / step); // meters to pixels

  // P1 label: center of Phase 1
  drawText(rgb, cols, rows, mToP(300), rows - mToP(500), "P1", [255, 255, 255], scale);
  // P2 label: center of Phase 2 area
  drawText(rgb, cols, rows, mToP(1200), rows - mToP(1200), "P2", [255, 255, 255], scale);
  // P3 label: center of P3 band
  drawText(rgb, cols, rows, mToP(3500), rows - mToP(1200), "P3", [255, 255, 255], scale);
  // P4 label: center of P4 area
  drawText(rgb, cols, rows, mToP(2200), rows - mToP(3500), "P4", [255, 255, 255], scale);

  // Add coordinate markers along edges
  const markerScale = 2;
  for (let m = 0; m <= 5000; m += 1000) {
    const px = mToP(m);
    const label = `${m / 1000}`;
    // Bottom edge (X axis)
    drawText(rgb, cols, rows, px + 2, rows - 12, label, [255, 255, 255], markerScale);
    // Left edge (Y axis)
    drawText(rgb, cols, rows, 2, rows - px - 12, label, [255, 255, 255], markerScale);
  }

  const png = encodePNG(cols, rows, rgb);
  writeFileSync(OUT_FILE, png);
  console.log(`Saved: ${OUT_FILE}`);
  console.log(`\nPhase layout (game coordinates, Y=0 is bottom):`);
  console.log(`  P1 Noob Island:  (0-1300, 0-1300)     - bottom-left`);
  console.log(`  P2 Frontier:     (0-3200, 0-3200)     - minus P1 area`);
  console.log(`  P3 The Siege:    top strip + right strip (minus P4)`);
  console.log(`  P4 Warzone:      (3200-5120, 3200-5120) - top-right`);
}

main().catch(e => { console.error(e); process.exit(1); });
