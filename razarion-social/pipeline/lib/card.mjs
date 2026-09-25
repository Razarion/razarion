// The drawing every rendered picture shares: the game's palette, the backdrop, the wordmark and
// the type fitting. The formats in lib/formats/ build on it, so a quiz, a weekly summary and a
// devlog sit next to each other in the grid as one account rather than three.
//
// render_cards.mjs and generate.mjs still carry their own copies of the same values; they predate
// this file.

import { createCanvas, loadImage } from '@napi-rs/canvas';

// 1080x1350 is Instagram's tallest feed format, and within Facebook's and X's limits.
export const WIDTH = 1080;
export const HEIGHT = 1350;
export const MARGIN = 96;

// Taken from the game's loading screen in razarion-frontend/src/index.html.
export const COLORS = {
  background: '#1c1917',
  backgroundDeep: '#0c0a09',
  accent: '#10b981',
  accentDark: '#014737',
  text: '#e7e5e4',
  muted: '#a8a29e',
  rule: '#3a352f',
  panel: '#292524',
};

export const FONT = 'Segoe UI, Arial, Helvetica, sans-serif';

/** A canvas with the backdrop, the green rule down the left edge and the wordmark already on it. */
export function newCard({ width = WIDTH, height = HEIGHT, kicker = null } = {}) {
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');

  const backdrop = ctx.createLinearGradient(0, 0, 0, height);
  backdrop.addColorStop(0, COLORS.background);
  backdrop.addColorStop(1, COLORS.backgroundDeep);
  ctx.fillStyle = backdrop;
  ctx.fillRect(0, 0, width, height);

  const rule = ctx.createLinearGradient(0, 0, 0, height);
  rule.addColorStop(0, COLORS.accent);
  rule.addColorStop(1, COLORS.accentDark);
  ctx.fillStyle = rule;
  ctx.fillRect(0, 0, 10, height);

  ctx.fillStyle = COLORS.accent;
  ctx.font = `700 30px ${FONT}`;
  ctx.letterSpacing = '5px';
  ctx.fillText('RAZARION', MARGIN, MARGIN + 30);
  if (kicker) {
    const at = MARGIN + ctx.measureText('RAZARION').width + 28;
    ctx.fillStyle = COLORS.muted;
    ctx.font = `600 24px ${FONT}`;
    ctx.fillText(kicker.toUpperCase(), at, MARGIN + 28);
  }
  ctx.letterSpacing = '0px';

  return { canvas, ctx, width, height };
}

export function footer(card, left = null) {
  const { ctx, width, height } = card;
  if (left) {
    ctx.fillStyle = COLORS.muted;
    ctx.font = `400 28px ${FONT}`;
    ctx.fillText(left, MARGIN, height - MARGIN + 10);
  }
  ctx.fillStyle = COLORS.accent;
  ctx.font = `600 28px ${FONT}`;
  const domain = 'razarion.com';
  ctx.fillText(domain, width - MARGIN - ctx.measureText(domain).width, height - MARGIN + 10);
}

export function toJpeg(card) {
  return card.canvas.toBuffer('image/jpeg', 92);
}

export function wrap(ctx, text, maxWidth) {
  const lines = [];
  for (const paragraph of String(text).split('\n')) {
    if (!paragraph.trim()) {
      lines.push('');
      continue;
    }
    let line = '';
    for (const word of paragraph.split(/\s+/)) {
      const candidate = line ? line + ' ' + word : word;
      if (ctx.measureText(candidate).width <= maxWidth || !line) {
        line = candidate;
      } else {
        lines.push(line);
        line = word;
      }
    }
    if (line) lines.push(line);
  }
  return lines;
}

/**
 * The largest size, between max and min, at which the text fits the box. Returns the lines at that
 * size; `truncated` says the text did not fit even at the smallest one and was cut.
 */
export function fitText(ctx, text, maxWidth, maxHeight, { max = 68, min = 30, weight = 600, leading = 1.34 } = {}) {
  for (let size = max; size >= min; size -= 2) {
    ctx.font = `${weight} ${size}px ${FONT}`;
    const lineHeight = Math.round(size * leading);
    const lines = wrap(ctx, text, maxWidth);
    if (lines.length * lineHeight <= maxHeight) return { size, lineHeight, lines, truncated: false };
  }
  ctx.font = `${weight} ${min}px ${FONT}`;
  const lineHeight = Math.round(min * leading);
  const lines = wrap(ctx, text, maxWidth);
  const fit = Math.floor(maxHeight / lineHeight);
  return { size: min, lineHeight, lines: lines.slice(0, fit), truncated: lines.length > fit };
}

/**
 * Draws an image into a box, scaled to fit and centred, never beyond `maxScale` of its own size.
 * The stored unit thumbnails are 200px, and past about twice that they turn to mush.
 */
export async function drawContained(ctx, source, x, y, w, h, { maxScale = 2 } = {}) {
  const image = await loadImage(source);
  const scale = Math.min(w / image.width, h / image.height, maxScale);
  const dw = image.width * scale;
  const dh = image.height * scale;
  ctx.imageSmoothingEnabled = true;
  ctx.imageSmoothingQuality = 'high';
  ctx.drawImage(image, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh);
}

/** Draws an image so it covers the box completely, cropping what overhangs. For scene renders. */
export async function drawCovered(ctx, source, x, y, w, h) {
  const image = await loadImage(source);
  const scale = Math.max(w / image.width, h / image.height);
  const sw = w / scale;
  const sh = h / scale;
  ctx.save();
  ctx.beginPath();
  ctx.rect(x, y, w, h);
  ctx.clip();
  ctx.imageSmoothingEnabled = true;
  ctx.imageSmoothingQuality = 'high';
  ctx.drawImage(image, (image.width - sw) / 2, (image.height - sh) / 2, sw, sh, x, y, w, h);
  ctx.restore();
}

export function centeredText(ctx, text, cx, y) {
  ctx.fillText(text, cx - ctx.measureText(text).width / 2, y);
}

/** 1234 -> "1,234". The posts are in English, so is the grouping. */
export function formatCount(n) {
  return Math.round(n).toLocaleString('en-US');
}
