#!/usr/bin/env node
// Puts the showcase material together into the long devlog video: a cold open, chapter cards, the
// chapter cuts of cut_showcase.mjs (--outro off), a split screen before | now, and the outro.
//
//   node assemble_showcase.mjs --story terrain-1             # data/showcase/cuts/devlog-terrain-1.mp4
//   node assemble_showcase.mjs --story terrain-1 --check     # only list what the story needs
//   node assemble_showcase.mjs --story bot-1 --thumbnail     # only the thumbnail of the story
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
const graphic = (name) => join(SHOWCASE_DIR, 'graphics', `${name}.png`);
const cut = (name) => join(CUT_DIR, `${name}-landscape.mp4`);

/** The run stamps of the rock takes on the finished material (record_showcase.mjs --label rock-final). */
const ROCK_NOW = { pond: '20261012-1642', tower: '20261012-1649', lake: '20261012-1650', ridge: '20261012-1647' };
/** The beach takes before the shore strip (--label beach-before) and with the lagoon (--label beach-lagoon). */
const BEACH_BEFORE = { shore: '20261012-1734', cove: '20261012-1735', strand: '20261012-1737', shallows: '20261012-1739' };
const BEACH_NOW = { shore: '20261014-1828', cove: '20261014-1830', strand: '20261014-1832', shallows: '20261014-1833' };
/** Takes of the terrain lab (record_lab.mjs): the wall at the ridge without and with the cliff experiments, the lab today. */
const labTake = (name) => join(PIPELINE_ROOT, 'data', 'lab', `${name}.mp4`);
const LAB_WALL = (state) => labTake(`20261021-1713-wall-grass-foot-${state}-landscape`);

/**
 * Segments: {clip, from, to, title?, subtitle?} plays a stretch of a file with an optional caption,
 * {card: [title, subtitle], seconds} a text card, {split: [left, right], labels, from, to} the same
 * camera move from two takes side by side, {image, seconds} a still picture (bot_devlog_graphics.mjs)
 * with a slow push in, {outro: seconds} the closing card.
 */
