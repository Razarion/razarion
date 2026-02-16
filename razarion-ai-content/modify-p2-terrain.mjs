/**
 * modify-p2-terrain.mjs
 *
 * Generates procedural terrain for the P2 region (X: 820-2000, Y: 800-2000)
 * of planet 117. P1 (X: 0-820, Y: 0-800) remains completely untouched.
 *
 * Features:
 *   - 3 small lakes as orientation points / obstacles
 *   - Hill ridge along Y≈1500 as natural barrier
 *   - Elevated plateau at (1800, 850) for bot outpost
 *   - Scattered hills for variety
 *   - Multi-octave noise for natural-looking base variation
 *   - Smooth blending at P2 boundaries
 *
 * Usage:
 *   node modify-p2-terrain.mjs              # generate + upload
 *   node modify-p2-terrain.mjs --dry-run    # generate only, no DB upload
 */
import { gunzipSync, gzipSync, deflateSync } from "node:zlib";
import { writeFileSync } from "node:fs";
import { execSync } from "node:child_process";

// --- Configuration ---
const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const PLANET_ID = 117;
const DRY_RUN = process.argv.includes("--dry-run");

const NODE_X_COUNT = 160;
const NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.1;
const HEIGHT_MIN = -200;

// Phase boundaries (game meters)
const P1_X = 820;
const P1_Y = 800;
const P2_MAX = 2000;

// Blend zone width in meters at P2 boundaries
const BLEND_ZONE = 120;

const OUT_FILE = "p2-heightmap.bin.gz";

// =============================================================================
// Height conversion
// =============================================================================
function uint16ToHeight(v) {
  return v * HEIGHT_PRECISION + HEIGHT_MIN;
}
function heightToUint16(h) {
  return Math.max(0, Math.min(65535, Math.round((h - HEIGHT_MIN) / HEIGHT_PRECISION)));
}

// =============================================================================
// Heightmap indexing  (tile-order layout)
// =============================================================================
function getIndex(x, y, tileXCount) {
  const tileX = Math.floor(x / NODE_X_COUNT);
  const tileY = Math.floor(y / NODE_Y_COUNT);
  const localX = x % NODE_X_COUNT;
  const localY = y % NODE_Y_COUNT;
  return TILE_NODE_SIZE * (tileY * tileXCount + tileX) + localY * NODE_X_COUNT + localX;
}

// =============================================================================
// Is point inside P2?
// =============================================================================
function isP2(x, y) {
  if (x >= P2_MAX || y >= P2_MAX) return false;
  if (x < P1_X && y < P1_Y) return false;  // P1
  return true;
}

// =============================================================================
// Noise functions  (hash-based value noise + FBM)
// =============================================================================
function hash2d(ix, iy) {
  // Robert Jenkins' 32-bit integer hash, seeded per (ix, iy)
  let h = (ix * 374761393 + iy * 668265263 + 1013904223) | 0;
  h = ((h ^ (h >>> 13)) * 1274126177) | 0;
  h = (h ^ (h >>> 16)) | 0;
  return (h & 0x7fffffff) / 0x7fffffff; // 0..1
}

function smoothstep(t) {
  const c = Math.max(0, Math.min(1, t));
  return c * c * (3 - 2 * c);
}

function noise2d(x, y) {
  const ix = Math.floor(x);
  const iy = Math.floor(y);
  const fx = smoothstep(x - ix);
  const fy = smoothstep(y - iy);
  const n00 = hash2d(ix, iy);
  const n10 = hash2d(ix + 1, iy);
  const n01 = hash2d(ix, iy + 1);
  const n11 = hash2d(ix + 1, iy + 1);
  return (n00 * (1 - fx) + n10 * fx) * (1 - fy) +
         (n01 * (1 - fx) + n11 * fx) * fy;
}

/** Fractional Brownian Motion  (layered noise) */
function fbm(x, y, octaves = 5, lacunarity = 2.0, gain = 0.5) {
  let value = 0, amplitude = 1, frequency = 1, maxAmp = 0;
  for (let i = 0; i < octaves; i++) {
    value += noise2d(x * frequency, y * frequency) * amplitude;
    maxAmp += amplitude;
    amplitude *= gain;
    frequency *= lacunarity;
  }
  return value / maxAmp; // normalised to 0..1
}

