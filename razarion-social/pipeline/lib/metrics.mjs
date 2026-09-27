// What each published post reached, per network, read back from the networks themselves.
//
// The ledger knows what a post was (format, tone, medium) and the posted_*.json files know where
// it went; this adds how far it got. Together they answer the question the formats and tones
// were recorded for: which of them carry.
//
// Numbers settle. A post collects nearly all of its reach in its first days, so a snapshot taken
// at SETTLED_DAYS or later is kept as final and the post is not asked again - which matters on X,
// where every post read costs money.
//
// The measures, chosen to be comparable within a network (never across networks):
//   reach         X impressions, Instagram reach, Facebook unique viewers
//   interactions  likes, replies/comments, reposts/shares, saves/bookmarks - whatever the network
//                 counts as someone doing something with the post
//
// YouTube is left out while the uploads are private (DEFAULT_PRIVACY in youtube.mjs): a private
// video has no audience to measure.

import { join } from 'node:path';
import {
  STATE_DIR, POSTED_FILE, POSTED_FB_FILE, POSTED_X_FILE, readJson, writeJson,
} from './paths.mjs';
import { loadLedger } from './ledger.mjs';
import * as instagram from './instagram.mjs';
import * as facebook from './facebook.mjs';
import { USD_PER_READ } from './x.mjs';
import { env } from '../../src/config.mjs';

export const METRICS_FILE = join(STATE_DIR, 'metrics.json');
export const SETTLED_DAYS = 7;
const DAY = 86_400_000;

export function loadMetrics() {
  return readJson(METRICS_FILE, { version: 1, items: {} });
}

export function saveMetrics(metrics) {
  writeJson(METRICS_FILE, metrics);
}

function ageDays(publishedAt, now) {
  return Math.round(((now - Date.parse(publishedAt)) / DAY) * 10) / 10;
}

export function isSettled(snapshot) {
  return !!snapshot && snapshot.age_days >= SETTLED_DAYS;
}

/**
 * Every published post per network: its id in the review files, its id on the network and when
 * it went out there. X also carries the posts of the account from before the pipeline - the
 * originals the backfill mirrored - because they are what the pipeline's posts are compared with.
 */
export function publishedPosts() {
  const out = { x: [], ig: [], fb: [] };

  for (const [id, rec] of Object.entries(readJson(POSTED_X_FILE, { posted: {} }).posted || {})) {
    out.x.push({ id, remoteId: rec.x_id, publishedAt: rec.published_at });
  }
  const known = new Set(out.x.map((p) => p.id));
  for (const item of loadLedger().items) {
    if (item.format === 'mirrored' && /^\d+$/.test(item.id) && !known.has(item.id)) {
      out.x.push({ id: item.id, remoteId: item.id, publishedAt: item.date });
    }
  }
  for (const [id, rec] of Object.entries(readJson(POSTED_FILE, { posted: {} }).posted || {})) {
    if (rec.ig_media_id) out.ig.push({ id, remoteId: rec.ig_media_id, publishedAt: rec.published_at });
  }
  for (const [id, rec] of Object.entries(readJson(POSTED_FB_FILE, { posted: {} }).posted || {})) {
    if (rec.fb_post_id) out.fb.push({ id, remoteId: rec.fb_post_id, publishedAt: rec.published_at });
  }
  return out;
}

