/**
 * Procedural sprite sheet generator for terrain zones.
 * Creates 4x4 grids (256x256) with transparent backgrounds.
 * Run: node generate-sprites.js
 */
const fs = require('fs');
const zlib = require('zlib');

const SIZE = 256;
const CELL = 64;
const GRID = 4;

// ===== PNG encoder =====
function crc32(buf) {
  let c = 0xffffffff;
  const table = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let cr = n;
    for (let k = 0; k < 8; k++) cr = (cr & 1) ? (0xedb88320 ^ (cr >>> 1)) : (cr >>> 1);
    table[n] = cr;
  }
  for (let i = 0; i < buf.length; i++) c = table[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function pngChunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const typeData = Buffer.concat([Buffer.from(type), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(typeData));
  return Buffer.concat([len, typeData, crc]);
}

function writePNG(pixels, w, h, path) {
  const raw = Buffer.alloc(h * (1 + w * 4));
  for (let y = 0; y < h; y++) {
    raw[y * (1 + w * 4)] = 0;
    for (let x = 0; x < w; x++) {
      const si = (y * w + x) * 4;
      const di = y * (1 + w * 4) + 1 + x * 4;
      raw[di] = pixels[si];
      raw[di + 1] = pixels[si + 1];
      raw[di + 2] = pixels[si + 2];
      raw[di + 3] = pixels[si + 3];
    }
  }
  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8; ihdr[9] = 6;
  fs.writeFileSync(path, Buffer.concat([
    sig, pngChunk('IHDR', ihdr),
    pngChunk('IDAT', zlib.deflateSync(raw)),
    pngChunk('IEND', Buffer.alloc(0))
  ]));
}

// ===== Drawing helpers =====
function setPixel(px, x, y, r, g, b, a) {
  if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
  x = Math.floor(x); y = Math.floor(y);
  const i = (y * SIZE + x) * 4;
  if (a < 255 && px[i + 3] > 0) {
    const sa = a / 255, da = px[i + 3] / 255;
    const oa = sa + da * (1 - sa);
    px[i] = Math.round((r * sa + px[i] * da * (1 - sa)) / oa);
    px[i + 1] = Math.round((g * sa + px[i + 1] * da * (1 - sa)) / oa);
    px[i + 2] = Math.round((b * sa + px[i + 2] * da * (1 - sa)) / oa);
    px[i + 3] = Math.round(oa * 255);
  } else {
    px[i] = r; px[i + 1] = g; px[i + 2] = b; px[i + 3] = a;
  }
}

function fillCircle(px, cx, cy, radius, r, g, b, a) {
  const r2 = radius * radius;
  for (let dy = -radius; dy <= radius; dy++) {
    for (let dx = -radius; dx <= radius; dx++) {
      if (dx * dx + dy * dy <= r2) {
        setPixel(px, cx + dx, cy + dy, r, g, b, a);
      }
    }
  }
}

function fillEllipse(px, cx, cy, rx, ry, r, g, b, a) {
  for (let dy = -ry; dy <= ry; dy++) {
    for (let dx = -rx; dx <= rx; dx++) {
      if ((dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) <= 1) {
        setPixel(px, cx + dx, cy + dy, r, g, b, a);
      }
    }
  }
}

// Seeded random
let seed = 42;
function rand() {
  seed = (seed * 16807 + 0) % 2147483647;
  return (seed - 1) / 2147483646;
}

function randRange(min, max) { return min + rand() * (max - min); }
function randInt(min, max) { return Math.floor(randRange(min, max + 1)); }
function clamp(v, lo, hi) { return Math.max(lo, Math.min(hi, v)); }
function vary(base, amount) { return clamp(Math.round(base + (rand() - 0.5) * 2 * amount), 0, 255); }

// ===== UPPER: Dense grass tufts, bushy plants, drooping grass =====

/** Single curved grass blade with pointed tip */
function drawBlade(px, ox, oy, baseX, baseY, h, lean, thick, r, g, b, alphaBase) {
  for (let t = 0; t < h; t++) {
    const frac = t / h;
    const curve = lean * frac * frac;
    const x = ox + baseX + curve;
    const y = oy + baseY - t;
    // Taper from thick at base to pointed tip
    const w = thick * (1 - frac * 0.85);
    const shade = 0.75 + frac * 0.25; // lighter tips
    const alpha = Math.round(alphaBase * (1 - frac * 0.3));
    for (let dx = -w; dx <= w; dx++) {
      const edge = 1 - Math.abs(dx) / (w + 0.5);
      const a = Math.max(0, Math.min(255, Math.round(alpha * edge)));
      setPixel(px, x + dx, y, Math.round(r * shade), Math.round(g * shade), Math.round(b * shade), a);
    }
  }
}

/** Dense grass tuft — fan of blades radiating from center base (like reference row 1 col 3, row 3 col 1) */
function drawGrassTuft(px, ox, oy) {
  const count = randInt(14, 24);
  const baseY = 60 + randInt(-2, 2);
  const centerX = 32;
  // Back layer (darker, shorter)
  for (let i = 0; i < Math.floor(count * 0.4); i++) {
    const bx = centerX + randRange(-10, 10);
    const h = randRange(16, 30);
    const lean = randRange(-10, 10);
    drawBlade(px, ox, oy, bx, baseY, h, lean, randRange(1.2, 2.0),
      vary(25, 10), vary(60, 15), vary(15, 8), 180);
  }
  // Front layer (main blades, brighter)
  for (let i = 0; i < Math.ceil(count * 0.6); i++) {
    const bx = centerX + randRange(-12, 12);
    const h = randRange(20, 38);
    const lean = randRange(-12, 12);
    drawBlade(px, ox, oy, bx, baseY, h, lean, randRange(1.0, 2.2),
      vary(30, 12), vary(75, 20), vary(18, 8), 220);
  }
  // Base fill — small dark ellipse at ground level
  fillEllipse(px, ox + centerX, oy + baseY, randRange(8, 14), randRange(2, 4),
    vary(20, 8), vary(45, 12), vary(12, 6), 160);
}

/** Drooping/cascading grass — long arching blades that droop outward (like reference row 3 col 3) */
function drawDroopingGrass(px, ox, oy) {
  const count = randInt(10, 18);
  const baseY = 56 + randInt(-2, 2);
  const centerX = 32;
  for (let i = 0; i < count; i++) {
    const side = rand() > 0.5 ? 1 : -1;
    const lean = side * randRange(6, 18);
    const h = randRange(22, 42);
    const bx = centerX + randRange(-6, 6);
    // Drooping: blade curves strongly outward and slightly downward at tip
    const thick = randRange(0.8, 1.8);
    const r = vary(28, 12), g = vary(70, 18), b = vary(16, 8);
    for (let t = 0; t < h; t++) {
      const frac = t / h;
      const curve = lean * frac * frac * 1.3;
      // Droop: after 70% height, start going down
      const droopY = frac > 0.7 ? (frac - 0.7) * (frac - 0.7) * 30 : 0;
      const x = ox + bx + curve;
      const y = oy + baseY - t + droopY;
      const w = thick * (1 - frac * 0.7);
      const shade = 0.7 + frac * 0.3;
      const alpha = Math.round(210 * (1 - frac * 0.4));
      for (let dx = -w; dx <= w; dx++) {
        const edge = 1 - Math.abs(dx) / (w + 0.5);
        const a = Math.max(0, Math.min(255, Math.round(alpha * edge)));
        setPixel(px, x + dx, y, Math.round(r * shade), Math.round(g * shade), Math.round(b * shade), a);
      }
    }
  }
  // Dark base
  fillEllipse(px, ox + centerX, oy + baseY, randRange(6, 10), randRange(2, 3),
    vary(18, 6), vary(40, 10), vary(10, 5), 140);
}

/** Small bushy plant — compact with leaf clusters (like reference row 1 col 1 shrub) */
function drawBushyPlant(px, ox, oy) {
  const baseY = 58 + randInt(-2, 2);
  const centerX = 32;
  // Short stems radiating outward
  const stemCount = randInt(5, 9);
  for (let i = 0; i < stemCount; i++) {
    const angle = randRange(-1.2, 1.2);
    const len = randRange(10, 22);
    const r = vary(25, 10), g = vary(55, 15), b = vary(14, 6);
    for (let t = 0; t < len; t++) {
      const frac = t / len;
      const x = ox + centerX + Math.sin(angle) * t * 0.8;
      const y = oy + baseY - t * Math.cos(angle) * 0.9;
      setPixel(px, x, y, r, g, b, 200);
      setPixel(px, x + 1, y, r, g, b, 150);
    }
    // Leaf cluster at tip
    const tipX = ox + centerX + Math.sin(angle) * len * 0.8;
    const tipY = oy + baseY - len * Math.cos(angle) * 0.9;
    const leafR = randRange(3, 6);
    fillEllipse(px, tipX, tipY, leafR, leafR * 0.7,
      vary(30, 12), vary(68, 18), vary(16, 8), vary(190, 30));
    // Highlight on leaf
    fillEllipse(px, tipX - 1, tipY - 1, leafR * 0.4, leafR * 0.3,
      vary(40, 10), vary(85, 15), vary(22, 8), 120);
  }
  // Dark base
  fillEllipse(px, ox + centerX, oy + baseY, randRange(5, 8), randRange(2, 3),
    vary(18, 6), vary(38, 10), vary(10, 5), 150);
}

/** Spiky grass — tall upright blades like ornamental grass (like reference row 1 col 2) */
function drawSpikyGrass(px, ox, oy) {
  const count = randInt(8, 16);
  const baseY = 60 + randInt(-2, 2);
  const centerX = 32;
  for (let i = 0; i < count; i++) {
    const bx = centerX + randRange(-8, 8);
    const h = randRange(25, 45);
    const lean = randRange(-4, 4);
    const thick = randRange(0.8, 1.5);
    drawBlade(px, ox, oy, bx, baseY, h, lean, thick,
      vary(30, 12), vary(72, 18), vary(18, 8), 230);
  }
  // Fill base
  fillEllipse(px, ox + centerX, oy + baseY, randRange(6, 10), randRange(2, 4),
    vary(22, 8), vary(48, 12), vary(12, 6), 170);
}

function generateUpper() {
  const px = new Uint8Array(SIZE * SIZE * 4);
  const drawFuncs = [drawGrassTuft, drawSpikyGrass, drawBushyPlant, drawDroopingGrass,
                     drawSpikyGrass, drawGrassTuft, drawDroopingGrass, drawBushyPlant,
                     drawDroopingGrass, drawBushyPlant, drawGrassTuft, drawSpikyGrass,
                     drawBushyPlant, drawGrassTuft, drawSpikyGrass, drawDroopingGrass];
  for (let cy = 0; cy < GRID; cy++) {
    for (let cx = 0; cx < GRID; cx++) {
      seed = 100 + cy * GRID + cx;
      const idx = cy * GRID + cx;
      drawFuncs[idx](px, cx * CELL, cy * CELL);
    }
  }
  return px;
}

// ===== UNDER: Angular stones & pebbles in warm earth/sand tones =====

/** Generate a random angular polygon (convex-ish) with numVerts vertices */
function makeAngularShape(cx, cy, rx, ry, numVerts) {
  const verts = [];
  const baseAngle = rand() * Math.PI * 2;
  for (let i = 0; i < numVerts; i++) {
    const angle = baseAngle + (i / numVerts) * Math.PI * 2 + randRange(-0.3, 0.3);
    const rFrac = randRange(0.6, 1.0);
    verts.push([cx + Math.cos(angle) * rx * rFrac, cy + Math.sin(angle) * ry * rFrac]);
  }
  return verts;
}

/** Check if point is inside polygon (ray casting) */
function pointInPoly(x, y, verts) {
  let inside = false;
  for (let i = 0, j = verts.length - 1; i < verts.length; j = i++) {
    const xi = verts[i][0], yi = verts[i][1];
    const xj = verts[j][0], yj = verts[j][1];
    if ((yi > y) !== (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) {
      inside = !inside;
    }
  }
  return inside;
}

function drawStone(px, cx, cy, rx, ry) {
  const palettes = [
    [120, 115, 105], [100, 95, 88], [110, 105, 95],
    [90, 85, 78],   [105, 100, 92],
  ];
  const pal = palettes[randInt(0, palettes.length - 1)];
  const baseR = vary(pal[0], 15), baseG = vary(pal[1], 12), baseB = vary(pal[2], 10);
  const numVerts = randInt(5, 8);
  const verts = makeAngularShape(cx, cy, rx, ry, numVerts);

  for (let dy = -ry - 2; dy <= ry + 2; dy++) {
    for (let dx = -rx - 2; dx <= rx + 2; dx++) {
      const px2 = cx + dx, py2 = cy + dy;
      if (!pointInPoly(px2, py2, verts)) continue;
      // Lighting: top-left lighter
      const light = 1.0 + (-dx / rx + -dy / ry) * 0.18;
      // Distance from center for edge darkening
      const dist = Math.sqrt(dx * dx / (rx * rx) + dy * dy / (ry * ry));
      const edge = 1.0 - Math.pow(Math.min(dist, 1), 3) * 0.25;
      const shade = light * edge;
      const noise = 1.0 + (rand() - 0.5) * 0.15;
      const r = clamp(Math.round(baseR * shade * noise), 0, 255);
      const g = clamp(Math.round(baseG * shade * noise), 0, 255);
      const b = clamp(Math.round(baseB * shade * noise), 0, 255);
      setPixel(px, px2, py2, r, g, b, 245);
    }
  }
  // Edge highlight on top edge — draw along polygon edges for angular look
  for (let i = 0; i < verts.length; i++) {
    const v0 = verts[i], v1 = verts[(i + 1) % verts.length];
    const edgeLen = Math.sqrt((v1[0]-v0[0])**2 + (v1[1]-v0[1])**2);
    for (let t = 0; t < edgeLen; t++) {
      const frac = t / edgeLen;
      const ex = v0[0] + (v1[0]-v0[0]) * frac;
      const ey = v0[1] + (v1[1]-v0[1]) * frac;
      // Only highlight top-ish edges
      if (ey < cy) {
        setPixel(px, ex, ey, clamp(baseR + 30, 0, 255), clamp(baseG + 25, 0, 255), clamp(baseB + 20, 0, 255), 120);
      } else {
        setPixel(px, ex, ey, clamp(baseR - 35, 0, 255), clamp(baseG - 30, 0, 255), clamp(baseB - 25, 0, 255), 100);
      }
    }
  }
}

/** Scattered small angular pebbles */
function drawPebbleScatter(px, ox, oy) {
  const count = randInt(6, 12);
  for (let i = 0; i < count; i++) {
    const cx = ox + 32 + randRange(-22, 22);
    const cy = oy + 38 + randRange(-14, 18);
    drawStone(px, cx, cy, randRange(2, 6), randRange(2, 5));
  }
}

/** Medium stones with smaller ones around */
function drawPebbleGroup(px, ox, oy) {
  const medCount = randInt(2, 4);
  for (let i = 0; i < medCount; i++) {
    const cx = ox + 32 + randRange(-14, 14);
    const cy = oy + 38 + randRange(-10, 14);
    drawStone(px, cx, cy, randRange(5, 10), randRange(3, 7));
  }
  const tinyCount = randInt(4, 8);
  for (let i = 0; i < tinyCount; i++) {
    const cx = ox + 32 + randRange(-22, 22);
    const cy = oy + 38 + randRange(-14, 18);
    drawStone(px, cx, cy, randRange(2, 4), randRange(1.5, 3));
  }
}

/** Dirt specks and small angular pebbles */
function drawDirtPatch(px, ox, oy) {
  const count = randInt(15, 30);
  for (let i = 0; i < count; i++) {
    const x = ox + 32 + randRange(-24, 24);
    const y = oy + 38 + randRange(-18, 20);
    const r = randRange(0.5, 2);
    fillCircle(px, x, y, r, vary(105, 15), vary(100, 12), vary(90, 10), vary(160, 40));
  }
  for (let i = 0; i < 4; i++) {
    drawStone(px, ox + 32 + randRange(-18, 18), oy + 38 + randRange(-10, 12), randRange(2, 5), randRange(2, 4));
  }
}

/** Bigger angular stone with small pebbles around */
function drawStoneCluster(px, ox, oy) {
  const cx = ox + 32 + randRange(-6, 6);
  const cy = oy + 38 + randRange(-3, 5);
  drawStone(px, cx, cy, randRange(8, 14), randRange(5, 10));
  const count = randInt(4, 8);
  for (let i = 0; i < count; i++) {
    drawStone(px, cx + randRange(-16, 16), cy + randRange(-8, 14), randRange(2, 5), randRange(2, 4));
  }
}

function generateUnder() {
  const px = new Uint8Array(SIZE * SIZE * 4);
  const drawFuncs = [drawPebbleScatter, drawPebbleGroup, drawDirtPatch, drawStoneCluster,
                     drawDirtPatch, drawPebbleScatter, drawStoneCluster, drawPebbleGroup,
                     drawStoneCluster, drawDirtPatch, drawPebbleScatter, drawPebbleGroup,
                     drawPebbleGroup, drawStoneCluster, drawDirtPatch, drawPebbleScatter];
  for (let cy = 0; cy < GRID; cy++) {
    for (let cx = 0; cx < GRID; cx++) {
      seed = 200 + cy * GRID + cx;
      drawFuncs[cy * GRID + cx](px, cx * CELL, cy * CELL);
    }
  }
  return px;
}

// ===== BEACH: Shells, driftwood, beach grass =====
function drawShell(px, cx, cy) {
  const size = randRange(3, 7);
  const shellR = vary(185, 15), shellG = vary(170, 12), shellB = vary(140, 10);
  // Fan shape
  for (let angle = -1.2; angle <= 1.2; angle += 0.05) {
    for (let r = 1; r < size; r++) {
      const x = cx + Math.cos(angle) * r;
      const y = cy - Math.sin(angle) * r * 0.7;
      const shade = 0.8 + Math.sin(angle * 5) * 0.15;
      const edgeFade = 1.0 - (r / size) * 0.2;
      setPixel(px, x, y,
        clamp(Math.round(shellR * shade * edgeFade), 0, 255),
        clamp(Math.round(shellG * shade * edgeFade), 0, 255),
        clamp(Math.round(shellB * shade * edgeFade), 0, 255), 230);
    }
  }
  // Ridge lines
  for (let r = 2; r < size; r += 2) {
    for (let angle = -1.0; angle <= 1.0; angle += 0.08) {
      const x = cx + Math.cos(angle) * r;
      const y = cy - Math.sin(angle) * r * 0.7;
      setPixel(px, x, y, shellR - 40, shellG - 40, shellB - 30, 150);
    }
  }
}

function drawDriftwood(px, ox, oy) {
  const startX = ox + 10 + randRange(0, 10);
  const startY = oy + 38 + randRange(-5, 10);
  const len = randRange(25, 44);
  const angle = randRange(-0.3, 0.3);
  const thick = randRange(2, 4);
  const woodR = vary(145, 18), woodG = vary(125, 15), woodB = vary(90, 12);
  for (let t = 0; t < len; t++) {
    const x = startX + Math.cos(angle) * t;
    const y = startY + Math.sin(angle) * t;
    const wobble = Math.sin(t * 0.5) * 0.5;
    for (let w = -thick; w <= thick; w++) {
      const frac = Math.abs(w) / thick;
      const shade = 0.7 + (1 - frac) * 0.3;
      const noise = 1 + (rand() - 0.5) * 0.1;
      const alpha = frac > 0.8 ? Math.round(255 * (1 - (frac - 0.8) / 0.2)) : 255;
      setPixel(px, x, y + w + wobble,
        clamp(Math.round(woodR * shade * noise), 0, 255),
        clamp(Math.round(woodG * shade * noise), 0, 255),
        clamp(Math.round(woodB * shade * noise), 0, 255), alpha);
    }
    // Wood grain lines
    if (t % 4 === 0) {
      for (let w = -thick + 1; w < thick; w++) {
        setPixel(px, x, y + w, woodR - 25, woodG - 25, woodB - 20, 80);
      }
    }
  }
}

function drawBeachShells(px, ox, oy) {
  const count = randInt(2, 5);
  for (let i = 0; i < count; i++) {
    drawShell(px, ox + 32 + randRange(-18, 18), oy + 38 + randRange(-10, 14));
  }
  // Scatter tiny sand pebbles
  for (let i = 0; i < 8; i++) {
    const x = ox + 32 + randRange(-22, 22);
    const y = oy + 42 + randRange(-12, 12);
    fillCircle(px, x, y, randRange(1, 2), vary(170, 15), vary(155, 12), vary(125, 10), vary(140, 30));
  }
}

function drawBeachGrass(px, ox, oy) {
  const count = randInt(4, 8);
  const baseY = 58 + randInt(-2, 2);
  for (let i = 0; i < count; i++) {
    const bx = 32 + randRange(-14, 14);
    const h = randRange(15, 30);
    const lean = randRange(-8, 8);
    // Pale yellowish-green
    drawBlade(px, ox, oy, bx, baseY, h, lean, randRange(1.0, 1.8), vary(140, 20), vary(145, 20), vary(80, 15), 220);
  }
}

function generateBeach() {
  const px = new Uint8Array(SIZE * SIZE * 4);
  const drawFuncs = [drawBeachShells, drawDriftwood, drawBeachGrass, drawBeachShells,
                     drawBeachGrass, drawBeachShells, drawDriftwood, drawBeachGrass,
                     drawBeachShells, drawDriftwood, drawBeachShells, drawBeachGrass,
                     drawDriftwood, drawBeachShells, drawBeachGrass, drawBeachShells];
  for (let cy = 0; cy < GRID; cy++) {
    for (let cx = 0; cx < GRID; cx++) {
      seed = 300 + cy * GRID + cx;
      drawFuncs[cy * GRID + cx](px, cx * CELL, cy * CELL);
    }
  }
  return px;
}

// ===== UNDERWATER: Seaweed, algae, coral =====
function drawSeaweed(px, ox, oy) {
  const count = randInt(2, 5);
  const baseY = 60;
  for (let s = 0; s < count; s++) {
    const startX = ox + 32 + randRange(-16, 16);
    const h = randRange(18, 40);
    const waveFreq = randRange(0.15, 0.35);
    const waveAmp = randRange(3, 8);
    const thick = randRange(1.5, 3);
    const baseR = vary(20, 15), baseG = vary(90, 30), baseB = vary(40, 20);
    for (let t = 0; t < h; t++) {
      const frac = t / h;
      const wave = Math.sin(t * waveFreq + s * 2) * waveAmp * frac;
      const x = startX + wave;
      const y = oy + baseY - t;
      const w = thick * (1 - frac * 0.5);
      const shade = 0.6 + frac * 0.4;
      for (let dx = -w; dx <= w; dx++) {
        const alpha = Math.max(0, Math.min(230, Math.round(230 * (1 - Math.abs(dx) / (w + 0.5)))));
        setPixel(px, x + dx, y,
          clamp(Math.round(baseR * shade), 0, 255),
          clamp(Math.round(baseG * shade), 0, 255),
          clamp(Math.round(baseB * shade), 0, 255), alpha);
      }
      // Leaf blobs
      if (t % 5 === 0 && t > 3) {
        const side = (t % 10 === 0) ? 1 : -1;
        fillEllipse(px, x + side * (w + 2), y, 3, 2,
          clamp(baseR + 10, 0, 255), clamp(baseG + 20, 0, 255), clamp(baseB + 10, 0, 255), 180);
      }
    }
  }
}

function drawAlgaeCluster(px, ox, oy) {
  const count = randInt(5, 12);
  for (let i = 0; i < count; i++) {
    const cx = ox + 32 + randRange(-20, 20);
    const cy = oy + 42 + randRange(-12, 14);
    const rx = randRange(3, 8);
    const ry = randRange(2, 5);
    const r = vary(15, 10), g = vary(80, 30), b = vary(50, 20);
    fillEllipse(px, cx, cy, rx, ry, r, g, b, vary(160, 30));
    // Highlight
    fillEllipse(px, cx - 1, cy - 1, rx * 0.5, ry * 0.5, r + 25, g + 30, b + 15, 100);
  }
}

function drawCoralBit(px, ox, oy) {
  const cx = ox + 32 + randRange(-8, 8);
  const cy = oy + 44 + randRange(-5, 8);
  // Coral colors: dark greens and browns only
  const palettes = [[20, 70, 30], [50, 80, 25], [60, 50, 20], [30, 60, 20]];
  const pal = palettes[randInt(0, palettes.length - 1)];
  // Branching structure
  const branches = randInt(3, 6);
  for (let b = 0; b < branches; b++) {
    const angle = randRange(-Math.PI * 0.8, Math.PI * 0.8);
    const len = randRange(6, 14);
    for (let t = 0; t < len; t++) {
      const x = cx + Math.sin(angle) * t;
      const y = cy - t * Math.cos(angle) * 0.8;
      const thick = (1 - t / len) * 3 + 1;
      fillCircle(px, x, y, thick,
        vary(pal[0], 15), vary(pal[1], 15), vary(pal[2], 15), 220);
    }
    // Tip blob
    const tipX = cx + Math.sin(angle) * len;
    const tipY = cy - len * Math.cos(angle) * 0.8;
    fillCircle(px, tipX, tipY, randRange(2, 4), pal[0] + 20, pal[1] + 20, pal[2] + 20, 200);
  }
}

function drawBubbles(px, ox, oy) {
  // Seaweed base
  drawSeaweed(px, ox, oy);
  // Rising bubbles
  const bubbleCount = randInt(3, 7);
  for (let i = 0; i < bubbleCount; i++) {
    const bx = ox + 32 + randRange(-18, 18);
    const by = oy + randRange(8, 45);
    const br = randRange(1.5, 3.5);
    // Bubble: subtle blue-green
    fillCircle(px, bx, by, br, 60, 120, 100, 100);
    fillCircle(px, bx, by, br + 0.5, 70, 130, 110, 50);
    // Highlight dot
    setPixel(px, bx - 1, by - 1, 120, 170, 150, 140);
  }
}

function generateUnderwater() {
  const px = new Uint8Array(SIZE * SIZE * 4);
  const drawFuncs = [drawSeaweed, drawAlgaeCluster, drawCoralBit, drawSeaweed,
                     drawAlgaeCluster, drawBubbles, drawAlgaeCluster, drawSeaweed,
                     drawAlgaeCluster, drawSeaweed, drawBubbles, drawSeaweed,
                     drawSeaweed, drawCoralBit, drawAlgaeCluster, drawBubbles];
  for (let cy = 0; cy < GRID; cy++) {
    for (let cx = 0; cx < GRID; cx++) {
      seed = 400 + cy * GRID + cx;
      drawFuncs[cy * GRID + cx](px, cx * CELL, cy * CELL);
    }
  }
  return px;
}

// ===== Generate all sheets =====
const dir = __dirname + '/public';
console.log('Generating sprite sheets...');

writePNG(generateUpper(), SIZE, SIZE, dir + '/sprites_upper_4x4.png');
console.log('  ✓ sprites_upper_4x4.png (grass, flowers, ferns)');

writePNG(generateUnder(), SIZE, SIZE, dir + '/sprites_under_4x4.png');
console.log('  ✓ sprites_under_4x4.png (stones, pebbles, dirt)');

writePNG(generateBeach(), SIZE, SIZE, dir + '/sprites_beach_4x4.png');
console.log('  ✓ sprites_beach_4x4.png (shells, driftwood, beach grass)');

writePNG(generateUnderwater(), SIZE, SIZE, dir + '/sprites_underwater_4x4.png');
console.log('  ✓ sprites_underwater_4x4.png (seaweed, algae, coral)');

console.log('Done!');
