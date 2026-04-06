import { gunzipSync } from "node:zlib";
import { writeFileSync } from "node:fs";

const BASE_URL = "http://localhost:8080";
const PLANET_ID = 117;
const NODE_X_COUNT = 160, NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.01, HEIGHT_MIN = -200;
const SPLATTER_UV_SCALE = 0.006;

const P1_MIN_X = 0, P1_MAX_X = 820;
const P1_MIN_Y = 0, P1_MAX_Y = 800;

// Large rock models
const STONE_MODELS = [
  { modelId: 17, name: "Rock1B", radius: 2 },
  { modelId: 18, name: "Rock1A", radius: 2 },
  { modelId: 19, name: "Rock3", radius: 3 },
  { modelId: 20, name: "Rock2", radius: 3 },
];

// ===== Perlin noise (same as procedural-textures.ts) =====
const perm = new Uint8Array(512);
const grad3 = [[1,1,0],[-1,1,0],[1,-1,0],[-1,-1,0],[1,0,1],[-1,0,1],[1,0,-1],[-1,0,-1],[0,1,1],[0,-1,1],[0,1,-1],[0,-1,-1]];
function initPerm(seed) { const p = new Uint8Array(256); for (let i = 0; i < 256; i++) p[i] = i; let s = seed; for (let i = 255; i > 0; i--) { s = (s * 16807) % 2147483647; const j = s % (i + 1); [p[i], p[j]] = [p[j], p[i]]; } for (let i = 0; i < 512; i++) perm[i] = p[i & 255]; }
function fade(t) { return t * t * t * (t * (t * 6 - 15) + 10); }
function lerp(a, b, t) { return a + t * (b - a); }
function clamp01(v) { return Math.max(0, Math.min(1, v)); }
function mod(n, m) { return ((n % m) + m) % m; }
function perlin2dTile(x, y, px, py) { const X = Math.floor(x), Y = Math.floor(y), xf = x - X, yf = y - Y, X0 = mod(X, px), Y0 = mod(Y, py), X1 = (X0+1)%px, Y1 = (Y0+1)%py, u = fade(xf), v = fade(yf); const aa = perm[(perm[X0&255]+Y0)&255], ab = perm[(perm[X0&255]+Y1)&255], ba = perm[(perm[X1&255]+Y0)&255], bb = perm[(perm[X1&255]+Y1)&255]; const dot = (g,dx,dy) => { const gr = grad3[g%12]; return gr[0]*dx+gr[1]*dy; }; return lerp(lerp(dot(aa,xf,yf),dot(ba,xf-1,yf),u),lerp(dot(ab,xf,yf-1),dot(bb,xf-1,yf-1),u),v); }
function fbmTile(x, y, oct, lac, pers, px, py) { let val = 0, amp = 1, freq = 1, max = 0; for (let i = 0; i < oct; i++) { val += perlin2dTile(x*freq,y*freq,px*freq,py*freq)*amp; max += amp; amp *= pers; freq *= lac; } return val / max; }
function warpedFbmTile(x, y, oct, ws, wseed, px, py) { const wx = perlin2dTile(x+wseed,y+wseed,px,py)*ws, wy = perlin2dTile(x+wseed+50,y+wseed+50,px,py)*ws; return fbmTile(x+wx,y+wy,oct,2.0,0.5,px,py); }
const SEED = 77, SCALE = 4, WARP = 1.5;
function splatterValue(nx, ny) { const large = warpedFbmTile(nx*SCALE,ny*SCALE,3,WARP,SEED,SCALE,SCALE), mid = fbmTile(nx*SCALE*2,ny*SCALE*2,2,2.0,0.5,SCALE*2,SCALE*2)*0.2, raw = (large+mid)*0.5+0.5, detail = fbmTile(nx*SCALE*12,ny*SCALE*12,3,2.0,0.5,SCALE*12,SCALE*12)*0.1, v = raw+detail, t = clamp01((v-0.48)/0.04); return t*t*(3-2*t); }

function uint16ToHeight(v) { return v * HEIGHT_PRECISION + HEIGHT_MIN; }
function seededRandom(seed) { let s = seed; return () => { s = (s * 16807) % 2147483647; return (s - 1) / 2147483646; }; }

