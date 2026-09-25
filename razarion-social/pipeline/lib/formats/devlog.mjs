// "New this week": what changed in the game, from the git history.
//
// The commits are the source, but not the text. Their subjects are written for whoever reads the
// code ("Split quest 392, place the quest prompt, answer provocation at once, log GC"), and a
// player needs one line in their own words. So this is a two-step format:
//
//   1. the first run collects the week's game commits into data/drafts/devlog-<week>.json and stops
//   2. a person (or later a writer model) fills `lines` with 2-5 player-facing sentences
//   3. the second run renders the card from those lines
//
// Only commits touching the game itself count - the social pipeline, docs and the content tooling
// changed nothing a player can see.

import { execFileSync } from 'node:child_process';
import { writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { DATA_DIR, PIPELINE_ROOT, ensureDir, readJson, writeJson, toRelative } from '../paths.mjs';
import { newCard, footer, toJpeg, fitText, COLORS, FONT, WIDTH, HEIGHT, MARGIN } from '../card.mjs';
import { isoWeek } from './week-in-numbers.mjs';
import { NotReady } from './not-ready.mjs';

const DRAFTS_DIR = join(DATA_DIR, 'drafts');
const REPO_ROOT = join(PIPELINE_ROOT, '..', '..');
const LINK = 'https://www.razarion.com';

const GAME_PATHS = [
  'razarion-share/src/main',
  'razarion-ui-service/src/main',
  'razarion-client-teavm/src/main',
  'razarion-client-worker-teavm/src/main',
  'razarion-frontend/src/app/game',
];

const MAX_LINES = 5;

function git(args) {
  return execFileSync('git', args, { cwd: REPO_ROOT, encoding: 'utf8' });
}

/**
 * The week's game commits on origin/master. Commits can come from the other checkout, so the local
 * branch is not the whole story; the fetch is best-effort and a stale origin is said, not hidden.
 */
function weekCommits(since) {
  try {
    git(['fetch', '--quiet', 'origin', 'master']);
  } catch {
    // offline or no credentials - origin/master as last fetched
  }
  const out = git([
    'log', 'origin/master', '--no-merges', `--since=${since.toISOString()}`,
    '--format=%h%x09%ad%x09%s', '--date=short', '--', ...GAME_PATHS,
  ]);
  return out.split('\n').filter(Boolean).map((line) => {
    const [hash, date, subject] = line.split('\t');
    return { hash, date, subject };
  });
}

function buildText(lines) {
  return [
    'New in Razarion this week:',
    lines.map((l) => `- ${l}`).join('\n'),
    'Open-source RTS in a browser tab. No download, no account.',
  ].join('\n\n');
}

function renderCard(lines, week) {
  const card = newCard({ kicker: 'New this week' });
  const { ctx } = card;

  ctx.fillStyle = COLORS.text;
  ctx.font = `700 72px ${FONT}`;
  ctx.fillText("What's new", MARGIN, 300);

  const top = 400;
  const bottom = HEIGHT - MARGIN - 80;
  const gap = 36;
  const available = (bottom - top - gap * (lines.length - 1)) / lines.length;
  let y = top;
  let truncated = false;
  for (const line of lines) {
    const fitted = fitText(ctx, line, WIDTH - MARGIN * 2 - 50, available, { max: 46, min: 28, weight: 500 });
    truncated ||= fitted.truncated;
    ctx.fillStyle = COLORS.accent;
    ctx.fillRect(MARGIN, y + fitted.size * 0.35, 14, 14);
    ctx.fillStyle = COLORS.text;
    ctx.font = `500 ${fitted.size}px ${FONT}`;
    let ly = y + fitted.size;
    for (const l of fitted.lines) {
      ctx.fillText(l, MARGIN + 50, ly);
      ly += fitted.lineHeight;
    }
    y += fitted.lines.length * fitted.lineHeight + gap;
  }
  footer(card, week.replace('-W', ' · week '));
  return { buffer: toJpeg(card), truncated };
}

export default {
  name: 'devlog',
  medium: 'photo',
  summary: "what changed in the game this week - drafted from git, worded by a person",

  async produce(ctx) {
    const week = isoWeek(ctx.now);
    const subject = `devlog:${week}`;
    if (ctx.lastOf('devlog', subject) && !ctx.args.force) throw new NotReady(`${week} already has its devlog.`);

    ensureDir(DRAFTS_DIR);
    const draftFile = join(DRAFTS_DIR, `devlog-${week}.json`);
    const draft = readJson(draftFile, null);

    if (!draft) {
      const commits = weekCommits(new Date(ctx.now.getTime() - 7 * 86400000));
      if (!commits.length) throw new NotReady('No game commits on origin/master in the last seven days.');
      if (!ctx.dryRun) {
        writeJson(draftFile, {
          week,
          instructions:
            `Write 2-${MAX_LINES} lines into "lines", each one thing a player notices, in their words. ` +
            'Leave out what a player never sees. Then run produce.mjs --format devlog again.',
          lines: [],
          commits,
        });
      }
      throw new NotReady(
        `${commits.length} game commit(s) this week. ` +
          (ctx.dryRun ? 'A real run writes the draft.' : `Draft written: ${toRelative(draftFile)} - fill in "lines", then run again.`)
      );
    }

    const lines = (draft.lines || []).map((l) => String(l).trim()).filter(Boolean);
    if (!lines.length) throw new NotReady(`${toRelative(draftFile)} has no lines yet.`);
    if (lines.length > MAX_LINES) throw new Error(`${lines.length} lines - the card holds ${MAX_LINES}.`);

    const file = ctx.mediaFile(`devlog-${week}`, 'jpg');
    const card = renderCard(lines, week);
    writeFileSync(file, card.buffer);

    return {
      subject,
      text: buildText(lines),
      link: LINK,
      tags: ['devlog'],
      media: [{ type: 'photo', file: toRelative(file), url: null }],
      flags: card.truncated ? ['card-truncated'] : [],
    };
  },
};
