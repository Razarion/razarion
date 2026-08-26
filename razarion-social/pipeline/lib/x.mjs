import { env } from '../../src/config.mjs';
import { HttpError, sleep } from '../../src/util/http.mjs';
import { step, warn } from '../../src/util/log.mjs';

const API = 'https://api.x.com/2';

// Reads are billed per post returned. Every caller gets the running count so the scripts can print
// what a run actually cost instead of leaving it to the invoice.
export const USD_PER_READ = 0.005;

function bearerToken() {
  const token = env.X_BEARER_TOKEN;
  if (!token) {
    throw new Error(
      'Missing X_BEARER_TOKEN in razarion-social/.env.\n' +
        'App-only bearer token from developer.x.com, from a project with read access and billing enabled.\n' +
        'X_CLIENT_ID / X_CLIENT_SECRET are for OAuth2 posting and cannot read a timeline.'
    );
  }
  return token;
}

function buildUrl(path, params) {
  const url = new URL(API + path);
  for (const [key, value] of Object.entries(params || {})) {
    if (value !== undefined && value !== null && value !== '') url.searchParams.set(key, String(value));
  }
  return url.toString();
}

// X answers 429 with the reset time in a header rather than a Retry-After, and answers 5xx often
// enough during a long pagination run that giving up on the first one would be wrong.
async function xFetch(url, attempt = 0) {
  const res = await fetch(url, {
    headers: { Authorization: 'Bearer ' + bearerToken(), 'User-Agent': 'razarion-pipeline/1.0' },
  });

  const text = await res.text();
  let body = text;
  try {
    body = text ? JSON.parse(text) : null;
  } catch {
    /* keep the raw text; the error path prints it */
  }

  if (res.ok) return body;

  if (res.status === 429 && attempt < 3) {
    const reset = Number(res.headers.get('x-rate-limit-reset'));
    const waitMs = Number.isFinite(reset) ? Math.max(0, reset * 1000 - Date.now()) + 1000 : 60000;
    const capped = Math.min(waitMs, 15 * 60 * 1000);
    warn(`Rate limited. Waiting ${Math.ceil(capped / 1000)}s before retrying.`);
    await sleep(capped);
    return xFetch(url, attempt + 1);
  }

  if (res.status >= 500 && attempt < 3) {
    const backoff = 2000 * 2 ** attempt;
    warn(`HTTP ${res.status} from X. Retrying in ${backoff / 1000}s.`);
    await sleep(backoff);
    return xFetch(url, attempt + 1);
  }

  throw new HttpError(res.status, body, explain(res.status) + url);
}

// X answers with a bare status and a two-word detail. Which of the three plausible causes it is
// costs a search every time, so the guess is written down here instead.
function explain(status) {
  const hints = {
    401: 'X_BEARER_TOKEN is missing, malformed or was regenerated (the old one dies instantly). ',
    402: 'No API credits. Developer portal -> Billing -> Credits, add a payment method and buy a pack. ',
    403: 'The project has no read access, or the app is not attached to a project with it. ',
    404: 'No such account, or the account is suspended or protected. ',
  };
  return hints[status] || '';
}

export async function lookupUser(username) {
  const body = await xFetch(buildUrl(`/users/by/username/${encodeURIComponent(username)}`, {
    'user.fields': 'id,name,username',
  }));
  if (!body || !body.data) {
    throw new Error(`X returned no user for "${username}": ${JSON.stringify(body)}`);
  }
  return body.data;
}

const TWEET_FIELDS = [
  'id',
  'text',
  'created_at',
  'entities',
  'attachments',
  'referenced_tweets',
  'conversation_id',
  'in_reply_to_user_id',
  'note_tweet',
  'public_metrics',
].join(',');

const MEDIA_FIELDS = [
  'media_key',
  'type',
  'url',
  'preview_image_url',
  'variants',
  'alt_text',
  'width',
  'height',
  'duration_ms',
].join(',');

/**
 * Pulls the user timeline between two timestamps.
 *
 * Deliberately does NOT pass exclude=replies: on X a thread is a chain of self-replies, so
 * excluding replies server-side would drop every thread continuation the account ever posted.
 * Filtering happens locally in fetch_posts.mjs, where a self-reply can be told apart from a reply
 * to somebody else.
 */
export async function fetchTimeline(userId, { startTime, endTime, limit = 3200, onPage } = {}) {
  const tweets = [];
  const media = new Map();
  const users = new Map();
  const referenced = new Map();
  const errors = [];
  let paginationToken;
  let pages = 0;

  while (tweets.length < limit) {
    const body = await xFetch(buildUrl(`/users/${userId}/tweets`, {
      max_results: Math.min(100, limit - tweets.length),
      start_time: startTime,
      end_time: endTime,
      'tweet.fields': TWEET_FIELDS,
      'media.fields': MEDIA_FIELDS,
      'user.fields': 'id,username,name',
      expansions: 'attachments.media_keys,referenced_tweets.id,referenced_tweets.id.author_id',
      pagination_token: paginationToken,
    }));

    pages++;
    const batch = (body && body.data) || [];
    tweets.push(...batch);

    const includes = (body && body.includes) || {};
    for (const m of includes.media || []) media.set(m.media_key, m);
    for (const u of includes.users || []) users.set(u.id, u);
    for (const t of includes.tweets || []) referenced.set(t.id, t);

    if (body && body.errors && body.errors.length) {
      errors.push(...body.errors.map((e) => ({ ...e, page: pages })));
      warn(`X reported ${body.errors.length} partial error(s) on page ${pages}; recorded in raw-timeline.json.`);
    }

    if (onPage) onPage({ page: pages, batch: batch.length, total: tweets.length });

    paginationToken = body && body.meta ? body.meta.next_token : undefined;
    if (!paginationToken || batch.length === 0) break;
  }

  return { tweets, media, users, referenced, errors, pages, reads: tweets.length };
}

// pbs.twimg.com serves a resized copy by default; name=orig is the file as uploaded.
export function photoUrl(mediaItem) {
  if (!mediaItem.url) return null;
  const url = new URL(mediaItem.url);
  url.searchParams.set('name', 'orig');
  return url.toString();
}

// Videos and animated GIFs arrive as a variant list. Animated GIFs carry no bit_rate at all, which
// is why the sort has to tolerate undefined rather than assume a number.
export function videoUrl(mediaItem) {
  const variants = (mediaItem.variants || []).filter((v) => v.content_type === 'video/mp4');
  if (!variants.length) return null;
  variants.sort((a, b) => (b.bit_rate || 0) - (a.bit_rate || 0));
  return variants[0].url;
}

export function bestMediaUrl(mediaItem) {
  return mediaItem.type === 'photo' ? photoUrl(mediaItem) : videoUrl(mediaItem);
}

export async function downloadTo(url, filePath) {
  const res = await fetch(url, { headers: { 'User-Agent': 'razarion-pipeline/1.0' } });
  if (!res.ok) throw new HttpError(res.status, await res.text(), url);
  const buffer = Buffer.from(await res.arrayBuffer());
  const { writeFileSync } = await import('node:fs');
  writeFileSync(filePath, buffer);
  return buffer.length;
}

export { step };