// =============================================================================
// Blend factor: 0 at P2 boundaries, 1 deep inside P2
// =============================================================================
function getBlendFactor(x, y) {
  let minDist = Infinity;

  // Distance to outer P2 boundary
  minDist = Math.min(minDist, P2_MAX - x);
  minDist = Math.min(minDist, P2_MAX - y);

  // Distance to map edge
  minDist = Math.min(minDist, x);
  minDist = Math.min(minDist, y);

  // Distance to P1 boundary (inner boundary of P2's L-shape)
  if (x < P1_X) {
    // Left of P1 right edge → distance to y = P1_Y (P1 top edge)
    minDist = Math.min(minDist, y - P1_Y);
  }
  if (y < P1_Y) {
    // Below P1 top edge → distance to x = P1_X (P1 right edge)
    minDist = Math.min(minDist, x - P1_X);
  }
  // Corner zone (near both P1 edges)
  if (x >= P1_X && x < P1_X + BLEND_ZONE && y >= P1_Y && y < P1_Y + BLEND_ZONE) {
    const dx = x - P1_X;
    const dy = y - P1_Y;
    minDist = Math.min(minDist, Math.sqrt(dx * dx + dy * dy));
  }

  if (minDist <= 0) return 0;
  if (minDist >= BLEND_ZONE) return 1;
  return smoothstep(minDist / BLEND_ZONE);
}

// =============================================================================
// Terrain feature primitives
// =============================================================================

/**
 * Apply lake: OVERRIDES current height (not additive).
 * Flat bottom with smooth shore transition.
 * Returns the lower of currentH and the lake profile.
 */
function applyLake(currentH, x, y, cx, cy, rx, ry, depth) {
  const dx = (x - cx) / rx;
  const dy = (y - cy) / ry;
  const d2 = dx * dx + dy * dy;
  if (d2 >= 1) return currentH;
  const d = Math.sqrt(d2);
  // Flat bottom: inner 70% at full depth, outer 30% = smooth shore ramp
  const shoreWidth = 0.30;
  let lakeH;
  if (d <= 1 - shoreWidth) {
    lakeH = depth; // flat bottom, no noise
  } else {
    const t = (1 - d) / shoreWidth; // 1 at inner shore edge, 0 at waterline
    lakeH = depth * smoothstep(t);
  }
  return Math.min(currentH, lakeH);
}

/** Elongated ridge between two endpoints. */
function ridgeFeature(x, y, x1, y1, x2, y2, halfWidth, height) {
  const ldx = x2 - x1, ldy = y2 - y1;
  const len2 = ldx * ldx + ldy * ldy;
  if (len2 === 0) return 0;
  let t = ((x - x1) * ldx + (y - y1) * ldy) / len2;
  t = Math.max(0, Math.min(1, t));
  const px = x1 + t * ldx;
  const py = y1 + t * ldy;
  const dist = Math.sqrt((x - px) ** 2 + (y - py) ** 2);
  if (dist >= halfWidth) return 0;
  const s = 1 - dist / halfWidth;
  return height * s * s;
}

/** Circular raised plateau / hill. */
function hillFeature(x, y, cx, cy, radius, height) {
  const d = Math.sqrt((x - cx) ** 2 + (y - cy) ** 2);
  if (d >= radius) return 0;
  return height * smoothstep(1 - d / radius);
}

