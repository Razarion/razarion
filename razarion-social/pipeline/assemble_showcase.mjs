#!/usr/bin/env node
// Puts the showcase material together into the long devlog video: a cold open, chapter cards, the
// chapter cuts of cut_showcase.mjs (--outro off), a split screen before | now, and the outro.
//
//   node assemble_showcase.mjs --story terrain-1             # data/showcase/cuts/devlog-terrain-1.mp4
//   node assemble_showcase.mjs --story terrain-1 --check     # only list what the story needs
//
// The story is written here, in the first person - the posts that do well are Beat's own voice.
// Each segment is rendered to the same format first (1920x1080, 30 fps, a silent audio track so
// that a music bed can be laid under it later) and then joined without another encode.

import { existsSync, mkdirSync, writeFileSync, rmSync } from 'node:fs';
import { execFile } from 'node:child_process';
import { promisify } from 'node:util';
import { join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { FFMPEG, probeVideo } from './lib/video.mjs';
import { COLORS, FONT, newCard } from './lib/card.mjs';
import { outroPng } from './lib/showcase-code.mjs';
import { createCanvas } from '@napi-rs/canvas';
import { info, step, ok, fail } from '../src/util/log.mjs';

const execFileAsync = promisify(execFile);
const SHOWCASE_DIR = join(PIPELINE_ROOT, 'data', 'showcase');
const CUT_DIR = join(SHOWCASE_DIR, 'cuts');
const WIDTH = 1920, HEIGHT = 1080, FPS = 30, FADE = 0.35;

const take = (name) => join(SHOWCASE_DIR, `${name}.mp4`);
const cut = (name) => join(CUT_DIR, `${name}-landscape.mp4`);

/**
 * Segments: {clip, from, to, title?, subtitle?} plays a stretch of a file with an optional caption,
 * {card: [title, subtitle], seconds} a text card, {split: [left, right], labels, from, to} the same
 * camera move from two takes side by side, {outro: seconds} the closing card.
 */
const STORIES = {
  'terrain-1': [
    { clip: take('20261007-1810-island-reveal-01-none-landscape'), from: 2, to: 6.5, title: 'My island looked boring.' },
    { clip: take('20261007-1929-island-textures-v2-hills-01-all-landscape'), from: 6.5, to: 11, title: 'So I changed it, one thing at a time.' },
    { card: ['Razarion devlog', 'Terrain: light, clouds, relief, wind, paths and the ground itself'], seconds: 3.5 },
    { card: ['1 · From above', 'Same camera, same island - one improvement more every few seconds'], seconds: 3 },
    { clip: cut('ch-island-reveal') },
    { card: ['2 · Over the hills', 'Low sun, long shadows, clouds drifting over the slopes'], seconds: 3 },
    { clip: cut('ch-hills-reveal') },
    { card: ['3 · At player height', 'Where you actually play'], seconds: 3 },
    { clip: cut('ch-ground-reveal') },
    { card: ['4 · The ground itself', 'The photo grass read like a balcony carpet - so the ground went through four versions'], seconds: 4.5 },
    { clip: cut('ch-hills-iterations') },
    { clip: cut('ch-ground-iterations') },
    {
      split: [take('20261007-1810-island-reveal-01-none-landscape'), take('20261007-1929-island-textures-v2-hills-01-all-landscape')],
      labels: ['Before', 'Now'], from: 4, to: 16,
    },
    { card: ['Next', 'Every phase of the world gets a look of its own'], seconds: 3.5 },
    { outro: 5 },
  ],
};

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const name = String(args.story ?? 'terrain-1');
  const story = STORIES[name];
  if (!story) throw new Error(`Unknown story "${name}". Known: ${Object.keys(STORIES).join(', ')}`);

  const missing = story.flatMap((s) => [s.clip, ...(s.split ?? [])]).filter((f) => f && !existsSync(f));
  for (const f of missing) info(`  missing: ${f}`);
  if (missing.length) throw new Error('The story needs files that are not there - record or cut them first (cut_showcase.mjs --outro off).');
  if (args.check) return ok('Everything the story needs is there.');

  const work = join(CUT_DIR, `work-${name}`);
  mkdirSync(work, { recursive: true });
  const parts = [];
  for (const [i, segment] of story.entries()) {
    const part = join(work, `${String(i).padStart(2, '0')}.mp4`);
    step(`segment ${i + 1}/${story.length}: ${describe(segment)}`);
    await renderSegment(segment, part, work, i);
    parts.push(part);
  }

  const list = join(work, 'parts.txt');
  writeFileSync(list, parts.map((p) => `file '${p.replace(/\\/g, '/')}'`).join('\n') + '\n');
  const out = join(CUT_DIR, `devlog-${name}.mp4`);
  await ffmpeg(['-f', 'concat', '-safe', '0', '-i', list, '-c', 'copy', '-movflags', '+faststart', out]);
  const probe = await probeVideo(out);
  if (!probe) throw new Error(`${out} came out unreadable.`);
  rmSync(work, { recursive: true, force: true });
  ok(`${out}: ${probe.width}x${probe.height}, ${Math.floor(probe.duration / 60)}:${String(Math.round(probe.duration % 60)).padStart(2, '0')} min`);
}

