#!/usr/bin/env node
// Generate Phase 2 terrain for Planet 117 based on reference minimap
// Reference: razarion-ai-content/minimap-planet-117-p2.png

const { gunzipSync, gzipSync } = require("node:zlib");

const BASE_URL = "http://localhost:8080";
const PLANET_ID = 117;
const ADMIN_EMAIL = "admin@admin.com";
const ADMIN_PASSWORD = "1234";

const NODE_X_COUNT = 160;
const NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;

function heightToUint16(meters) {
  return Math.max(0, Math.min(65535, Math.round((meters + 200) / 0.1)));
}
function uint16ToHeight(u16) {
  return u16 * 0.1 - 200;
}

// ============================================================
// Noise helpers
// ============================================================
function noise(x, y, seed) {
  return (
    Math.sin(x * 0.0131 + seed + 1.7) * Math.cos(y * 0.0173 + seed * 1.3) * 0.5 +
    Math.sin(x * 0.0293 + y * 0.0311 + seed * 2.1) * 0.25 +
    Math.cos(x * 0.0531 - y * 0.0471 + seed * 3.7) * 0.125 +
    Math.sin(x * 0.0971 + y * 0.0893 + seed * 5.3) * 0.0625
  );
}

function fineNoise(x, y, seed) {
  return (
    Math.sin(x * 0.157 + y * 0.139 + seed) * 0.5 +
    Math.cos(x * 0.213 - y * 0.191 + seed * 1.7) * 0.3 +
    Math.sin(x * 0.371 + y * 0.337 + seed * 2.9) * 0.2
  );
}

function smoothstep(edge0, edge1, x) {
  const t = Math.max(0, Math.min(1, (x - edge0) / (edge1 - edge0)));
  return t * t * (3 - 2 * t);
}

// Wobble a radius for organic shapes
function wobbleRadius(angle, seed, amplitude) {
  return (
    Math.sin(angle * 3 + seed) * amplitude * 0.4 +
    Math.sin(angle * 5 + seed * 2.3) * amplitude * 0.3 +
    Math.cos(angle * 7 + seed * 3.7) * amplitude * 0.2 +
    Math.sin(angle * 11 + seed * 5.1) * amplitude * 0.1
  );
}

// ============================================================
// 1. Rolling Base Terrain (gentle undulation 2-6m)
// ============================================================
function baseTerrainHeight(x, y) {
  const n = noise(x, y, 42) * 2.0 + noise(x, y, 123) * 1.0 + fineNoise(x, y, 77) * 0.5;
  return Math.max(0.5, 3.5 + n);
}