const STORIES = {
  'terrain-1': [
    { clip: take('20261007-1810-island-reveal-01-none-landscape'), from: 2, to: 6.5, title: 'My island looked boring.' },
    { clip: take('20261008-1833-island-final-01-all-landscape'), from: 6.5, to: 11, title: 'So I changed it, one thing at a time.' },
    { card: ['Razarion devlog', 'Terrain: light, clouds, relief, wind, paths and the ground itself'], seconds: 3.5 },
    { card: ['1 · From above', 'Same camera, same island - one improvement more every few seconds'], seconds: 3 },
    { clip: cut('ch-island-reveal') },
    { card: ['2 · Over the hills', 'Low sun, long shadows, clouds drifting over the slopes'], seconds: 3 },
    { clip: cut('ch-hills-reveal') },
    { card: ['3 · At player height', 'Where you actually play'], seconds: 3 },
    { clip: cut('ch-ground-reveal') },
    { card: ['4 · The ground itself', 'The photo grass read like a balcony carpet - so the ground went through five versions'], seconds: 4.5 },
    { clip: cut('ch-hills-iterations') },
    { clip: cut('ch-ground-iterations') },
    {
      split: [take('20261007-1810-island-reveal-01-none-landscape'), take('20261008-1833-island-final-01-all-landscape')],
      labels: ['Before', 'Now'], from: 4, to: 16,
    },
    { card: ['Next', 'Every phase of the world gets a look of its own'], seconds: 3.5 },
    { outro: 5 },
  ],
  // Opens on the finished bases - the picture that makes people stay - and only then shows where
  // they came from.
  'bot-1': [
    { clip: take('20261011-0748-garrison-final-reveal-05-all-landscape'), from: 3, to: 8, title: 'My RTS bot bases look like this now.' },
    { clip: take('20261011-0755-platform-final-reveal-05-all-landscape'), from: 4, to: 8.5 },
    { clip: take('20261011-0748-garrison-final-reveal-01-none-landscape'), from: 3, to: 8, title: 'A week ago: grey parking lots.' },
    { card: ['Razarion devlog', 'Bot bases: markings, a logo, clouds and a bed of gravel'], seconds: 3.5 },
    { card: ['1 · The Garrison', 'Same camera - one improvement more every few seconds'], seconds: 3 },
    { clip: cut('ch-garrison-reveal') },
    { image: graphic('bot-bake'), seconds: 7 },
    { card: ['2 · The naval platform', 'An L of plates in the sea'], seconds: 3 },
    { clip: cut('ch-platform-reveal') },
    { image: graphic('bot-pebbles'), seconds: 6 },
    { card: ['3 · Under the hood', 'The same base as a wire model'], seconds: 3 },
    {
      split: [take('20261011-0807-garrison-wire-01-all-landscape'), take('20261011-0748-garrison-final-reveal-05-all-landscape')],
      labels: ['Wireframe', 'Rendered'], from: 4, to: 14,
    },
    { clip: take('20261011-0808-platform-wire-01-all-landscape'), from: 6, to: 12 },
    { card: ['4 · Ramps and big plates', 'Chevrons point the way up'], seconds: 3 },
    { clip: take('20261011-0803-spire-final-01-all-landscape'), from: 4, to: 12 },
    { clip: take('20261011-0801-occupy-final-01-all-landscape'), from: 4, to: 11 },
    { image: graphic('bot-shadow'), seconds: 7 },
    { image: graphic('bot-performance'), seconds: 7 },
    {
      split: [take('20261011-0748-garrison-final-reveal-01-none-landscape'), take('20261011-0748-garrison-final-reveal-05-all-landscape')],
      labels: ['Before', 'Now'], from: 4, to: 16,
    },
    { card: ['Next', 'The bases get company: props and buildings made for them'], seconds: 3.5 },
    { outro: 5 },
  ],
  // Before: the takes of dcb48d423, the cliffs and boulders as they were; now: the same camera plans
  // on the shared rock material.
  'rock-1': [
    { clip: take(`${ROCK_NOW.pond}-pond-rock-final-01-all-landscape`), from: 3, to: 8, title: 'Cliffs and boulders in my RTS - one stone now.' },
    { clip: take('20261011-1102-pond-rock-before-01-all-landscape'), from: 3, to: 8, title: 'Before: two kinds of rock that did not match.' },
    { card: ['Razarion devlog', 'Rocks: one stone for cliffs and boulders, moss, and the light of the ground'], seconds: 3.5 },
    { image: graphic('rock-one-stone'), seconds: 7 },
    { card: ['1 · A pond', 'Same camera - before, then now'], seconds: 3 },
    { clip: cut('ch-rock-pond') },
    { card: ['2 · A tall rock at a cliff', 'The boulder now continues the cliff behind it'], seconds: 3 },
    { clip: cut('ch-rock-tower') },
    { image: graphic('rock-moss'), seconds: 8 },
    { card: ['3 · Around a lake', 'A ring of cliff, lit by the same sun, clouds and sky as the ground'], seconds: 3.5 },
    { clip: cut('ch-rock-lake') },
    { image: graphic('rock-shadow-bug'), seconds: 7 },
    { card: ['4 · A ridge', 'Low over the rocks in the south-west'], seconds: 3 },
    { clip: cut('ch-rock-ridge') },
    {
      split: [take('20261011-1103-tower-rock-before-01-all-landscape'), take(`${ROCK_NOW.tower}-tower-rock-final-01-all-landscape`)],
      labels: ['Before', 'Now'], from: 4, to: 16,
    },
    { card: ['Next', 'The terrain keeps going - one piece at a time'], seconds: 3.5 },
    { outro: 5 },
  ],
  // Before: the takes of 856ade507, sand by height alone; now: the shore strip, wet sand, the sea floor,
  // the lagoon and caustics (record_showcase.mjs --label beach-lagoon, state 01-all).
  'beach-1': [
    { clip: take(`${BEACH_NOW.cove}-cove-beach-lagoon-01-all-landscape`), from: 3, to: 8, title: 'The beaches in my RTS, now.' },
    { clip: take(`${BEACH_BEFORE.shore}-shore-beach-before-01-all-landscape`), from: 3, to: 8, title: 'Before: the surf broke on grass.' },
    { card: ['Razarion devlog', 'Beaches: sand along every waterline, wet sand, and a turquoise lagoon'], seconds: 3.5 },
    { card: ['1 · Grass in the sea', 'Most of the coast rises out of the water within a metre or two - too steep for sand by height alone'], seconds: 4.5 },
    { clip: cut('ch-beach-shore') },
    { card: ['2 · A beach all the way round', 'Now a strip of sand follows every waterline, wider and narrower with the noise'], seconds: 4 },
    { clip: cut('ch-beach-cove') },
    { card: ['3 · Wet sand', 'Dark where the surf runs - and the surf runs on sand only'], seconds: 3.5 },
    { clip: cut('ch-beach-strand') },
    { card: ['4 · The lagoon', 'Pale aqua over the sand, turquoise, then blue - and a net of light on the floor'], seconds: 3.5 },
    { clip: cut('ch-beach-shallows') },
    {
      split: [take(`${BEACH_BEFORE.cove}-cove-beach-before-01-all-landscape`), take(`${BEACH_NOW.cove}-cove-beach-lagoon-01-all-landscape`)],
      labels: ['Before', 'Now'], from: 4, to: 16,
    },
    { card: ['Next', 'The terrain keeps going - one piece at a time'], seconds: 3.5 },
    { outro: 5 },
  ],
  // A devlog of an experiment that did not make it: the lab stays, the cliff walls are parked on a
  // branch. The wall takes are of the lab with the experiments switched (terrain-mountains branch).
  'lab-1': [
    { clip: LAB_WALL('03-cliffStep+grassFoot'), from: 2, to: 7, title: 'I tried to give the cliffs in my RTS a real wall.' },
    { clip: LAB_WALL('01-none'), from: 2, to: 7, title: 'Today they are painted onto the ground.' },
    { card: ['Razarion devlog', 'A terrain lab - and cliffs that went back in the drawer'], seconds: 3.5 },
    { card: ['1 · A lab for the terrain', 'The game\'s own renderer over the starting island, without the game: sliders, switches, one reload'], seconds: 4.5 },
    { clip: labTake('20261023-1931-beach-lab-today-01-none-landscape') },
    { clip: labTake('20261023-1932-ridge-lab-today-01-none-landscape'), from: 2, to: 12 },
    { card: ['2 · A step instead of a slope', 'Slopes over 3 m become a flat foot, a wall and a flat top - only in the picture, the game keeps its heights'], seconds: 4.5 },
    { clip: cut('ch-lab-wall') },
    { card: ['3 · The wall itself', 'A mesh of its own along the step, 20 cm quads pushed out into rounded blocks by noise'], seconds: 4 },
    { clip: LAB_WALL('02-cliffStep') },
    {
      split: [LAB_WALL('01-none'), LAB_WALL('03-cliffStep+grassFoot')],
      labels: ['Painted', 'Wall'], from: 3, to: 13,
    },
    { card: ['Not good enough', 'It reads like a garden wall, not like a cliff. So it goes back in the drawer - for now.'], seconds: 4.5 },
    { card: ['Next', 'The lab stays - the next terrain idea starts there'], seconds: 3.5 },
    { outro: 5 },
  ],
};

