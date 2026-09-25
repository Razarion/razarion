// A week in the shared world, in numbers: units destroyed, bot bases razed, levels gained.
//
// The numbers come from the game history (GET /rest/editor/game-history-summary), which answers
// in counts only - no player, base or bot name leaves the server that way, so nothing on the card
// can point at a person.
//
// A small number is left out rather than printed. "4 levels gained this week" tells a stranger the
// world is empty, and the post exists to tell them the opposite; with fewer than three figures
// worth showing, the format declines the week instead of posting a weak one.

import { writeFileSync } from 'node:fs';
import { toRelative } from '../paths.mjs';
import { newCard, footer, toJpeg, formatCount, COLORS, FONT, WIDTH, MARGIN } from '../card.mjs';
import { NotReady } from './not-ready.mjs';

const LINK = 'https://www.razarion.com';

/** Below this a figure is not shown. */
const MIN_FIGURE = 10;
const MIN_FIGURES = 3;
const MAX_FIGURES = 4;

export function isoWeek(date) {
  const d = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()));
  const day = d.getUTCDay() || 7;
  d.setUTCDate(d.getUTCDate() + 4 - day);
  const start = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
  return `${d.getUTCFullYear()}-W${String(Math.ceil(((d - start) / 86400000 + 1) / 7)).padStart(2, '0')}`;
}

function sum(rows, test) {
  return rows.filter(test).reduce((n, r) => n + r.count, 0);
}

/**
 * The figures, most striking first. Each one says in its label what it counts, so the card can
 * be read without the caption: "bot units destroyed" rather than "kills".
 */
export function figures(rows) {
  const human = (r) => r.source === 'HUMAN';
  const list = [
    {
      key: 'destroyed',
      label: 'bot units destroyed',
      value: sum(rows, (r) => r.type === 'ITEM_DESTROYED' && human(r) && !r.targetHuman),
    },
    {
      key: 'built',
      label: 'units and buildings built',
      value: sum(rows, (r) => r.type === 'ITEM_CREATED' && human(r)),
    },
    {
      key: 'bases',
      label: 'bot bases razed',
      value: sum(rows, (r) => r.type === 'BASE_DEFEATED' && human(r) && !r.targetHuman),
    },
    { key: 'levels', label: 'levels gained', value: sum(rows, (r) => r.type === 'LEVEL_UP') },
    { key: 'quests', label: 'quests completed', value: sum(rows, (r) => r.type === 'QUEST_PASSED') },
    {
      key: 'lost',
      label: 'player units lost to bots',
      value: sum(rows, (r) => r.type === 'ITEM_DESTROYED' && r.source === 'BOT' && r.targetHuman),
    },
    {
      key: 'founded',
      label: 'new bases founded',
      value: sum(rows, (r) => r.type === 'BASE_CREATED' && human(r)),
    },
  ];
  return list.filter((f) => f.value >= MIN_FIGURE);
}

/** The unit players built most, by its player-facing name. */
function mostBuilt(rows, items) {
  const byName = new Map();
  for (const r of rows) {
    if (r.type !== 'ITEM_CREATED' || r.source !== 'HUMAN' || !r.itemTypeName) continue;
    byName.set(r.itemTypeName, (byName.get(r.itemTypeName) || 0) + r.count);
  }
  const top = [...byName].sort((a, b) => b[1] - a[1])[0];
  if (!top || top[1] < MIN_FIGURE) return null;
  const item = items.find((i) => i.internalName === top[0]);
  return item?.name ? { name: item.name, count: top[1] } : null;
}

function buildText(shown, top) {
  const parts = shown.map((f) => `${formatCount(f.value)} ${f.label}`);
  const list = parts.length > 1 ? parts.slice(0, -1).join(', ') + ' and ' + parts.at(-1) : parts[0];
  const blocks = [`Last week in the shared world of Razarion: ${list}.`];
  if (top) blocks.push(`Most built: the ${top.name}, ${formatCount(top.count)} times.`);
  blocks.push('One persistent world, open-source, in a browser tab. No download, no account.');
  return blocks.join('\n\n');
}

function renderCard(shown, top, week) {
  const card = newCard({ kicker: 'The week in numbers' });
  const { ctx } = card;

  ctx.fillStyle = COLORS.text;
  ctx.font = `700 64px ${FONT}`;
  ctx.fillText('Last week in the', MARGIN, 290);
  ctx.fillText('shared world', MARGIN, 370);

  // Two columns of big figures, the way a scoreboard reads.
  const colW = (WIDTH - MARGIN * 2) / 2;
  shown.forEach((f, i) => {
    const x = MARGIN + (i % 2) * colW;
    const y = 540 + Math.floor(i / 2) * 250;
    ctx.fillStyle = COLORS.accent;
    ctx.font = `800 104px ${FONT}`;
    ctx.fillText(formatCount(f.value), x, y);
    ctx.fillStyle = COLORS.muted;
    ctx.font = `500 32px ${FONT}`;
    ctx.fillText(f.label, x, y + 52);
  });

  if (top) {
    ctx.fillStyle = COLORS.rule;
    ctx.fillRect(MARGIN, 1060, WIDTH - MARGIN * 2, 2);
    ctx.fillStyle = COLORS.text;
    ctx.font = `600 38px ${FONT}`;
    ctx.fillText(`Most built: ${top.name}`, MARGIN, 1130);
  }
  footer(card, week.replace('-W', ' · week '));
  return toJpeg(card);
}

export default {
  name: 'week-in-numbers',
  medium: 'photo',
  summary: 'last week in the shared world, from the game history (counts only)',

  async produce(ctx) {
    // Named after the week that just ended, since that is what the seven days cover.
    const week = isoWeek(new Date(ctx.now.getTime() - 3 * 86400000));
    const subject = `week:${week}`;
    if (ctx.lastOf('week-in-numbers', subject) && !ctx.args.force) {
      throw new NotReady(`${week} already has its post.`);
    }

    const rows = await ctx.api('/rest/editor/game-history-summary?days=7');
    const shown = figures(rows).slice(0, MAX_FIGURES);
    if (shown.length < MIN_FIGURES) {
      throw new NotReady(
        `Only ${shown.length} figure(s) reached ${MIN_FIGURE} this week - too quiet to post` +
          (shown.length ? ` (${shown.map((f) => `${f.value} ${f.label}`).join(', ')}).` : '.')
      );
    }
    const top = mostBuilt(rows, await ctx.items());

    const file = ctx.mediaFile(`week-${week}`, 'jpg');
    writeFileSync(file, renderCard(shown, top, week));

    return {
      subject,
      text: buildText(shown, top),
      link: LINK,
      tags: ['strategygame', 'mmorts'],
      media: [{ type: 'photo', file: toRelative(file), url: null }],
      facts: { week, figures: shown, mostBuilt: top },
    };
  },
};
