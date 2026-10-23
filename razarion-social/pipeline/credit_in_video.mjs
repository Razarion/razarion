#!/usr/bin/env node
// Writes the music credit into the closing seconds of a clip, so the post texts need none.
//
//   node credit_in_video.mjs data/own/own-...-portrait-music.mp4 [more files]
//
// CC BY 4.0 asks for attribution in a manner reasonable to the medium; in a video that is the video
// itself. The file is replaced in place (a .bak of the original is kept next to it) - the media of
// a composed post is referenced by its path, and upload_media.mjs uploads by content, so the
// changed bytes go up as a new object on the next run.

import { copyFileSync, existsSync, writeFileSync, rmSync } from 'node:fs';
import { execFile } from 'node:child_process';
import { promisify } from 'node:util';
import { createCanvas } from '@napi-rs/canvas';
import { FFMPEG, probeVideo } from './lib/video.mjs';
import { COLORS, FONT } from './lib/card.mjs';
import { ok, fail } from '../src/util/log.mjs';

export const MUSIC_CREDIT = 'Music: "Inspired" by Kevin MacLeod (incompetech.com) · CC BY 4.0';
const SECONDS = 4;

async function credit(file) {
  // Always from the original: a second run must not stack a second credit on the first
  if (!existsSync(`${file}.bak`)) copyFileSync(file, `${file}.bak`);
  const source = `${file}.bak`;
  const probe = await probeVideo(source);
  if (!probe) throw new Error(`${file} is not readable.`);
  const { width, height, duration } = probe;
  const png = `${file}.credit.png`;
  writeFileSync(png, creditPng(width, height));
  const out = `${file}.credited.mp4`;
  const from = Math.max(0, duration - SECONDS).toFixed(2);
  await promisify(execFile)(FFMPEG, ['-y', '-v', 'error', '-i', source, '-loop', '1', '-i', png, '-filter_complex',
    `[0:v][1:v]overlay=0:0:shortest=1:enable='gte(t,${from})',format=yuv420p[v]`,
    '-map', '[v]', '-map', '0:a?', '-c:v', 'libx264', '-preset', 'medium', '-crf', '18', '-c:a', 'copy',
    '-movflags', '+faststart', out], { maxBuffer: 32 * 1024 * 1024 });
  rmSync(png, { force: true });
  // Not a rename: Windows refuses one while the review page has the old file open
  copyFileSync(out, file);
  rmSync(out, { force: true });
  ok(`${file}: credit in the last ${SECONDS} s`);
}

/** A line low in the frame, small but readable on a phone. */
function creditPng(width, height) {
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');
  const size = Math.round(Math.min(width, height) * 0.026);
  ctx.font = `400 ${size}px ${FONT}`;
  const textWidth = ctx.measureText(MUSIC_CREDIT).width;
  const x = (width - textWidth) / 2, y = height - size * 3;
  ctx.fillStyle = 'rgba(12,10,9,0.6)';
  ctx.fillRect(x - size * 0.6, y - size * 1.2, textWidth + size * 1.2, size * 1.7);
  ctx.fillStyle = COLORS.muted;
  ctx.fillText(MUSIC_CREDIT, x, y);
  return canvas.toBuffer('image/png');
}

const files = process.argv.slice(2);
if (!files.length) {
  fail('Which files? node credit_in_video.mjs <clip.mp4> [...]');
  process.exit(1);
}
for (const f of files) await credit(f);
