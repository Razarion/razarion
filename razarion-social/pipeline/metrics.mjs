#!/usr/bin/env node
// How far the posts got, and which formats and tones carry.
//
//   node metrics.mjs              # fetch what is not settled yet, then the report
//   node metrics.mjs --report     # the report from what is stored, no fetching
//   node metrics.mjs --refresh    # fetch every post again, settled or not (costs on X)
//   node metrics.mjs --only x     # one network: x, ig or fb
//
// Stores snapshots in state/metrics.json (lib/metrics.mjs). A post measured at 7 days or older is
// settled and not fetched again, so after the first run a fetch asks only for the last week's
// posts - on X that is what keeps it to cents.
//
// The report compares within a network only. An Instagram reach and an X impression are different
// things counted by different companies; putting them side by side would suggest a comparison
// that is not there.

import { parseArgs } from './lib/args.mjs';
import { loadLedger } from './lib/ledger.mjs';
import {
  FETCHERS, NETWORK_LABELS, SETTLED_DAYS, isSettled, loadMetrics, saveMetrics, publishedPosts,
} from './lib/metrics.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

// A post younger than this is left out of the medians: on its first day it has only begun.
const MIN_AGE_DAYS = 2;

const REACH_NAME = { x: 'impressions', ig: 'reach', fb: 'unique viewers' };

function median(values) {
  if (!values.length) return null;
  const s = [...values].sort((a, b) => a - b);
  const mid = s.length >> 1;
  return s.length % 2 ? s[mid] : Math.round((s[mid - 1] + s[mid]) / 2);
}

async function fetchAll(args) {
  const metrics = loadMetrics();
  const posts = publishedPosts();
  const now = new Date();
  const only = args.only ? String(args.only) : null;

  for (const [net, fetcher] of Object.entries(FETCHERS)) {
    if (only && only !== net) continue;
    const due = posts[net].filter((p) => args.refresh || !isSettled(metrics.items[p.id]?.[net]));
    if (!due.length) {
      step(`${NETWORK_LABELS[net]}: all ${posts[net].length} posts settled, nothing to fetch`);
      continue;
    }
    step(`${NETWORK_LABELS[net]}: fetching ${due.length} of ${posts[net].length} posts`);
    try {
      const { result, failures = [], cost } = await fetcher(due, now);
      for (const [id, snapshot] of result) {
        metrics.items[id] ??= {};
        metrics.items[id][net] = snapshot;
      }
      info(`    ${result.size} measured${cost !== undefined ? `, about $${cost.toFixed(3)}` : ''}`);
      for (const f of failures.slice(0, 5)) warn(`    ${f}`);
      if (failures.length > 5) warn(`    ... and ${failures.length - 5} more`);
    } catch (err) {
      warn(`    ${err.message}`);
    }
    // Saved per network, so a failure on the next one does not lose what this one fetched.
    saveMetrics(metrics);
  }
  return metrics;
}

function table(title, rows) {
  if (!rows.length) return;
  info(`  ${title.padEnd(26)} posts   reach  interactions`);
  for (const r of rows) {
    info(`  ${r.key.padEnd(26)} ${String(r.n).padStart(5)} ${String(r.reach).padStart(7)} ${String(r.interactions).padStart(13)}`);
  }
}

function groupBy(entries, keyOf) {
  const groups = new Map();
  for (const e of entries) {
    const key = keyOf(e);
    if (!key) continue;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(e);
  }
  return [...groups]
    .map(([key, list]) => ({
      key,
      n: list.length,
      reach: median(list.map((e) => e.m.reach)),
      interactions: median(list.map((e) => e.m.interactions)),
    }))
    .sort((a, b) => b.reach - a.reach);
}

function report(metrics) {
  const ledger = new Map(loadLedger().items.map((e) => [e.id, e]));
  info('');
  info(`Medians per group, posts at least ${MIN_AGE_DAYS} days old. Settled at ${SETTLED_DAYS} days.`);

  for (const net of Object.keys(FETCHERS)) {
    const entries = Object.entries(metrics.items)
      .filter(([, m]) => m[net] && m[net].age_days >= MIN_AGE_DAYS)
      .map(([id, m]) => ({ id, m: m[net], l: ledger.get(id) }));
    const fresh = Object.values(metrics.items).filter((m) => m[net] && m[net].age_days < MIN_AGE_DAYS).length;
    info('');
    info(`${NETWORK_LABELS[net]} - reach = ${REACH_NAME[net]}, ${entries.length} posts${fresh ? ` (+${fresh} too fresh)` : ''}`);
    if (!entries.length) continue;
    // On X "mirrored" are the account's own posts, each in its day. On Instagram and Facebook they
    // are the August backfill, dozens a day into a new feed - their reach says nothing about what
    // they were.
    if (net !== 'x') info('  (mirrored = the August backfill, posted in bulk; not comparable)');
    table('by format', groupBy(entries, (e) => e.l?.format ?? 'unknown'));
    table('by tone', groupBy(entries, (e) => e.l?.tone ?? (e.l?.format === 'mirrored' ? 'hand-written' : null)));
    table('by medium', groupBy(entries, (e) => e.l?.medium ?? null));

    const recent = entries
      .filter((e) => e.l && e.l.format !== 'mirrored')
      .sort((a, b) => Date.parse(b.l.date) - Date.parse(a.l.date))
      .slice(0, 6);
    if (recent.length) {
      info('  latest pipeline posts');
      for (const e of recent) {
        info(`    ${e.l.date.slice(0, 10)}  ${String(e.m.reach).padStart(6)} reach  ${String(e.m.interactions).padStart(3)} int.  ${e.l.format}${e.l.tone ? ' / ' + e.l.tone : ''}  ${e.l.subject ?? ''}`);
      }
    }
  }
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const metrics = args.report ? loadMetrics() : await fetchAll(args);
  report(metrics);
  if (!args.report) {
    info('');
    ok('Stored in state/metrics.json.');
  }
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
