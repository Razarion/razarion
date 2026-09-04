import { CAPTIONS_FILE, FB_POSTS_FILE, X_POSTS_FILE, readJson, writeJson } from './paths.mjs';

// Instagram's caption ceiling and X's post ceiling. Facebook's is 63206, which nothing here
// approaches.
const MAX_IG = 2200;
const MAX_X = 280;

// X counts every link as 23 characters regardless of its real length.
const X_LINK_WEIGHT = 23;

export function xLength(text) {
  return text.replace(/https?:\/\/\S+/g, 'x'.repeat(X_LINK_WEIGHT)).length;
}

// The six that fit the account everywhere else, so a composed or generated post is not visibly
// different from a mirrored one.
export const BASE_HASHTAGS = ['rts', 'indiedev', 'opensource', 'webassembly', 'browsergame', 'gamedev'];

/**
 * Turns one piece of writing into the three shapes the feeds want.
 *
 * The differences are not stylistic: a link is clickable on X and Facebook and dead on Instagram,
 * Instagram is the only one that rewards hashtags, and only Instagram refuses a post without
 * media. Everything else stays identical so the same thing is being said in all three places.
 */
export function buildEntries({ id, date, text, link, tags = [], media = [], source = 'composed' }) {
  const hashtags = [...BASE_HASHTAGS, ...tags].slice(0, 10).map((t) => '#' + t);

  // X gets the text without the link, and it is the one network where that is the better post.
  // A link costs $0.20 an post against $0.015 without one - thirteen times the price - and X also
  // damps the reach of anything carrying one, so the link buys a smaller audience at a higher
  // price. The route to the site is the profile, the same as on Instagram.
  const xText = text;
  const xFlags = [];
  if (xLength(xText) > MAX_X) xFlags.push('too-long');
  if (!media.length) xFlags.push('text-only');

  const igCaption = [text, link ? 'Link in bio.' : null, hashtags.join(' ')]
    .filter(Boolean)
    .join('\n\n');
  const igFlags = [];
  if (igCaption.length > MAX_IG) igFlags.push('too-long');
  if (!media.length) igFlags.push('needs-card');

  const fbMessage = link ? `${text}\n\n${link}` : text;

  const common = { id, date, x_url: null, status: 'review', edited: false, source };
  const copyMedia = () => media.map((m) => ({ ...m }));

  return {
    ig: {
      ...common,
      flags: igFlags,
      notes: [],
      needs_card: media.length === 0,
      media: copyMedia(),
      caption: igCaption,
      source_text: text,
    },
    fb: {
      ...common,
      flags: [],
      notes: link ? ['has-links'] : [],
      media: copyMedia(),
      message: fbMessage,
      source_text: text,
    },
    x: {
      ...common,
      flags: xFlags,
      notes: [],
      media: copyMedia(),
      text: xText,
      source_text: text,
    },
    lengths: { x: xLength(xText), ig: igCaption.length, fb: fbMessage.length },
    flags: { x: xFlags, ig: igFlags },
  };
}

const TARGETS = [
  { file: CAPTIONS_FILE, key: 'captions', pick: (e) => e.ig, label: 'Instagram' },
  { file: FB_POSTS_FILE, key: 'posts', pick: (e) => e.fb, label: 'Facebook' },
  { file: X_POSTS_FILE, key: 'posts', pick: (e) => e.x, label: 'X' },
];

/**
 * Appends the three entries to the review files the publishers already read.
 *
 * Writing into those rather than into a file of its own is what makes the rest of the pipeline
 * work unchanged: the same review gate, the same upload step, the same publishers.
 */
export function writeEntries(entries) {
  for (const target of TARGETS) {
    const doc = readJson(target.file, { generated_at: null, counts: {}, [target.key]: [] });
    const list = doc[target.key] || (doc[target.key] = []);
    const entry = target.pick(entries);
    if (list.some((e) => e.id === entry.id)) {
      throw new Error(`${target.label}: an entry with id ${entry.id} already exists.`);
    }
    list.push(entry);
    list.sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
    writeJson(target.file, doc);
  }
}
