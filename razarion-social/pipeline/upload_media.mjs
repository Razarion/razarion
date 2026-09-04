#!/usr/bin/env node
// Step 4: put the media somewhere Instagram can fetch it from.
//
// The Instagram publishing API never accepts a file upload. It is handed a URL and downloads the
// file itself, which means every image and video needs a public home first.
//
//   node upload_media.mjs               # upload media of every entry marked "ok"
//   node upload_media.mjs --dry-run     # list what would go up, upload nothing
//   node upload_media.mjs --to r2       # Cloudflare R2 instead of a GitHub release
//   node upload_media.mjs --all         # everything, whatever the review says

import { readFileSync, writeFileSync, existsSync, statSync } from 'node:fs';
import { basename, join } from 'node:path';
import { createCanvas, loadImage } from '@napi-rs/canvas';
import { parseArgs } from './lib/args.mjs';
import {
  PIPELINE_ROOT, CAPTIONS_FILE, UPLOADS_FILE, POSTED_FILE,
  FB_POSTS_FILE, POSTED_FB_FILE, readJson, writeJson, toRelative,
} from './lib/paths.mjs';
import {
  FORMATS, PLATFORM_FORMAT, derivedPath, needsTranscode, probeVideo, transcodeVideo,
} from './lib/video.mjs';
import { r2Config, putObject, contentTypeFor, sha256 } from './lib/r2.mjs';
import { githubConfig, ensureRelease, listAssets, uploadAsset } from './lib/github.mjs';
import { env } from '../src/config.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

// Instagram documents 8 MB for images and 1 GB for Reels. The image ceiling is the one a card or a
// screenshot can plausibly hit, so it is worth catching here rather than as a container ERROR.
const MAX_IMAGE_BYTES = 8 * 1024 * 1024;
const MAX_VIDEO_BYTES = 1024 * 1024 * 1024;

function mediaOf(entry) {
  return (entry.media || []).filter((m) => m.file);
}

// Instagram accepts image aspect ratios from 4:5 to 1.91:1 and rejects anything else outright.
// The targets sit just inside the boundary so rounding cannot land on the wrong side of it.
const MIN_RATIO = 0.8;
const MAX_RATIO = 1.91;
const PAD_TO_MIN = 0.81;
const PAD_TO_MAX = 1.9;
const PAD_COLOUR = '#1c1917';

/**
 * Put one file into the shape the network it is going to expects.
 *
 * The split is by media type, not by network. An image only needs work for Instagram - Facebook
 * takes PNG at any shape - but a video needs it for both, because a reel slot is 9:16 on both and a
 * landscape recording dropped into one is shown as a stripe with most of the screen wasted.
 */
async function prepareMedia(item, absolutePath, dryRun, raw, platform) {
  if (item.type === 'photo') return prepareImage(item, absolutePath, dryRun, raw);
  return prepareVideo(item, absolutePath, dryRun, platform);
}

/**
 * Derive the copy of a clip that `platform` wants, next to the master.
 *
 * The master file is never touched. Each network's copy is written beside it as
 * `<name>--<format>.mp4` and reused on the next run, so re-publishing a queue does not re-encode
 * anything. A clip that already matches the target is passed through untouched rather than being
 * re-encoded into slightly worse pixels for no reason.
 */
async function prepareVideo(item, absolutePath, dryRun, platform) {
  const formatName = PLATFORM_FORMAT[platform];
  if (!formatName) return absolutePath;
  const format = FORMATS[formatName];

  const probe = await probeVideo(absolutePath);
  if (!probe) {
    warn(`${basename(absolutePath)}: ffprobe cannot read this file; left as is.`);
    return absolutePath;
  }
  if (!needsTranscode(probe, format)) return absolutePath;

  const target = derivedPath(absolutePath, formatName);
  if (existsSync(target)) {
    item.file = toRelative(target);
    return target;
  }

  if (dryRun) {
    step(`would convert ${basename(absolutePath)} (${probe.width}x${probe.height}) to ${format.label}`);
    return absolutePath;
  }

  await transcodeVideo(absolutePath, target, format);
  const after = await probeVideo(target);
  const cut = after && probe.duration - after.duration > 0.5
    ? `, trimmed to ${format.maxSeconds}s`
    : '';
  step(`converted ${basename(absolutePath)} ${probe.width}x${probe.height} to ${format.label}${cut}`);

  item.file = toRelative(target);
  return target;
}

