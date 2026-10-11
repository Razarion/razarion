#!/usr/bin/env node
// The explanatory pictures of the bot devlog (assemble_showcase.mjs --story bot-1), drawn from the
// renderer's own code rather than redrawn by hand: the bake of a plate's markings is run here on the
// Platform's real boxes, the pebble texture comes out of the same function the game calls.
//
//   node bot_devlog_graphics.mjs        # data/showcase/graphics/bot-*.png, 1920x1080
//
// The renderer files are TypeScript that imports Babylon; the few calls the bake needs from it are
// replaced by stand-ins that hand the pixels back, and the result is transpiled with the frontend's
// esbuild.

import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { createRequire } from 'node:module';
import { createCanvas } from '@napi-rs/canvas';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { COLORS, FONT, newCard } from './lib/card.mjs';
import { ok, step } from '../src/util/log.mjs';

const REPO = join(PIPELINE_ROOT, '..', '..');
const RENDERER = join(REPO, 'razarion-frontend', 'src', 'app', 'game', 'renderer');
const OUT = join(PIPELINE_ROOT, 'data', 'showcase', 'graphics');
const WORK = join(OUT, 'work');
const W = 1920, H = 1080;

/** The Platform's flat boxes (bot config "(Bot1) Water", local db). */
const PLATFORM = [[382, 308], [382, 300], [382, 292], [390, 308], [398, 308], [406, 308], [414, 308], [422, 308], [390, 300],
  [398, 300], [406, 300], [414, 300], [422, 300], [390, 292], [398, 292], [406, 292], [414, 292], [422, 292], [422, 284],
  [430, 284], [430, 292], [430, 300], [430, 276], [422, 276], [430, 268], [422, 268], [422, 260], [430, 260], [430, 308],
  [414, 284], [414, 276], [414, 268], [414, 260]];

async function main() {
  mkdirSync(WORK, { recursive: true });
  const bake = await loadBake();
  const pebbles = await loadPebbles();

  step('bake pipeline');
  writeFileSync(join(OUT, 'bot-bake.png'), bakePipeline(bake));
  step('pebbles');
  writeFileSync(join(OUT, 'bot-pebbles.png'), pebblePicture(pebbles));
  step('shadow precision');
  writeFileSync(join(OUT, 'bot-shadow.png'), shadowChart());
  step('performance');
  writeFileSync(join(OUT, 'bot-performance.png'), performanceChart());
  ok(`graphics in ${OUT}`);
}

// ---------- the renderer's code, made runnable here ----------

async function transpile(source, name) {
  const require = createRequire(join(REPO, 'razarion-frontend', 'package.json'));
  const esbuild = require('esbuild');
  const { code } = await esbuild.transform(source, { loader: 'ts', format: 'esm' });
  const file = join(WORK, name);
  writeFileSync(file, code);
  return import(pathToFileURL(file).href + `?t=${Date.now()}`);
}

