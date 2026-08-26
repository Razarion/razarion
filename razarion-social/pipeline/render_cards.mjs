#!/usr/bin/env node
// Step 3: give the text-only posts something to be posted as.
//
// Instagram refuses a post without an image or a video, so a post that was only words on X needs
// a card rendered for it. Only entries flagged needs-card get one, and only those a human has not
// already set to "skip" - rendering a card for a post that is not going to be published is work
// nobody asked for.
//
//   node render_cards.mjs             # render every card that is missing
//   node render_cards.mjs --force     # re-render, e.g. after editing a caption
//   node render_cards.mjs --id 123    # just this one

import { existsSync } from 'node:fs';
import { join } from 'node:path';
import { createCanvas, GlobalFonts } from '@napi-rs/canvas';
import { writeFileSync } from 'node:fs';
import { parseArgs } from './lib/args.mjs';
import { CARDS_DIR, CAPTIONS_FILE, ensureDir, readJson, writeJson, toRelative } from './lib/paths.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

// 1080x1350 is Instagram's tallest feed format: the most screen a single image gets before the
// crop, which for a card that is nothing but text is the whole point.
const WIDTH = 1080;
const HEIGHT = 1350;
const MARGIN = 96;

// Taken from the game's own loading screen in razarion-frontend/src/index.html, so a card looks
// like it came from the same project as the screenshots next to it in the grid.
const BACKGROUND = '#1c1917';
const BACKGROUND_DEEP = '#0c0a09';
const ACCENT = '#10b981';
const ACCENT_DARK = '#014737';
const TEXT = '#e7e5e4';
const MUTED = '#a8a29e';

const FONT_STACK = 'Segoe UI, Arial, Helvetica, sans-serif';

const MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

function formatDate(iso) {
  const d = new Date(iso);
  return `${d.getUTCDate()} ${MONTHS[d.getUTCMonth()]} ${d.getUTCFullYear()}`;
}

/**
 * The caption minus everything that is caption furniture rather than what the post said.
 *
 * Hashtags and "Link in bio." belong under the picture, not inside it - printing them on the card
 * would say the same thing twice and eat the space the actual sentence needs.
 */
