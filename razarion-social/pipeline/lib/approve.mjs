// The auto-approval: what a person checked on the review page, checked here instead, so a post the
// planner made can go out without anybody looking at it.
//
// Only posts the planner made unattended (ledger "auto", plan.mjs --auto) are touched. A post made
// by hand - compose.mjs, a devlog cut - stays on "review" for whoever made it.
//
// An entry that passes is set to "ok", exactly as the Approve button would. One that does not is
// left on "review" with `held` saying why; the dashboard shows it, the buffer no longer counts it
// (lib/queue.mjs), and it waits for a person. What is checked:
//
//   - the hard limits a publisher would refuse at publish time (the same as the review page)
//   - flags that mean the post is not whole: a text too long, a card cut off, nothing to show
//   - the writer's text, not the format's template (unless the settings allow templates)
//
// What the writer may say is already fenced in by lib/writer.mjs - numbers only from the facts, no
// links, no promises - and a battle that shows no fight never becomes a post (lib/formats/battle.mjs).

import { readJson, writeJson } from './paths.mjs';
import { REVIEW_TARGETS, loadQueue } from './queue.mjs';
import { loadLedger } from './ledger.mjs';
import { xLength } from './entries.mjs';
import { onMaster } from './links.mjs';
import { MAX_TITLE } from './youtube.mjs';

// Which fields of each network's entry a person may change, and what a publisher would refuse.
// YouTube itself takes a 100-character title; MAX_TITLE is what a phone shows, and only a warning.
export const NETWORKS = {
  x: { ...REVIEW_TARGETS.x, fields: ['text'], limits: { text: 280 }, measure: { text: xLength } },
  ig: { ...REVIEW_TARGETS.ig, fields: ['caption'], limits: { caption: 2200 } },
  fb: { ...REVIEW_TARGETS.fb, fields: ['message'], limits: { message: 63206 } },
  yt: {
    ...REVIEW_TARGETS.yt, fields: ['title', 'description'],
    limits: { title: 100, description: 5000 }, soft: { title: MAX_TITLE },
  },
};

// Flags that mean the post is not whole. "no-landscape-take" is not one: X gets the portrait take
// letterboxed, and "title-truncated" was shortened at a word, which is what a person would do.
const BLOCKING = ['too-long', 'needs-card', 'card-truncated', 'empty-text', 'link-only'];

export function measure(net, field, value) {
  const fn = NETWORKS[net].measure?.[field];
  return fn ? fn(value) : [...value].length;
}

/** What a publisher would refuse about this entry - the review page will not approve it either. */
export function approvalProblems(net, entry) {
  const n = NETWORKS[net];
  const problems = [];
  for (const field of n.fields) {
    const length = measure(net, field, entry[field] ?? '');
    if (length > n.limits[field]) problems.push(`${field} has ${length} characters, ${n.limits[field]} is the limit`);
    if (field === 'title' && !String(entry[field] ?? '').trim()) problems.push('the title is empty');
  }
  if (net === 'ig' && !entry.media?.length && !entry.needs_card) problems.push('no media, and no card requested');
  return problems;
}

/** Why an unattended entry may not go out, or [] when it may. */
function holdReasons(net, entry, item, settings) {
  const reasons = approvalProblems(net, entry);
  for (const flag of entry.flags || []) if (BLOCKING.includes(flag)) reasons.push(`flag ${flag}`);
  if (net !== 'yt' && !(entry.media || []).length) reasons.push('no media');
  if (item.tone === 'template' && !settings.approveTemplate) reasons.push('template text - the writer gave nothing usable');
  return reasons;
}

/**
 * Approves or holds every unattended entry still on "review". Returns what it did, one line each,
 * for the engine's log: { approved: [{id, net}], held: [{id, net, reason}] }.
 */
export function autoApprove(settings) {
  const result = { approved: [], held: [] };
  if (!settings.autoApprove) return result;

  const ledger = new Map(loadLedger().items.map((e) => [e.id, e]));
  const pending = loadQueue().flatMap((post) => Object.entries(post.entries)
    .filter(([, e]) => e.state === 'review' && !e.entry.held && ledger.get(post.id)?.auto)
    .map(([net]) => ({ id: post.id, net })));
  if (!pending.length) return result;

  for (const [net, n] of Object.entries(NETWORKS)) {
    const mine = pending.filter((p) => p.net === net);
    if (!mine.length) continue;
    // Re-read right before writing, as the review page does, so a save made there in between is kept.
    const doc = readJson(n.file, { [n.key]: [] });
    for (const { id } of mine) {
      const entry = doc[n.key].find((e) => e.id === id);
      if (!entry || entry.status !== 'review') continue;
      for (const field of n.fields) entry[field] = onMaster(entry[field]);
      const reasons = holdReasons(net, entry, ledger.get(id), settings);
      if (reasons.length) {
        entry.held = reasons.join('; ');
        result.held.push({ id, net, reason: entry.held });
      } else {
        entry.status = 'ok';
        entry.approved_by = 'auto';
        entry.approved_at = new Date().toISOString();
        result.approved.push({ id, net });
      }
    }
    writeJson(n.file, doc);
  }
  return result;
}