/** bakeBotGroundMarkings with its textures and distance fields handed back instead of uploaded. */
async function loadBake() {
  let src = readFileSync(join(RENDERER, 'bot-ground-markings.ts'), 'utf8');
  src = src.replace(/^import .*$/gm, '');
  src = src.replace(/RawTexture\.CreateRGBATexture\(data, w, h,[^;]*;/, '({ data, w, h }) as any; (globalThis as any).__bake.paint = { data, w, h };');
  src = src.replace(/RawTexture\.CreateLuminanceTexture\(gravel, w, h,[^;]*;/, '({}) as any; (globalThis as any).__bake.gravel = { data: gravel, w, h };');
  src = src.replace(/const outside = chessboardOutside\(inside, w, h, mpp\);/, '$&\n  (globalThis as any).__bake.fields = { inside, dist, outside, w, h, mpp };');
  src = src.replace(/Texture\.[A-Z_]+/g, '0');
  globalThis.__bake = {};
  // The logo is drawn on a canvas
  globalThis.document = { createElement: () => createCanvas(1, 1) };
  const module = await transpile(src, 'markings.mjs');
  return (boxes) => {
    globalThis.__bake = {};
    module.bakeBotGroundMarkings(null, 'devlog', boxes.map(([x, z]) => ({ x, z, yRot: 0, zRot: 0 })));
    return globalThis.__bake;
  };
}

/** The pebble texture of bot-ground-gravel-material.ts, as RGBA bytes. */
async function loadPebbles() {
  const full = readFileSync(join(RENDERER, 'bot-ground-gravel-material.ts'), 'utf8');
  const constants = full.match(/^const PEBBLE_[A-Z_]+ = .*$/gm).join('\n');
  let fn = full.slice(full.indexOf('function pebbles('), full.indexOf('\n}\n', full.indexOf('function pebbles(')) + 3);
  fn = fn.replace(/pebbleTexture = RawTexture\.CreateRGBATexture\([\s\S]*?return pebbleTexture;/, 'return { data, size };');
  fn = fn.replace(/if \(pebbleTexture && pebbleScene === scene\) \{[\s\S]*?\}\n/, '');
  fn = fn.replace('function pebbles(scene: Scene): Texture', 'export function pebbles(scene: any): any');
  const module = await transpile(`${constants}\n${fn}`, 'pebbles.mjs');
  return module.pebbles(null);
}

// ---------- pictures ----------

function bakePipeline(bake) {
  const result = bake(PLATFORM);
  const { inside, dist, w, h } = result.fields;
  const card = newCard({ width: W, height: H, kicker: 'devlog · bot bases' });
  const { ctx } = card;
  title(ctx, 'One texture per plate', 'Baked once when the plate appears, from the boxes the bot is made of');

  const panels = [
    ['1 · The boxes', 'what the bot config says'],
    ['2 · Distance to the edge', 'of the whole plate, not of each box'],
    ['3 · Painted', 'stripes, stains, cracks, logo'],
  ];
  const size = 470, gap = 90, top = 330, left = (W - (3 * size + 2 * gap)) / 2;
  const maxDist = dist.reduce((m, v) => Math.max(m, v), 0);
  const pictures = [
    // The box grid
    (i, j, k) => inside[k] ? (gridLine(i, j, w, h) ? [16, 185, 129, 255] : [68, 64, 60, 255]) : null,
    // Distance field: green near the edge, fading inwards
    (i, j, k) => {
      if (!inside[k]) return null;
      const t = Math.min(1, dist[k] / Math.min(8, maxDist));
      const band = (dist[k] % 1) < 0.12 ? 1.25 : 1;
      return [Math.round((16 + t * 30) * band), Math.round((185 - t * 140) * band), Math.round((129 - t * 90) * band), 255];
    },
    // The painted texture over asphalt grey
    (i, j, k) => {
      if (!inside[k]) return null;
      const p = result.paint.data, a = p[k * 4 + 3] / 255;
      return [0, 1, 2].map((c) => Math.round(92 * (1 - a) + p[k * 4 + c])).concat(255);
    },
  ];
  pictures.forEach((pixel, n) => {
    const x = left + n * (size + gap);
    drawField(ctx, x, top, size, w, h, pixel);
    ctx.fillStyle = COLORS.text;
    ctx.font = `700 40px ${FONT}`;
    ctx.fillText(panels[n][0], x, top + size + 70);
    ctx.fillStyle = COLORS.muted;
    ctx.font = `400 30px ${FONT}`;
    ctx.fillText(panels[n][1], x, top + size + 115);
    if (n < 2) arrow(ctx, x + size + 18, top + size / 2, x + size + gap - 18);
  });
  return card.canvas.toBuffer('image/png');

  function gridLine(i, j) {
    const m = result.fields.mpp;
    // Box edges lie 4 m off the texture origin (it starts a whole box outside the plate)
    const x = ((i * m - 4) % 8 + 8) % 8, z = ((j * m - 4) % 8 + 8) % 8;
    return x < m * 2 || z < m * 2;
  }
}

function pebblePicture({ data, size }) {
  const card = newCard({ width: W, height: H, kicker: 'devlog · bot bases' });
  const { ctx } = card;
  title(ctx, 'Gravel, grown from points', 'Each stone is the area closest to one random point; the gap to the next point makes the joint');
  const tile = createCanvas(size, size);
  const tctx = tile.getContext('2d');
  const image = tctx.createImageData(size, size);
  image.data.set(data);
  tctx.putImageData(image, 0, 0);
  // Left: the texture big; right: the same texture tiled, as the ground sees it
  ctx.imageSmoothingEnabled = true;
  ctx.drawImage(tile, 160, 300, 620, 620);
  for (let ty = 0; ty < 3; ty++) for (let tx = 0; tx < 3; tx++) ctx.drawImage(tile, 1060 + tx * 210, 300 + ty * 210, 210, 210);
  ctx.strokeStyle = COLORS.accent;
  ctx.lineWidth = 4;
  ctx.strokeRect(160, 300, 620, 620);
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 30px ${FONT}`;
  ctx.fillText('one tile · 2 m, 400 stones', 160, 970);
  ctx.fillText('tiled seamlessly over the ground', 1060, 970);
  return card.canvas.toBuffer('image/png');
}

function shadowChart() {
  const card = newCard({ width: W, height: H, kicker: 'devlog · a bug on the way' });
  const { ctx } = card;
  title(ctx, 'Where the shadows went', 'The map stores e^(50 · depth); half float ends at 65 504. With the low sun, most ground lies beyond');
  const x0 = 220, y0 = 930, x1 = 1760, y1 = 330;
  const yOf = (log) => y0 - (log / 26) * (y0 - y1);           // natural log of the stored value, 0..26
  const xOf = (d) => x0 + d * (x1 - x0);
  // Axes
  ctx.strokeStyle = COLORS.rule;
  ctx.lineWidth = 2;
  ctx.beginPath(); ctx.moveTo(x0, y1); ctx.lineTo(x0, y0); ctx.lineTo(x1, y0); ctx.stroke();
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 28px ${FONT}`;
  ctx.fillText('depth in the shadow map  →', x1 - 380, y0 + 50);
  ctx.save(); ctx.translate(x0 - 40, y1 + 360); ctx.rotate(-Math.PI / 2); ctx.fillText('stored value (log)  →', 0, 0); ctx.restore();
  // Region beyond half float
  const limit = Math.log(65504);
  const dLimit = limit / 50;
  ctx.fillStyle = 'rgba(220,38,38,0.12)';
  ctx.fillRect(xOf(dLimit), y1, x1 - xOf(dLimit), y0 - y1);
  ctx.strokeStyle = '#dc2626';
  ctx.setLineDash([12, 10]);
  ctx.beginPath(); ctx.moveTo(x0, yOf(limit)); ctx.lineTo(x1, yOf(limit)); ctx.stroke();
  ctx.setLineDash([]);
  ctx.fillStyle = '#f87171';
  ctx.font = `700 32px ${FONT}`;
  ctx.fillText('half float overflows: no shadow', xOf(dLimit) + 380, y0 - 60);
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 28px ${FONT}`;
  ctx.fillText('16-bit limit', x0 + 20, yOf(limit) - 16);
  // The curve
  ctx.strokeStyle = COLORS.accent;
  ctx.lineWidth = 6;
  ctx.beginPath();
  for (let d = 0; d <= 0.52 && 50 * d <= 26; d += 0.005) {
    const y = yOf(50 * d);
    d === 0 ? ctx.moveTo(xOf(d), y) : ctx.lineTo(xOf(d), y);
  }
  ctx.stroke();
  ctx.fillStyle = COLORS.text;
  ctx.font = `700 36px ${FONT}`;
  ctx.fillText('Fix: 32-bit float - room up to e^88', xOf(dLimit) + 380, y0 - 120);
  return card.canvas.toBuffer('image/png');
}

function performanceChart() {
  const card = newCard({ width: W, height: H, kicker: 'devlog · what it costs' });
  const { ctx } = card;
  title(ctx, 'Did it get slower?', 'Measured before and after: same camera, four starts each, desktop browser');
  const rows = [
    ['Start to first frame', 1.21, 1.31, 's'],
    ['Frame time (median)', 16.6, 17.4, 'ms'],
    ['Render time (median)', 14.5, 15.2, 'ms'],
    ['Draw calls', 598, 576, ''],
  ];
  const left = 560, right = 1700, top = 340, rowH = 150;
  rows.forEach(([name, before, after, unit], i) => {
    const y = top + i * rowH, max = Math.max(before, after) * 1.15;
    ctx.fillStyle = COLORS.text;
    ctx.font = `600 34px ${FONT}`;
    ctx.fillText(name, 160, y + 50);
    for (const [k, value, color, label] of [[0, before, '#57534e', 'before'], [1, after, COLORS.accent, 'now']]) {
      const by = y + k * 52, bw = (value / max) * (right - left);
      ctx.fillStyle = color;
      ctx.fillRect(left, by + 10, bw, 40);
      ctx.fillStyle = COLORS.text;
      ctx.font = `400 28px ${FONT}`;
      ctx.fillText(`${value} ${unit}  ${label}`, left + bw + 18, by + 41);
    }
  });
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 28px ${FONT}`;
  ctx.fillText('About 5 % more per frame for markings, clouds and gravel - worth it.', 160, H - 90);
  return card.canvas.toBuffer('image/png');
}

// ---------- helpers ----------

function title(ctx, text, sub) {
  ctx.fillStyle = COLORS.text;
  ctx.font = `700 76px ${FONT}`;
  ctx.fillText(text, 160, 210);
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 34px ${FONT}`;
  ctx.fillText(sub, 160, 265);
}

/** A w x h field drawn into a size x size square, north up, transparent where pixel() says null. */
function drawField(ctx, x, y, size, w, h, pixel) {
  const scale = size / Math.max(w, h);
  const cw = Math.round(w * scale), ch = Math.round(h * scale);
  const canvas = createCanvas(cw, ch);
  const c = canvas.getContext('2d');
  const image = c.createImageData(cw, ch);
  for (let py = 0; py < ch; py++) {
    for (let px = 0; px < cw; px++) {
      const i = Math.min(w - 1, Math.floor(px / scale)), j = Math.min(h - 1, Math.floor((ch - 1 - py) / scale));
      const value = pixel(i, j, j * w + i);
      if (!value) continue;
      image.data.set(value, (py * cw + px) * 4);
    }
  }
  c.putImageData(image, 0, 0);
  ctx.fillStyle = COLORS.panel;
  ctx.fillRect(x, y, size, size);
  ctx.drawImage(canvas, x + (size - cw) / 2, y + (size - ch) / 2);
}

function arrow(ctx, x0, y, x1) {
  ctx.strokeStyle = COLORS.accent;
  ctx.fillStyle = COLORS.accent;
  ctx.lineWidth = 6;
  ctx.beginPath(); ctx.moveTo(x0, y); ctx.lineTo(x1 - 16, y); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(x1, y); ctx.lineTo(x1 - 22, y - 16); ctx.lineTo(x1 - 22, y + 16); ctx.closePath(); ctx.fill();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
