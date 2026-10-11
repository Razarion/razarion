import {RawTexture} from "@babylonjs/core/Materials/Textures/rawTexture";
import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import type {Scene} from "@babylonjs/core/scene";

/** One 8 m bot-ground box in game coordinates (x east, z = game y north), centred on (x, z). */
export interface BotGroundFootprint {
  x: number;
  z: number;
  // Ramps: rotation around the vertical (yRot) and the tilt (zRot) shorten and turn the footprint
  yRot: number;
  zRot: number;
}

/**
 * The painted layer of one bot ground: premultiplied rgb paint, alpha coverage, over the rectangle
 * origin..origin+size. gravel: the coverage of the gravel bed around the plate (red), same rectangle.
 */
export interface BotGroundMarkings {
  texture: Texture;
  gravel: Texture;
  originX: number;
  originZ: number;
  sizeX: number;
  sizeZ: number;
}

const BOX = 8;
const PIXELS_PER_METRE = 16;
const MAX_TEXTURE_SIZE = 1024;
// Bands measured inwards from the outline of the whole plate, in metres
const LIP_WIDTH = 0.15;
const STRIPE_WIDTH = 0.8;
const STRIPE_PERIOD = 1.1;
const RAZCORE_RED = [0.62, 0.14, 0.09];
// Ramp chevrons (m): one per CHEVRON_PERIOD up the ramp, CHEVRON_DUTY of it painted
const CHEVRON_PERIOD = 2;
const CHEVRON_DUTY = 0.3;
const CHEVRON_SLOPE = 0.6;
const CHEVRON_MARGIN = 0.9;
/** The gravel bed around a plate (m): closed up to GRAVEL_FULL, single pebbles out to GRAVEL_REACH. */
export const GRAVEL_FULL = 0.15;
export const GRAVEL_REACH = 0.6;
const EMBLEM_NAME = "RazCore";
// Logo radius (m): smaller plates get no logo, larger ones no bigger one
const EMBLEM_MIN_RADIUS = 1.5;
const EMBLEM_MAX_RADIUS = 3;
// Bars of the eight trigrams (bit 4 = outer bar), 1 = whole, 0 = broken
const TRIGRAMS = [0b111, 0b011, 0b101, 0b001, 0b000, 0b100, 0b010, 0b110];

/**
 * Bakes the markings of one bot ground into a texture: the plate reads as a base, not as a grid of
 * 8 m boxes. Hazard stripes run only along the outline of the whole plate (inner corners included),
 * nothing marks the box borders (no grid), oil stains, tyre marks, cracks and patches break
 * the asphalt up, and a large plate carries the RazCore logo and name where it has the most room.
 * Ramps carry yellow chevrons pointing uphill.
 * Around the plate lies a gravel bed (bot-ground-gravel-material.ts); its coverage is baked here too.
 * Deterministic: the same boxes always give the same picture.
 */
