// How a clip is described on YouTube: title, description, tags, file name.
//
// Lifted out of build_yt_posts.mjs so that the manual-upload preparation and publish_youtube.mjs
// describe the same clip the same way. Two copies of this would drift, and the pipeline already
// has one scar from exactly that: compose.mjs carried its own copy of the entry shaping and kept
// putting the link into the X text long after the shared version had stopped.

// YouTube truncates around 70 characters on a phone, so the title has to say what it is before
// that. The prefix is part of the budget.
export const MAX_TITLE = 70;
export const PREFIX = 'Razarion – ';

/**
 * What a new entry asks for, and the one line to change when the compliance audit is through.
 *
 * Until it is, an OAuth client that has not passed the audit may only create private videos, and
 * asking for public is not refused - it is silently reset. So "private" is not caution here, it is
 * a description of what will happen; the video is then made public by hand in Studio.
 *
 * Change this to 'public' once the audit has passed and the whole queue goes out finished. Entries
 * already written keep whatever privacy they were given, so a run in flight is not affected.
 */
export const DEFAULT_PRIVACY = 'private';

const BASE_TAGS = [
  'razarion',
  'browser rts',
  'rts game',
  'real time strategy',
  'open source game',
  'webassembly',
  'indie game',
  'gamedev',
];

const TOPIC_TAGS = [
  [/\b(multiplayer|pvp|co-?op|players?)\b/i, 'multiplayer rts'],
  [/\b(terrain|map|level|biome|planet|landscape)\b/i, 'terrain rendering'],
  [/\b(unit|tank|weapon|combat|battle|attack|explosion|lightning|tesla)\b/i, 'rts combat'],
  [/\b(babylon|shader|render|lighting|texture|mesh)\b/i, 'babylonjs'],
  [/\b(angular|typescript|ui|interface|hud)\b/i, 'game ui'],
  [/\b(teavm|java|spring|wasm)\b/i, 'teavm'],
  [/\b(factory|build|assembl|production)\b/i, 'base building'],
  [/\b(harvest|resource|crystal|economy)\b/i, 'rts economy'],
];

const BOILERPLATE =
  'Razarion is an open-source multiplayer RTS that runs in a browser tab - no download, ' +
  'no account. One persistent world shared by every player. Currently in alpha.\n\n' +
  'Play: https://www.razarion.com\n' +
  'Source: https://github.com/Razarion/razarion';

const MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

export function formatDate(iso) {
  const d = new Date(iso);
  return `${d.getUTCDate()} ${MONTHS[d.getUTCMonth()]} ${d.getUTCFullYear()}`;
}

// Emoji belong in a social post but not in a YouTube title, where they crowd out the words that
// have to survive truncation.
export function stripDecoration(text) {
  return text
    .replace(/[\u{1F000}-\u{1FAFF}\u{2600}-\u{27BF}\u{2B00}-\u{2BFF}\u{FE0F}]/gu, '')
    .replace(/#[A-Za-z0-9_]+/g, '')
    .replace(/https?:\/\/\S+/g, '')
    .replace(/(^|[^A-Za-z0-9_])@([A-Za-z0-9_]{1,15})\b/g, '$1$2')
    .replace(/\s{2,}/g, ' ')
    .trim();
}

/**
 * Builds a title out of whatever the post said.
 *
 * The first sentence is nearly always the claim the clip is making, so that is the subject. It is a
 * starting point rather than an answer - the entry is there to be edited, and a title is the one
 * thing worth rewriting by hand.
 */
export function buildTitle(text) {
  const clean = stripDecoration(text);
  const firstSentence = clean.split(/(?<=[.!?])\s+/)[0] || clean;
  let subject = firstSentence.replace(/[.!?]+$/, '').trim();

  if (!subject) return { title: PREFIX.replace(/ – $/, ' clip'), truncated: false };

  // The prefix already says Razarion. A sentence that opens with the name again reads as a stutter.
  subject = subject.replace(/^Razarion\s+(is\s+|now\s+)?/i, '');
  if (!subject) subject = firstSentence.replace(/[.!?]+$/, '').trim();
  subject = subject.charAt(0).toUpperCase() + subject.slice(1);

  const room = MAX_TITLE - PREFIX.length;
  let truncated = false;
  if (subject.length > room) {
    // Cut at a word boundary rather than mid-word, and say so, so the review knows to look.
    subject = subject.slice(0, room).replace(/\s+\S*$/, '');
    truncated = true;
  }
  // A title ending in a comma is a sentence that was interrupted, which is what truncation does.
  subject = subject.replace(/[,;:–-]+$/, '').trim();
  return { title: PREFIX + subject, truncated };
}

/**
 * The description: what the post said, where it came from, and what Razarion is.
 *
 * `origin` is the middle block and is optional on purpose. A clip mirrored from the X archive says
 * so; one composed here was never on X, and claiming it was would be a small lie in a field
 * nobody would ever check.
 */
export function buildDescription(text, { origin = null } = {}) {
  const clean = stripDecoration(text || '').trim();
  const blocks = [];
  if (clean) blocks.push(clean);
  if (origin) blocks.push(origin);
  blocks.push(BOILERPLATE);
  return blocks.join('\n\n');
}

export function fromXOn(date) {
  return `Originally posted on X on ${formatDate(date)}.`;
}

export function buildTags(text, extra = []) {
  const chosen = [];
  for (const [pattern, tag] of TOPIC_TAGS) {
    if (chosen.length >= 5) break;
    if (pattern.test(text)) chosen.push(tag);
  }
  return [...BASE_TAGS, ...chosen, ...extra.filter((t) => !BASE_TAGS.includes(t))];
}

export function slugify(title) {
  return title
    .replace(PREFIX, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 50);
}

/**
 * Whether YouTube will file this clip as a Short: aspect ratio at or below 1.05, at most 180
 * seconds. Nothing has to be declared for it - the sorting is done on the file itself - so this is
 * only ever reported, never sent.
 */
export function becomesShort({ width, height, durationSeconds }) {
  if (!width || !height) return false;
  return width / height <= 1.05 && (durationSeconds || 0) <= 180;
}