// =============================================================================
// Compute target height for a P2 point
// =============================================================================
function computeP2Height(x, y) {
  // --- Base noise: gentle rolling terrain, centered at +2m to avoid accidental water ---
  let h = 2;
  h += (fbm(x * 0.005, y * 0.005, 5) - 0.5) * 5;           // ±2.5m
  h += (fbm(x * 0.002 + 50, y * 0.002 + 50, 3) - 0.5) * 3; // ±1.5m (broader)

  // --- Main ridge "Barrier Ridge" dividing P2 ---
  h += ridgeFeature(x, y, 920, 1480, 1850, 1420, 130, 22);
  // Secondary ridge spur
  h += ridgeFeature(x, y, 1100, 1380, 1550, 1550, 90, 14);
  // Small ridge near bottom-right
  h += ridgeFeature(x, y, 1700, 300, 1900, 550, 70, 10);

  // --- Elevated plateau "High Ground" (bot outpost) ---
  h += hillFeature(x, y, 1800, 850, 160, 13);

  // --- Scattered hills ---
  h += hillFeature(x, y, 1000, 500, 90, 9);    // Guard Hill
  h += hillFeature(x, y, 1400, 1050, 110, 11);  // Overlook
  h += hillFeature(x, y, 550, 1700, 75, 8);     // West Knoll
  h += hillFeature(x, y, 1750, 1750, 100, 10);  // East Knoll
  h += hillFeature(x, y, 1900, 1250, 85, 7);    // Outpost Hill
  h += hillFeature(x, y, 300, 1000, 70, 6);     // Sentinel

  // --- Lakes: OVERRIDE terrain (applied last, carves into landscape) ---
  // Lake 1 "Frontier Lake": bottom strip of P2
  h = applyLake(h, x, y, 1600, 400, 120, 90, -15);
  // Lake 2 "Hidden Pond": upper P2 area, behind the ridge
  h = applyLake(h, x, y, 1200, 1650, 90, 70, -12);
  // Lake 3 "Marsh Pond": west side of upper P2
  h = applyLake(h, x, y, 450, 1250, 55, 65, -8);

  // Clamp to sane range
  return Math.max(-50, Math.min(60, h));
}

