// The code behind each terrain improvement, for the panels in cut_showcase.mjs and the links in the
// post. Read from git at the commit that made the change, so the picture and the GitHub permalink
// show the same lines - never from the working tree, which moves on.

import { execFileSync } from 'node:child_process';
import { basename, join } from 'node:path';
import { createCanvas } from '@napi-rs/canvas';
import { PIPELINE_ROOT } from './paths.mjs';
import { COLORS, FONT, newCard } from './card.mjs';

const REPO_ROOT = join(PIPELINE_ROOT, '..', '..');
export const GITHUB_REPO = 'https://github.com/Razarion/razarion';

/**
 * Per showcase feature: file, first and last line (1-based, inclusive) at `ref` - the range the
 * permalink covers. `pick` shows only some of those lines (a phone cannot read 100 columns),
 * `trimComments` drops comments at the end of a line, `unwrap` takes JavaScript out of the Java
 * string it is written in.
 */
export const SNIPPETS = {
  light: {
    ref: 'bde8a8c9a', from: 525, to: 532, pick: [525, 527, 532], trimComments: true,
    file: 'razarion-frontend/src/app/game/renderer/babylon-render-service-access-impl.service.ts',
  },
  clouds: {
    ref: 'bde8a8c9a', from: 651, to: 671, pick: [651, 653, 654, 656, 669, 670, 671],
    file: 'razarion-frontend/src/app/game/renderer/ground-material.ts',
  },
  relief: {
    ref: 'bde8a8c9a', from: 424, to: 435, unwrap: 'java-string',
    file: 'razarion-client-worker-teavm/src/main/java/com/btxtech/worker/TeaVMNativeTerrainShapeAccess.java',
  },
  wind: {
    ref: 'bde8a8c9a', from: 64, to: 69, pick: [64, 65, 66, 69],
    file: 'razarion-frontend/src/app/game/renderer/wind-plugin.ts',
  },
  paths: {
    ref: 'bde8a8c9a', from: 76, to: 81,
    file: 'razarion-ai-content/scripts/noob-island-paths.mjs',
  },
  growth: {
    // master, resolved to its commit when the cut is made: the commit that added this was squashed
    ref: 'master', from: 20, to: 29, pick: [20, 25, 26, 27, 28, 29],
    file: 'razarion-frontend/src/app/game/renderer/vegetation-mask.ts',
  },
};

const git = (args) => execFileSync('git', args, { cwd: REPO_ROOT, encoding: 'utf8' });

/** The lines and the permalink; `published` says whether GitHub has the commit yet. */
export function loadSnippet(feature) {
  const spec = SNIPPETS[feature];
  if (!spec) return null;
  const sha = git(['rev-parse', spec.ref]).trim();
  const all = git(['show', `${sha}:${spec.file}`]).split('\n');
  const numbers = spec.pick ?? Array.from({ length: spec.to - spec.from + 1 }, (_, i) => spec.from + i);
  const lines = numbers.map((no) => {
    let line = all[no - 1].replace(/\r$/, '');
    if (spec.unwrap === 'java-string') line = line.replace(/^(\s*)"/, '$1').replace(/"\s*\+?\s*$/, '');
    if (spec.trimComments) line = line.replace(/\s+\/\/.*$/, '');
    return line;
  });
  const indent = Math.min(...lines.filter((l) => l.trim()).map((l) => l.match(/^ */)[0].length));
  let published = false;
  try {
    published = git(['branch', '-r', '--contains', sha]).trim().length > 0;
  } catch { /* unknown commit on the remote side */ }
  return {
    feature,
    file: spec.file,
    lines: lines.map((l) => l.slice(indent).replace(/\s+$/, '')),
    url: `${GITHUB_REPO}/blob/${sha}/${spec.file}#L${spec.from}-L${spec.to}`,
    published,
  };
}

const KEYWORDS = new Set(['const', 'let', 'var', 'for', 'if', 'else', 'return', 'new', 'function', 'continue',
  'float', 'vec2', 'vec3', 'this', 'true', 'false', 'null', '#ifdef', '#endif']);
const TOKEN = /(\/\/.*$|"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|`[^`]*`|#?[A-Za-z_][A-Za-z0-9_]*|\d+(?:\.\d+)?|\s+|.)/g;
const COLOR = {
  comment: '#8a837c',
  string: '#e3b778',
  keyword: COLORS.accent,
  number: '#7cc4e8',
  plain: COLORS.text,
};

function colourOf(token) {
  if (token.startsWith('//')) return COLOR.comment;
  if (/^["'`]/.test(token)) return COLOR.string;
  if (KEYWORDS.has(token)) return COLOR.keyword;
  if (/^\d/.test(token)) return COLOR.number;
  return COLOR.plain;
}

/**
 * The snippet as a translucent panel `width` wide: file name on top, the lines below, the type
 * size picked so the longest line fits.
 */
export function codePanelPng(snippet, width) {
  const pad = Math.round(width * 0.04);
  const longest = Math.max(...snippet.lines.map((l) => l.length), 30);
  // Monospace glyphs are ~0.55 of the size wide
  const size = Math.max(12, Math.min(26, Math.floor((width - 2 * pad) / (longest * 0.56))));
  const lineHeight = Math.round(size * 1.35);
  const header = Math.round(size * 2.2);
  const height = header + pad + snippet.lines.length * lineHeight + pad;
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');

  ctx.fillStyle = 'rgba(12,10,9,0.82)';
  roundRect(ctx, 0, 0, width, height, Math.round(size * 0.6));
  ctx.fill();
  ctx.fillStyle = COLORS.accent;
  ctx.fillRect(0, header - 2, width, 2);

  ctx.textBaseline = 'middle';
  ctx.font = `600 ${Math.round(size * 0.9)}px Consolas, 'Courier New', monospace`;
  ctx.fillStyle = COLORS.muted;
  ctx.fillText(basename(snippet.file), pad, header / 2);

  ctx.font = `400 ${size}px Consolas, 'Courier New', monospace`;
  snippet.lines.forEach((line, i) => {
    let x = pad;
    const y = header + pad + i * lineHeight + lineHeight / 2;
    for (const token of line.match(TOKEN) ?? []) {
      ctx.fillStyle = colourOf(token);
      ctx.fillText(token, x, y);
      x += ctx.measureText(token).width;
    }
  });
  return { png: canvas.toBuffer('image/png'), width, height };
}

function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}

/** The closing card: open source, the repository, where to play. */
export function outroPng(width, height) {
  const card = newCard({ width, height });
  const { ctx } = card;
  const scale = Math.min(width, height) / 1080;
  const centre = (text, y, size, weight, colour) => {
    ctx.font = `${weight} ${Math.round(size * scale)}px ${FONT}`;
    ctx.fillStyle = colour;
    ctx.fillText(text, (width - ctx.measureText(text).width) / 2, y);
  };
  centre('The code is open source', height * 0.42, 72, 700, COLORS.text);
  centre(GITHUB_REPO.replace('https://', ''), height * 0.42 + 110 * scale, 52, 600, COLORS.accent);
  centre('Play free in the browser: razarion.com', height * 0.42 + 210 * scale, 40, 400, COLORS.muted);
  return card.canvas.toBuffer('image/png');
}

