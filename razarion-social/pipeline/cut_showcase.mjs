#!/usr/bin/env node
// Cuts a --reveal run of record_showcase.mjs into one continuous camera move in which the terrain
// improvements switch in one after the other.
//
//   node cut_showcase.mjs --run 20261007-1800-hills-reveal                    # landscape
//   node cut_showcase.mjs --run 20261007-1800-hills-reveal --shape portrait
//   node cut_showcase.mjs --run 20261007-1800-hills-reveal --code off         # without the code panels
//   node cut_showcase.mjs --run 20261007-1800-hills-reveal --outro off        # as a chapter, no closing card
//   node cut_showcase.mjs --run 20261007-1800-hills-reveal --hook "My island looked boring"   # a Reel's first line
//   node cut_showcase.mjs --list                                              # the runs in the log
//   node cut_showcase.mjs --takes 1824-ground-reveal-07,1840-ground-ground-prototype \n//        --labels "Ground today,New ground mix" --name ground-before-after        # any takes of one shot
//
// Every take of a run flew the same camera plan, so state i can supply seconds [i*L, (i+1)*L] of
// its own take and the camera never jumps: the picture keeps moving while light, clouds, relief...
// come on. Neighbouring states overlap by a short crossfade. A label in the corner names what
// was just added.

import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { execFile } from 'node:child_process';
import { promisify } from 'node:util';
import { join } from 'node:path';
import { createCanvas } from '@napi-rs/canvas';
import { parseArgs } from './lib/args.mjs';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { FFMPEG, probeVideo } from './lib/video.mjs';
import { COLORS, FONT } from './lib/card.mjs';
import { GITHUB_REPO, codePanelPng, loadSnippet, outroPng } from './lib/showcase-code.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const execFileAsync = promisify(execFile);
const SHOWCASE_DIR = join(PIPELINE_ROOT, 'data', 'showcase');
const LOG_FILE = join(SHOWCASE_DIR, 'log.json');
const CUT_DIR = join(SHOWCASE_DIR, 'cuts');
const FPS = 30;
const OUTRO_SECONDS = 4;
const HOOK_SECONDS = 3;

