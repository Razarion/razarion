#!/usr/bin/env node
// Step 2: turn the X text in data/posts.json into Instagram captions in data/captions.json.
//
// Nothing here publishes. Every entry lands with status "review" and publish.mjs will only ever
// touch entries a human has set to "ok" - the review pass is a gate, not a suggestion.
//
//   node build_captions.mjs              # generate, keeping any edits already made
//   node build_captions.mjs --date-line  # prepend the original posting date to each caption
//   node build_captions.mjs --force      # regenerate everything, discarding edits

import { parseArgs } from './lib/args.mjs';
import { POSTS_FILE, CAPTIONS_FILE, readJson, writeJson, toRelative } from './lib/paths.mjs';
import { info, ok, warn, fail } from '../src/util/log.mjs';

// Instagram's documented ceilings. Both are checked here rather than at publish time, because
// finding out after an upload that the caption was 30 characters too long is a bad trade.
const MAX_CAPTION = 2200;
const MAX_HASHTAGS = 30;
const MAX_CAROUSEL = 10;

// Six that fit the account on every post, per the brief. Topical tags top this up to at most ten.
const BASE_HASHTAGS = ['rts', 'indiedev', 'opensource', 'webassembly', 'browsergame', 'gamedev'];

// Deliberately small and deterministic. A tag that appears because the post genuinely mentions
// the thing reads as written by a person; a tag drawn from a pool at random does not.
const TOPIC_HASHTAGS = [
  [/\b(multiplayer|pvp|co-?op|players?)\b/i, 'multiplayer'],
  [/\b(terrain|map|level|biome|planet)\b/i, 'leveldesign'],
  [/\b(unit|tank|weapon|combat|battle|attack)\b/i, 'strategygame'],
  [/\b(babylon|shader|render|lighting|texture|mesh)\b/i, 'babylonjs'],
  [/\b(angular|typescript|ui|interface|hud)\b/i, 'angular'],
  [/\b(teavm|java|spring)\b/i, 'javagamedev'],
  [/\b(devlog|progress|update|milestone|alpha|release)\b/i, 'devlog'],
  [/\b(quest|tutorial|progression|unlock|tech tree)\b/i, 'gamedesign'],
];

const LINK_HINT = 'Link in bio.';

const MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

function formatDate(iso) {
  const d = new Date(iso);
  return `${d.getUTCDate()} ${MONTHS[d.getUTCMonth()]} ${d.getUTCFullYear()}`;
}

