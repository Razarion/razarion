// "Which one wins?" - two combat units side by side with their real numbers, and a question.
//
// A question is the one kind of post a small account can get answers to, and the numbers make it
// a fair one: everything on the card comes from the unit config on the live server. The answer is
// deliberately not given. The card says what a unit is on paper; who actually wins depends on
// range, speed and terrain, which is what makes it worth asking - and what a follow-up clip from
// the studio can show.

import { existsSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { DATA_DIR, toRelative } from '../paths.mjs';
import { newCard, footer, toJpeg, drawContained, drawCovered, centeredText, COLORS, FONT, WIDTH, MARGIN } from '../card.mjs';
import { NotReady } from './not-ready.mjs';

const SCENES_DIR = join(DATA_DIR, 'scenes');
const LINK = 'https://www.razarion.com';

/** The same pair again after this long is fine: the numbers may have changed, the audience has. */
const REPEAT_AFTER_DAYS = 120;

export function slugOf(name) {
  return name.toLowerCase().replace(/[^a-z0-9]+/g, '-');
}

/** Player-facing combat units. The bots' copies carry the same name at other prices. */
function fighters(items) {
  return items
    .filter((i) => i.name && i.weaponType && i.health > 0 && i.weaponType.damage > 0)
    .filter((i) => !/^\(Bot\d*\)/.test(String(i.internalName || '')))
    .sort((a, b) => (a.price || 0) - (b.price || 0));
}

function subjectOf(a, b) {
  return 'duel:' + [slugOf(a.name), slugOf(b.name)].sort().join('|');
}

function pairs(units) {
  const out = [];
  for (let i = 0; i < units.length; i++) {
    for (let j = i + 1; j < units.length; j++) out.push([units[i], units[j]]);
  }
  return out;
}

function stats(u) {
  const w = u.weaponType;
  return {
    health: u.health,
    damage: w.damage,
    reload: w.reloadTime,
    range: w.range,
    price: u.price,
    mobile: Boolean(u.physicalAreaConfig?.speed),
    water: u.physicalAreaConfig?.terrainType === 'WATER',
  };
}

/** "3 Vipers or 1 Badger" when the prices divide into each other closely enough to be fair. */
function samePrice(a, b) {
  if (!(a.price > 0 && b.price > 0)) return null;
  const [cheap, dear] = a.price <= b.price ? [a, b] : [b, a];
  const ratio = dear.price / cheap.price;
  const n = Math.round(ratio);
  if (n < 2 || Math.abs(ratio - n) > 0.1) return null;
  return { cheap, dear, n, total: cheap.price * n, dearTotal: dear.price };
}

function plural(name, n) {
  return n === 1 ? name : name + 's';
}

function seconds(value) {
  return Number.isInteger(value) ? `${value}s` : `${value.toFixed(1)}s`;
}

function describe(u) {
  const s = stats(u);
  const kind = s.water ? 'a warship' : s.mobile ? null : 'a tower';
  const lead = kind ? `${u.name} (${kind})` : u.name;
  return `${lead}: ${s.health} health, ${s.damage} damage every ${seconds(s.reload)}, range ${s.range}, costs ${s.price}.`;
}

function buildText(a, b, even) {
  const blocks = [`${a.name} or ${b.name}?`, `${describe(a)}\n${describe(b)}`];
  if (even && !stats(even.dear).mobile) {
    // A tower is not sent anywhere; it is what the others run into.
    blocks.push(
      `${even.n} ${plural(even.cheap.name, even.n)} cost about as much as one ${even.dear.name}. They attack it - who is left standing?`
    );
  } else if (even) {
    blocks.push(
      `For about the same Razarion you get ${even.n} ${plural(even.cheap.name, even.n)} or one ${even.dear.name}. Which side are you sending?`
    );
  } else {
    blocks.push('One against one - which wins?');
  }
  blocks.push('Open-source RTS in a browser tab, no download, no account.');
  return blocks.join('\n\n');
}

async function unitImage(ctx, unit) {
  const scene = join(SCENES_DIR, `${slugOf(unit.name)}.png`);
  if (existsSync(scene)) return { source: scene, scene: true };
  return { source: await ctx.image(unit.thumbnail), scene: false };
}

const ROWS = [
  ['HEALTH', (s) => s.health, 'high'],
  ['DAMAGE', (s) => s.damage, 'high'],
  ['RELOAD', (s) => s.reload, 'low', seconds],
  ['RANGE', (s) => s.range, 'high'],
  ['COST', (s) => s.price, 'low'],
];

async function renderCard(ctx, a, b, even) {
  const card = newCard({ kicker: 'Which one wins?' });
  const { ctx: g } = card;
  const panelW = (WIDTH - MARGIN * 2 - 80) / 2;
  const panelTop = 220;
  const imageH = 380;
  const sa = stats(a);
  const sb = stats(b);

  for (const [index, unit] of [a, b].entries()) {
    const x = MARGIN + index * (panelW + 80);
    g.fillStyle = COLORS.panel;
    g.fillRect(x, panelTop, panelW, imageH);
    const img = await unitImage(ctx, unit);
    if (img.scene) await drawCovered(g, img.source, x, panelTop, panelW, imageH);
    else await drawContained(g, img.source, x + 20, panelTop + 20, panelW - 40, imageH - 40);

    g.fillStyle = COLORS.text;
    g.font = `700 60px ${FONT}`;
    centeredText(g, unit.name, x + panelW / 2, panelTop + imageH + 80);

    const own = index === 0 ? sa : sb;
    const other = index === 0 ? sb : sa;
    let y = panelTop + imageH + 160;
    for (const [label, pick, better, show = String] of ROWS) {
      const mine = pick(own);
      const theirs = pick(other);
      const wins = better === 'high' ? mine > theirs : mine < theirs;
      g.fillStyle = COLORS.muted;
      g.font = `600 22px ${FONT}`;
      g.letterSpacing = '3px';
      g.fillText(label, x + 8, y);
      g.letterSpacing = '0px';
      g.fillStyle = wins ? COLORS.accent : COLORS.text;
      g.font = `700 40px ${FONT}`;
      const value = show(mine);
      g.fillText(value, x + panelW - 8 - g.measureText(value).width, y + 4);
      y += 64;
    }
  }

  g.fillStyle = COLORS.accent;
  g.font = `800 56px ${FONT}`;
  centeredText(g, 'VS', WIDTH / 2, panelTop + imageH / 2 + 20);

  if (even) {
    g.fillStyle = COLORS.text;
    g.font = `600 36px ${FONT}`;
    centeredText(
      g,
      `${even.n} ${plural(even.cheap.name, even.n)} ${stats(even.dear).mobile ? 'or' : 'against'} 1 ${even.dear.name}?`,
      WIDTH / 2,
      1170
    );
  }
  footer(card, 'Green = better on paper');
  return toJpeg(card);
}

export default {
  name: 'duel',
  medium: 'photo',
  summary: 'two combat units, their real numbers, "which one wins?"',
  tones: ['question', 'punchy'],

  async produce(ctx) {
    const units = fighters(await ctx.items());
    if (units.length < 2) throw new NotReady('Fewer than two combat units on the server.');

    let pair;
    if (ctx.args.subject) {
      const names = String(ctx.args.subject).toLowerCase().split(/[,|]/).map((s) => s.trim());
      const found = names.map((n) => units.find((u) => u.name.toLowerCase() === n || slugOf(u.name) === n));
      if (found.length !== 2 || found.some((u) => !u)) {
        throw new Error(`--subject wants two units, e.g. "viper,badger". Available: ${units.map((u) => u.name).join(', ')}`);
      }
      pair = found;
    } else {
      // The pair done longest ago - or never - goes next. A pair done recently is left alone.
      const cutoff = ctx.now.getTime() - REPEAT_AFTER_DAYS * 86400000;
      const ranked = pairs(units)
        .map((p) => ({ p, last: ctx.lastOf('duel', subjectOf(...p)) }))
        .filter(({ last }) => !last || Date.parse(last.date) < cutoff)
        .sort((x, y) => (x.last ? Date.parse(x.last.date) : 0) - (y.last ? Date.parse(y.last.date) : 0));
      if (!ranked.length) throw new NotReady(`Every pair has had a duel in the last ${REPEAT_AFTER_DAYS} days.`);
      // Among the untouched ones, prefer a pair with an even-price question - it is the better post.
      const fresh = ranked.filter((r) => !r.last);
      pair = (fresh.find((r) => samePrice(...r.p)) || ranked[0]).p;
    }

    const [a, b] = pair;
    const even = samePrice(a, b);
    const file = ctx.mediaFile(`duel-${slugOf(a.name)}-${slugOf(b.name)}`, 'jpg');
    writeFileSync(file, await renderCard(ctx, a, b, even));

    return {
      subject: subjectOf(a, b),
      text: buildText(a, b, even),
      link: LINK,
      tags: ['strategygame'],
      media: [{ type: 'photo', file: toRelative(file), url: null }],
      facts: {
        units: [a, b].map((u) => {
          const s = stats(u);
          return {
            name: u.name,
            kind: s.water ? 'warship' : s.mobile ? 'unit' : 'tower',
            health: s.health,
            damage: s.damage,
            reloadSeconds: s.reload,
            range: s.range,
            price: s.price,
          };
        }),
        samePrice: even ? { count: even.n, cheaper: even.cheap.name, dearer: even.dear.name } : null,
        answer: 'not known - the card only shows the numbers on paper',
      },
    };
  },
};