/**
 * Makes an image something Instagram will actually take: JPEG, and within the accepted shape.
 *
 * Two things get rejected at publish time rather than at upload time, which is the expensive place
 * to find out. A PNG fails on format. A wide screenshot (a UI panel at 3.7:1) or a tall one (a
 * phone capture at 0.52:1) fails on aspect ratio.
 *
 * Cropping to fit would cut the content, and the content is the point - a UI panel with its edges
 * removed shows nothing worth seeing. So the image is padded onto the game's own background colour
 * instead: nothing is lost, and the bars read as part of the design rather than as damage.
 */
async function prepareImage(item, absolutePath, dryRun, raw) {
  // Facebook takes PNG and imposes no aspect ratio, so preparing a file for it means leaving it
  // alone. Padding a screenshot there would add bars for no reason.
  if (raw || item.type !== 'photo') return absolutePath;

  const isJpeg = /\.jpe?g$/i.test(absolutePath);
  const probe = await loadImage(absolutePath);
  const ratio = probe.width / probe.height;
  const needsPadding = ratio < MIN_RATIO || ratio > MAX_RATIO;

  if (isJpeg && !needsPadding) return absolutePath;

  if (dryRun) {
    const what = [!isJpeg && 'convert to JPEG', needsPadding && `pad ${ratio.toFixed(2)}:1 into range`]
      .filter(Boolean)
      .join(' and ');
    step(`would ${what}: ${basename(absolutePath)}`);
    return absolutePath;
  }

  const target = absolutePath.replace(/\.[^.]+$/, needsPadding ? '-ig.jpg' : '.jpg');
  if (!existsSync(target)) {
    let width = probe.width;
    let height = probe.height;
    if (ratio > MAX_RATIO) height = Math.round(probe.width / PAD_TO_MAX);
    if (ratio < MIN_RATIO) width = Math.round(probe.height * PAD_TO_MIN);

    const canvas = createCanvas(width, height);
    const ctx = canvas.getContext('2d');
    // JPEG has no transparency, so the background is needed even without padding: without it the
    // transparent parts of a PNG come out black.
    ctx.fillStyle = PAD_COLOUR;
    ctx.fillRect(0, 0, width, height);
    ctx.drawImage(probe, Math.round((width - probe.width) / 2), Math.round((height - probe.height) / 2));
    writeFileSync(target, canvas.toBuffer('image/jpeg', 92));

    if (needsPadding) {
      step(`padded ${basename(absolutePath)} from ${ratio.toFixed(2)}:1 to ${(width / height).toFixed(2)}:1`);
    } else {
      step(`converted ${basename(absolutePath)} to JPEG`);
    }
  }

  item.file = toRelative(target);
  return target;
}

/**
 * Both hosts do the same job - hand Instagram a URL it can GET without credentials - so they are
 * wrapped to the same two calls and chosen at the command line.
 */