// ============================================================
// 2. Lakes (from reference image - many, large, organic shapes)
//    depth = -10m as requested
// ============================================================
const lakes = [
  // Large western lake system (Y=800-1300 area)
  { cx: 180, cy: 1100, rx: 300, ry: 360, depth: -10, seed: 11 },
  { cx: 100, cy: 900, rx: 235, ry: 270, depth: -10, seed: 12 },
  { cx: 350, cy: 950, rx: 215, ry: 180, depth: -10, seed: 13 },
  // Upper-west lakes (Y=1400-1700)
  { cx: 130, cy: 1450, rx: 250, ry: 235, depth: -10, seed: 14 },
  { cx: 80, cy: 1650, rx: 200, ry: 180, depth: -10, seed: 15 },
  { cx: 300, cy: 1550, rx: 160, ry: 145, depth: -10, seed: 16 },
  // Center lakes
  { cx: 550, cy: 1500, rx: 145, ry: 125, depth: -10, seed: 17 },
  { cx: 750, cy: 1400, rx: 125, ry: 110, depth: -10, seed: 18 },
  { cx: 950, cy: 1550, rx: 135, ry: 115, depth: -10, seed: 19 },
  { cx: 700, cy: 1100, rx: 90, ry: 80, depth: -10, seed: 20 },
  // East/northeast lakes
  { cx: 1150, cy: 1650, rx: 115, ry: 100, depth: -10, seed: 21 },
  { cx: 1500, cy: 1700, rx: 160, ry: 145, depth: -10, seed: 22 },
  { cx: 1700, cy: 1500, rx: 110, ry: 90, depth: -10, seed: 23 },
  { cx: 1750, cy: 650, rx: 135, ry: 115, depth: -10, seed: 24 },
  // Southeast area
  { cx: 1350, cy: 850, rx: 100, ry: 90, depth: -10, seed: 25 },
  { cx: 1550, cy: 350, rx: 110, ry: 100, depth: -10, seed: 26 },
  // Medium ponds scattered
  { cx: 450, cy: 1200, rx: 65, ry: 55, depth: -10, seed: 27 },
  { cx: 1000, cy: 1250, rx: 55, ry: 45, depth: -10, seed: 28 },
  { cx: 850, cy: 800, rx: 80, ry: 70, depth: -10, seed: 29 },
  { cx: 1200, cy: 1350, rx: 65, ry: 55, depth: -10, seed: 30 },
  { cx: 200, cy: 1800, rx: 90, ry: 80, depth: -10, seed: 31 },
  { cx: 600, cy: 1750, rx: 70, ry: 65, depth: -10, seed: 32 },
  { cx: 1100, cy: 500, rx: 70, ry: 65, depth: -10, seed: 33 },
  { cx: 400, cy: 750, rx: 80, ry: 70, depth: -10, seed: 34 },
  // Pond near center
  { cx: 900, cy: 1350, rx: 45, ry: 45, depth: -10, seed: 35 },
];

// Returns blend factor 0..1 (0=no lake, 1=lake center) for smooth interpolation
function lakeFactor(x, y) {
  let maxFactor = 0;
  for (const lake of lakes) {
    const dx = x - lake.cx;
    const dy = y - lake.cy;
    const angle = Math.atan2(dy, dx);
    const wobble = wobbleRadius(angle, lake.seed, Math.min(lake.rx, lake.ry) * 0.3);
    const rxW = lake.rx + wobble;
    const ryW = lake.ry + wobble;
    const normDist = Math.sqrt((dx * dx) / (rxW * rxW) + (dy * dy) / (ryW * ryW));
    if (normDist >= 1) continue;
    const t = 1 - normDist;
    const profile = t * t * (3 - 2 * t);
    if (profile > maxFactor) maxFactor = profile;
  }
  return maxFactor;
}

// ============================================================
// 3. Elongated Ridges (from reference - two brown/orange features)
// ============================================================
const ridges = [
  // Main ridge: horizontal, center of Phase 2 area
  { cx: 750, cy: 1250, semiX: 420, semiY: 75, height: 30, seed: 41 },
  // Secondary ridge: upper area, shorter
  { cx: 550, cy: 1820, semiX: 260, semiY: 55, height: 25, seed: 42 },
];

function ridgesHeight(x, y) {
  let total = 0;
  for (const ridge of ridges) {
    const dx = x - ridge.cx;
    const dy = y - ridge.cy;
    const angle = Math.atan2(dy, dx);
    const wobble = wobbleRadius(angle, ridge.seed, 15);
    const normDist = Math.sqrt(
      (dx * dx) / ((ridge.semiX + wobble) * (ridge.semiX + wobble)) +
      (dy * dy) / ((ridge.semiY + wobble) * (ridge.semiY + wobble))
    );
    if (normDist >= 1) continue;
    const t = 1 - normDist;
    const profile = t * t * (3 - 2 * t);
    const noiseVar = fineNoise(x, y, ridge.seed) * 5;
    total += (ridge.height + noiseVar) * profile;
  }
  return total;
}

