#!/usr/bin/env node
// Prepares the video clips for a manual upload to YouTube Studio.
//
// This is the by-hand route, and it exists because of the audit: a video uploaded through an API
// project that has not passed Google's compliance review is locked private and cannot be appealed
// - the Razarion cloud project was created in June 2024, well after the July 2020 cutoff. Dragging
// the files into Studio takes an hour and produces public videos; the audit takes weeks.
//
// What this takes off your hands is the typing: every clip gets a title, a description and tags,
// and the files are named so that dragging them into Studio lands them in date order.
//
// The automated route is publish_youtube.mjs, which reads yt_posts.json rather than the X archive
// this works from. Both describe a clip through lib/youtube.mjs, so a title written here and a
// title uploaded there are the same title.
//
//   node build_yt_posts.mjs           # write data/youtube/ and its metadata.json
//   node build_yt_posts.mjs --force   # regenerate, discarding edits

import { copyFileSync, existsSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { parseArgs } from './lib/args.mjs';
import {
  PIPELINE_ROOT, DATA_DIR, POSTS_FILE, ensureDir, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import {
  buildTitle, buildDescription, buildTags, slugify, fromXOn, becomesShort,
} from './lib/youtube.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const YOUTUBE_DIR = join(DATA_DIR, 'youtube');
const METADATA_FILE = join(YOUTUBE_DIR, 'metadata.json');


function main() {
  const args = parseArgs(process.argv.slice(2));
  const force = Boolean(args.force);
  // Nothing here can ask YouTube what is already on the channel, so the record of that is kept by
  // hand: run --mark-uploaded once the batch is up, and later runs list only what came after.
  const markUploaded = Boolean(args['mark-uploaded']);

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
    const short = becomesShort({
      width: clip.width,
      height: clip.height,
      durationSeconds: (clip.duration_ms || 0) / 1000,
    });

    const entry = {
      id: post.id,
      date: post.date,
      file: `data/youtube/${filename}`,
      becomes_short: short,
      duration_s: clip.duration_ms ? Math.round(clip.duration_ms / 1000) : null,
      dimensions: clip.width ? `${clip.width}x${clip.height}` : null,
      flags: truncated ? ['title-truncated'] : [],
      edited: false,
      uploaded_at: null,
      title,
      description: buildDescription(post.text || '', { origin: fromXOn(post.date) }),
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
        uploaded_at: prev.uploaded_at || null,
      };
    }
    return { ...entry, uploaded_at: (prev && prev.uploaded_at) || null };
  });

  if (markUploaded) {
    const stamp = new Date().toISOString();
    let marked = 0;
    for (const v of videos) {
      if (!v.uploaded_at) {
        v.uploaded_at = stamp;
        marked++;
      }
    }
    info(`--mark-uploaded: ${marked} clip(s) recorded as uploaded.`);
  }

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
      uploaded: videos.filter((v) => v.uploaded_at).length,
      not_uploaded_yet: videos.filter((v) => !v.uploaded_at).length,
    },
    videos,
  });

  // metadata.json is the record; this is the thing you actually keep open while working through
  // Studio, because copying three fields out of a JSON array is worse than it needs to be.
  const sheet = [
    '# YouTube upload sheet',
    '',
    `${videos.length} clips, oldest first. Drag every file from this folder into Studio at once -`,
    'the names sort by date - then work down this list, one video per block.',
    '',
    'Category: Gaming. Not made for kids. Set the recording date to the date in the heading.',
    '',
    '---',
    '',
  ];
  videos.forEach((v, index) => {
    const number = String(index + 1).padStart(2, '0');
    sheet.push(`## ${number} · ${v.date.slice(0, 10)}${v.becomes_short ? ' · SHORT (portrait)' : ''}`);
    sheet.push('');
    sheet.push('`' + v.file.split('/').pop() + '`');
    sheet.push('');
    sheet.push('**Title**');
    sheet.push('');
    sheet.push('    ' + v.title);
    sheet.push('');
    sheet.push('**Description**');
    sheet.push('');
    for (const line of v.description.split('\n')) sheet.push('    ' + line);
    sheet.push('');
    sheet.push('**Tags**');
    sheet.push('');
    sheet.push('    ' + v.tags.join(', '));
    sheet.push('');
    sheet.push('---');
    sheet.push('');
  });
  const sheetFile = join(YOUTUBE_DIR, 'upload-sheet.md');
  writeFileSync(sheetFile, sheet.join('\n'), 'utf8');

  info('');
  ok(`${videos.length} clips prepared in ${toRelative(YOUTUBE_DIR)}`);
  const pending = videos.filter((v) => !v.uploaded_at).length;
  info(`  not on the channel yet: ${pending}${pending === videos.length ? ' (none marked uploaded)' : ''}`);
  info(`  work through them with ${toRelative(sheetFile)}`);
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
