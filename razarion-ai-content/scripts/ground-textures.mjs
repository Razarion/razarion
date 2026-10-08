#!/usr/bin/env node
// Generates the ground shader's grass and earth textures (see the comment below) and writes them
// as WebP next to the other ground textures. Deterministic: run it again after changing a value.
//
//   node scripts/ground-textures.mjs            # writes razarion-frontend/public/renderer/textures/ground-{grass,earth}-diffuse.webp
//   node scripts/ground-textures.mjs --out DIR  # somewhere else, e.g. for a preview

import sharp from 'sharp';
import { mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

/**
 * Grass and earth textures for the ground shader, generated instead of photographed.
 *
 * At the game's camera a metre of ground covers about 17 screen pixels. The photo textures put their
 * contrast into single blades (75 px/m) - averaged away on screen into an even, saturated "lawn
 * carpet" - and the earth was a pale grey that read as concrete. These put the structure where the
 * eye can see it: grass tufts of 0.3-1 m with shaded gaps between them, patches of bare soil, colour
 * drifting over a few metres; brown earth with pebbles and the odd sprig of grass.
 *
 * RGB is the colour, alpha the height (0 low - 1 high): tufts and pebbles stand up, gaps and soil lie
 * low. The ground shader blends materials by it, so grass grows into earth tuft by tuft.
 *
 * Both tile seamlessly every PERIOD_M metres. Pure functions, no Babylon - the renderer wraps them
 * in textures, and a preview script can run them as they are.
 */
export const GROUND_TEXTURE_SIZE = 512;
export const GROUND_TEXTURE_PERIOD_M = 16;
// ---------- tileable noise ----------
function hash(x, y, seed) {
    let h = Math.imul(x, 0x27d4eb2d) ^ Math.imul(y + 0x3c6ef372, 0x165667b1) ^ Math.imul(seed + 0x5bd1e995, 0x9e3779b1);
    h = Math.imul(h ^ (h >>> 15), 0x85ebca6b);
    h = Math.imul(h ^ (h >>> 13), 0xc2b2ae35);
    h ^= h >>> 16;
    return (h >>> 0) / 4294967296;
}
const smooth = (t) => t * t * t * (t * (t * 6 - 15) + 10);
const mix = (a, b, t) => a + (b - a) * t;
const clamp01 = (v) => Math.max(0, Math.min(1, v));
const smoothstep = (e0, e1, v) => {
    const t = clamp01((v - e0) / (e1 - e0));
    return t * t * (3 - 2 * t);
};
const mixRgb = (a, b, t) => [mix(a[0], b[0], t), mix(a[1], b[1], t), mix(a[2], b[2], t)];
const scaleRgb = (a, s) => [a[0] * s, a[1] * s, a[2] * s];
/** Gradient noise on a lattice that repeats every `period` cells, roughly -1..1. */
function gradientNoise(x, y, period, seed) {
    const x0 = Math.floor(x), y0 = Math.floor(y);
    const fx = x - x0, fy = y - y0;
    const corner = (cx, cy) => {
        const a = hash(((cx % period) + period) % period, ((cy % period) + period) % period, seed) * Math.PI * 2;
        return Math.cos(a) * (x - cx) + Math.sin(a) * (y - cy);
    };
    const u = smooth(fx), v = smooth(fy);
    return mix(mix(corner(x0, y0), corner(x0 + 1, y0), u), mix(corner(x0, y0 + 1), corner(x0 + 1, y0 + 1), u), v) * 1.4;
}
/** fBm, 0..1. `cells` lattice cells across one period; every octave doubles it, so all of them tile. */
function fbm(u, v, cells, octaves, seed) {
    let sum = 0, amp = 1, norm = 0;
    for (let o = 0; o < octaves; o++) {
        const c = cells << o;
        sum += gradientNoise(u * c, v * c, c, seed + o * 101) * amp;
        norm += amp;
        amp *= 0.5;
    }
    return clamp01(sum / norm * 0.5 + 0.5);
}
/**
 * Cellular noise: one feature point per cell, `cells` cells across one period. Returns the
 * distance to the nearest point (in cell units) and that cell's random value.
 */
function cellular(u, v, cells, seed) {
    const x = u * cells, y = v * cells;
    const cx = Math.floor(x), cy = Math.floor(y);
    let best = 9, id = 0;
    for (let dy = -1; dy <= 1; dy++) {
        for (let dx = -1; dx <= 1; dx++) {
            const gx = cx + dx, gy = cy + dy;
            const wx = ((gx % cells) + cells) % cells, wy = ((gy % cells) + cells) % cells;
            const px = gx + hash(wx, wy, seed), py = gy + hash(wx, wy, seed + 7);
            const d = Math.hypot(px - x, py - y);
            if (d < best) {
                best = d;
                id = hash(wx, wy, seed + 13);
            }
        }
    }
    return { distance: best, id };
}
// ---------- materials ----------
const GRASS_FRESH = [0.31, 0.40, 0.17];
const GRASS_OLIVE = [0.38, 0.39, 0.20];
const GRASS_DRY = [0.52, 0.48, 0.24];
const SOIL = [0.40, 0.32, 0.23];
/** fBm sampled through a second fBm: shapes lose their roundness and grid. */
function warped(u, v, cells, octaves, seed, warpCells, warpAmount) {
    const wu = (fbm(u, v, warpCells, 2, seed + 500) - 0.5) * warpAmount;
    const wv = (fbm(u, v, warpCells, 2, seed + 600) - 0.5) * warpAmount;
    return fbm(u + wu, v + wv, cells, octaves, seed);
}
/** u, v: 0..1 across one period. */
function grassTexel(u, v) {
    const perM = GROUND_TEXTURE_PERIOD_M;
    // Colour drifting over ~4 m: fresher and more olive stretches
    const drift = fbm(u, v, perM / 4, 3, 11);
    let base = mixRgb(GRASS_FRESH, GRASS_OLIVE, smoothstep(0.3, 0.7, drift));
    // Tufts: organic clumps of ~0.5-1.5 m from warped noise, and finer bunches inside them
    const clump = warped(u, v, perM / 1.0, 3, 21, perM / 3, 0.04);
    const bunch = fbm(u, v, perM / 0.3, 2, 23);
    const tuft = clamp01(smoothstep(0.35, 0.7, clump) * 0.75 + bunch * 0.35);
    // Drier tops on a few clumps
    const dry = smoothstep(0.62, 0.8, warped(u, v, perM / 2, 2, 25, perM / 4, 0.05)) * tuft;
    base = mixRgb(base, GRASS_DRY, dry * 0.55);
    // Gaps a little shaded, tops a little lit - the contrast of tufts, not of black holes
    let rgb = scaleRgb(base, mix(0.72, 1.12, tuft));
    // Bare soil in small patches (~1-2 m), only where the grass is thin
    const soilNoise = warped(u, v, perM / 1.5, 3, 31, perM / 4, 0.05);
    const soil = smoothstep(0.62, 0.74, soilNoise) * (1 - tuft * 0.6);
    rgb = mixRgb(rgb, SOIL, soil * 0.8);
    // A little grain, far below the tuft contrast
    const grain = (fbm(u, v, perM / 0.12, 1, 41) - 0.5) * 0.06;
    rgb = scaleRgb(rgb, 1 + grain);
    return { rgb, height: clamp01(0.25 + tuft * 0.6 - soil * 0.2) };
}
const EARTH_LIGHT = [0.66, 0.57, 0.45];
const EARTH_DARK = [0.56, 0.47, 0.36];
const PEBBLE = [0.64, 0.61, 0.56];
const SPRIG = [0.30, 0.38, 0.14];
function earthTexel(u, v) {
    const perM = GROUND_TEXTURE_PERIOD_M;
    // Lighter and darker earth over ~3 m, damper spots over ~1 m
    const drift = warped(u, v, perM / 3, 3, 51, perM / 4, 0.04);
    const damp = smoothstep(0.58, 0.75, fbm(u, v, perM / 1, 2, 53));
    let rgb = mixRgb(EARTH_LIGHT, EARTH_DARK, smoothstep(0.3, 0.7, drift));
    rgb = scaleRgb(rgb, 1 - damp * 0.12);
    // Clods: a soft relief of ~0.3 m
    const clod = fbm(u, v, perM / 0.3, 2, 57);
    rgb = scaleRgb(rgb, 0.93 + clod * 0.14);
    let height = 0.3 + clod * 0.2 - damp * 0.1;
    // Pebbles gather in nests of a metre or two; outside them there are hardly any
    const nest = smoothstep(0.5, 0.7, warped(u, v, perM / 2, 2, 59, perM / 4, 0.05));
    const p = cellular(u, v, Math.round(perM / 0.2), 61);
    if (p.id < 0.05 + nest * 0.5) {
        const radius = 0.12 + hash(Math.floor(p.id * 9973), 3, 62) * 0.22;
        const stone = 1 - smoothstep(radius * 0.6, radius, p.distance);
        if (stone > 0) {
            const tint = hash(Math.floor(p.id * 9973), 5, 63);
            const colour = mixRgb(PEBBLE, EARTH_DARK, tint * 0.5);
            rgb = mixRgb(rgb, scaleRgb(colour, 0.85 + tint * 0.25), stone);
            height = Math.max(height, 0.5 + stone * 0.45);
        }
    }
    // The odd sprig of grass, ~0.8 m cells, one in six
    const s = cellular(u, v, Math.round(perM / 0.8), 71);
    if (s.id < 0.17) {
        const sprig = 1 - smoothstep(0.08, 0.22, s.distance + (fbm(u, v, perM / 0.2, 1, 73) - 0.5) * 0.15);
        rgb = mixRgb(rgb, SPRIG, sprig);
        height = Math.max(height, 0.4 + sprig * 0.35);
    }
    return { rgb, height: clamp01(height) };
}
function render(size, texel) {
    const data = new Uint8Array(size * size * 4);
    for (let y = 0; y < size; y++) {
        for (let x = 0; x < size; x++) {
            const { rgb, height } = texel(x / size, y / size);
            const i = (y * size + x) * 4;
            data[i] = Math.round(clamp01(rgb[0]) * 255);
            data[i + 1] = Math.round(clamp01(rgb[1]) * 255);
            data[i + 2] = Math.round(clamp01(rgb[2]) * 255);
            data[i + 3] = Math.round(height * 255);
        }
    }
    return data;
}
/** RGBA, GROUND_TEXTURE_SIZE square, one period each. */
export function generateGroundTextures(size = GROUND_TEXTURE_SIZE) {
    return { grass: render(size, grassTexel), earth: render(size, earthTexel) };
}

// ---------- write ----------

const here = dirname(fileURLToPath(import.meta.url));
const outArg = process.argv.indexOf('--out');
const outDir = outArg > 0 ? process.argv[outArg + 1] : join(here, '..', '..', 'razarion-frontend', 'public', 'renderer', 'textures');
mkdirSync(outDir, { recursive: true });
const { grass, earth } = generateGroundTextures();
for (const [name, data] of [['ground-grass-diffuse.webp', grass], ['ground-earth-diffuse.webp', earth]]) {
  // Lossy colour, lossless alpha: the alpha channel is the height the shader blends by
  await sharp(Buffer.from(data), { raw: { width: GROUND_TEXTURE_SIZE, height: GROUND_TEXTURE_SIZE, channels: 4 } })
    .webp({ quality: 90, alphaQuality: 100 })
    .toFile(join(outDir, name));
  console.log('wrote', join(outDir, name));
}