// ============================================================
// 4. Scattered Hills (small circular brown/green features)
// ============================================================
const hills = [
  { cx: 1600, cy: 1100, r: 60, h: 18 },
  { cx: 300, cy: 1900, r: 45, h: 15 },
  { cx: 1100, cy: 450, r: 55, h: 14 },
  { cx: 1700, cy: 400, r: 40, h: 12 },
  { cx: 500, cy: 650, r: 50, h: 12 },
  { cx: 950, cy: 700, r: 40, h: 10 },
  { cx: 1400, cy: 1500, r: 35, h: 10 },
  { cx: 350, cy: 1350, r: 30, h: 8 },
  { cx: 1250, cy: 1100, r: 40, h: 12 },
  { cx: 1800, cy: 1000, r: 35, h: 10 },
];

function hillsHeight(x, y) {
  let total = 0;
  for (const hill of hills) {
    const dx = x - hill.cx, dy = y - hill.cy;
    const dist = Math.sqrt(dx * dx + dy * dy);
    if (dist >= hill.r) continue;
    const t = 1 - dist / hill.r;
    total += hill.h * t * t * (3 - 2 * t);
  }
  return total;
}

// ============================================================
// 5. Green elevated patches (medium elevation areas)
// ============================================================
const greenPatches = [
  { cx: 500, cy: 1100, rx: 100, ry: 80, h: 6, seed: 51 },
  { cx: 300, cy: 1700, rx: 80, ry: 70, h: 5, seed: 52 },
  { cx: 1000, cy: 900, rx: 90, ry: 75, h: 5, seed: 53 },
  { cx: 1300, cy: 1200, rx: 70, ry: 60, h: 4, seed: 54 },
  { cx: 800, cy: 1600, rx: 80, ry: 65, h: 5, seed: 55 },
  { cx: 1500, cy: 1200, rx: 60, ry: 55, h: 4, seed: 56 },
  { cx: 200, cy: 1350, rx: 70, ry: 60, h: 5, seed: 57 },
  { cx: 1600, cy: 800, rx: 75, ry: 65, h: 5, seed: 58 },
];

function greenPatchesHeight(x, y) {
  let total = 0;
  for (const patch of greenPatches) {
    const dx = x - patch.cx, dy = y - patch.cy;
    const normDist = Math.sqrt(dx * dx / (patch.rx * patch.rx) + dy * dy / (patch.ry * patch.ry));
    if (normDist >= 1) continue;
    const t = 1 - normDist;
    total += patch.h * t * t * (3 - 2 * t);
  }
  return total;
}

// ============================================================
// Phase 1 Protection & Transitions
// ============================================================
function isPhase1Core(x, y) {
  return x < 810 && y < 790;
}

function phase1BlendFactor(x, y) {
  if (x < 810 && y < 790) return 0;
  const blendWidth = 60;
  let blend = 1;
  if (x >= 810 && x < 810 + blendWidth && y < 790) {
    blend = Math.min(blend, smoothstep(810, 810 + blendWidth, x));
  }
  if (y >= 790 && y < 790 + blendWidth && x < 810) {
    blend = Math.min(blend, smoothstep(790, 790 + blendWidth, y));
  }
  if (x >= 810 && x < 810 + blendWidth && y >= 790 && y < 790 + blendWidth) {
    blend = Math.min(blend, Math.max(
      smoothstep(810, 810 + blendWidth, x),
      smoothstep(790, 790 + blendWidth, y)
    ));
  }
  return blend;
}

function edgeFadeFactor(x, y) {
  let fade = 1;
  if (x > 2000) fade = Math.min(fade, 1 - smoothstep(2000, 2100, x));
  if (y > 2000) fade = Math.min(fade, 1 - smoothstep(2000, 2100, y));
  return fade;
}

