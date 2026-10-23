#!/usr/bin/env node
// The explanatory pictures of the rock devlog (assemble_showcase.mjs --story rock-1), drawn from the
// game's own textures and the numbers in rock-material.ts, not redrawn by hand.
//
//   node rock_devlog_graphics.mjs        # data/showcase/graphics/rock-*.png, 1920x1080

import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { createCanvas, loadImage } from '@napi-rs/canvas';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { COLORS, FONT, newCard } from './lib/card.mjs';
import { ok, step } from '../src/util/log.mjs';

const REPO = join(PIPELINE_ROOT, '..', '..');
const TEXTURES = join(REPO, 'razarion-frontend', 'public', 'renderer', 'textures');
const ROCK_MATERIAL = join(REPO, 'razarion-frontend', 'src', 'app', 'game', 'renderer', 'rock-material.ts');
const OUT = join(PIPELINE_ROOT, 'data', 'showcase', 'graphics');
const W = 1920, H = 1080;

async function main() {
  mkdirSync(OUT, { recursive: true });
  const rock = await pixels(join(TEXTURES, 'rock-diffuse.webp'));
  const old = await loadImage(readFileSync(join(TEXTURES, 'ground-mountain-diffuse.webp')));
  const constants = rockConstants();

  step('one stone');
  writeFileSync(join(OUT, 'rock-one-stone.png'), oneStone(old, rock));
  step('moss');
  writeFileSync(join(OUT, 'rock-moss.png'), mossPicture(rock, constants));
  step('shadow bug');
  writeFileSync(join(OUT, 'rock-shadow-bug.png'), shadowBug());
  ok(`graphics in ${OUT}`);
}

/** The texture with its pixels at hand: RGB the colour, alpha the height. */
async function pixels(file) {
  const image = await loadImage(readFileSync(file));
  const canvas = createCanvas(image.width, image.height);
  const ctx = canvas.getContext('2d');
  ctx.drawImage(image, 0, 0);
  const { data } = ctx.getImageData(0, 0, image.width, image.height);
  return { image, canvas, data, size: image.width };
}

/** The moss numbers as rock-material.ts has them, so the picture follows the game. */
function rockConstants() {
  const source = readFileSync(ROCK_MATERIAL, 'utf8');
  const number = (name) => {
    const m = source.match(new RegExp(`${name}:\\s*([^,\\n]+),`));
    if (!m) throw new Error(`${name} not found in rock-material.ts`);
    return Function(`return (${m[1]})`)();
  };
  const color = source.match(/MOSS_COLOR:\s*\[([^\]]+)\]/)[1].split(',').map(Number);
  return {
    scale: number('SCALE'), edge0: number('MOSS_EDGE0'), edge1: number('MOSS_EDGE1'),
    patchScale: number('MOSS_PATCH_SCALE'), strength: number('MOSS'), color,
  };
}