async function main() {
  initPerm(SEED);
  const shape = await (await fetch(`${BASE_URL}/rest/terrainshape/${PLANET_ID}`)).json();
  const tileXCount = shape.nativeTerrainShapeTiles.length;
  let buf = Buffer.from(await (await fetch(`${BASE_URL}/rest/terrainHeightMap/${PLANET_ID}`)).arrayBuffer());
  if (buf[0] === 0x1f && buf[1] === 0x8b) buf = gunzipSync(buf);
  const bytes = new Uint8Array(buf), hmLen = Math.floor(bytes.length / 2), hm = new Uint16Array(hmLen);
  for (let i = 0; i < hmLen; i++) hm[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);

  const getHeight = (x, y) => { const tx = Math.floor(x/NODE_X_COUNT), ty = Math.floor(y/NODE_Y_COUNT), idx = TILE_NODE_SIZE*(ty*tileXCount+tx)+(y%NODE_Y_COUNT)*NODE_X_COUNT+(x%NODE_X_COUNT); return idx >= hmLen || idx < 0 ? 0 : uint16ToHeight(hm[idx]); };
  const maxSlope = (x, y) => { const h = getHeight(x, y); let m = 0; for (let dy = -1; dy <= 1; dy++) for (let dx = -1; dx <= 1; dx++) { if (!dx && !dy) continue; m = Math.max(m, Math.abs(h - getHeight(x+dx, y+dy))); } return m; };

  // Find under patches, sample sparsely
  const SAMPLE_STEP = 8;
  const candidates = [];
  for (let y = P1_MIN_Y + 2; y < P1_MAX_Y - 2; y += SAMPLE_STEP) {
    for (let x = P1_MIN_X + 2; x < P1_MAX_X - 2; x += SAMPLE_STEP) {
      const h = getHeight(x, y);
      if (h < 0.3 || h > 2.5) continue;
      if (maxSlope(x, y) > 0.15) continue;
      const nx = (x * SPLATTER_UV_SCALE) % 1.0, ny = (y * SPLATTER_UV_SCALE) % 1.0;
      const sv = splatterValue(nx < 0 ? nx + 1 : nx, ny < 0 ? ny + 1 : ny);
      if (sv > 0.3) continue;
      candidates.push({ x, y, h });
    }
  }
  console.log(`Found ${candidates.length} under sample points`);

  // Very sparse — only a few large rocks
  const SPACING = 80;
  const rand = seededRandom(789);
  for (let i = candidates.length - 1; i > 0; i--) { const j = Math.floor(rand() * (i + 1)); [candidates[i], candidates[j]] = [candidates[j], candidates[i]]; }

  const selected = [];
  for (const pos of candidates) {
    if (selected.some(p => Math.abs(p.x - pos.x) < SPACING && Math.abs(p.y - pos.y) < SPACING)) continue;
    selected.push(pos);
  }
  console.log(`Selected ${selected.length} stone positions`);

  // One stone per position, small scale
  const positions = selected.map(pos => {
    const model = STONE_MODELS[Math.floor(rand() * STONE_MODELS.length)];
    const rotZ = rand() * Math.PI * 2;
    const scale = 0.4 + rand() * 0.4; // 0.4-0.8
    return { x: pos.x, y: pos.y, model, rotZ, scale };
  });

  const configSql = STONE_MODELS.map(m =>
    `INSERT IGNORE INTO TERRAIN_OBJECT (internalName, radius, model3DId_id) VALUES ('${m.name}', ${m.radius}, ${m.modelId});`
  );
  const posSql = positions.map(({ x, y, model, rotZ, scale }) =>
    `INSERT INTO TERRAIN_OBJECT_POSITION (internalName, x, y, terrainObjectEntity_id, planet, rotationX, rotationY, rotationZ, scaleX, scaleY, scaleZ) VALUES ('${model.name}@${x},${y}', ${x}, ${y}, (SELECT id FROM TERRAIN_OBJECT WHERE internalName='${model.name}' LIMIT 1), 117, 0, 0, ${rotZ.toFixed(3)}, ${scale.toFixed(2)}, ${scale.toFixed(2)}, ${scale.toFixed(2)});`
  );

  writeFileSync("place-under-stones.sql", [...configSql, "", ...posSql].join("\n"));
  console.log(`SQL written: ${positions.length} stones`);
}

main().catch(e => { console.error(e); process.exit(1); });