async function openStorage(name) {
  if (name === 'r2') {
    const config = r2Config();
    return {
      label: `R2 bucket ${config.bucket}`,
      put: async (key, body, contentType) => (await putObject(config, key, body, contentType)).url,
    };
  }

  if (name !== 'github') throw new Error(`Unknown storage "${name}". Use github or r2.`);

  const config = githubConfig();
  const release = await ensureRelease(config);
  const existing = await listAssets(config, release.id);
  return {
    label: `GitHub release ${config.repo}@${config.tag}`,
    put: async (key, body, contentType) => {
      // An asset name is unique inside a release, and the name carries the content hash, so an
      // asset that is already there is the same bytes and can be reused as is.
      const known = existing.get(key);
      if (known) return known.browser_download_url;
      const uploaded = await uploadAsset(config, release.id, key, body, contentType);
      return uploaded.url;
    },
  };
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const dryRun = Boolean(args['dry-run']);
  const all = Boolean(args.all);
  const backendName = args.to || env.MEDIA_STORAGE || 'github';
  // --limit is there to try the whole chain on one file before committing 57 MB to it.
  const limit = args.limit ? Number(args.limit) : Infinity;

  // Two review files feed two networks. For images Facebook gets the untouched originals and only
  // Instagram insists on JPEG and a shape; for video both want a 9:16 reel, so `raw` covers images
  // alone and the clip conversion is chosen by platform further down.
  const target = args.source === 'fb' ? 'fb' : 'ig';
  const sourceFile = target === 'fb' ? FB_POSTS_FILE : CAPTIONS_FILE;
  const listKey = target === 'fb' ? 'posts' : 'captions';
  const stateFile = target === 'fb' ? POSTED_FB_FILE : POSTED_FILE;
  const raw = target === 'fb' || Boolean(args.raw);

  const doc = readJson(sourceFile);
  if (!doc) {
    throw new Error(
      `No ${toRelative(sourceFile)}. Run: node ${target === 'fb' ? 'build_fb_posts.mjs' : 'build_captions.mjs'}`
    );
  }

  const uploads = readJson(UPLOADS_FILE, { uploaded: {} });

  // Media of a post that is already out is finished work. Re-preparing and re-uploading it would
  // burn time and storage on files nothing will ever fetch again.
  const posted = readJson(stateFile, { posted: {} });
  const wanted = doc[listKey].filter((entry) => {
    if (entry.status === 'skip') return false;
    if (posted.posted[entry.id]) return false;
    return all || entry.status === 'ok';
  });

  if (!wanted.length) {
    warn(`Nothing to upload: no entry in ${toRelative(sourceFile)} has status "ok" yet.`);
    info('Set status on the entries you want published, or pass --all.');
    return;
  }

  const storage = dryRun ? null : await openStorage(backendName);
  info(dryRun ? `Dry run against ${backendName}` : `Uploading to ${storage.label}`);

  let uploaded = 0;
  let reused = 0;
  let bytes = 0;

  for (const entry of wanted) {
    for (const item of mediaOf(entry)) {
      if (uploaded >= limit) break;
      let file = join(PIPELINE_ROOT, item.file);
      if (!existsSync(file)) {
        warn(`${entry.id}: ${item.file} is missing on disk; skipped.`);
        continue;
      }
      file = await prepareMedia(item, file, dryRun, raw, target === 'fb' ? 'facebook' : 'instagram');

      const size = statSync(file).size;
      const maxBytes = item.type === 'photo' ? MAX_IMAGE_BYTES : MAX_VIDEO_BYTES;
      if (size > maxBytes) {
        warn(`${entry.id}: ${item.file} is ${(size / 1024 / 1024).toFixed(1)} MB, over Instagram's limit; skipped.`);
        continue;
      }

      // Keyed by content, not by name: re-rendering a card gives the same filename different
      // bytes, and Instagram caches aggressively enough that reusing the URL would serve the old
      // image. A changed file becomes a new object.
      const body = readFileSync(file);
      const digest = sha256(body);
      const known = uploads.uploaded[digest];
      if (known && known.backend === backendName) {
        item.url = known.url;
        reused++;
        continue;
      }

      const key = digest.slice(0, 12) + '-' + basename(file);
      if (dryRun) {
        step(`would upload ${item.file} (${(size / 1024).toFixed(0)} KB) as ${key}`);
        uploaded++;
        bytes += size;
        continue;
      }

      const url = await storage.put(key, body, contentTypeFor(file));
      uploads.uploaded[digest] = {
        url,
        key,
        backend: backendName,
        source: item.file,
        size,
        uploaded_at: new Date().toISOString(),
      };
      item.url = url;
      uploaded++;
      bytes += size;
      step(`${item.file} -> ${url}`);
    }
  }

  if (!dryRun) {
    writeJson(UPLOADS_FILE, uploads);
    writeJson(sourceFile, doc);
  }

  info('');
  ok(`${uploaded} file(s) ${dryRun ? 'would be uploaded' : 'uploaded'} (${(bytes / 1024 / 1024).toFixed(1)} MB)`);
  if (reused) info(`  ${reused} already up there, reused`);
  if (dryRun) {
    info('  Dry run: nothing was uploaded and no URLs were written.');
  } else {
    info(`  URLs recorded in ${toRelative(UPLOADS_FILE)} and in ${toRelative(sourceFile)}`);
    info(`  Next: node ${target === 'fb' ? 'publish_fb.mjs' : 'publish.mjs'}   (dry run by default)`);
  }
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