/** What the newest feature of a state is called on screen. */
const LABELS = {
  none: 'Before',
  light: '+ Light',
  clouds: '+ Cloud shadows',
  relief: '+ Relief shading',
  wind: '+ Wind',
  paths: '+ Paths',
  growth: '+ Growth under plants',
};

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const log = existsSync(LOG_FILE) ? JSON.parse(readFileSync(LOG_FILE, 'utf8')) : [];
  const runOf = (t) => t.file.replace(/^data\/showcase\//, '').replace(/-\d\d-[^-]+-(landscape|portrait)\.mp4$/, '');
  if (args.list || (!args.run && !args.takes)) {
    const runs = [...new Set(log.map(runOf))];
    for (const r of runs) info(`  ${r}  (${log.filter((t) => runOf(t) === r).length} takes)`);
    if (!args.list) throw new Error('Which run? Pass --run <name> (see --list).');
    return;
  }
  const shape = String(args.shape ?? 'landscape');
  // --takes: takes of different runs, in the given order (they must have flown the same shot)
  const takes = args.takes
    ? String(args.takes).split(',').map((part) => {
      const take = log.find((t) => t.file.includes(part.trim()) && t.shape === shape);
      if (!take) throw new Error(`No ${shape} take matches "${part}".`);
      return take;
    })
    : log.filter((t) => runOf(t) === String(args.run) && t.shape === shape).sort((a, b) => a.file.localeCompare(b.file));
  if (takes.length < 2) throw new Error(`${takes.length} ${shape} take(s); a reveal needs at least two.`);
  if (new Set(takes.map((t) => t.shot)).size > 1) throw new Error('The takes are of different shots - the camera would jump.');
  const customLabels = args.labels ? String(args.labels).split(',').map((l) => l.trim()) : null;
  const name = String(args.name ?? args.run);

  const probes = await Promise.all(takes.map((t) => probeVideo(join(PIPELINE_ROOT, t.file))));
  const duration = Math.min(...probes.map((p) => p.duration));
  const { width, height } = probes[0];
  const n = takes.length;
  const segment = duration / n;
  const fade = Math.min(0.6, segment / 3);
  info(`${n} states over ${duration.toFixed(1)}s: ${segment.toFixed(2)}s each, ${fade.toFixed(2)}s crossfade`);

  mkdirSync(CUT_DIR, { recursive: true });
  const withCode = args.code !== 'off' && !customLabels;
  const portrait = height > width;
  const added = takes.map((t, i) => t.features.filter((f) => !(i ? takes[i - 1].features : []).includes(f)));
  const labelFiles = takes.map((t, i) => {
    const text = customLabels ? customLabels[i] ?? '' : added[i].length === 1 ? LABELS[added[i][0]] : added[i].length ? `+ ${added[i].join(', ')}` : LABELS.none;
    const file = join(CUT_DIR, `label-${shape}-${i}.png`);
    writeFileSync(file, labelPng(text, width));
    return file;
  });
  // The code that made each step, as a panel over the picture: right half in landscape, top in portrait
  const snippets = takes.map((_, i) => (withCode && added[i].length === 1 ? loadSnippet(added[i][0]) : null));
  const panelWidth = Math.round(portrait ? width - 2 * 48 : width * 0.44);
  const panelFiles = snippets.map((snippet, i) => {
    if (!snippet) return null;
    const file = join(CUT_DIR, `code-${shape}-${i}.png`);
    writeFileSync(file, codePanelPng(snippet, panelWidth).png);
    return file;
  });
  const outroFile = join(CUT_DIR, `outro-${shape}.png`);
  writeFileSync(outroFile, outroPng(width, height));

  // Inputs: the takes, the labels, the code panels, the outro card
  const inputs = [...takes.map((t) => ['-i', join(PIPELINE_ROOT, t.file)]), ...labelFiles.map((f) => ['-loop', '1', '-i', f])].flat();
  let nextInput = n * 2;
  const panelInput = panelFiles.map((f) => {
    if (!f) return null;
    inputs.push('-loop', '1', '-i', f);
    return nextInput++;
  });
  const outroInput = nextInput;
  inputs.push('-loop', '1', '-t', String(OUTRO_SECONDS + 1), '-i', outroFile);
  // --hook: a big line for the first seconds - a Reel is decided before the first step is over
  const hookInput = args.hook ? nextInput + 1 : null;
  if (args.hook) {
    const hookFile = join(CUT_DIR, `hook-${shape}.png`);
    writeFileSync(hookFile, hookPng(String(args.hook), width, height));
    inputs.push('-loop', '1', '-i', hookFile);
  }

  const filters = [];
  takes.forEach((_, i) => {
    const start = i * segment;
    const end = i === n - 1 ? duration : Math.min(duration, (i + 1) * segment + fade);
    // The take's own clock, so segment i shows the camera where the plan has it at that moment
    filters.push(`[${i}:v]fps=${FPS},trim=start=${start.toFixed(3)}:end=${end.toFixed(3)},setpts=PTS-STARTPTS,settb=AVTB,format=yuv420p[s${i}]`);
  });
  let last = 's0';
  for (let i = 1; i < n; i++) {
    filters.push(`[${last}][s${i}]xfade=transition=fade:duration=${fade.toFixed(3)}:offset=${(i * segment).toFixed(3)}[x${i}]`);
    last = `x${i}`;
  }
  // Label and code panel from the end of a state's crossfade until the next state starts fading in
  takes.forEach((_, i) => {
    const from = i === 0 ? 0 : i * segment + fade;
    const to = i === n - 1 ? duration : (i + 1) * segment;
    const when = `enable='between(t,${from.toFixed(3)},${to.toFixed(3)})':shortest=1`;
    filters.push(`[${last}][${n + i}:v]overlay=0:H-h:${when}[l${i}]`);
    last = `l${i}`;
    if (panelInput[i] != null) {
      const at = portrait ? '48:160' : 'W-w-48:48';
      filters.push(`[${last}][${panelInput[i]}:v]overlay=${at}:${when}[c${i}]`);
      last = `c${i}`;
    }
  });
  // Into the outro card: open source, the repository, where to play
  if (hookInput != null) {
    filters.push(`[${last}][${hookInput}:v]overlay=0:0:enable='between(t,0,${HOOK_SECONDS})':shortest=1[hook]`);
    last = 'hook';
  }
  filters.push(`[${last}]fps=${FPS},settb=AVTB,format=yuv420p[main]`);
  // --outro off: a chapter of a longer video (assemble_showcase.mjs), which has one outro at its end
  const withOutro = args.outro !== 'off';
  if (withOutro) filters.push(`[${outroInput}:v]fps=${FPS},format=yuv420p,settb=AVTB[outro]`);
  filters.push(withOutro
    ? `[main][outro]xfade=transition=fade:duration=0.6:offset=${(duration - 0.6).toFixed(3)}[final]`
    : '[main]null[final]');
  const total = withOutro ? duration + OUTRO_SECONDS - 0.6 : duration;

  const out = String(args.out ?? join(CUT_DIR, `${name}-${shape}.mp4`));
  step(`cutting ${out}`);
  await execFileAsync(FFMPEG, [
    '-y', '-v', 'error', ...inputs,
    '-filter_complex', filters.join(';'),
    '-map', '[final]', '-t', total.toFixed(3), '-an',
    '-c:v', 'libx264', '-preset', 'medium', '-crf', '18', '-pix_fmt', 'yuv420p', '-movflags', '+faststart',
    out,
  ], { maxBuffer: 32 * 1024 * 1024 });
  const probe = await probeVideo(out);
  if (!probe || probe.duration < total * 0.9) throw new Error(`The cut ${out} came out broken.`);
  ok(`${out}: ${probe.width}x${probe.height}, ${probe.duration.toFixed(1)}s`);

  // The permalinks for the post text: every code panel links to exactly the lines it shows
  const shown = snippets.filter(Boolean);
  if (shown.length) {
    const linkFile = out.replace(/\.mp4$/, '.links.txt');
    const lines = [`Code: ${GITHUB_REPO}`, ...shown.map((sn) => `${LABELS[sn.feature].replace(/^\+ /, '')}: ${sn.url}`)];
    writeFileSync(linkFile, lines.join('\n') + '\n');
    info(`  links: ${linkFile}`);
    for (const sn of shown.filter((x) => !x.published)) {
      warn(`  ${sn.feature}: commit not on GitHub yet - its link only works after a push (${sn.url.split('/blob/')[1].slice(0, 9)})`);
    }
  }
}

