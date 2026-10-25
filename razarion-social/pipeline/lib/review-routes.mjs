// The review page's server side: the posts with their texts per network, saving an edit or a
// status, and the media. review.mjs serves it on its own; engine.mjs serves it together with the
// engine's dashboard on the same port.
//
// It writes into the same review files the publishers read (data/captions.json, fb_posts.json,
// x_posts.json, yt_posts.json) and nothing else, so approving here is exactly setting "status":
// "ok" there. A changed text is saved with "edited": true, as the hand edit would be.
//
// Local only. The server listens on 127.0.0.1, and a write has to carry a custom header, which a
// page on another origin cannot send without a preflight this server never answers.

import { createReadStream, existsSync, readFileSync, statSync } from 'node:fs';
import { extname, isAbsolute, join, relative, resolve } from 'node:path';
import { PIPELINE_ROOT, DATA_DIR, readJson, writeJson } from './paths.mjs';
import { loadLedger } from './ledger.mjs';
import { loadMetrics } from './metrics.mjs';
import { loadQueue, isOpen, isWaiting } from './queue.mjs';
import { NETWORKS, measure, approvalProblems } from './approve.mjs';
import { fail } from '../../src/util/log.mjs';

const PAGE = join(PIPELINE_ROOT, 'review', 'page.html');

// How many of the finished posts "All" shows, newest first. The backfill alone is 80 entries.
const HISTORY = 40;

const STATUSES = new Set(['review', 'ok', 'skip']);

// The queue (lib/queue.mjs) in the shape the page renders: per network the editable fields with
// their lengths and limits, per post what the ledger knows about it.
function collectPosts(all) {
  const ledger = new Map(loadLedger().items.map((e) => [e.id, e]));
  const metrics = loadMetrics().items;

  const list = loadQueue().map((q) => {
    const post = { id: q.id, date: q.date, source_text: null, networks: {} };
    for (const [net, { entry, posted: rec, state }] of Object.entries(q.entries)) {
      const n = NETWORKS[net];
      post.source_text ??= entry.source_text ?? null;
      post.networks[net] = {
        state,
        published_at: rec?.published_at ?? null,
        url: rec?.url ?? null,
        metrics: metrics[q.id]?.[net] ?? null,
        edited: !!entry.edited,
        flags: entry.flags || [],
        notes: entry.notes || [],
        media: entry.media || [],
        needs_card: !!entry.needs_card,
        card: entry.card ?? null,
        privacy: entry.privacy ?? null,
        tags: entry.tags ?? null,
        held: entry.held ?? null,
        approved_by: entry.approved_by ?? null,
        fields: Object.fromEntries(n.fields.map((f) => [f, entry[f] ?? ''])),
        limits: n.limits,
        soft: n.soft ?? {},
        lengths: Object.fromEntries(n.fields.map((f) => [f, measure(net, f, entry[f] ?? '')])),
      };
    }
    const l = ledger.get(q.id);
    return {
      ...post,
      open: isOpen(q),
      waiting: isWaiting(q),
      ledger: l ? { format: l.format, subject: l.subject, tone: l.tone ?? null, summary: l.summary, auto: !!l.auto } : null,
    };
  });
  return all ? list.slice(0, Math.max(HISTORY, list.filter((p) => p.open).length)) : list.filter((p) => p.open);
}

// Changes one network's entry. Re-reads the file first, so a producer that appended a post while
// the page was open is not overwritten by a stale copy.
function saveEntry({ id, network, status, fields }) {
  const n = NETWORKS[network];
  if (!n) throw httpError(400, `Unknown network "${network}".`);
  const posted = readJson(n.posted, { posted: {} }).posted || {};
  if (posted[id]) throw httpError(409, `${n.label}: ${id} is already published and can no longer be changed.`);

  const doc = readJson(n.file, { [n.key]: [] });
  const entry = (doc[n.key] || []).find((e) => e.id === id);
  if (!entry) throw httpError(404, `${n.label}: no entry ${id}.`);

  if (fields) {
    for (const [field, value] of Object.entries(fields)) {
      if (!n.fields.includes(field)) throw httpError(400, `${n.label}: "${field}" cannot be edited here.`);
      if (typeof value !== 'string') throw httpError(400, `${n.label}: "${field}" must be text.`);
      const normalized = value.replace(/\r\n/g, '\n');
      if (normalized !== (entry[field] ?? '')) {
        entry[field] = normalized;
        entry.edited = true;
      }
    }
    // buildEntries set "too-long" against the text it wrote; after an edit it has to describe
    // the text that is there now.
    if (entry.flags && (network === 'x' || network === 'ig')) {
      const field = n.fields[0];
      const over = measure(network, field, entry[field] ?? '') > n.limits[field];
      entry.flags = entry.flags.filter((f) => f !== 'too-long');
      if (over) entry.flags.push('too-long');
    }
  }

  if (status !== undefined) {
    if (!STATUSES.has(status)) throw httpError(400, `Unknown status "${status}".`);
    if (status === 'ok') {
      const problems = approvalProblems(network, entry);
      if (problems.length) throw httpError(422, `${n.label}: not approved - ${problems.join('; ')}.`);
    }
    entry.status = status;
    // A person decided: the auto-approval's hold no longer applies, and the approval is theirs.
    delete entry.held;
    if (status === 'ok') entry.approved_by = 'person';
    else delete entry.approved_by;
  }

  writeJson(n.file, doc);
  return collectPosts(true).find((p) => p.id === id);
}

