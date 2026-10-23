#!/usr/bin/env node
// Turns a scanned rock material from ambientCG (CC0, no attribution required) into the rock textures
// that cliffs and rock models share (rock-material.ts): colour with the height in alpha, and the
// OpenGL normal map. Download the 1K-JPG zip of the material from ambientcg.com and unpack it first.
//
//   node scripts/rock-texture-scan.mjs --src DIR/Rock034            # writes razarion-frontend/public/renderer/textures/rock-{diffuse,normal}.webp
//   node scripts/rock-texture-scan.mjs --src DIR/Rock034 --out DIR  # somewhere else, e.g. for a preview

import sharp from 'sharp';
import { mkdirSync, readdirSync } from 'node:fs';
import { join, dirname, basename } from 'node:path';
import { fileURLToPath } from 'node:url';

const SIZE = 512;

const arg = (name) => {
  const i = process.argv.indexOf(name);
  return i > 0 ? process.argv[i + 1] : undefined;
};
const src = arg('--src');
if (!src) {
  console.error('Pass --src <unpacked ambientCG material folder>');
  process.exit(1);
}
const here = dirname(fileURLToPath(import.meta.url));
const outDir = arg('--out') ?? join(here, '..', '..', 'razarion-frontend', 'public', 'renderer', 'textures');
mkdirSync(outDir, { recursive: true });

const files = readdirSync(src);
const find = (suffix) => {
  const f = files.find((name) => name.endsWith(suffix));
  if (!f) throw new Error(`No *${suffix} in ${src}`);
  return join(src, f);
};

const color = await sharp(find('_Color.jpg')).resize(SIZE, SIZE).removeAlpha().raw().toBuffer();
const height = await sharp(find('_Displacement.jpg')).resize(SIZE, SIZE).greyscale().raw().toBuffer();
const rgba = Buffer.alloc(SIZE * SIZE * 4);
for (let i = 0; i < SIZE * SIZE; i++) {
  rgba[i * 4] = color[i * 3];
  rgba[i * 4 + 1] = color[i * 3 + 1];
  rgba[i * 4 + 2] = color[i * 3 + 2];
  rgba[i * 4 + 3] = height[i];
}
// Lossy colour, lossless alpha: the alpha is the height the moss and the gravel read
await sharp(rgba, { raw: { width: SIZE, height: SIZE, channels: 4 } })
  .webp({ quality: 90, alphaQuality: 100 }).toFile(join(outDir, 'rock-diffuse.webp'));
await sharp(find('_NormalGL.jpg')).resize(SIZE, SIZE).webp({ quality: 92 }).toFile(join(outDir, 'rock-normal.webp'));
console.log(`wrote rock-diffuse.webp and rock-normal.webp from ${basename(src)} to ${outDir}`);
