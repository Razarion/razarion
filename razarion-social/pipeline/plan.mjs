#!/usr/bin/env node
// The planner: keeps a few posts waiting for review, so there is always something to approve and
// the schedule never runs dry. It decides what to make and runs produce.mjs for it; it approves
// nothing and publishes nothing.
//
//   node plan.mjs               # fill the queue up to the target
//   node plan.mjs --dry-run     # say what it would make, make nothing
//   node plan.mjs --target 4    # keep four posts open instead of three
//
// What it makes: the formats in PLAN, the one that ran longest ago first, each no more often than
// its gap allows. A format that has nothing worth making (every duel pair done lately, a quiet
// week, a devlog draft nobody has filled in) says so and the next one is tried - produce.mjs and
// the formats already decide that, so the planner does not second-guess them.
//
// A battle is filmed on the live planet (lib/formats/battle.mjs) and takes a few minutes; the other
// formats take seconds.

import { spawn } from 'node:child_process';
import { join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import { PIPELINE_ROOT } from './lib/paths.mjs';
import { loadLedger, lastOf } from './lib/ledger.mjs';
import { loadQueue, isOpen, isWaiting } from './lib/queue.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

// Posts open - waiting for review, or approved and not out yet - that the planner tops up to.
// Three is one scheduled week (Monday, Wednesday, Friday) of approved posts.
const TARGET = 3;

// A run makes at most this many. A queue emptied by a week away is refilled over two runs, and
// the texts of one run are not all written in the same breath.
const MAX_PER_RUN = 2;

// Days a format waits after its last post. The formats' own rules still apply on top: a duel
// pair repeats only after 120 days, a week and a devlog only once per ISO week.
const PLAN = [
  // Clips reach four to five times what a card does on Instagram (metrics.mjs, September), so a
  // battle may go every day.
  { format: 'battle', gapDays: 1 },
  { format: 'week-in-numbers', gapDays: 6 },
  { format: 'devlog', gapDays: 6 },
  { format: 'duel', gapDays: 2 },
];

const DAY = 86_400_000;

/** The formats that may run now, the one that ran longest ago first; never-run ones lead. */
function candidates(ledger, now = new Date()) {
  return PLAN
    .map((p, order) => ({ ...p, order, last: lastOf(ledger, p.format)?.date ?? null }))
    .filter((p) => !p.last || now - Date.parse(p.last) >= p.gapDays * DAY)
    .sort((a, b) => (a.last ? Date.parse(a.last) : 0) - (b.last ? Date.parse(b.last) : 0) || a.order - b.order);
}

/** Runs produce.mjs for one format. Resolves to its output and whether a post came out of it. */
function produce(format) {
  const before = loadLedger().items.length;
  return new Promise((resolve) => {
    const child = spawn(process.execPath, [join(PIPELINE_ROOT, 'produce.mjs'), '--format', format], {
      cwd: PIPELINE_ROOT,
      stdio: ['ignore', 'pipe', 'pipe'],
    });
    let output = '';
    child.stdout.on('data', (d) => { output += d; });
    child.stderr.on('data', (d) => { output += d; });
    child.on('close', (code) => {
      // The ledger, not the exit code, says whether a post exists: a format with nothing to make
      // exits 0 as well.
      resolve({ code, output, made: loadLedger().items.length > before });
    });
  });
}

function indent(text) {
  return text.trimEnd().split('\n').map((l) => '    ' + l).join('\n');
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const target = Number(args.target ?? TARGET);
  const dryRun = Boolean(args['dry-run']);

  const open = loadQueue().filter(isOpen);
  const waiting = open.filter(isWaiting).length;
  info(`Queue: ${open.length} open (${waiting} waiting for review, ${open.length - waiting} approved and not out yet), target ${target}.`);

  const wanted = Math.min(MAX_PER_RUN, target - open.length);
  if (wanted <= 0) {
    ok('The queue is full enough. Nothing made.');
    return;
  }

  const due = candidates(loadLedger());
  if (!due.length) {
    ok('Every format ran within its gap. Nothing made.');
    return;
  }
  info(`Due, in order: ${due.map((p) => `${p.format} (last ${p.last ? p.last.slice(0, 10) : 'never'})`).join(', ')}`);

  if (dryRun) {
    warn(`DRY RUN. Would make up to ${wanted}, trying the formats in that order.`);
    return;
  }

  let made = 0;
  for (const { format } of due) {
    if (made >= wanted) break;
    step(`produce.mjs --format ${format}`);
    const result = await produce(format);
    info(indent(result.output));
    if (result.made) {
      made++;
    } else if (result.code !== 0) {
      warn(`${format} failed (exit ${result.code}); trying the next format.`);
    }
  }

  if (made) ok(`${made} post(s) made. Read and approve them: node review.mjs --open`);
  else warn('No format had anything to make this time.');
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