function describe(s) {
  if (s.clip) return `${s.clip.split(/[\\/]/).pop()}${s.title ? ` "${s.title}"` : ''}`;
  if (s.card) return `card "${s.card[0]}"`;
  if (s.split) return `split ${s.labels.join(' | ')}`;
  return 'outro';
}

const ENCODE = ['-c:v', 'libx264', '-preset', 'medium', '-crf', '18', '-pix_fmt', 'yuv420p', '-r', String(FPS),
  '-c:a', 'aac', '-b:a', '128k', '-ar', '48000', '-ac', '2'];
const SILENCE = ['-f', 'lavfi', '-i', 'anullsrc=channel_layout=stereo:sample_rate=48000'];

async function renderSegment(s, out, work, i) {
  const fades = (length) => `fade=t=in:st=0:d=${FADE},fade=t=out:st=${(length - FADE).toFixed(3)}:d=${FADE}`;
  if (s.card || s.outro) {
    const seconds = s.seconds ?? s.outro;
    const png = join(work, `card-${i}.png`);
    writeFileSync(png, s.card ? cardPng(s.card[0], s.card[1]) : outroPng(WIDTH, HEIGHT));
    return ffmpeg(['-loop', '1', '-t', String(seconds), '-i', png, ...SILENCE,
      '-vf', `fps=${FPS},format=yuv420p,${fades(seconds)}`, '-t', String(seconds), ...ENCODE, out]);
  }
  if (s.clip) {
    const probe = await probeVideo(s.clip);
    const from = s.from ?? 0, to = Math.min(s.to ?? probe.duration, probe.duration), length = to - from;
    const base = `fps=${FPS},scale=${WIDTH}:${HEIGHT},trim=start=${from}:end=${to},setpts=PTS-STARTPTS,format=yuv420p`;
    if (!s.title) {
      return ffmpeg(['-i', s.clip, ...SILENCE, '-filter_complex', `[0:v]${base},${fades(length)}[v]`,
        '-map', '[v]', '-map', '1:a', '-t', length.toFixed(3), ...ENCODE, out]);
    }
    const png = join(work, `title-${i}.png`);
    writeFileSync(png, captionPng(s.title));
    return ffmpeg(['-i', s.clip, '-loop', '1', '-i', png, ...SILENCE, '-filter_complex',
      `[0:v]${base}[b];[b][1:v]overlay=0:0:shortest=1,${fades(length)}[v]`,
      '-map', '[v]', '-map', '2:a', '-t', length.toFixed(3), ...ENCODE, out]);
  }
  if (s.split) {
    // The same camera move from both takes, each showing its own half of the frame
    const length = s.to - s.from, half = WIDTH / 2;
    const png = join(work, `split-${i}.png`);
    writeFileSync(png, splitLabelsPng(s.labels));
    const side = (n) => `[${n}:v]fps=${FPS},scale=${WIDTH}:${HEIGHT},trim=start=${s.from}:end=${s.to},setpts=PTS-STARTPTS,crop=${half}:${HEIGHT}:${n === 0 ? 0 : half}:0[h${n}]`;
    return ffmpeg(['-i', s.split[0], '-i', s.split[1], '-loop', '1', '-i', png, ...SILENCE, '-filter_complex',
      `${side(0)};${side(1)};[h0][h1]hstack,format=yuv420p[b];[b][2:v]overlay=0:0:shortest=1,${fades(length)}[v]`,
      '-map', '[v]', '-map', '3:a', '-t', length.toFixed(3), ...ENCODE, out]);
  }
  throw new Error('Unknown segment.');
}