function tidy(text) {
  return text
    .replace(/[ \t]{2,}/g, ' ')
    .replace(/[ \t]+$/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

/**
 * Strips the "@" and keeps the name.
 *
 * A bare @handle in an Instagram caption is a live link to whoever holds that name on Instagram,
 * which is almost never the account meant on X. Removing the whole word would break sentences
 * built around it, so the name survives as plain text and the entry is flagged for review.
 */
function stripMentions(text) {
  const stripped = [];
  let out = text.replace(/(^|[^A-Za-z0-9_])@([A-Za-z0-9_]{1,15})\b/g, (m, lead, name) => {
    stripped.push(name);
    return lead + name;
  });

  // "Title https://youtu.be/... via @YouTube" is X's share suffix. With the link and the @ gone,
  // "via YouTube" is an attribution to nothing, so the whole tail goes.
  for (const name of stripped) {
    out = out.replace(new RegExp('\\s*\\bvia ' + name + '\\b[.!]?\\s*$', 'i'), '');
  }
  return { text: out, stripped };
}

/**
 * Lifts a trailing hashtag paragraph out of the body.
 *
 * Posts that already end in "#RTS #Razarion" would otherwise get a second hashtag block appended
 * underneath the first. The tags the post came with are kept - they are the author's - and merged
 * into the single block at the end.
 */
function splitTrailingHashtags(text) {
  const paragraphs = text.split(/\n{2,}/);
  const own = [];
  while (paragraphs.length && /^\s*(?:#[A-Za-z0-9_]+\s*)+$/.test(paragraphs[paragraphs.length - 1])) {
    own.unshift(...paragraphs.pop().trim().split(/\s+/));
  }
  return { body: paragraphs.join('\n\n').trim(), own };
}

// A sentence ending in a preposition or a colon existed only to hand over to the link that
// followed it. "Play it at https://..." has nothing left to say once the URL is gone.
const HANDOVER_TAIL = /(?:\b(?:at|on|in|to|via|from|here|see|check|read|watch|try)\b|[:\-–—])$/i;

/**
 * Takes links out of the body, sentence by sentence.
 *
 * Links are not clickable in an Instagram caption, so a URL in the body is dead weight that also
 * reads as spam - the caption points at the profile link instead. Deleting the URL alone leaves a
 * dangling fragment, so a sentence that was only there to carry the link goes with it. A sentence
 * that says something of its own keeps its text and gets flagged, because guessing wrong about
 * which is which is exactly what the review step is for.
 */
function stripLinks(text) {
  const hasUrl = /https?:\/\/\S+/;
  let remnant = false;

  const lines = text.split('\n').map((line) => {
    if (!hasUrl.test(line)) return line;

    const kept = [];
    for (const sentence of line.split(/(?<=[.!?])\s+/)) {
      if (!hasUrl.test(sentence)) {
        kept.push(sentence);
        continue;
      }
      const residue = sentence.replace(/https?:\/\/\S+/g, '').replace(/\s+/g, ' ').trim();
      const stem = residue.replace(/[.!?,;]+$/, '').trim();
      // Short and ending in a handover word means it was scaffolding for the link ("Play it at").
      // Short but self-contained is usually the title of what was linked, which is worth keeping.
      if (!stem || stem.length <= 12 || HANDOVER_TAIL.test(stem)) continue;
      kept.push(residue);
      remnant = true;
    }
    return kept.join(' ');
  });

  return { text: lines.join('\n'), remnant };
}

function existingHashtags(text) {
  return new Set([...text.matchAll(/#([A-Za-z0-9_]+)/g)].map((m) => m[1].toLowerCase()));
}

function pickHashtags(text) {
  const already = existingHashtags(text);
  const chosen = [];
  for (const [pattern, tag] of TOPIC_HASHTAGS) {
    if (chosen.length >= 4) break;
    if (pattern.test(text) && !already.has(tag) && !chosen.includes(tag)) chosen.push(tag);
  }
  const tags = [...BASE_HASHTAGS.filter((t) => !already.has(t)), ...chosen];
  return tags.slice(0, 10).map((t) => '#' + t);
}

function buildCaption(post, { dateLine }) {
  const flags = [];   // something to decide or fix
  const notes = [];   // happened, worth knowing, needs nothing

  let body = post.text || '';
  const mentions = stripMentions(body);
  body = mentions.text;
  if (mentions.stripped.length) flags.push('had-mentions');

  const hadLinks = post.links.length > 0 || /https?:\/\//.test(body);
  const stripped = stripLinks(body);
  body = tidy(stripped.text);
  if (hadLinks) notes.push('links-removed');
  if (stripped.remnant) flags.push('link-remnant');

  // A text-only post built around a link loses its whole point on Instagram, where the link
  // cannot be followed. Worth a decision rather than a card nobody can act on.
  if (hadLinks && !post.has_media) flags.push('link-only');

  const withoutTags = splitTrailingHashtags(body);
  body = withoutTags.body;

  if (!body) flags.push('empty-text');
  // The house style avoids exclamation marks, but these are posts that were already published in
  // the author's own voice. Worth seeing, not worth 36 entries demanding attention.
  if (body.includes('!')) notes.push('has-exclamation');
  if (post.kind === 'thread') notes.push('thread');
  if (post.is_quote) flags.push('quote');
  if (!post.has_media) flags.push('needs-card');
  if (post.media.length > MAX_CAROUSEL) flags.push('too-many-media');

  // The post's own tags lead, ours fill up behind them, and the cap counts both.
  const seen = new Set(withoutTags.own.map((t) => t.slice(1).toLowerCase()));
  const hashtags = [
    ...withoutTags.own,
    ...pickHashtags(body).filter((t) => !seen.has(t.slice(1).toLowerCase())),
  ].slice(0, 10);

  const blocks = [];
  if (body) blocks.push(body);
  if (dateLine) blocks.push(`Originally posted on ${formatDate(post.date)}.`);
  if (hadLinks) blocks.push(LINK_HINT);
  blocks.push(hashtags.join(' '));

  const caption = blocks.join('\n\n');
  if (caption.length > MAX_CAPTION) flags.push('too-long');
  const tagCount = [...caption.matchAll(/#[A-Za-z0-9_]+/g)].length;
  if (tagCount > MAX_HASHTAGS) flags.push('too-many-hashtags');

  return { caption, flags, notes };
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const dateLine = Boolean(args['date-line']);
  const force = Boolean(args.force);

  const source = readJson(POSTS_FILE);
  if (!source) {
    throw new Error(`No ${toRelative(POSTS_FILE)}. Run: node fetch_posts.mjs`);
  }

  const previous = readJson(CAPTIONS_FILE);
  const previousById = new Map(((previous && previous.captions) || []).map((c) => [c.id, c]));

  let kept = 0;
  const captions = source.posts.map((post) => {
    const generated = buildCaption(post, { dateLine });
    const entry = {
      id: post.id,
      date: post.date,
      x_url: post.x_url,
      status: 'review',
      edited: false,
      flags: generated.flags,
      notes: generated.notes,
      needs_card: !post.has_media,
      media: post.media.map((m) => ({ type: m.type, file: m.file })),
      caption: generated.caption,
      source_text: post.text,
    };

    // An entry a human has touched is the whole point of this step; regenerating over it would
    // throw away the review. Only the machine-derived fields are refreshed.
    const prev = previousById.get(post.id);

    // A rendered card is the media of a text-only post. posts.json knows nothing about it, so it
    // has to be carried over here or the entry loses the only thing that makes it postable.
    if (prev && prev.card) {
      entry.card = prev.card;
      if (!entry.media.length) entry.media = [{ type: 'photo', file: prev.card }];
    }

    if (prev && !force && (prev.edited === true || prev.status !== 'review')) {
      kept++;
      return { ...entry, status: prev.status, edited: prev.edited, caption: prev.caption, notes: prev.notes };
    }
    return entry;
  });

  if (force && previousById.size) {
    warn('--force: regenerated every caption, including ones that had been edited.');
  }

  // posts.json holds whatever the last fetch asked for, which in incremental mode is only what is
  // new. Entries from earlier fetches are carried over rather than dropped, so captions.json stays
  // the full record and publish.mjs keeps seeing the whole history.
  const fetched = new Set(source.posts.map((p) => p.id));
  const carriedOver = [...previousById.values()].filter((entry) => !fetched.has(entry.id));
  if (carriedOver.length) {
    captions.push(...carriedOver);
    captions.sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
  }

  const counts = {
    total: captions.length,
    kept_from_review: kept,
    awaiting_review: captions.filter((c) => c.status === 'review').length,
    ready: captions.filter((c) => c.status === 'ok').length,
    skipped: captions.filter((c) => c.status === 'skip').length,
    needs_card: captions.filter((c) => c.needs_card).length,
    flagged: captions.filter((c) => c.flags.length).length,
  };

  writeJson(CAPTIONS_FILE, {
    generated_at: new Date().toISOString(),
    review: {
      how: 'Read every caption. Set status to "ok" to publish it, or "skip" to leave it out.',
      editing: 'Edit the caption text freely, then set "edited": true so a re-run keeps your version.',
      note: 'publish.mjs only ever posts entries with status "ok". Anything left on "review" is ignored.',
    },
    counts,
    captions,
  });

  info('');
  ok(`${captions.length} captions written to ${toRelative(CAPTIONS_FILE)}`);
  info(`  awaiting your review: ${counts.awaiting_review}`);
  if (counts.kept_from_review) info(`  kept your edits on: ${counts.kept_from_review}`);
  info(`  need a text card (no media): ${counts.needs_card}`);
  info(`  flagged for a closer look: ${counts.flagged}`);
  info('');
  info('Open data/captions.json, set status on each entry, then continue with the publish step.');
}

try {
  main();
} catch (err) {
  fail(err.message);
  process.exit(1);
}