function cardText(caption) {
  return caption
    .split(/\n{2,}/)
    .filter((block) => !/^\s*(?:#[A-Za-z0-9_]+\s*)+$/.test(block))
    .filter((block) => block.trim() !== 'Link in bio.')
    .filter((block) => !/^Originally posted on .+\.$/.test(block.trim()))
    .join('\n\n')
    .trim();
}

function wrap(ctx, text, maxWidth) {
  const lines = [];
  for (const paragraph of text.split('\n')) {
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
 * Picks the largest size the text still fits at.
 *
 * The posts run from one line to a full paragraph, and a fixed size would either overflow the long
 * ones or leave the short ones looking lost in the middle of an empty card.
 */
function fitText(ctx, text, maxWidth, maxHeight) {
  for (let size = 68; size >= 30; size -= 2) {
    ctx.font = `600 ${size}px ${FONT_STACK}`;
    const lineHeight = Math.round(size * 1.34);
    const lines = wrap(ctx, text, maxWidth);
    if (lines.length * lineHeight <= maxHeight) return { size, lineHeight, lines };
  }
  ctx.font = `600 30px ${FONT_STACK}`;
  const lineHeight = Math.round(30 * 1.34);
  const lines = wrap(ctx, text, maxWidth);
  // Longer than the card can hold. Cut it and say so, rather than printing over the footer.
  const max = Math.floor(maxHeight / lineHeight);
  return { size: 30, lineHeight, lines: lines.slice(0, max), truncated: lines.length > max };
}

function renderCard(entry) {
  const canvas = createCanvas(WIDTH, HEIGHT);
  const ctx = canvas.getContext('2d');

  const backdrop = ctx.createLinearGradient(0, 0, 0, HEIGHT);
  backdrop.addColorStop(0, BACKGROUND);
  backdrop.addColorStop(1, BACKGROUND_DEEP);
  ctx.fillStyle = backdrop;
  ctx.fillRect(0, 0, WIDTH, HEIGHT);

  // A single accent rule down the left edge, the way the loading screen carries its green.
  const rule = ctx.createLinearGradient(0, 0, 0, HEIGHT);
  rule.addColorStop(0, ACCENT);
  rule.addColorStop(1, ACCENT_DARK);
  ctx.fillStyle = rule;
  ctx.fillRect(0, 0, 10, HEIGHT);

  ctx.fillStyle = ACCENT;
  ctx.font = `700 30px ${FONT_STACK}`;
  ctx.letterSpacing = '5px';
  ctx.fillText('RAZARION', MARGIN, MARGIN + 30);
  ctx.letterSpacing = '0px';

  const text = cardText(entry.caption);
  const bodyTop = MARGIN + 150;
  const bodyBottom = HEIGHT - MARGIN - 110;
  const fitted = fitText(ctx, text, WIDTH - MARGIN * 2, bodyBottom - bodyTop);

  ctx.fillStyle = TEXT;
  ctx.font = `600 ${fitted.size}px ${FONT_STACK}`;
  // Vertically centred in the body area: a two-line post floating at the top of a tall card reads
  // as a mistake, and centring costs nothing for the long ones.
  const blockHeight = fitted.lines.length * fitted.lineHeight;
  let y = bodyTop + Math.max(0, (bodyBottom - bodyTop - blockHeight) / 2) + fitted.size;
  for (const line of fitted.lines) {
    ctx.fillText(line, MARGIN, y);
    y += fitted.lineHeight;
  }

  ctx.fillStyle = MUTED;
  ctx.font = `400 28px ${FONT_STACK}`;
  ctx.fillText(formatDate(entry.date), MARGIN, HEIGHT - MARGIN - 40);

  ctx.fillStyle = ACCENT;
  ctx.font = `600 28px ${FONT_STACK}`;
  const domain = 'razarion.com';
  ctx.fillText(domain, WIDTH - MARGIN - ctx.measureText(domain).width, HEIGHT - MARGIN - 40);

  return { buffer: canvas.toBuffer('image/jpeg', 92), truncated: Boolean(fitted.truncated) };
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const force = Boolean(args.force);
  const onlyId = args.id ? String(args.id) : null;

  const doc = readJson(CAPTIONS_FILE);
  if (!doc) throw new Error(`No ${toRelative(CAPTIONS_FILE)}. Run: node build_captions.mjs`);

  info(`Fonts available: ${GlobalFonts.families.length ? 'yes' : 'none found - text will not render'}`);
  ensureDir(CARDS_DIR);

  let rendered = 0;
  let skipped = 0;
  let truncated = 0;

  for (const entry of doc.captions) {
    if (onlyId && entry.id !== onlyId) continue;
    if (!entry.needs_card) continue;
    if (entry.status === 'skip') {
      skipped++;
      continue;
    }

    const file = join(CARDS_DIR, entry.id + '.jpg');
    if (existsSync(file) && !force) {
      // Point the entry at the card even when there is nothing to render. build_captions.mjs
      // rebuilds media from posts.json, where a text-only post has none, so skipping this line
      // leaves an entry marked ok with no media at all - and publish.mjs quietly passes it over.
      entry.card = toRelative(file);
      entry.media = [{ type: 'photo', file: entry.card }];
      continue;
    }

    const card = renderCard(entry);
    writeFileSync(file, card.buffer);
    entry.card = toRelative(file);
    // The card is what gets posted, so it takes the media slot the post never had.
    entry.media = [{ type: 'photo', file: entry.card }];
    rendered++;
    if (card.truncated) {
      truncated++;
      if (!entry.flags.includes('card-truncated')) entry.flags.push('card-truncated');
      warn(`${entry.id}: text too long for the card, it was cut. Shorten the caption and re-run.`);
    }
    step(`${entry.id} -> ${toRelative(file)}`);
  }

  writeJson(CAPTIONS_FILE, doc);

  info('');
  ok(`${rendered} card(s) rendered into ${toRelative(CARDS_DIR)}`);
  if (skipped) info(`  ${skipped} skipped because their status is "skip"`);
  if (truncated) warn(`  ${truncated} had to be cut - check those captions`);
  info('  captions.json now points at the card as the post media.');
}

try {
  main();
} catch (err) {
  fail(err.message);
  process.exit(1);
}