// =============================================================================
// Main
// =============================================================================
async function main() {
  console.log("=== P2 Terrain Generator for Planet 117 ===\n");

  // 1) Fetch terrain shape to know tile layout
  console.log("Fetching terrain shape...");
  const shapeRes = await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`);
  if (!shapeRes.ok) throw new Error(`GET terrainshape failed: ${shapeRes.status}`);
  const terrainShape = await shapeRes.json();
  const tileXCount = terrainShape.nativeTerrainShapeTiles.length;
  const tileYCount = terrainShape.nativeTerrainShapeTiles[0].length;
  const totalX = tileXCount * NODE_X_COUNT;
  const totalY = tileYCount * NODE_Y_COUNT;
  console.log(`  Map: ${totalX} x ${totalY} m  (${tileXCount} x ${tileYCount} tiles)\n`);

  // 2) Fetch current heightmap
  console.log("Fetching heightmap...");
  const hmRes = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
  if (!hmRes.ok) throw new Error(`GET terrainHeightMap failed: ${hmRes.status}`);
  let buffer = Buffer.from(await hmRes.arrayBuffer());
  if (buffer.length >= 2 && buffer[0] === 0x1f && buffer[1] === 0x8b) {
    buffer = gunzipSync(buffer);
  }
  const bytes = new Uint8Array(buffer);
  const hmLen = Math.floor(bytes.length / 2);
  const hm = new Uint16Array(hmLen);
  for (let i = 0; i < hmLen; i++) {
    hm[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);
  }
  console.log(`  Heightmap: ${hmLen} values (${bytes.length} bytes raw)\n`);

  // 3) Create a COPY of the heightmap and modify P2 nodes
  const modified = new Uint16Array(hm);
  let modifiedCount = 0;
  let minH = Infinity, maxH = -Infinity;

  console.log("Generating P2 terrain...");
  const xEnd = Math.min(totalX, P2_MAX);
  const yEnd = Math.min(totalY, P2_MAX);

  for (let y = 0; y < yEnd; y++) {
    for (let x = 0; x < xEnd; x++) {
      if (!isP2(x, y)) continue;

      const idx = getIndex(x, y, tileXCount);
      if (idx >= hmLen) continue;

      const existingH = uint16ToHeight(hm[idx]);
      const blend = getBlendFactor(x, y);
      const targetH = computeP2Height(x, y);
      const finalH = existingH * (1 - blend) + targetH * blend;

      modified[idx] = heightToUint16(finalH);
      modifiedCount++;
      if (finalH < minH) minH = finalH;
      if (finalH > maxH) maxH = finalH;
    }
  }

  console.log(`  Modified ${modifiedCount.toLocaleString()} nodes`);
  console.log(`  Height range: ${minH.toFixed(1)}m .. ${maxH.toFixed(1)}m\n`);

  // 4) Generate zoomed minimap of P2 from MODIFIED data + height histogram
  {
    // --- Height histogram for P2 ---
    const buckets = { "< -10m": 0, "-10 to -5m": 0, "-5 to -1m": 0, "-1 to 0m": 0, "0 to 3m": 0, "3 to 10m": 0, "> 10m": 0 };
    for (let y = 0; y < yEnd; y += 4) {
      for (let x = 0; x < xEnd; x += 4) {
        if (!isP2(x, y)) continue;
        const idx = getIndex(x, y, tileXCount);
        if (idx >= hmLen) continue;
        const h = uint16ToHeight(modified[idx]);
        if      (h < -10) buckets["< -10m"]++;
        else if (h < -5)  buckets["-10 to -5m"]++;
        else if (h < -1)  buckets["-5 to -1m"]++;
        else if (h < 0)   buckets["-1 to 0m"]++;
        else if (h < 3)   buckets["0 to 3m"]++;
        else if (h < 10)  buckets["3 to 10m"]++;
        else               buckets["> 10m"]++;
      }
    }
    console.log("  P2 height distribution (sampled every 4m):");
    for (const [k,v] of Object.entries(buckets)) console.log(`    ${k.padEnd(14)} ${v}`);

    // --- Zoomed minimap: just the P2 region (0-2100 x 0-2100), 1px = 3m ---
    const zoomStep = 3;
    const zoomXMax = 2100, zoomYMax = 2100;
    const cols = Math.ceil(zoomXMax / zoomStep);
    const rows = Math.ceil(zoomYMax / zoomStep);
    const rgb = new Uint8Array(cols * rows * 3);
    for (let oy = 0; oy < rows; oy++) {
      const srcY = (rows - 1 - oy) * zoomStep;
      for (let ox = 0; ox < cols; ox++) {
        const srcX = ox * zoomStep;
        const idx = getIndex(srcX, srcY, tileXCount);
        let r = 0, g = 0, b = 0;
        if (idx < hmLen) {
          const h = uint16ToHeight(modified[idx]);
          if      (h < -10) [r,g,b] = [5, 15, 60];
          else if (h < -5)  [r,g,b] = [10, 30, 100];
          else if (h < -2)  [r,g,b] = [20, 55, 140];
          else if (h < 0)   [r,g,b] = [40, 90, 180];
          else if (h < 1)   [r,g,b] = [160, 200, 100];
          else if (h < 3)   [r,g,b] = [80, 170, 50];
          else if (h < 10)  [r,g,b] = [100, 150, 40];
          else if (h < 20)  [r,g,b] = [170, 150, 60];
          else if (h < 30)  [r,g,b] = [150, 120, 60];
          else if (h < 50)  [r,g,b] = [130, 110, 90];
          else              [r,g,b] = [160, 155, 150];
        }
        const px = (oy * cols + ox) * 3;
        rgb[px] = r; rgb[px+1] = g; rgb[px+2] = b;
      }
    }
    // Minimal PNG encoder
    const ihdr = new Uint8Array(13);
    const iv = new DataView(ihdr.buffer);
    iv.setUint32(0, cols, false); iv.setUint32(4, rows, false);
    ihdr[8] = 8; ihdr[9] = 2;
    const rowSz = 1 + cols * 3;
    const raw = new Uint8Array(rows * rowSz);
    for (let y = 0; y < rows; y++) { raw[y*rowSz] = 0; raw.set(rgb.subarray(y*cols*3, (y+1)*cols*3), y*rowSz+1); }
    const cmpd = deflateSync(raw, { level: 6 });
    const crcTbl = []; for (let n=0;n<256;n++){let c=n;for(let k=0;k<8;k++)c=c&1?0xedb88320^(c>>>1):c>>>1;crcTbl[n]=c;}
    const crc32 = b => { let c=0xffffffff; for(let i=0;i<b.length;i++) c=crcTbl[(c^b[i])&0xff]^(c>>>8); return (c^0xffffffff)>>>0; };
    const chunk = (ty, d) => {
      const t=new Uint8Array([ty.charCodeAt(0),ty.charCodeAt(1),ty.charCodeAt(2),ty.charCodeAt(3)]);
      const l=new Uint8Array(4); new DataView(l.buffer).setUint32(0,d.length,false);
      const ci=new Uint8Array(4+d.length); ci.set(t,0); ci.set(d,4);
      const cv=crc32(ci); const cb=new Uint8Array(4); new DataView(cb.buffer).setUint32(0,cv,false);
      const ch=new Uint8Array(4+4+d.length+4); ch.set(l,0);ch.set(t,4);ch.set(d,8);ch.set(cb,8+d.length); return ch;
    };
    const sig=new Uint8Array([137,80,78,71,13,10,26,10]);
    const ic=chunk("IHDR",ihdr), dc=chunk("IDAT",cmpd), ec=chunk("IEND",new Uint8Array(0));
    const png=new Uint8Array(sig.length+ic.length+dc.length+ec.length);
    let o=0; png.set(sig,o);o+=sig.length; png.set(ic,o);o+=ic.length; png.set(dc,o);o+=dc.length; png.set(ec,o);
    writeFileSync("minimap-p2-preview.png", png);
    console.log(`\n  Zoomed P2 minimap: minimap-p2-preview.png (${cols}x${rows} px, ${zoomStep}m/px)\n`);
  }

  // 5) Encode back to little-endian uint16 bytes
  const outBytes = new Uint8Array(hmLen * 2);
  for (let i = 0; i < hmLen; i++) {
    outBytes[i * 2] = modified[i] & 0xff;
    outBytes[i * 2 + 1] = (modified[i] >> 8) & 0xff;
  }

  // 5) GZIP compress and save
  const compressed = gzipSync(Buffer.from(outBytes), { level: 6 });
  writeFileSync(OUT_FILE, compressed);
  console.log(`Saved: ${OUT_FILE}  (${(compressed.length / 1024 / 1024).toFixed(1)} MB compressed)\n`);

  // 6) Upload to MariaDB
  if (DRY_RUN) {
    console.log("--dry-run: skipping DB upload.");
    console.log("To upload manually:");
    console.log(`  docker cp ${OUT_FILE} db:/tmp/${OUT_FILE}`);
    console.log(`  docker exec db mariadb -u root -p1234 razarion -e "UPDATE PLANET SET compressedHeightMap=LOAD_FILE('/tmp/${OUT_FILE}') WHERE id=${PLANET_ID}"`);
    return;
  }

  console.log("Uploading to MariaDB via docker...");
  try {
    execSync(`docker cp ${OUT_FILE} db:/tmp/${OUT_FILE}`, { stdio: "pipe" });
    console.log("  Copied file into container.");

    execSync(
      `docker exec db mariadb -u root -p1234 razarion -e "UPDATE PLANET SET compressedHeightMap=LOAD_FILE('/tmp/${OUT_FILE}') WHERE id=${PLANET_ID}"`,
      { stdio: "pipe" }
    );
    console.log("  DB updated successfully!");

    // Verify non-null
    const verify = execSync(
      `docker exec db mariadb -u root -p1234 razarion -N -e "SELECT LENGTH(compressedHeightMap) FROM PLANET WHERE id=${PLANET_ID}"`,
      { encoding: "utf8" }
    ).trim();
    console.log(`  Verification: compressedHeightMap length = ${verify} bytes`);
  } catch (e) {
    console.error("  DB upload failed:", e.message);
    console.log("\n  Manual upload commands:");
    console.log(`    docker cp ${OUT_FILE} db:/tmp/${OUT_FILE}`);
    console.log(`    docker exec db mariadb -u root -p1234 razarion -e "UPDATE PLANET SET compressedHeightMap=LOAD_FILE('/tmp/${OUT_FILE}') WHERE id=${PLANET_ID}"`);
  }
}

main().catch(e => {
  console.error(e);
  process.exit(1);
});
