#!/usr/bin/env node
// Preflight. Answers one question: is everything ready for a live run, and if not, what is missing.
//
// Reads only - it changes nothing and posts nothing.
//
//   node check.mjs

import { existsSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { loadImage } from '@napi-rs/canvas';
import {
  PIPELINE_ROOT, CAPTIONS_FILE, POSTS_FILE, UPLOADS_FILE, POSTED_FILE, readJson, toRelative,
} from './lib/paths.mjs';
import { FORMATS, PLATFORM_FORMAT, probeVideo } from './lib/video.mjs';
import { env } from '../src/config.mjs';
import { info, ok, warn, fail } from '../src/util/log.mjs';

const MAX_CAPTION = 2200;
const MAX_HASHTAGS = 30;
const MAX_IMAGE_BYTES = 8 * 1024 * 1024;
const MAX_CAROUSEL = 10;

// Instagram rejects image aspect ratios outside 4:5 to 1.91:1, and it does so at publish time
// rather than at upload time. upload_media.mjs pads anything outside the range, so this is a
// heads-up rather than a blocker - but a 36003 halfway through a two-hour run is worth the few
// seconds it costs to measure every file first.
const MIN_RATIO = 0.8;
const MAX_RATIO = 1.91;

// Instagram takes JPEG and nothing else for images. A PNG is accepted by the upload and then fails
// at publish time with a format error, which is the worst possible place to find out.
const ALLOWED_IMAGE = /\.jpe?g$/i;

// A reel shorter than this is rejected at publish time, and there is nothing upload_media.mjs can
// do about it - the only fix is a longer recording. Worth knowing before a queue runs, not after.
const MIN_REEL_SECONDS = 3;

const blockers = [];
const notices = [];

const block = (msg) => blockers.push(msg);
const notice = (msg) => notices.push(msg);

function checkCaptions() {
  const doc = readJson(CAPTIONS_FILE);
  if (!doc) {
    block(`No ${toRelative(CAPTIONS_FILE)} - run build_captions.mjs`);
    return null;
  }

  const byStatus = {};
  for (const entry of doc.captions) byStatus[entry.status] = (byStatus[entry.status] || 0) + 1;

  info('Review');
  for (const [status, count] of Object.entries(byStatus)) info(`  ${status}: ${count}`);

  if (byStatus.review) {
    block(`${byStatus.review} entr(ies) still on status "review" - they will not be published.`);
  }
  const unknown = Object.keys(byStatus).filter((s) => !['ok', 'skip', 'review'].includes(s));
  if (unknown.length) block(`Unknown status value(s): ${unknown.join(', ')}`);

  return doc;
}

async function checkMedia(doc) {
  // Only what could still go out. Media of a published post is finished work: its file has already
  // been through conversion, and reporting that it "still needs upload_media.mjs" - or that a clip
  // published to X two months ago is too short to be a reel - is a finding nobody can act on. The
  // whole point of a preflight is that everything it says is worth reading.
  const posted = readJson(POSTED_FILE, { posted: {} });
  const live = doc.captions.filter((e) => e.status === 'ok' && !posted.posted[e.id]);
  info('');
  info(`Media for the ${live.length} unpublished entr(ies) marked ok`);

  let missingFiles = 0;
  let notJpeg = 0;
  let oversize = 0;
  let noMedia = 0;
  let notUploaded = 0;
  let carouselOver = 0;
  let offRatio = 0;
  let clips = 0;
  let clipsTooShort = 0;
  let clipsTrimmed = 0;
  let clipsUnreadable = 0;

  for (const entry of live) {
    const media = entry.media || [];
    if (!media.length) {
      noMedia++;
      block(`${entry.id} (${entry.date.slice(0, 10)}) is ok but has no media at all.`);
      continue;
    }
    if (media.length > MAX_CAROUSEL) {
      carouselOver++;
      notice(`${entry.id} has ${media.length} media; only the first ${MAX_CAROUSEL} will be posted.`);
    }

    for (const item of media) {
      if (!item.file) {
        block(`${entry.id} has a media entry with no file.`);
        continue;
      }
      const file = join(PIPELINE_ROOT, item.file);
      if (!existsSync(file)) {
        missingFiles++;
        block(`${entry.id}: ${item.file} is missing on disk.`);
        continue;
      }
      if (item.type === 'photo' && !ALLOWED_IMAGE.test(item.file)) {
        notJpeg++;
        if (item.url) {
          block(`${entry.id}: ${item.file} was uploaded as a non-JPEG. Delete its entry from uploads.json and re-upload.`);
        } else {
          notice(`${entry.id}: ${item.file} is not a JPEG; upload_media.mjs converts it before uploading.`);
        }
      }
      if (item.type === 'photo' && statSync(file).size > MAX_IMAGE_BYTES) {
        oversize++;
        block(`${entry.id}: ${item.file} is over Instagram's 8 MB image limit.`);
      }
      // Only what has not gone up yet: an uploaded file has already been through the padding step.
      if (item.type === 'photo' && !item.url) {
        try {
          const image = await loadImage(file);
          const ratio = image.width / image.height;
          if (ratio < MIN_RATIO || ratio > MAX_RATIO) {
            offRatio++;
            notice(
              `${entry.id}: ${item.file} is ${ratio.toFixed(2)}:1, outside 0.8-1.91; ` +
                'upload_media.mjs pads it onto the background colour.'
            );
          }
        } catch {
          notice(`${entry.id}: ${item.file} could not be read as an image.`);
        }
      }

      // Clips are measured for what upload_media.mjs cannot fix on its own. The shape and the codec
      // it converts; a clip too short for a reel, or longer than the slot, is a decision about the
      // recording and belongs in front of the run rather than inside it.
      if (item.type !== 'photo' && !item.url) {
        clips++;
        const probe = await probeVideo(file);
        if (!probe) {
          clipsUnreadable++;
          block(`${entry.id}: ${item.file} cannot be read by ffprobe - is it a valid video?`);
        } else {
          const reel = FORMATS[PLATFORM_FORMAT.instagram];
          if (probe.duration && probe.duration < MIN_REEL_SECONDS) {
            clipsTooShort++;
            block(
              `${entry.id}: ${item.file} is ${probe.duration.toFixed(1)}s; Instagram rejects a reel ` +
                `under ${MIN_REEL_SECONDS}s. Use a longer recording.`
            );
          }
          if (probe.duration > reel.maxSeconds) {
            clipsTrimmed++;
            notice(
              `${entry.id}: ${item.file} is ${Math.round(probe.duration)}s; the reel copy is cut ` +
                `to ${reel.maxSeconds}s.`
            );
          }
        }
      }

      if (!item.url) notUploaded++;
    }
  }

  info(`  files on disk: ${missingFiles ? missingFiles + ' MISSING' : 'all present'}`);
  info(`  image format:  ${notJpeg ? notJpeg + ' PNG, converted to JPEG on upload' : 'all JPEG'}`);
  info(`  aspect ratio:  ${offRatio ? offRatio + ' outside 0.8-1.91, padded on upload' : 'all within range'}`);
  if (clips) {
    const clipNotes = [
      clipsUnreadable ? `${clipsUnreadable} unreadable` : null,
      clipsTooShort ? `${clipsTooShort} under ${MIN_REEL_SECONDS}s` : null,
      clipsTrimmed ? `${clipsTrimmed} trimmed for the reel slot` : null,
    ].filter(Boolean);
    info(`  clips:         ${clips} to convert${clipNotes.length ? ' - ' + clipNotes.join(', ') : ', all usable'}`);
  }
  info(`  uploaded:      ${notUploaded ? notUploaded + ' still without a public URL' : 'all have URLs'}`);
  if (notUploaded) notice(`${notUploaded} file(s) still need upload_media.mjs before publishing.`);
  if (noMedia || oversize || carouselOver) info(`  other: ${noMedia} without media, ${oversize} oversize`);
}

function checkCaptionText(doc) {
  const live = doc.captions.filter((e) => e.status === 'ok');
  let tooLong = 0;
  let empty = 0;
  let tooManyTags = 0;

  for (const entry of live) {
    const caption = entry.caption || '';
    if (!caption.trim()) {
      empty++;
      block(`${entry.id} is ok but its caption is empty.`);
    }
    if (caption.length > MAX_CAPTION) {
      tooLong++;
      block(`${entry.id}: caption is ${caption.length} characters, over the ${MAX_CAPTION} limit.`);
    }
    const tags = [...caption.matchAll(/#[A-Za-z0-9_]+/g)].length;
    if (tags > MAX_HASHTAGS) {
      tooManyTags++;
      block(`${entry.id}: ${tags} hashtags, over the ${MAX_HASHTAGS} limit.`);
    }
  }

  info('');
  info('Captions');
  info(`  length:   ${tooLong ? tooLong + ' too long' : 'all within 2200 characters'}`);
  info(`  hashtags: ${tooManyTags ? tooManyTags + ' over the limit' : 'all within 30'}`);
  info(`  empty:    ${empty || 'none'}`);

  const flagged = live.filter((e) => e.flags && e.flags.length);
  if (flagged.length) {
    const counts = {};
    for (const e of flagged) for (const f of e.flags) counts[f] = (counts[f] || 0) + 1;
    // Flags do not block anything - they were the review's job, and the review is done.
    info(`  flags still on ok entries: ${JSON.stringify(counts)}`);
  }
}

function checkCredentials() {
  info('');
  info('Credentials in razarion-social/.env');
  const backend = env.MEDIA_STORAGE || 'github';
  const hosting =
    backend === 'r2'
      ? ['R2_ACCOUNT_ID', 'R2_BUCKET', 'R2_ACCESS_KEY_ID', 'R2_SECRET_ACCESS_KEY', 'R2_PUBLIC_BASE_URL']
      : ['GITHUB_TOKEN'];

  const groups = {
    'X (fetching)': ['X_BEARER_TOKEN'],
    [`Media hosting (${backend})`]: hosting,
    'Instagram (publishing)': ['INSTAGRAM_USER_ID', 'INSTAGRAM_ACCESS_TOKEN'],
  };

  for (const [label, keys] of Object.entries(groups)) {
    const missing = keys.filter((k) => !env[k]);
    if (!missing.length) {
      info(`  ${label}: set`);
    } else {
      info(`  ${label}: missing ${missing.join(', ')}`);
      if (label !== 'X (fetching)') block(`${label}: ${missing.join(', ')} not set in .env`);
    }
  }

  if (env.R2_PUBLIC_BASE_URL && !/^https:\/\//.test(env.R2_PUBLIC_BASE_URL)) {
    block('R2_PUBLIC_BASE_URL has to be an https URL - Instagram will not fetch from anything else.');
  }
}

function checkState(doc) {
  const posted = readJson(POSTED_FILE, { posted: {} });
  const done = Object.keys(posted.posted).length;
  const queue = doc.captions.filter((e) => e.status === 'ok' && !posted.posted[e.id]);

  info('');
  info('Queue');
  info(`  already published: ${done}`);
  info(`  waiting:           ${queue.length}`);
  if (queue.length > 100) {
    notice(`${queue.length} posts is more than the 100/24h limit - the run will stop and continue next time.`);
  }
  const uploads = readJson(UPLOADS_FILE, { uploaded: {} });
  info(`  files uploaded:    ${Object.keys(uploads.uploaded).length}`);
}

const posts = readJson(POSTS_FILE);
if (!posts) {
  fail(`No ${toRelative(POSTS_FILE)} - run fetch_posts.mjs first.`);
  process.exit(1);
}

const doc = checkCaptions();
if (doc) {
  await checkMedia(doc);
  checkCaptionText(doc);
  checkState(doc);
}
checkCredentials();

info('');
if (notices.length) {
  for (const n of notices) warn(n);
  info('');
}

if (blockers.length) {
  for (const b of blockers) fail(b);
  info('');
  fail(`${blockers.length} thing(s) to fix before a live run.`);
  process.exit(1);
}

ok('Ready. Next: node upload_media.mjs, then node publish.mjs --live --limit 1');