export function bakeBotGroundMarkings(scene: Scene, name: string, boxes: BotGroundFootprint[]): BotGroundMarkings {
  let minX = Infinity, minZ = Infinity, maxX = -Infinity, maxZ = -Infinity;
  for (const b of boxes) {
    minX = Math.min(minX, b.x - BOX);
    minZ = Math.min(minZ, b.z - BOX);
    maxX = Math.max(maxX, b.x + BOX);
    maxZ = Math.max(maxZ, b.z + BOX);
  }
  const sizeX = maxX - minX, sizeZ = maxZ - minZ;
  const ppm = Math.min(PIXELS_PER_METRE, MAX_TEXTURE_SIZE / Math.max(sizeX, sizeZ));
  const w = Math.max(1, Math.round(sizeX * ppm)), h = Math.max(1, Math.round(sizeZ * ppm));
  const mpp = sizeX / w; // metres per pixel
  const wx = (i: number) => minX + (i + 0.5) * mpp;
  const wz = (j: number) => minZ + (j + 0.5) * (sizeZ / h);

  // Plate mask: union of all box footprints (ramps turned and shortened by their tilt)
  const inside = new Uint8Array(w * h);
  // 1 where only a ramp covers the pixel: no hazard stripes there, the way in stays open
  const rampOnly = new Uint8Array(w * h);
  for (const b of boxes) {
    const halfLong = BOX / 2 * Math.abs(Math.cos(b.zRot)), halfSide = BOX / 2;
    const cos = Math.cos(b.yRot), sin = Math.sin(b.yRot);
    const reach = BOX * 0.75;
    const i0 = Math.max(0, Math.floor((b.x - reach - minX) / mpp)), i1 = Math.min(w - 1, Math.ceil((b.x + reach - minX) / mpp));
    const j0 = Math.max(0, Math.floor((b.z - reach - minZ) * h / sizeZ)), j1 = Math.min(h - 1, Math.ceil((b.z + reach - minZ) * h / sizeZ));
    for (let j = j0; j <= j1; j++) {
      for (let i = i0; i <= i1; i++) {
        const dx = wx(i) - b.x, dz = wz(j) - b.z;
        // Babylon's rotation.y turns local +x to (cos, -sin) in x/z
        const u = dx * cos - dz * sin, v = dx * sin + dz * cos;
        if (Math.abs(u) <= halfLong && Math.abs(v) <= halfSide) {
          inside[j * w + i] = 1;
          if (b.zRot !== 0 && !boxes.some(o => o.zRot === 0 && Math.abs(wx(i) - o.x) <= BOX / 2 && Math.abs(wz(j) - o.z) <= BOX / 2)) {
            rampOnly[j * w + i] = 1;
          }
        }
      }
    }
  }
  const dist = distanceInside(inside, w, h, mpp);
  const outside = chessboardOutside(inside, w, h, mpp);

  const random = seededRandom(Math.round(boxes[0].x * 31 + boxes[0].z * 17));
  const pixels = new Float32Array(w * h * 4); // straight rgb + alpha while painting
  const paint = (k: number, r: number, g: number, b: number, a: number) => {
    // "over" compositing in straight alpha
    const da = pixels[k + 3], oa = a + da * (1 - a);
    if (oa <= 0) return;
    pixels[k] = (r * a + pixels[k] * da * (1 - a)) / oa;
    pixels[k + 1] = (g * a + pixels[k + 1] * da * (1 - a)) / oa;
    pixels[k + 2] = (b * a + pixels[k + 2] * da * (1 - a)) / oa;
    pixels[k + 3] = oa;
  };

  // The gravel bed: closed next to the plate, then thinning out with a ragged edge. Square
  // corners, like a bed laid around a foundation.
  const gravel = new Uint8Array(w * h);
  for (let k = 0; k < gravel.length; k++) {
    const o = outside[k];
    if (o <= 0 || o > GRAVEL_REACH + 0.65) continue;
    // Frayed: bays and tongues of ~1.5 m, ragged again at ~0.4 m and ~0.15 m
    const x = wx(k % w), z = wz(Math.floor(k / w));
    const ragged = o + (valueNoise(x * 0.7 + 3, z * 0.7 - 5) - 0.5) * 0.9 + (valueNoise(x * 2.6 - 11, z * 2.6 + 7) - 0.5) * 0.5
      + (valueNoise(x * 6.5 + 23, z * 6.5 - 19) - 0.5) * 0.35;
    const t = Math.min(1, Math.max(0, (GRAVEL_REACH - ragged) / (GRAVEL_REACH - GRAVEL_FULL)));
    gravel[k] = Math.round(t * 255);
  }

  // Patches of newer and older asphalt, under everything else
  const area = boxes.length * BOX * BOX;
  for (let n = Math.max(1, Math.round(area / 110)); n > 0; n--) {
    const cx = minX + random() * sizeX, cz = minZ + random() * sizeZ;
    const hx = 1 + random() * 2.5, hz = 1 + random() * 2.5;
    const shade = random() < 0.5 ? 0.24 : 0.42, alpha = 0.1 + random() * 0.08;
    forEachPixelIn(cx - hx, cz - hz, cx + hx, cz + hz, (i, j, k) => {
      if (!inside[j * w + i]) return;
      paint(k, shade, shade, shade * 1.03, alpha * (0.75 + 0.25 * valueNoise(wx(i) * 2, wz(j) * 2)));
    });
  }

  // Tyre marks: two parallel arcs where vehicles turned
  for (let n = Math.max(1, Math.round(area / 900)); n > 0; n--) {
    const cx = minX + random() * sizeX, cz = minZ + random() * sizeZ;
    const radius = 3 + random() * 6, start = random() * Math.PI * 2, sweep = 0.8 + random() * 1.4;
    for (const offset of [-0.8, 0.8]) {
      const r = radius + offset;
      for (let t = 0; t <= 1; t += 0.15 / (r * sweep)) {
        const a = start + t * sweep;
        const x = cx + Math.cos(a) * r, z = cz + Math.sin(a) * r;
        const fade = Math.sin(t * Math.PI);
        dab(x, z, 0.2, (i, j, k) => inside[j * w + i] && paint(k, 0.04, 0.04, 0.04, 0.1 * fade));
      }
    }
  }

  // Oil stains: dark, soft, ragged
  for (let n = Math.max(1, Math.round(area / 70)); n > 0; n--) {
    const cx = minX + random() * sizeX, cz = minZ + random() * sizeZ;
    const radius = 0.5 + random() * random() * 2.2;
    forEachPixelIn(cx - radius * 1.5, cz - radius * 1.5, cx + radius * 1.5, cz + radius * 1.5, (i, j, k) => {
      if (!inside[j * w + i]) return;
      const x = wx(i), z = wz(j);
      const r = Math.hypot(x - cx, (z - cz) * 1.3) / radius + (valueNoise(x * 2.5, z * 2.5) - 0.5) * 0.6;
      if (r < 1) paint(k, 0.02, 0.02, 0.025, 0.55 * (1 - r) * (1 - r));
    });
  }

  // Cracks: thin random walks
  for (let n = Math.max(1, Math.round(area / 220)); n > 0; n--) {
    let x = minX + random() * sizeX, z = minZ + random() * sizeZ, heading = random() * Math.PI * 2;
    for (let s = Math.round((2 + random() * 5) / 0.1); s > 0; s--) {
      heading += (random() - 0.5) * 0.7;
      x += Math.cos(heading) * 0.1;
      z += Math.sin(heading) * 0.1;
      dab(x, z, mpp * 0.5, (i, j, k) => inside[j * w + i] && paint(k, 0.03, 0.03, 0.03, 0.6));
    }
  }

  // Ramps: yellow chevrons pointing uphill, in rows across the ramp. A ramp box is tilted by zRot
  // about its local z, so its local +x end is the high one; the painted stripe keeps clear of the
  // flat boxes it overlaps.
  for (const b of boxes) {
    if (b.zRot === 0) continue;
    const halfLong = BOX / 2 * Math.abs(Math.cos(b.zRot)), halfSide = BOX / 2;
    const cos = Math.cos(b.yRot), sin = Math.sin(b.yRot), up = Math.sign(Math.sin(b.zRot)) || 1;
    forEachPixelIn(b.x - BOX, b.z - BOX, b.x + BOX, b.z + BOX, (i, j, k) => {
      const x = wx(i), z = wz(j), dx = x - b.x, dz = z - b.z;
      const u = (dx * cos - dz * sin) * up, v = dx * sin + dz * cos;
      if (Math.abs(u) > halfLong || Math.abs(v) > halfSide - CHEVRON_MARGIN) return;
      if (boxes.some(o => o.zRot === 0 && Math.abs(x - o.x) < BOX / 2 && Math.abs(z - o.z) < BOX / 2)) return;
      // Lines u = c - slope * |v|: every one a chevron with its tip uphill
      const phase = mod((u + Math.abs(v) * CHEVRON_SLOPE) / CHEVRON_PERIOD, 1);
      const edge = mpp / CHEVRON_PERIOD;
      const coverage = Math.min(1, Math.max(0, (CHEVRON_DUTY - phase) / edge + 0.5)) * Math.min(1, Math.max(0, phase / edge + 0.5));
      if (coverage <= 0) return;
      const wear = 0.75 + 0.25 * valueNoise(x * 1.9, z * 1.9);
      if (valueNoise(x * 3.3 + 9, z * 3.3 - 4) < 0.18) return;
      paint(k, 0.86, 0.62, 0.08, 0.85 * coverage * wear);
    });
  }

  // The emblem and the name beside it, where the plate has the most room
  let best = 0, bestK = -1;
  for (let k = 0; k < dist.length; k++) {
    if (dist[k] > best) {
      best = dist[k];
      bestK = k;
    }
  }
  // The logo with the name is about 5 logo radii wide; it must stay clear of the stripes
  const radius = Math.min(EMBLEM_MAX_RADIUS, (best - 1) / 2.5);
  const emblem = radius >= EMBLEM_MIN_RADIUS ? drawEmblem(radius, mpp) : null;
  if (emblem) {
    const left = wx(bestK % w) - emblem.width / 2, top = wz(Math.floor(bestK / w)) + emblem.height / 2;
    forEachPixelIn(left, top - emblem.height, left + emblem.width, top, (i, j, k) => {
      const x = wx(i), z = wz(j);
      const coverage = emblem.coverage(x - left, top - z);
      if (coverage <= 0) return;
      // Faded paint, worn through where the traffic runs
      const wear = 0.55 + 0.45 * smoothstep(0.3, 0.6, valueNoise(x * 1.7, z * 1.7));
      paint(k, RAZCORE_RED[0], RAZCORE_RED[1], RAZCORE_RED[2], 0.8 * coverage * wear);
    });
  }

  // Outline: hazard stripes along the outer edge of the whole plate, a concrete lip outside them.
  // Stripe and band edges are anti-aliased over one pixel, or they stair-step close up.
  const stripePixel = mpp * Math.SQRT2 / STRIPE_PERIOD;
  for (let j = 0; j < h; j++) {
    for (let i = 0; i < w; i++) {
      const d = dist[j * w + i];
      if (d <= 0 || d > LIP_WIDTH + STRIPE_WIDTH + mpp || rampOnly[j * w + i]) continue;
      const k = (j * w + i) * 4, x = wx(i), z = wz(j);
      if (d <= LIP_WIDTH) {
        paint(k, 0.62, 0.61, 0.57, 0.85);
        continue;
      }
      const band = Math.min(1, Math.max(0, (LIP_WIDTH + STRIPE_WIDTH - d) / mpp + 0.5));
      const phase = mod((x + z) / STRIPE_PERIOD, 1);
      const toEdge = Math.min(phase, Math.abs(phase - 0.5), 1 - phase) / stripePixel;
      const yellow = phase < 0.5 ? 0.5 + 0.5 * Math.min(1, toEdge) : 0.5 - 0.5 * Math.min(1, toEdge);
      const chipped = valueNoise(x * 3.1 - 5, z * 3.1 + 11) < 0.22;
      const wear = 0.8 + 0.2 * valueNoise(x * 1.3, z * 1.3);
      paint(k, 0.06, 0.06, 0.06, 0.8 * wear * band);
      if (!chipped) paint(k, 0.86, 0.62, 0.08, 0.9 * wear * band * yellow);
    }
  }

  // Premultiplied alpha: filtering and mipmaps then blend paint with "no paint" instead of with
  // black, which drew dark seams along every stripe and along the plate edge. Raw bytes, since a
  // canvas would round-trip them through its own premultiplication.
  const data = new Uint8Array(w * h * 4);
  for (let k = 0; k < pixels.length; k += 4) {
    const a = Math.min(1, Math.max(0, pixels[k + 3]));
    for (let c = 0; c < 3; c++) {
      data[k + c] = Math.round(Math.min(1, Math.max(0, pixels[k + c])) * a * 255);
    }
    data[k + 3] = Math.round(a * 255);
  }
  // Row 0 is the southern edge (minZ): no flip, so v = 0 there
  const texture = RawTexture.CreateRGBATexture(data, w, h, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
  texture.name = name;
  texture.wrapU = Texture.CLAMP_ADDRESSMODE;
  texture.wrapV = Texture.CLAMP_ADDRESSMODE;
  texture.anisotropicFilteringLevel = 4;
  const gravelTexture = RawTexture.CreateLuminanceTexture(gravel, w, h, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
  gravelTexture.name = name + " gravel";
  gravelTexture.wrapU = Texture.CLAMP_ADDRESSMODE;
  gravelTexture.wrapV = Texture.CLAMP_ADDRESSMODE;

  return {texture, gravel: gravelTexture, originX: minX, originZ: minZ, sizeX, sizeZ};

  function forEachPixelIn(x0: number, z0: number, x1: number, z1: number, f: (i: number, j: number, k: number) => void) {
    const i0 = Math.max(0, Math.floor((x0 - minX) / mpp)), i1 = Math.min(w - 1, Math.ceil((x1 - minX) / mpp));
    const j0 = Math.max(0, Math.floor((z0 - minZ) * h / sizeZ)), j1 = Math.min(h - 1, Math.ceil((z1 - minZ) * h / sizeZ));
    for (let j = j0; j <= j1; j++) {
      for (let i = i0; i <= i1; i++) {
        f(i, j, (j * w + i) * 4);
      }
    }
  }

  function dab(x: number, z: number, radius: number, f: (i: number, j: number, k: number) => void) {
    forEachPixelIn(x - radius, z - radius, x + radius, z + radius, (i, j, k) => {
      if (Math.hypot(wx(i) - x, wz(j) - z) <= radius) f(i, j, k);
    });
  }
}

/**
 * The RazCore logo, painted like a stencil: eight trigrams of three bars, whole or broken, in an
 * octagon (after the Dharma Initiative logo), a ring in the middle with an R, and "RazCore" to the
 * right. Drawn on a canvas in metres / mpp pixels with north up, so the name reads from the game camera.
 * coverage(x, y) takes metres from the top left corner of the drawing (x east, y south). Null where
 * there is no DOM (tests, Node).
 */
function drawEmblem(radius: number, mpp: number): { width: number, height: number, coverage: (x: number, y: number) => number } | null {
  if (typeof document === "undefined") {
    return null;
  }
  const font = (size: number) => `bold ${Math.round(size / mpp)}px Arial, Helvetica, sans-serif`;
  const canvas = document.createElement("canvas");
  let context = canvas.getContext("2d")!;
  context.font = font(radius * 0.8);
  const nameWidth = context.measureText(EMBLEM_NAME).width * mpp;
  const gap = radius * 0.3;
  const width = 2 * radius + gap + nameWidth + radius * 0.1, height = 2 * radius;
  canvas.width = Math.ceil(width / mpp);
  canvas.height = Math.ceil(height / mpp);
  context = canvas.getContext("2d")!;
  context.scale(1 / mpp, 1 / mpp); // draw in metres
  context.fillStyle = "#fff";
  context.strokeStyle = "#fff";

  // Trigrams: side n faces 45 degrees * n from north; outer bar first
  context.save();
  context.translate(radius, radius);
  for (let n = 0; n < 8; n++) {
    const lines = TRIGRAMS[n];
    context.save();
    context.rotate(n * Math.PI / 4);
    for (let bar = 0; bar < 3; bar++) {
      const distance = radius * (0.93 - bar * 0.15);
      const half = distance * Math.tan(Math.PI / 8) * 0.86, thick = radius * 0.09;
      if (lines & (4 >> bar)) {
        context.fillRect(-half, -distance, 2 * half, thick);
      } else {
        const gapHalf = radius * 0.07;
        context.fillRect(-half, -distance, half - gapHalf, thick);
        context.fillRect(gapHalf, -distance, half - gapHalf, thick);
      }
    }
    context.restore();
  }
  // The middle: a ring with the R
  context.lineWidth = radius * 0.08;
  context.beginPath();
  context.arc(0, 0, radius * 0.4, 0, Math.PI * 2);
  context.stroke();
  context.font = font(radius * 0.5);
  context.textAlign = "center";
  context.textBaseline = "middle";
  context.scale(mpp, mpp);
  context.fillText("R", 0, radius * 0.03 / mpp);
  context.restore();

  // The name, centred on the logo's middle line
  context.font = font(radius * 0.8);
  context.textAlign = "left";
  context.textBaseline = "middle";
  context.save();
  context.scale(mpp, mpp);
  context.fillText(EMBLEM_NAME, (2 * radius + gap) / mpp, radius * 1.04 / mpp);
  context.restore();

  const pixels = context.getImageData(0, 0, canvas.width, canvas.height).data;
  return {
    width, height,
    coverage: (x, y) => {
      const i = Math.floor(x / mpp), j = Math.floor(y / mpp);
      return i < 0 || j < 0 || i >= canvas.width || j >= canvas.height ? 0 : pixels[(j * canvas.width + i) * 4 + 3] / 255;
    },
  };
}

/** Chessboard distance in metres from each outside pixel to the plate (0 inside): square corners. */
function chessboardOutside(inside: Uint8Array, w: number, h: number, mpp: number): Float32Array {
  const big = 1e9;
  const d = new Float32Array(w * h);
  for (let k = 0; k < d.length; k++) d[k] = inside[k] ? 0 : big;
  const at = (i: number, j: number) => (i < 0 || j < 0 || i >= w || j >= h) ? big : d[j * w + i];
  for (let j = 0; j < h; j++) {
    for (let i = 0; i < w; i++) {
      const k = j * w + i;
      if (!d[k]) continue;
      d[k] = Math.min(d[k], at(i - 1, j) + 1, at(i, j - 1) + 1, at(i - 1, j - 1) + 1, at(i + 1, j - 1) + 1);
    }
  }
  for (let j = h - 1; j >= 0; j--) {
    for (let i = w - 1; i >= 0; i--) {
      const k = j * w + i;
      if (!d[k]) continue;
      d[k] = Math.min(d[k], at(i + 1, j) + 1, at(i, j + 1) + 1, at(i + 1, j + 1) + 1, at(i - 1, j + 1) + 1);
    }
  }
  // The half pixel puts the edge on the pixel border
  for (let k = 0; k < d.length; k++) d[k] = d[k] ? (d[k] - 0.5) * mpp : 0;
  return d;
}

/** Distance in metres from each inside pixel to the nearest outside pixel (0 outside); 3-4 chamfer. */
function distanceInside(inside: Uint8Array, w: number, h: number, mpp: number): Float32Array {
  const big = 1e9;
  const d = new Float32Array(w * h);
  for (let k = 0; k < d.length; k++) d[k] = inside[k] ? big : 0;
  const at = (i: number, j: number) => (i < 0 || j < 0 || i >= w || j >= h) ? 0 : d[j * w + i];
  for (let j = 0; j < h; j++) {
    for (let i = 0; i < w; i++) {
      const k = j * w + i;
      if (!d[k]) continue;
      d[k] = Math.min(d[k], at(i - 1, j) + 3, at(i, j - 1) + 3, at(i - 1, j - 1) + 4, at(i + 1, j - 1) + 4);
    }
  }
  for (let j = h - 1; j >= 0; j--) {
    for (let i = w - 1; i >= 0; i--) {
      const k = j * w + i;
      if (!d[k]) continue;
      d[k] = Math.min(d[k], at(i + 1, j) + 3, at(i, j + 1) + 3, at(i + 1, j + 1) + 4, at(i - 1, j + 1) + 4);
    }
  }
  // Chamfer units are thirds of a pixel; the half pixel puts the edge on the pixel border
  for (let k = 0; k < d.length; k++) d[k] = d[k] ? (d[k] / 3 - 0.5) * mpp : 0;
  return d;
}

function seededRandom(seed: number): () => number {
  let s = (Math.abs(seed) % 2147483646) + 1;
  return () => {
    s = (s * 16807) % 2147483647;
    return (s - 1) / 2147483646;
  };
}

/** Smooth value noise in 0..1 */
function valueNoise(x: number, z: number): number {
  const xi = Math.floor(x), zi = Math.floor(z);
  const xf = x - xi, zf = z - zi;
  const u = xf * xf * (3 - 2 * xf), v = zf * zf * (3 - 2 * zf);
  const a = hash(xi, zi), b = hash(xi + 1, zi), c = hash(xi, zi + 1), d = hash(xi + 1, zi + 1);
  return a + (b - a) * u + (c - a) * v + (a - b - c + d) * u * v;
}

function hash(x: number, z: number): number {
  let n = Math.imul(x, 374761393) + Math.imul(z, 668265263);
  n = Math.imul(n ^ (n >>> 13), 1274126177);
  return ((n ^ (n >>> 16)) >>> 0) / 4294967295;
}

function smoothstep(e0: number, e1: number, x: number): number {
  const t = Math.min(1, Math.max(0, (x - e0) / (e1 - e0)));
  return t * t * (3 - 2 * t);
}

function mod(n: number, m: number): number {
  return ((n % m) + m) % m;
}