/** The thumbnail: a still of the finished base with a short, big line on it. */
const THUMBNAILS = {
  // The Platform: the finished base with the logo, and open sea at the lower left for the words
  'bot-1': { clip: take('20261011-0755-platform-final-reveal-05-all-landscape'), at: 9, lines: ['BOT BASES', 'REBUILT'], top: 470 },
  'rock-1': { clip: take(`${ROCK_NOW.lake}-lake-rock-final-01-all-landscape`), at: 22, lines: ['ROCKS', 'ONE STONE'], top: 300 },
  'beach-1': { clip: take(`${BEACH_NOW.cove}-cove-beach-lagoon-01-all-landscape`), at: 20, lines: ['BEACHES', 'FIXED'], top: 300 },
  'lab-1': { clip: LAB_WALL('03-cliffStep+grassFoot'), at: 8, lines: ['CLIFFS', 'NOT YET'], top: 470 },
};

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const name = String(args.story ?? 'terrain-1');
  const story = STORIES[name];
  if (!story) throw new Error(`Unknown story "${name}". Known: ${Object.keys(STORIES).join(', ')}`);

  if (args.thumbnail) return makeThumbnail(name);
  const missing = story.flatMap((s) => [s.clip, s.image, ...(s.split ?? [])]).filter((f) => f && !existsSync(f));
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
  if (THUMBNAILS[name]) await makeThumbnail(name);
  ok(`${out}: ${probe.width}x${probe.height}, ${Math.floor(probe.duration / 60)}:${String(Math.round(probe.duration % 60)).padStart(2, '0')} min`);
}

