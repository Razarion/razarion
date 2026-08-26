#!/usr/bin/env node
// Step 1: pull @razariongame's own posts from the X user timeline into data/posts.json.
//
// Not the archive export: GET /2/users/:id/tweets reaches back 3200 posts and takes start_time,
// so the whole of 2026 is in range. The seven-day ceiling people run into belongs to search, not
// to this endpoint.
//
//   node fetch_posts.mjs                       # everything since 2026-01-01, media included
//   node fetch_posts.mjs --since 2026-06-01T00:00:00Z
//   node fetch_posts.mjs --no-media --limit 50 # cheap look at what is there
//   node fetch_posts.mjs --since ... --merge   # incremental: keep what earlier runs found

import { existsSync } from 'node:fs';
import { join, extname } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import {
  DATA_DIR, MEDIA_DIR, POSTS_FILE, RAW_TIMELINE_FILE,
  ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { lookupUser, fetchTimeline, bestMediaUrl, downloadTo, USD_PER_READ } from './lib/x.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const DEFAULT_SINCE = '2026-01-01T00:00:00Z';
const DEFAULT_HANDLE = 'razariongame';

const HTML_ENTITIES = {
  '&amp;': '&',
  '&lt;': '<',
  '&gt;': '>',
  '&quot;': '"',
  '&#39;': String.fromCharCode(39),
};

function unescapeHtml(text) {
  return text.replace(/&(amp|lt|gt|quot|#39);/g, (m) => HTML_ENTITIES[m] || m);
}

function tidy(text) {
  return text
    .replace(/[ \t]+$/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

/**
 * Replaces every t.co link with what it points at.
 *
 * Two kinds of link are removed rather than expanded: the one X appends for the tweet's own
 * attached media (it renders as the image, not as a link), and the one pointing at a quoted
 * tweet, which is recorded separately so the caption step can decide what to do with it.
 */
function expandText(tweet) {
  const note = tweet.note_tweet && tweet.note_tweet.text ? tweet.note_tweet : null;
  const entities = (note && note.entities) || tweet.entities || {};
  let text = note ? note.text : tweet.text;

  const quotedIds = new Set(
    (tweet.referenced_tweets || []).filter((r) => r.type === 'quoted').map((r) => r.id)
  );
  const links = [];
  let quotedUrl = null;

  for (const url of entities.urls || []) {
    const expanded = url.expanded_url || url.url;
    const display = url.display_url || '';
    const isOwnMedia = display.startsWith('pic.x.com') || display.startsWith('pic.twitter.com');
    const status = /\/status\/(\d+)/.exec(expanded);
    const isQuoteLink = status && quotedIds.has(status[1]);

    if (isOwnMedia) {
      text = text.split(url.url).join('');
    } else if (isQuoteLink) {
      quotedUrl = expanded;
      text = text.split(url.url).join('');
    } else {
      text = text.split(url.url).join(expanded);
      links.push(expanded);
    }
  }

  const mentions = (entities.mentions || []).map((m) => m.username);
  return { text: tidy(unescapeHtml(text)), links, mentions, quotedUrl };
}

function classify(tweet, selfId) {
  const refs = tweet.referenced_tweets || [];
  if (refs.some((r) => r.type === 'retweeted') || /^RT @/.test(tweet.text)) return 'retweet';
  // in_reply_to_user_id is set on every reply. Pointing at ourselves means a thread continuation,
  // pointing at anybody else means a conversation that makes no sense lifted out of context.
  if (tweet.in_reply_to_user_id && tweet.in_reply_to_user_id !== selfId) return 'reply';
  if (refs.some((r) => r.type === 'replied_to')) return 'self_reply';
  if (refs.some((r) => r.type === 'quoted')) return 'quote';
  return 'original';
}

function extensionFor(mediaItem, url) {
  if (mediaItem.type === 'photo') {
    return extname(new URL(url).pathname) || '.jpg';
  }
  return '.mp4';
}

function mediaFor(tweet, mediaMap) {
  const keys = (tweet.attachments && tweet.attachments.media_keys) || [];
  const out = [];
  for (const key of keys) {
    const item = mediaMap.get(key);
    if (!item) {
      warn(`Media ${key} on post ${tweet.id} was not returned by the API; skipped.`);
      continue;
    }
    const source = bestMediaUrl(item);
    if (!source) {
      warn(`Media ${key} on post ${tweet.id} has no downloadable variant; skipped.`);
      continue;
    }
    out.push({
      key,
      type: item.type,
      source_url: source,
      alt: item.alt_text || null,
      width: item.width || null,
      height: item.height || null,
      duration_ms: item.duration_ms || null,
      file: null,
    });
  }
  return out;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const handle = args.user || DEFAULT_HANDLE;
  const since = args.since || DEFAULT_SINCE;
  const until = args.until || undefined;
  const limit = args.limit ? Number(args.limit) : 3200;
  const withMedia = !args['no-media'];

  info(`Fetching @${handle} from ${since}${until ? ` to ${until}` : ''}`);

  const user = await lookupUser(handle);
  step(`user id ${user.id}`);

  const timeline = await fetchTimeline(user.id, {
    startTime: since,
    endTime: until,
    limit,
    onPage: ({ page, total }) => step(`page ${page}: ${total} posts so far`),
  });

  ensureDir(DATA_DIR);
  writeJson(RAW_TIMELINE_FILE, {
    fetched_at: new Date().toISOString(),
    account: user,
    request: { since, until: until || null, limit },
    partial_errors: timeline.errors,
    tweets: timeline.tweets,
    includes: {
      media: [...timeline.media.values()],
      users: [...timeline.users.values()],
      tweets: [...timeline.referenced.values()],
    },
  });

  const sinceMs = Date.parse(since);
  const skipped = [];
  const note = (tweet, reason) => skipped.push({
    id: tweet.id,
    date: tweet.created_at,
    reason,
    text: (tweet.text || '').slice(0, 140),
    x_url: `https://x.com/${handle}/status/${tweet.id}`,
  });

  // Keep originals, quotes and self-replies; the last of those is what a thread is made of.
  const kept = new Map();
  for (const tweet of timeline.tweets) {
    if (Date.parse(tweet.created_at) < sinceMs) {
      note(tweet, 'outside-window');
      continue;
    }
    const kind = classify(tweet, user.id);
    if (kind === 'retweet') {
      note(tweet, 'retweet');
      continue;
    }
    if (kind === 'reply') {
      note(tweet, 'reply-to-other-account');
      continue;
    }
    kept.set(tweet.id, { tweet, kind });
  }

  // A conversation_id is the id of the tweet that started the thread, so grouping by it and then
  // looking for that id inside the group tells us whether we hold the whole thread or a fragment.
  const conversations = new Map();
  for (const entry of kept.values()) {
    const key = entry.tweet.conversation_id || entry.tweet.id;
    if (!conversations.has(key)) conversations.set(key, []);
    conversations.get(key).push(entry);
  }

  const posts = [];
  for (const [conversationId, members] of conversations) {
    members.sort((a, b) => Date.parse(a.tweet.created_at) - Date.parse(b.tweet.created_at));
    const root = members.find((m) => m.tweet.id === conversationId);
    if (!root) {
      // The thread began before the window, or under a reply to someone else. Posting the tail on
      // its own would read as a fragment, so it goes to the skip list for a human to look at.
      for (const m of members) note(m.tweet, 'thread-part-without-root');
      continue;
    }

    const parts = members.map((m) => {
      const expanded = expandText(m.tweet);
      return {
        id: m.tweet.id,
        date: m.tweet.created_at,
        text: expanded.text,
        links: expanded.links,
        mentions: expanded.mentions,
        quoted_url: expanded.quotedUrl,
        media: mediaFor(m.tweet, timeline.media),
      };
    });

    const media = parts.flatMap((p) => p.media);
    const links = [...new Set(parts.flatMap((p) => p.links))];
    const mentions = [...new Set(parts.flatMap((p) => p.mentions))];
    const quotedUrl = parts.map((p) => p.quoted_url).find(Boolean) || null;

    posts.push({
      id: root.tweet.id,
      date: root.tweet.created_at,
      kind: members.length > 1 ? 'thread' : 'single',
      is_quote: Boolean(quotedUrl),
      tweet_ids: members.map((m) => m.tweet.id),
      text: parts.map((p) => p.text).filter(Boolean).join('\n\n'),
      parts: members.length > 1 ? parts.map(({ media: _drop, ...rest }) => rest) : undefined,
      links,
      mentions,
      quoted_url: quotedUrl,
      media,
      has_media: media.length > 0,
      x_url: `https://x.com/${handle}/status/${root.tweet.id}`,
    });
  }

  posts.sort((a, b) => Date.parse(a.date) - Date.parse(b.date));

  if (withMedia) {
    ensureDir(MEDIA_DIR);
    const all = posts.flatMap((p) => p.media);
    let done = 0;
    for (const item of all) {
      const file = join(MEDIA_DIR, item.key + extensionFor(item, item.source_url));
      if (!existsSync(file)) {
        try {
          await downloadTo(item.source_url, file);
        } catch (err) {
          warn(`Could not download ${item.key}: ${err.message}`);
          continue;
        }
      }
      item.file = toRelative(file);
      done++;
      if (done % 10 === 0) step(`${done}/${all.length} media files`);
    }
    if (all.length) step(`${done}/${all.length} media files on disk`);
  } else {
    warn('--no-media: posts.json records the source URLs but nothing was downloaded.');
  }

  skipped.sort((a, b) => Date.parse(a.date) - Date.parse(b.date));

  const counts = {
    read_from_api: timeline.reads,
    posts: posts.length,
    threads: posts.filter((p) => p.kind === 'thread').length,
    quotes: posts.filter((p) => p.is_quote).length,
    with_media: posts.filter((p) => p.has_media).length,
    text_only: posts.filter((p) => !p.has_media).length,
    skipped: skipped.length,
    skipped_by_reason: skipped.reduce(
      (acc, s) => ({ ...acc, [s.reason]: (acc[s.reason] || 0) + 1 }),
      {}
    ),
  };

  // --merge keeps what earlier runs found. An incremental sync asks only for what is new, and
  // without this the record of the backfill - including which posts were skipped and why - would
  // be replaced by a file describing the last five minutes.
  let finalPosts = posts;
  let finalSkipped = skipped;
  if (args.merge) {
    const existing = readJson(POSTS_FILE, { posts: [], skipped: [] });
    const byId = new Map([...(existing.posts || []), ...posts].map((p) => [p.id, p]));
    finalPosts = [...byId.values()].sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
    const skippedById = new Map([...(existing.skipped || []), ...skipped].map((s) => [s.id, s]));
    finalSkipped = [...skippedById.values()].sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
    step(`merged with ${(existing.posts || []).length} post(s) from earlier runs`);
  }

  writeJson(POSTS_FILE, {
    generated_at: new Date().toISOString(),
    account: user,
    window: { since, until: until || null },
    fetched_now: { posts: posts.length, skipped: skipped.length },
    counts,
    posts: finalPosts,
    skipped: finalSkipped,
  });

  info('');
  ok(`${posts.length} posts written to ${toRelative(POSTS_FILE)}`);
  info(`  threads merged from self-replies: ${counts.threads}`);
  info(`  quote posts (flagged for review): ${counts.quotes}`);
  info(`  with media: ${counts.with_media}   text only (need a card): ${counts.text_only}`);
  info(`  skipped: ${counts.skipped} ${JSON.stringify(counts.skipped_by_reason)}`);
  info(`  API posts read: ${counts.read_from_api} (about $${(counts.read_from_api * USD_PER_READ).toFixed(2)})`);
  info('');
  info('Next: node build_captions.mjs');
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