function oneStone(old, rock) {
  const card = newCard({ width: W, height: H, kicker: 'devlog · rocks' });
  const { ctx } = card;
  title(ctx, 'One stone for cliffs and rocks', 'Before: a cliff photo and the boulders\' own painted texture, each lit its own way');
  const size = 560, top = 330;
  ctx.imageSmoothingEnabled = true;
  ctx.drawImage(old, 160, top, size, size);
  ctx.drawImage(rock.image, W - 160 - size, top, size, size);
  ctx.strokeStyle = COLORS.accent;
  ctx.lineWidth = 4;
  ctx.strokeRect(W - 160 - size, top, size, size);
  arrow(ctx, 160 + size + 90, top + size / 2, W - 160 - size - 90);
  ctx.fillStyle = COLORS.text;
  ctx.font = `600 34px ${FONT}`;
  ctx.fillText('Cliffs before', 160, top + size + 60);
  ctx.fillText('Cliffs and boulders now', W - 160 - size, top + size + 60);
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 28px ${FONT}`;
  ctx.fillText('a photo of rocky ground', 160, top + size + 104);
  ctx.fillText('a scanned rock (CC0), 5 m a tile,', W - 160 - size, top + size + 104);
  ctx.fillText('laid on in world space: one surface', W - 160 - size, top + size + 144);
  return card.canvas.toBuffer('image/png');
}

/**
 * The moss rule on a strip of rock that turns from a flat top (left) to a wall (right):
 * up + 0.6 patch + 0.7 height - 0.9, smoothstepped - the same sum as rock-material.ts.
 */
function mossPicture(rock, k) {
  const card = newCard({ width: W, height: H, kicker: 'devlog · rocks' });
  const { ctx } = card;
  title(ctx, 'Moss grows where it can stay', 'On what looks up, in patches, and first in the cracks - one rule for every cliff and boulder');
  const sw = 1600, sh = 440, x0 = 160, y0 = 340;
  const metres = 32, perPixel = metres / sw;
  const strip = createCanvas(sw, sh);
  const sctx = strip.getContext('2d');
  const image = sctx.createImageData(sw, sh);
  const sample = (u, v, channel) => {
    const s = rock.size;
    const px = ((Math.floor(u * s) % s) + s) % s, py = ((Math.floor(v * s) % s) + s) % s;
    return rock.data[(py * s + px) * 4 + channel] / 255;
  };
  const smooth = (e0, e1, x) => { const t = Math.min(1, Math.max(0, (x - e0) / (e1 - e0))); return t * t * (3 - 2 * t); };
  for (let py = 0; py < sh; py++) {
    for (let px = 0; px < sw; px++) {
      const wx = px * perPixel, wz = py * perPixel;
      const up = Math.cos((px / (sw - 1)) * Math.PI / 2);
      const u = wx * k.scale, v = wz * k.scale;
      const height = sample(u, v, 3);
      const patch = sample(wx * k.patchScale, wz * k.patchScale, 3);
      const share = k.strength * smooth(k.edge0, k.edge1, up + 0.6 * patch + 0.7 * height - 0.9);
      const shade = 0.6 * height + 0.7;
      const i = (py * sw + px) * 4;
      for (let c = 0; c < 3; c++) {
        const stone = sample(u, v, c);
        const value = stone + (k.color[c] * shade - stone) * share;
        image.data[i + c] = Math.round(Math.min(1, value) * 255);
      }
      image.data[i + 3] = 255;
    }
  }
  sctx.putImageData(image, 0, 0);
  ctx.drawImage(strip, x0, y0);
  ctx.strokeStyle = COLORS.rule;
  ctx.lineWidth = 2;
  ctx.strokeRect(x0, y0, sw, sh);
  // What the strip stands for
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 30px ${FONT}`;
  ctx.fillText('flat top', x0, y0 + sh + 50);
  const wall = 'steep wall';
  ctx.fillText(wall, x0 + sw - ctx.measureText(wall).width, y0 + sh + 50);
  arrow(ctx, x0 + 200, y0 + sh + 40, x0 + sw - 220);
  ctx.fillStyle = COLORS.text;
  ctx.font = `600 34px ${FONT}`;
  ctx.fillText('moss = facing up  +  0.6 · patch  +  0.7 · stone height  −  0.9', x0, y0 + sh + 130);
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 28px ${FONT}`;
  ctx.fillText('The patches are the same rock texture read 5 × larger - no extra texture needed.', x0, y0 + sh + 180);
  return card.canvas.toBuffer('image/png');
}

function shadowBug() {
  const card = newCard({ width: W, height: H, kicker: 'devlog · a bug on the way' });
  const { ctx } = card;
  title(ctx, 'Where the shadows went - again', 'Babylon loads some shaders only when first needed, by URL');
  const boxes = [
    ['Game asks for', 'shadowMap.fragment', COLORS.text],
    ['Dev server answers', '<!DOCTYPE html> ...', '#f87171'],
    ['GPU: no shadow anywhere', 'compile error', '#f87171'],
  ];
  const bw = 460, bh = 200, gap = 110, top = 400;
  boxes.forEach(([head, body, color], i) => {
    const x = 160 + i * (bw + gap);
    ctx.fillStyle = COLORS.panel;
    ctx.fillRect(x, top, bw, bh);
    ctx.fillStyle = COLORS.muted;
    ctx.font = `400 28px ${FONT}`;
    ctx.fillText(head, x + 30, top + 60);
    ctx.fillStyle = color;
    ctx.font = `700 34px Consolas, ${FONT}`;
    ctx.fillText(body, x + 30, top + 130);
    if (i < boxes.length - 1) arrow(ctx, x + bw + 15, top + bh / 2, x + bw + gap - 15);
  });
  ctx.fillStyle = COLORS.text;
  ctx.font = `700 40px ${FONT}`;
  ctx.fillText('Fix: import the shaders up front, so nothing is fetched at run time.', 160, 760);
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 28px ${FONT}`;
  ctx.fillText('Adding new Babylon imports made the dev server rebuild its bundles - and the lazy fetch landed on the page instead.', 160, 820);
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