function describe(s) {
  if (s.clip) return `${s.clip.split(/[\\/]/).pop()}${s.title ? ` "${s.title}"` : ''}`;
  if (s.card) return `card "${s.card[0]}"`;
  if (s.split) return `split ${s.labels.join(' | ')}`;
  if (s.image) return `image ${s.image.split(/[\/]/).pop()}`;
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
  if (s.image) {
    // A slow push in, so a still picture does not read as a frozen video
    const frames = Math.round(s.seconds * FPS);
    return ffmpeg(['-loop', '1', '-t', String(s.seconds), '-i', s.image, ...SILENCE, '-filter_complex',
      `[0:v]scale=${WIDTH * 2}:${HEIGHT * 2},zoompan=z='1+0.06*on/${frames}':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=${frames}:s=${WIDTH}x${HEIGHT}:fps=${FPS},format=yuv420p,${fades(s.seconds)}[v]`,
      '-map', '[v]', '-map', '1:a', '-t', String(s.seconds), ...ENCODE, out]);
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

async function makeThumbnail(name) {
  const spec = THUMBNAILS[name];
  if (!spec) throw new Error(`No thumbnail for "${name}".`);
  const frame = join(CUT_DIR, `devlog-${name}-frame.png`);
  await ffmpeg(['-ss', String(spec.at), '-i', spec.clip, '-frames:v', '1', '-vf', `scale=${WIDTH}:${HEIGHT}`, frame]);
  const { loadImage } = await import('@napi-rs/canvas');
  const canvas = createCanvas(1280, 720);
  const ctx = canvas.getContext('2d');
  ctx.drawImage(await loadImage(frame), 0, 0, 1280, 720);
  // Dark on the left for the words, the base stays bright
  const shade = ctx.createLinearGradient(0, 0, 820, 0);
  shade.addColorStop(0, 'rgba(12,10,9,0.85)');
  shade.addColorStop(1, 'rgba(12,10,9,0)');
  ctx.fillStyle = shade;
  ctx.fillRect(0, 0, 760, 720);
  ctx.font = `800 120px ${FONT}`;
  spec.lines.forEach((line, i) => {
    ctx.lineWidth = 10;
    ctx.strokeStyle = 'rgba(0,0,0,0.6)';
    ctx.strokeText(line, 60, (spec.top ?? 300) + i * 135);
    ctx.fillStyle = i === 0 ? COLORS.text : COLORS.accent;
    ctx.fillText(line, 60, (spec.top ?? 300) + i * 135);
  });
  const out = join(CUT_DIR, `devlog-${name}-thumbnail.png`);
  writeFileSync(out, canvas.toBuffer('image/png'));
  rmSync(frame, { force: true });
  ok(`thumbnail ${out}`);
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
