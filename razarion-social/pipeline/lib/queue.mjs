// The queue: every post in the review files, with where each of its network entries stands.
//
// One post is up to four entries with the same id, one per review file, and whether it went out
// is not in those files but in the publishers' posted_*.json. review.mjs shows this, plan.mjs
// counts it; both read it from here so the page and the planner never disagree about what is
// still waiting.

import {
  CAPTIONS_FILE, FB_POSTS_FILE, X_POSTS_FILE, YT_POSTS_FILE,
  POSTED_FILE, POSTED_FB_FILE, POSTED_X_FILE, POSTED_YT_FILE, readJson,
} from './paths.mjs';

export const REVIEW_TARGETS = {
  x: { label: 'X', file: X_POSTS_FILE, key: 'posts', posted: POSTED_X_FILE },
  ig: { label: 'Instagram', file: CAPTIONS_FILE, key: 'captions', posted: POSTED_FILE },
  fb: { label: 'Facebook', file: FB_POSTS_FILE, key: 'posts', posted: POSTED_FB_FILE },
  yt: { label: 'YouTube', file: YT_POSTS_FILE, key: 'videos', posted: POSTED_YT_FILE },
};

/**
 * id -> { id, date, entries: { net: { entry, posted, state } } }, newest first. `state` is
 * "posted" once a publisher recorded the entry, otherwise its review status.
 */
export function loadQueue() {
  const posts = new Map();
  for (const [net, t] of Object.entries(REVIEW_TARGETS)) {
    const list = readJson(t.file, { [t.key]: [] })[t.key] || [];
    const posted = readJson(t.posted, { posted: {} }).posted || {};
    for (const entry of list) {
      if (!posts.has(entry.id)) posts.set(entry.id, { id: entry.id, date: entry.date, entries: {} });
      const rec = posted[entry.id] ?? null;
      posts.get(entry.id).entries[net] = { entry, posted: rec, state: rec ? 'posted' : entry.status };
    }
  }
  return [...posts.values()].sort((a, b) => Date.parse(b.date) - Date.parse(a.date));
}

/** Still to go out somewhere: an entry waiting for review, or approved and not yet published. */
export function isOpen(post) {
  return Object.values(post.entries).some((e) => e.state === 'review' || e.state === 'ok');
}

/** Waiting for a person: at least one entry still on "review". */
export function isWaiting(post) {
  return Object.values(post.entries).some((e) => e.state === 'review');
}
