// The content ledger: one record per piece of content, whatever made it and wherever it went.
//
// The review files hold what is waiting and the posted_*.json files hold what went out, but
// neither knows what a post *was* - a duel, a weekly summary, a clip of the harbour - and neither
// knows about anything posted by hand. Picking the next post needs both: which formats ran lately,
// which subjects were already done, and whether a clip has been out before. The viper4 Reel of
// 2026-09-20 went out outside the pipeline, and was nearly posted a second time because nothing
// here had heard of it.
//
// Where a post went is not copied in. It is read from the posted_*.json files, which the
// publishers keep and which are written the moment a post exists - a second copy would only be a
// second thing to go stale.

import { createHash } from 'node:crypto';
import { createReadStream, existsSync } from 'node:fs';
import { isAbsolute, join } from 'node:path';
import {
  PIPELINE_ROOT, STATE_DIR, POSTED_FILE, POSTED_FB_FILE, POSTED_X_FILE, POSTED_YT_FILE,
  readJson, writeJson, toRelative,
} from './paths.mjs';

export const LEDGER_FILE = join(STATE_DIR, 'ledger.json');

const NETWORKS = [
  ['instagram', POSTED_FILE],
  ['facebook', POSTED_FB_FILE],
  ['x', POSTED_X_FILE],
  ['youtube', POSTED_YT_FILE],
];

export function loadLedger() {
  return readJson(LEDGER_FILE, { version: 1, items: [] });
}

export function saveLedger(ledger) {
  ledger.items.sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
  writeJson(LEDGER_FILE, ledger);
}

function absolute(file) {
  return isAbsolute(file) ? file : join(PIPELINE_ROOT, file);
}

/** sha256 of a file, streamed - clips run to a few hundred MB. */
export function hashFile(file) {
  return new Promise((resolvePromise, reject) => {
    const hash = createHash('sha256');
    createReadStream(absolute(file))
      .on('data', (chunk) => hash.update(chunk))
      .on('error', reject)
      .on('end', () => resolvePromise(hash.digest('hex')));
  });
}

/**
 * Every file a media list refers to, each once. A clip item carries its portrait and landscape
 * masters besides `file`, and all of them identify the post.
 */
export function mediaFiles(media = []) {
  const files = new Set();
  for (const m of media) {
    for (const f of [m.file, m.portrait, m.landscape]) if (f) files.add(f);
  }
  return [...files];
}

export async function hashMedia(media) {
  const out = [];
  for (const file of mediaFiles(media)) {
    out.push({ file: toRelative(absolute(file)), sha256: existsSync(absolute(file)) ? await hashFile(file) : null });
  }
  return out;
}

/**
 * Adds or replaces one record. `subject` is what the post is about in a form the format can
 * compare - "duel:badger|viper", "week:2026-39" - so a format can tell that it has done this one
 * before without parsing captions.
 */
export function record(ledger, item) {
  const entry = {
    id: item.id,
    format: item.format,
    subject: item.subject ?? null,
    date: item.date ?? new Date().toISOString(),
    medium: item.medium ?? mediumOf(item.media),
    media: item.media ?? [],
    summary: item.summary ? String(item.summary).slice(0, 200) : null,
    source: item.source ?? 'pipeline',
    // The writer's tone (lib/writer.mjs), or "template" when the format's own text went out. What
    // a later comparison of reach per tone reads.
    ...(item.tone ? { tone: item.tone } : {}),
    ...(item.note ? { note: item.note } : {}),
  };
  const at = ledger.items.findIndex((e) => e.id === entry.id);
  if (at >= 0) ledger.items[at] = { ...ledger.items[at], ...entry };
  else ledger.items.push(entry);
  return entry;
}

export function mediumOf(media = []) {
  if (!media.length) return 'text';
  if (media.some((m) => m.type === 'video' || /\.(mp4|mov|webm)$/i.test(m.file || ''))) return 'video';
  return media.length > 1 ? 'carousel' : 'photo';
}

/** The ledger records carrying one of these hashes - the same file posted or queued before. */
export function findByHash(ledger, hashes) {
  const wanted = new Set(hashes.filter(Boolean));
  if (!wanted.size) return [];
  return ledger.items.filter((e) => e.media.some((m) => m.sha256 && wanted.has(m.sha256)));
}

/** The most recent record of a format and subject, or null. */
export function lastOf(ledger, format, subject = undefined) {
  const hits = ledger.items.filter(
    (e) => e.format === format && (subject === undefined || e.subject === subject)
  );
  return hits.length ? hits[hits.length - 1] : null;
}

/** id -> the networks it is live on, read from what the publishers recorded. */
export function publishedMap() {
  const out = new Map();
  for (const [network, file] of NETWORKS) {
    const state = readJson(file, { posted: {} });
    for (const [id, rec] of Object.entries(state.posted || {})) {
      if (!out.has(id)) out.set(id, {});
      out.get(id)[network] = rec.published_at || true;
    }
  }
  return out;
}