// ============================================================
// Main
// ============================================================
async function main() {
  console.log("Phase 2 Terrain Generator (Reference-based) for Planet", PLANET_ID);

  // Auth
  console.log("Authenticating...");
  const credentials = Buffer.from(`${ADMIN_EMAIL}:${ADMIN_PASSWORD}`).toString("base64");
  const authResp = await fetch(`${BASE_URL}/rest/user/auth`, {
    method: "POST",
    headers: { Authorization: `Basic ${credentials}` },
  });
  if (!authResp.ok) throw new Error(`Auth failed: ${authResp.status}`);
  const jwt = await authResp.text();

  // Terrain shape
  console.log("Fetching terrain shape...");
  const shapeResp = await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`, {
    headers: { Accept: "application/json" },
  });
  const terrainShape = await shapeResp.json();
  const tileXCount = terrainShape.nativeTerrainShapeTiles.length;
  const tileYCount = terrainShape.nativeTerrainShapeTiles[0].length;
  const totalXNodes = tileXCount * NODE_X_COUNT;
  const totalYNodes = tileYCount * NODE_Y_COUNT;
  console.log(`Planet: ${totalXNodes} x ${totalYNodes} m`);

  // Download heightmap
  console.log("Downloading heightmap...");
  const hmResp = await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`);
  let hmBuf = Buffer.from(await hmResp.arrayBuffer());
  if (hmBuf.length >= 2 && hmBuf[0] === 0x1f && hmBuf[1] === 0x8b) {
    hmBuf = gunzipSync(hmBuf);
  }
  const hmLen = Math.floor(hmBuf.length / 2);
  const heightMap = new Uint16Array(hmLen);
  for (let i = 0; i < hmLen; i++) {
    heightMap[i] = hmBuf[i * 2] + (hmBuf[i * 2 + 1] << 8);
  }

  // Generate
  console.log("Generating Phase 2 terrain...");
  let modified = 0;
  const maxX = Math.min(2100, totalXNodes);
  const maxY = Math.min(2100, totalYNodes);

  for (let ny = 0; ny < maxY; ny++) {
    for (let nx = 0; nx < maxX; nx++) {
      if (isPhase1Core(nx, ny)) continue;

      const tileX = Math.floor(nx / NODE_X_COUNT);
      const tileY = Math.floor(ny / NODE_Y_COUNT);
      const localX = nx % NODE_X_COUNT;
      const localY = ny % NODE_Y_COUNT;
      const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + localY * NODE_X_COUNT + localX;
      if (idx >= hmLen) continue;

      const existingH = uint16ToHeight(heightMap[idx]);

      // Sum all land features
      let landH = baseTerrainHeight(nx, ny);
      landH += ridgesHeight(nx, ny);
      landH += hillsHeight(nx, ny);
      landH += greenPatchesHeight(nx, ny);

      // Lakes: blend smoothly from land height down to -10m
      const lf = lakeFactor(nx, ny);
      const LAKE_BOTTOM = -10;
      let h;
      if (lf > 0) {
        // Smooth blend: at lf=1 (center) -> -10m, at lf=0 (edge) -> landH
        h = landH * (1 - lf) + LAKE_BOTTOM * lf;
      } else {
        h = landH;
      }

      // Phase 1 blend
      const blend = phase1BlendFactor(nx, ny);
      h = existingH * (1 - blend) + h * blend;

      // Edge fade
      const fade = edgeFadeFactor(nx, ny);
      h = existingH * (1 - fade) + h * fade;

      heightMap[idx] = heightToUint16(h);
      modified++;
    }
  }
  console.log(`Modified ${modified} nodes.`);

  // Upload
  console.log("Uploading...");
  const outBuf = Buffer.alloc(hmLen * 2);
  for (let i = 0; i < hmLen; i++) {
    outBuf[i * 2] = heightMap[i] & 0xff;
    outBuf[i * 2 + 1] = (heightMap[i] >> 8) & 0xff;
  }
  const compressed = gzipSync(outBuf);
  const uploadResp = await fetch(
    `${BASE_URL}/rest/editor/planeteditor/updateCompressedHeightMap/${PLANET_ID}`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${jwt}`,
        "Content-Type": "application/octet-stream",
      },
      body: compressed,
    }
  );
  if (!uploadResp.ok) throw new Error(`Upload failed: ${uploadResp.status} ${await uploadResp.text()}`);
  console.log("Done! Heightmap uploaded.");
}

main().catch((err) => { console.error("ERROR:", err.message); process.exit(1); });