function cardPng(title, subtitle) {
  const card = newCard({ width: WIDTH, height: HEIGHT });
  const { ctx } = card;
  ctx.fillStyle = COLORS.text;
  ctx.font = `700 92px ${FONT}`;
  ctx.fillText(title, 160, HEIGHT * 0.47);
  ctx.fillStyle = COLORS.muted;
  ctx.font = `400 44px ${FONT}`;
  wrap(ctx, subtitle, 160, HEIGHT * 0.47 + 90, WIDTH - 320, 58);
  return card.canvas.toBuffer('image/png');
}

/** A full-frame overlay with a big caption low in the picture. */
function captionPng(text) {
  const canvas = createCanvas(WIDTH, HEIGHT);
  const ctx = canvas.getContext('2d');
  const gradient = ctx.createLinearGradient(0, HEIGHT * 0.6, 0, HEIGHT);
  gradient.addColorStop(0, 'rgba(12,10,9,0)');
  gradient.addColorStop(1, 'rgba(12,10,9,0.75)');
  ctx.fillStyle = gradient;
  ctx.fillRect(0, HEIGHT * 0.6, WIDTH, HEIGHT * 0.4);
  ctx.font = `700 84px ${FONT}`;
  ctx.fillStyle = COLORS.text;
  ctx.fillText(text, (WIDTH - ctx.measureText(text).width) / 2, HEIGHT - 120);
  return canvas.toBuffer('image/png');
}

function splitLabelsPng([left, right]) {
  const canvas = createCanvas(WIDTH, HEIGHT);
  const ctx = canvas.getContext('2d');
  ctx.fillStyle = COLORS.accent;
  ctx.fillRect(WIDTH / 2 - 2, 0, 4, HEIGHT);
  ctx.font = `700 60px ${FONT}`;
  for (const [text, x] of [[left, WIDTH / 4], [right, (WIDTH * 3) / 4]]) {
    const w = ctx.measureText(text).width;
    ctx.fillStyle = 'rgba(12,10,9,0.7)';
    ctx.fillRect(x - w / 2 - 28, HEIGHT - 150, w + 56, 88);
    ctx.fillStyle = COLORS.text;
    ctx.fillText(text, x - w / 2, HEIGHT - 86);
  }
  return canvas.toBuffer('image/png');
}

function wrap(ctx, text, x, y, maxWidth, lineHeight) {
  let line = '';
  for (const word of text.split(' ')) {
    const next = line ? `${line} ${word}` : word;
    if (ctx.measureText(next).width > maxWidth && line) {
      ctx.fillText(line, x, y);
      line = word;
      y += lineHeight;
    } else {
      line = next;
    }
  }
  if (line) ctx.fillText(line, x, y);
}

async function ffmpeg(args) {
  await execFileAsync(FFMPEG, ['-y', '-v', 'error', ...args], { maxBuffer: 32 * 1024 * 1024 });
}

main().catch((e) => {
  fail(String(e.message).slice(0, 600));
  process.exit(1);
});