// X: one request per hundred posts, billed per post returned.
async function fetchX(posts, now) {
  const token = env.X_BEARER_TOKEN;
  if (!token) throw new Error('X_BEARER_TOKEN is missing in razarion-social/.env - X is not measured.');
  const result = new Map();
  let reads = 0;
  for (let i = 0; i < posts.length; i += 100) {
    const batch = posts.slice(i, i + 100);
    const url = `https://api.x.com/2/tweets?ids=${batch.map((p) => p.remoteId).join(',')}&tweet.fields=public_metrics`;
    const res = await fetch(url, { headers: { Authorization: 'Bearer ' + token } });
    const body = await res.json().catch(() => null);
    if (!res.ok) throw new Error(`X: HTTP ${res.status} ${JSON.stringify(body?.detail ?? body?.title ?? body).slice(0, 200)}`);
    const byRemote = new Map((body.data || []).map((t) => [t.id, t.public_metrics]));
    reads += byRemote.size;
    for (const p of batch) {
      const m = byRemote.get(p.remoteId);
      if (!m) continue; // deleted, or not visible to an app token
      result.set(p.id, {
        at: now.toISOString(),
        age_days: ageDays(p.publishedAt, now),
        reach: m.impression_count,
        interactions: m.like_count + m.reply_count + m.retweet_count + m.quote_count + m.bookmark_count,
        likes: m.like_count,
        replies: m.reply_count,
        reposts: m.retweet_count + m.quote_count,
        bookmarks: m.bookmark_count,
      });
    }
  }
  return { result, cost: reads * USD_PER_READ };
}

// Instagram: one insights call per post. "views" is newer than the rest and not offered for every
// media type, so a refusal is retried without it.
async function fetchInstagram(posts, now) {
  const [, token] = instagram.credentials();
  const result = new Map();
  const failures = [];
  for (const p of posts) {
    let data;
    try {
      data = (await instagram.get(`/${p.remoteId}/insights`, { metric: 'reach,views,likes,comments,shares,saved', access_token: token })).data;
    } catch {
      try {
        data = (await instagram.get(`/${p.remoteId}/insights`, { metric: 'reach,likes,comments,shares,saved', access_token: token })).data;
      } catch (err) {
        failures.push(`${p.id}: ${String(err.message).split('\n')[0]}`);
        continue;
      }
    }
    const v = Object.fromEntries(data.map((d) => [d.name, d.values?.[0]?.value ?? d.total_value?.value ?? 0]));
    result.set(p.id, {
      at: now.toISOString(),
      age_days: ageDays(p.publishedAt, now),
      reach: v.reach ?? 0,
      views: v.views ?? null,
      interactions: (v.likes ?? 0) + (v.comments ?? 0) + (v.shares ?? 0) + (v.saved ?? 0),
      likes: v.likes ?? 0,
      comments: v.comments ?? 0,
      shares: v.shares ?? 0,
      saved: v.saved ?? 0,
    });
  }
  return { result, failures };
}

// Facebook: the counts come from the post, the views from its insights. Meta retired
// post_impressions in 2025; post_media_view and post_total_media_view_unique replaced them.
async function fetchFacebook(posts, now) {
  const [, token] = facebook.pageCredentials();
  const result = new Map();
  const failures = [];
  for (const p of posts) {
    try {
      const post = await facebook.get(`/${p.remoteId}`, {
        fields: 'shares,reactions.summary(total_count).limit(0),comments.summary(total_count).limit(0)',
        access_token: token,
      });
      const insights = await facebook.get(`/${p.remoteId}/insights`, {
        metric: 'post_media_view,post_total_media_view_unique,post_clicks',
        access_token: token,
      });
      // A metric can come back twice (lifetime and another period); the first is the lifetime one.
      const v = {};
      for (const d of insights.data || []) v[d.name] ??= d.values?.[0]?.value ?? 0;
      const reactions = post.reactions?.summary?.total_count ?? 0;
      const comments = post.comments?.summary?.total_count ?? 0;
      const shares = post.shares?.count ?? 0;
      result.set(p.id, {
        at: now.toISOString(),
        age_days: ageDays(p.publishedAt, now),
        reach: v.post_total_media_view_unique ?? 0,
        views: v.post_media_view ?? null,
        interactions: reactions + comments + shares,
        reactions,
        comments,
        shares,
        clicks: v.post_clicks ?? 0,
      });
    } catch (err) {
      failures.push(`${p.id}: ${String(err.message).split('\n')[0]}`);
    }
  }
  return { result, failures };
}

export const FETCHERS = { x: fetchX, ig: fetchInstagram, fb: fetchFacebook };
export const NETWORK_LABELS = { x: 'X', ig: 'Instagram', fb: 'Facebook' };
