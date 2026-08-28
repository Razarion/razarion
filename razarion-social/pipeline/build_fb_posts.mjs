#!/usr/bin/env node
// Step 2b: turn the X text in data/posts.json into Facebook posts in data/fb_posts.json.
//
// Deliberately derived from posts.json rather than from captions.json. Those captions were bent
// into Instagram's shape - links stripped out, "Link in bio." appended, text cards standing in for
// missing media, a hashtag set glued on. Every one of those decisions is wrong here.
//
//   node build_fb_posts.mjs           # generate, keeping any edits already made
//   node build_fb_posts.mjs --force   # regenerate everything, discarding edits

import { parseArgs } from './lib/args.mjs';
import { POSTS_FILE, FB_POSTS_FILE, readJson, writeJson, toRelative } from './lib/paths.mjs';
import { info, ok, warn, fail } from '../src/util/log.mjs';

// Facebook's own limit is 63206 characters, which nothing here comes close to. It is checked
// anyway, because the alternative is finding out from a rejected post.
const MAX_TEXT = 63206;

function tidy(text) {
  return text
    .replace(/[ \t]{2,}/g, ' ')
    .replace(/[ \t]+$/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

/**
 * Drops the "@" and keeps the name, then removes a trailing "via <name>".
 *
 * The same reasoning as on the Instagram side: an @handle points at whoever holds that name on
 * Facebook, which is not the account meant on X. What differs is everything else - links stay,
 * because on Facebook they are clickable and even produce a preview card.
 */
function stripMentions(text) {
  const stripped = [];
  let out = text.replace(/(^|[^A-Za-z0-9_])@([A-Za-z0-9_]{1,15})\b/g, (m, lead, name) => {
    stripped.push(name);
    return lead + name;
  });
  for (const name of stripped) {
    out = out.replace(new RegExp('\\s*\\bvia ' + name + '\\b[.!]?\\s*$', 'i'), '');
  }
  return { text: out, stripped };
}

function buildPost(post) {
  const flags = [];
  const notes = [];

  const mentions = stripMentions(post.text || '');
  let message = tidy(mentions.text);
  if (mentions.stripped.length) flags.push('had-mentions');

  // Kept, not appended to. The author's own tags are part of what they wrote; the set that was
  // added for Instagram reach would read as noise here, where hashtags do almost nothing.
  if (/#[A-Za-z0-9_]+/.test(message)) notes.push('has-hashtags');
  if (post.links.length) notes.push('has-links');
  if (post.quoted_url) flags.push('quote');
  if (!message) flags.push('empty-text');
  if (message.length > MAX_TEXT) flags.push('too-long');
  if (!post.has_media) notes.push('text-only');
  if (post.media.length > 1) notes.push('multi-photo');

  return { message, flags, notes };
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const force = Boolean(args.force);

  const source = readJson(POSTS_FILE);
  if (!source) throw new Error(`No ${toRelative(POSTS_FILE)}. Run: node fetch_posts.mjs`);

  const previous = readJson(FB_POSTS_FILE);
  const previousById = new Map(((previous && previous.posts) || []).map((p) => [p.id, p]));

  let kept = 0;
  const posts = source.posts.map((post) => {
    const generated = buildPost(post);
    const entry = {
      id: post.id,
      date: post.date,
      x_url: post.x_url,
      status: 'review',
      edited: false,
      flags: generated.flags,
      notes: generated.notes,
      // The original files, not the Instagram-prepared ones: Facebook has no aspect ratio limits
      // and takes PNG, so there is nothing here to correct for.
      media: post.media.map((m) => ({ type: m.type, file: m.file, url: null })),
      message: generated.message,
      source_text: post.text,
    };

    const prev = previousById.get(post.id);
    if (prev && !force && (prev.edited === true || prev.status !== 'review')) {
      kept++;
      return {
        ...entry,
        status: prev.status,
        edited: prev.edited,
        message: prev.message,
        media: prev.media && prev.media.some((m) => m.url) ? prev.media : entry.media,
      };
    }
    return entry;
  });

  if (force && previousById.size) {
    warn('--force: regenerated every post, including ones that had been edited.');
  }

  // posts.json holds what the last fetch asked for, and says nothing about posts written here
  // rather than mirrored from X. Both are carried over: entries from an earlier, narrower fetch,
  // and anything compose.mjs added, which has no counterpart on X at all.
  const fetched = new Set(source.posts.map((p) => p.id));
  const carriedOver = [...previousById.values()].filter((entry) => !fetched.has(entry.id));
  if (carriedOver.length) {
    posts.push(...carriedOver);
    posts.sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
  }

  const counts = {
    total: posts.length,
    kept_from_review: kept,
    awaiting_review: posts.filter((p) => p.status === 'review').length,
    ready: posts.filter((p) => p.status === 'ok').length,
    skipped: posts.filter((p) => p.status === 'skip').length,
    text_only: posts.filter((p) => p.notes.includes('text-only')).length,
    with_links: posts.filter((p) => p.notes.includes('has-links')).length,
    flagged: posts.filter((p) => p.flags.length).length,
  };

  writeJson(FB_POSTS_FILE, {
    generated_at: new Date().toISOString(),
    review: {
      how: 'Read every message. Set status to "ok" to publish it, or "skip" to leave it out.',
      editing: 'Edit the message freely, then set "edited": true so a re-run keeps your version.',
      note: 'publish_fb.mjs only ever posts entries with status "ok".',
      difference: 'Links stay in the text here - on Facebook they are clickable. Text-only posts ' +
        'need no card. Nothing is appended.',
    },
    counts,
    posts,
  });

  info('');
  ok(`${posts.length} Facebook posts written to ${toRelative(FB_POSTS_FILE)}`);
  info(`  awaiting your review: ${counts.awaiting_review}`);
  if (counts.kept_from_review) info(`  kept your edits on: ${counts.kept_from_review}`);
  info(`  text only (fine on Facebook, no card needed): ${counts.text_only}`);
  info(`  carrying a clickable link: ${counts.with_links}`);
  info(`  flagged for a closer look: ${counts.flagged}`);
}

try {
  main();
} catch (err) {
  fail(err.message);
  process.exit(1);
}