/** Full-frame overlay: the hook line large, a third down from the top. */
function hookPng(text, width, height) {
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');
  const size = Math.round(width * (height > width ? 0.075 : 0.045));
  ctx.font = `800 ${size}px ${FONT}`;
  const lines = [];
  let line = '';
  for (const word of text.split(' ')) {
    const next = line ? `${line} ${word}` : word;
    if (ctx.measureText(next).width > width * 0.86 && line) { lines.push(line); line = word; } else { line = next; }
  }
  if (line) lines.push(line);
  const top = height * 0.3 - (lines.length * size * 1.2) / 2;
  lines.forEach((l, i) => {
    const w = ctx.measureText(l).width, x = (width - w) / 2, y = top + i * size * 1.2;
    ctx.fillStyle = 'rgba(12,10,9,0.7)';
    ctx.fillRect(x - size * 0.3, y - size * 0.95, w + size * 0.6, size * 1.2);
    ctx.fillStyle = COLORS.text;
    ctx.fillText(l, x, y);
  });
  return canvas.toBuffer('image/png');
}

/** A label strip as wide as the video, text bottom left on a soft dark gradient. */
function labelPng(text, width) {
  const scale = width / 1920;
  const height = Math.round(150 * scale);
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');
  const gradient = ctx.createLinearGradient(0, 0, 0, height);
  gradient.addColorStop(0, 'rgba(12,10,9,0)');
  gradient.addColorStop(1, 'rgba(12,10,9,0.6)');
  ctx.fillStyle = gradient;
  ctx.fillRect(0, 0, width, height);
  const size = Math.round(Math.max(44, 56 * scale));
  ctx.font = `700 ${size}px ${FONT}`;
  ctx.textBaseline = 'alphabetic';
  const x = Math.round(64 * scale), y = height - Math.round(44 * scale);
  ctx.fillStyle = COLORS.accent;
  ctx.fillRect(x - Math.round(20 * scale), y - size * 0.8, Math.max(6, Math.round(8 * scale)), size * 0.9);
  ctx.fillStyle = COLORS.text;
  ctx.fillText(text, x, y);
  return canvas.toBuffer('image/png');
}

main().catch((e) => {
  fail(e.message);
  process.exit(1);
});
