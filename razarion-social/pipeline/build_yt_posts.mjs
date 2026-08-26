#!/usr/bin/env node
// Prepares the video clips for a manual upload to YouTube Studio.
//
// Deliberately not an uploader. Videos uploaded through an API project that has not passed
// Google's compliance audit are locked private and cannot be appealed - the Razarion cloud project
// was created in June 2024, well after the July 2020 cutoff, so that applies here. Uploading by
// hand takes an hour and produces public videos; the audit takes weeks.
//
// What this does take off your hands is the typing: every clip gets a title, a description and
// tags, and the files are named so that dragging them into Studio lands them in date order.
//
//   node build_yt_posts.mjs           # write data/youtube/ and its metadata.json
//   node build_yt_posts.mjs --force   # regenerate, discarding edits

import { copyFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import {
  PIPELINE_ROOT, DATA_DIR, POSTS_FILE, ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const YOUTUBE_DIR = join(DATA_DIR, 'youtube');
const METADATA_FILE = join(YOUTUBE_DIR, 'metadata.json');

// YouTube truncates around 70 characters on a phone, so the title has to say what it is before
// that. The prefix is part of the budget.
const MAX_TITLE = 70;
const PREFIX = 'Razarion – ';

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
  [/\b(unit|tank|weapon|combat|battle|attack|explosion)\b/i, 'rts combat'],
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

function formatDate(iso) {
  const d = new Date(iso);
  return `${d.getUTCDate()} ${MONTHS[d.getUTCMonth()]} ${d.getUTCFullYear()}`;
}

// Emoji belong in a social post but not in a YouTube title, where they crowd out the words that
// have to survive truncation.
function stripDecoration(text) {
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
 * starting point rather than an answer - metadata.json is there to be edited, and a title is the
 * one thing worth rewriting by hand.
 */
function buildTitle(text) {
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

function buildDescription(post) {
  const clean = stripDecoration(post.text || '').trim();
  const blocks = [];
  if (clean) blocks.push(clean);
  blocks.push(`Originally posted on X on ${formatDate(post.date)}.`);
  blocks.push(BOILERPLATE);
  return blocks.join('\n\n');
}

function buildTags(text) {
  const chosen = [];
  for (const [pattern, tag] of TOPIC_TAGS) {
    if (chosen.length >= 5) break;
    if (pattern.test(text)) chosen.push(tag);
  }
  return [...BASE_TAGS, ...chosen];
}

function slugify(title) {
  return title
    .replace(PREFIX, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 50);
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const force = Boolean(args.force);

  const source = readJson(POSTS_FILE);
  if (!source) throw new Error(`No ${toRelative(POSTS_FILE)}. Run: node fetch_posts.mjs`);

  const previous = readJson(METADATA_FILE);
  const previousById = new Map(((previous && previous.videos) || []).map((v) => [v.id, v]));

  ensureDir(YOUTUBE_DIR);

  const withVideo = source.posts.filter((p) =>
    (p.media || []).some((m) => m.type === 'video' || m.type === 'animated_gif')
  );

  let kept = 0;
  let copied = 0;

  const videos = withVideo.map((post) => {
    const clip = post.media.find((m) => m.type === 'video' || m.type === 'animated_gif');
    const { title, truncated } = buildTitle(post.text || '');

    const filename = `${post.date.slice(0, 10)}_${slugify(title) || post.id}.mp4`;
    const destination = join(YOUTUBE_DIR, filename);
    const sourceFile = join(PIPELINE_ROOT, clip.file);

    if (existsSync(sourceFile) && (!existsSync(destination) || force)) {
      copyFileSync(sourceFile, destination);
      copied++;
    } else if (!existsSync(sourceFile)) {
      warn(`${post.id}: ${clip.file} is missing on disk.`);
    }

    // Portrait and square clips become Shorts on their own; nothing needs to be chosen for them.
    // Everything else stays a normal video whatever it is labelled here.
    const ratio = clip.width && clip.height ? clip.width / clip.height : null;
    const short = ratio !== null && ratio <= 1.05 && (clip.duration_ms || 0) <= 180000;

    const entry = {
      id: post.id,
      date: post.date,
      file: `data/youtube/${filename}`,
      becomes_short: short,
      duration_s: clip.duration_ms ? Math.round(clip.duration_ms / 1000) : null,
      dimensions: clip.width ? `${clip.width}x${clip.height}` : null,
      flags: truncated ? ['title-truncated'] : [],
      edited: false,
      title,
      description: buildDescription(post),
      tags: buildTags(post.text || ''),
      x_url: post.x_url,
    };

    const prev = previousById.get(post.id);
    if (prev && !force && prev.edited === true) {
      kept++;
      // A title written by hand is not a truncated one, whatever the generator would have produced.
      return {
        ...entry,
        flags: entry.flags.filter((f) => f !== 'title-truncated'),
        title: prev.title,
        description: prev.description,
        tags: prev.tags,
        edited: true,
      };
    }
    return entry;
  });

  videos.sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
  const truncatedTitles = videos.filter((v) => v.flags.includes('title-truncated')).length;

  writeJson(METADATA_FILE, {
    generated_at: new Date().toISOString(),
    how: [
      'The clips are in this folder, named by date - drag them all into YouTube Studio at once.',
      'For each one, copy title, description and tags from here.',
      'Edit anything you like first; set "edited": true and a re-run will keep your version.',
      'Category: Gaming. Not made for kids.',
    ],
    counts: {
      total: videos.length,
      becomes_short: videos.filter((v) => v.becomes_short).length,
      normal_video: videos.filter((v) => !v.becomes_short).length,
      titles_truncated: truncatedTitles,
    },
    videos,
  });

  info('');
  ok(`${videos.length} clips prepared in ${toRelative(YOUTUBE_DIR)}`);
  info(`  copied now: ${copied}`);
  if (kept) info(`  kept your edits on: ${kept}`);
  info(`  will become Shorts (portrait or square): ${videos.filter((v) => v.becomes_short).length}`);
  info(`  normal videos (landscape): ${videos.filter((v) => !v.becomes_short).length}`);
  if (truncatedTitles) warn(`  ${truncatedTitles} title(s) had to be cut - worth rewriting by hand`);
  info('');
  info(`Metadata: ${toRelative(METADATA_FILE)}`);
}

try {
  main();
} catch (err) {
  fail(err.message);
  process.exit(1);
}