function httpError(code, message) {
  return Object.assign(new Error(message), { code });
}

const TYPES = {
  '.html': 'text/html; charset=utf-8', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.png': 'image/png',
  '.webp': 'image/webp', '.gif': 'image/gif', '.mp4': 'video/mp4', '.webm': 'video/webm', '.mov': 'video/quicktime',
};

// Media are referenced relative to the pipeline root and served only from inside data/. Ranges
// are answered, because a browser will not seek in a video that is served whole.
function serveMedia(req, res, rel) {
  const file = resolve(PIPELINE_ROOT, rel);
  const inside = relative(DATA_DIR, file);
  if (!inside || inside.startsWith('..') || isAbsolute(inside) || !existsSync(file)) {
    res.writeHead(404).end();
    return;
  }
  const size = statSync(file).size;
  const type = TYPES[extname(file).toLowerCase()] || 'application/octet-stream';
  const range = /^bytes=(\d*)-(\d*)$/.exec(req.headers.range || '');
  if (range) {
    const start = range[1] ? Number(range[1]) : size - Number(range[2]);
    const end = range[1] && range[2] ? Math.min(Number(range[2]), size - 1) : size - 1;
    if (start > end || start < 0) {
      res.writeHead(416, { 'Content-Range': `bytes */${size}` }).end();
      return;
    }
    res.writeHead(206, {
      'Content-Type': type, 'Content-Length': end - start + 1,
      'Content-Range': `bytes ${start}-${end}/${size}`, 'Accept-Ranges': 'bytes',
    });
    createReadStream(file, { start, end }).pipe(res);
    return;
  }
  res.writeHead(200, { 'Content-Type': type, 'Content-Length': size, 'Accept-Ranges': 'bytes' });
  createReadStream(file).pipe(res);
}

function sendJson(res, code, value) {
  res.writeHead(code, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' });
  res.end(JSON.stringify(value));
}

async function readBody(req) {
  let body = '';
  for await (const chunk of req) {
    body += chunk;
    if (body.length > 1_000_000) throw httpError(413, 'Request too large.');
  }
  try {
    return JSON.parse(body);
  } catch {
    throw httpError(400, 'Body is not JSON.');
  }
}

/**
 * Answers the review page's requests. Resolves to true when the request was one of them, false
 * when it was not, so a server can try its own routes next.
 */
export async function handleReview(req, res, url) {
  try {
    if (req.method === 'GET' && url.pathname === '/') {
      res.writeHead(200, { 'Content-Type': TYPES['.html'], 'Cache-Control': 'no-store' });
      res.end(readFileSync(PAGE));
      return true;
    }
    if (req.method === 'GET' && url.pathname === '/api/posts') {
      sendJson(res, 200, { posts: collectPosts(url.searchParams.get('all') === '1') });
      return true;
    }
    if (req.method === 'GET' && url.pathname === '/media') {
      serveMedia(req, res, url.searchParams.get('file') || '');
      return true;
    }
    if (req.method === 'POST' && url.pathname === '/api/save') {
      if (req.headers['x-review'] !== '1') throw httpError(403, 'Missing X-Review header.');
      const post = saveEntry(await readBody(req));
      sendJson(res, 200, { post });
      return true;
    }
    return false;
  } catch (err) {
    sendError(res, err);
    return true;
  }
}

export function sendError(res, err) {
  if (!err.code || typeof err.code !== 'number') fail(err.stack || err.message);
  sendJson(res, typeof err.code === 'number' ? err.code : 500, { error: err.message });
}

export { collectPosts, sendJson, readBody, httpError };
