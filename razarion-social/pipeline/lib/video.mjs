// Video the way each network wants it.
//
// Two shapes cover all four networks. Instagram, Facebook and YouTube Shorts are phone feeds and
// want 9:16; X is read on a desktop far more and wants 16:9. A clip is therefore best recorded
// twice, once in each shape, and a media item carries both masters (`portrait`, `landscape`). Each
// network gets a copy derived from the master of its own shape. When only one master exists it is
// cropped into the other shape - never padded, because bars of any kind read as a frame around a
// video that was not made for the feed.
//
// ffmpeg and ffprobe come from npm rather than from the machine. The pipeline already runs on two
// checkouts and a scheduled task, and "works here, missing there" is the failure this avoids.

import { execFile, execFileSync } from 'node:child_process';
import { existsSync, statSync } from 'node:fs';
import { basename, extname, join } from 'node:path';
import { promisify } from 'node:util';
import ffmpegPath from 'ffmpeg-static';
import ffprobeStatic from 'ffprobe-static';

const execFileAsync = promisify(execFile);

export const FFMPEG = ffmpegPath;
export const FFPROBE = ffprobeStatic.path;

/**
 * What each network actually accepts, as opposed to what it recommends.
 *
 * `maxSeconds` is the hard limit that makes a publish fail rather than look poor - the documented
 * API limits, not the in-app editor's: Instagram publishes reels of 3-90 s through the API even
 * though the app takes three minutes, YouTube files anything vertical up to 180 s as a Short, X
 * takes 140 s without Premium.
 *
 * Every format is filled edge to edge (`cover`): the clip is scaled until it covers the frame and
 * the overhang is cut off both sides equally. The posts that went out with a blurred backdrop
 * behind a landscape clip looked framed, and a flat colour or black looks worse.
 *
 * `revision` goes into the name of the derived file. Derived copies are reused by name, so without
 * it a change to how a format is built would never reach a clip that had been converted before.
 */
export const FORMATS = {
  reel: {
    label: 'reel 9:16',
    width: 1080,
    height: 1920,
    maxSeconds: 90,
    revision: 2,
    // A reel without an audio stream is taken by the API and then plays as a black frame on some
    // clients, and a clip recorded off a canvas has no audio at all.
    needsAudio: true,
  },
  // YouTube decides Short or normal video on the file alone: vertical or square, at most 180 s. It
  // re-encodes whatever it is given, so a portrait master that already fits goes up untouched.
  short: {
    label: 'Short 9:16',
    width: 1080,
    height: 1920,
    maxSeconds: 180,
  },
  // X accepts anything from 1:3 to 3:1 up to 1920x1200, but its timeline is read on a desktop -
  // 24 % of its desktop visitors reach the game against 5 % on mobile - where a portrait clip is a
  // narrow strip in the middle of the column. 16:9 fills it.
  landscape: {
    label: 'landscape 16:9',
    width: 1920,
    height: 1080,
    maxSeconds: 140,
  },
};

/** Which shape each network gets. */
export const PLATFORM_FORMAT = {
  instagram: 'reel',
  facebook: 'reel',
  youtube: 'short',
  x: 'landscape',
};

/** 'portrait' or 'landscape': which of a media item's two masters a format is cut from. */
export function orientationOf(formatName) {
  const format = FORMATS[formatName];
  return format && format.height > format.width ? 'portrait' : 'landscape';
}

/**
 * The master a network's copy is derived from, as a path relative to the pipeline.
 *
 * The master of the matching shape when the item has one; otherwise whatever the item has. Items
 * written before there were two masters carry only `file`, and that is what they keep using.
 */
export function masterFor(item, formatName) {
  return item[orientationOf(formatName)] || item.portrait || item.landscape || item.file;
}

/**
 * How much of a master survives the crop into `formatName`, 0..1 along the side that is cut.
 * 1 means the shapes match and nothing is lost.
 */
export function keptShare(probe, formatName) {
  const format = FORMATS[formatName];
  if (!probe || !format || !probe.width || !probe.height) return 1;
  const source = probe.width / probe.height;
  const target = format.width / format.height;
  return Math.min(source, target) / Math.max(source, target);
}

export function isVideoFile(file) {
  return /\.(mp4|mov|m4v|webm|avi|mkv)$/i.test(file);
}

/**
 * Measure a clip. Returns null for a file ffprobe cannot read, so a broken download is reported as
 * a finding rather than crashing a run halfway through a queue.
 */
export async function probeVideo(file) {
  if (!existsSync(file)) return null;
  let raw;
  try {
    const { stdout } = await execFileAsync(FFPROBE, [
      '-v', 'error',
      '-print_format', 'json',
      '-show_format',
      '-show_streams',
      file,
    ], { maxBuffer: 8 * 1024 * 1024 });
    raw = JSON.parse(stdout);
  } catch {
    return null;
  }

  const video = (raw.streams || []).find((s) => s.codec_type === 'video');
  const audio = (raw.streams || []).find((s) => s.codec_type === 'audio');
  if (!video) return null;

  // Rotated phone captures carry their shape in a side-data matrix rather than in width/height.
  // Reading the stored numbers alone would call a portrait clip landscape and pad it the wrong way.
  const rotation = rotationOf(video);
  const swapped = rotation === 90 || rotation === 270;
  const width = swapped ? video.height : video.width;
  const height = swapped ? video.width : video.height;

  const duration = Number(raw.format?.duration ?? video.duration ?? 0) || 0;

  return {
    file,
    width,
    height,
    ratio: height ? width / height : 0,
    duration,
    rotation,
    videoCodec: video.codec_name || null,
    audioCodec: audio?.codec_name || null,
    hasAudio: Boolean(audio),
    fps: parseFps(video.avg_frame_rate || video.r_frame_rate),
    bytes: statSync(file).size,
  };
}

function rotationOf(stream) {
  const tag = Number(stream.tags?.rotate);
  if (Number.isFinite(tag)) return ((tag % 360) + 360) % 360;
  const matrix = (stream.side_data_list || []).find((s) => s.rotation !== undefined);
  if (matrix) return ((Math.round(matrix.rotation) % 360) + 360) % 360;
  return 0;
}

function parseFps(value) {
  if (!value) return null;
  const [num, den] = String(value).split('/').map(Number);
  if (!den) return num || null;
  return Math.round((num / den) * 100) / 100;
}

/**
 * Find the black bars a clip already carries, so they are not baked into the next frame as well.
 *
 * The archive clips were recorded from a browser window and most of them arrive pillarboxed: the
 * explosion clip is stored 1008x480 but its picture is 640x480 sitting at x=184. Fitting that into
 * 9:16 keeps the bars, and they end up as hard black edges inside the finished frame -
 * which reads as a broken export rather than a framing choice.
 *
 * cropdetect is easy to fool: a genuinely dark frame looks exactly like a letterbox. So the clip is
 * sampled across its length and the widest detection wins, rather than the last one. A dark moment
 * can then only ever suggest a tighter crop than the union, never win the vote, which is why the
 * remaining guard can be loose: real pillarboxing is routinely severe - a 4:3 game view inside a
 * 2.1:1 recording is 36 % bar on its own - so only a crop that keeps under 40 % of a side is
 * refused, on the grounds that nothing legitimate looks like that.
 */
export async function detectContentCrop(file, probe = null) {
  const meta = probe || await probeVideo(file);
  if (!meta || !meta.width || !meta.height) return null;

  // Three samples: after the opening frames, the middle, and near the end. Short clips collapse to
  // the start, which is fine - there is nothing else to look at.
  const points = meta.duration > 2
    ? [meta.duration * 0.15, meta.duration * 0.5, meta.duration * 0.85]
    : [0];

  let left = meta.width, top = meta.height, right = 0, bottom = 0, seen = 0;

  for (const at of points) {
    const { stderr } = await execFileAsync(FFMPEG, [
      '-ss', String(Math.max(0, at)),
      '-i', file,
      '-vf', 'cropdetect=24:2:0',
      '-frames:v', '12',
      '-f', 'null', '-',
    ], { maxBuffer: 8 * 1024 * 1024 }).catch((e) => ({ stderr: e.stderr || '' }));

    for (const line of String(stderr).split('\n')) {
      const m = line.match(/crop=(\d+):(\d+):(\d+):(\d+)/);
      if (!m) continue;
      const [w, h, x, y] = m.slice(1).map(Number);
      if (!w || !h) continue;
      left = Math.min(left, x);
      top = Math.min(top, y);
      right = Math.max(right, x + w);
      bottom = Math.max(bottom, y + h);
      seen++;
    }
  }

  if (!seen) return null;

  // Even numbers only - see the note on H.264 4:2:0 below.
  const w = Math.max(2, Math.floor((right - left) / 2) * 2);
  const h = Math.max(2, Math.floor((bottom - top) / 2) * 2);
  const x = Math.floor(left / 2) * 2;
  const y = Math.floor(top / 2) * 2;

  if (w >= meta.width && h >= meta.height) return null;
  if (w < meta.width * 0.4 || h < meta.height * 0.4) return null;
  // A couple of pixels is encoder noise, not a bar worth a second encode.
  if (meta.width - w < 8 && meta.height - h < 8) return null;

  return { width: w, height: h, x, y };
}

/**
 * The filter chain that turns a clip of any shape into `format`.
 *
 * The clip is scaled until it covers the frame and the overhang is cut off both sides equally, so
 * the middle of the recording - where the studio and director cameras put the action - stays. From
 * a master of the other shape that is only the middle third, which is why every clip should come
 * with a master of each shape and check.mjs says so when one is missing.
 *
 * The even-width rounding is not cosmetic: H.264 4:2:0 cannot encode odd dimensions, and ffmpeg
 * fails the run rather than rounding for you. The format sizes are even already.
 */
function filterFor(format, crop = null) {
  const { width: w, height: h } = format;
  // Strip the bars the source already carries before anything is measured or scaled.
  const source = crop
    ? `[0:v]crop=${crop.width}:${crop.height}:${crop.x}:${crop.y}[src]`
    : null;
  const input = crop ? '[src]' : '[0:v]';

  return [
    source,
    `${input}scale=${w}:${h}:force_original_aspect_ratio=increase,crop=${w}:${h},setsar=1[v]`,
  ].filter(Boolean).join(';');
}

/**
 * Does this clip already satisfy the format, or does it need a pass through ffmpeg?
 *
 * Re-encoding a clip that is already right costs quality for nothing, so the check is deliberately
 * about what the networks reject: the shape, the container, the codecs. A 1080x1920 H.264 file goes
 * up untouched.
 */
export function needsTranscode(probe, format) {
  if (!probe) return true;
  if (!format) return false;
  const shapeOff = probe.width !== format.width || probe.height !== format.height;
  const codecOff = probe.videoCodec !== 'h264';
  // A missing audio track is as much a reason to re-encode as a wrong one where the format needs
  // one - see `needsAudio` on the reel. Everything else here would have said "already fine".
  const audioOff = probe.hasAudio
    ? probe.audioCodec !== 'aac'
    : Boolean(format.needsAudio);
  const tooLong = format.maxSeconds && probe.duration > format.maxSeconds + 0.5;
  // Bars the source carries are only worth an encode when the frame is being rebuilt anyway; on
  // their own they are a cosmetic gain that costs a generation of quality.
  return shapeOff || codecOff || audioOff || tooLong;
}

/**
 * Write `input` into `format` at `output`.
 *
 * `-movflags +faststart` moves the index to the front of the file. Instagram and Facebook fetch the
 * file over HTTP and start reading before the download finishes; with the index at the end they
 * report a generic processing error and there is nothing in the response to say why.
 */
export async function transcodeVideo(input, output, format, { maxSeconds = null, crop = undefined } = {}) {
  const limit = maxSeconds ?? format.maxSeconds ?? null;
  const args = ['-y', '-i', input];

  const probe = await probeVideo(input);
  // `crop: null` is an explicit "leave the bars alone"; undefined means "work it out".
  const bars = crop === undefined ? await detectContentCrop(input, probe) : crop;
  const silent = probe && !probe.hasAudio && format.needsAudio;
  if (silent) args.push('-f', 'lavfi', '-i', 'anullsrc=channel_layout=stereo:sample_rate=44100');

  args.push(
    '-filter_complex', filterFor(format, bars),
    '-map', '[v]',
    '-map', silent ? '1:a' : '0:a?',
    '-c:v', 'libx264',
    '-profile:v', 'high',
    '-pix_fmt', 'yuv420p',
    '-preset', 'medium',
    '-crf', '21',
    '-r', '30',
    '-c:a', 'aac',
    '-b:a', '128k',
    '-movflags', '+faststart',
  );

  if (limit) args.push('-t', String(limit));
  if (silent) args.push('-shortest');
  args.push(output);

  await execFileAsync(FFMPEG, args, { maxBuffer: 32 * 1024 * 1024 });
  return output;
}

/**
 * The copy of `master` that `formatName` wants: the master itself when it already fits, otherwise a
 * derived file next to it, made once and reused on every later run.
 *
 * Returns `{ file, probe, converted }`; `file` is null when ffprobe cannot read the master. A dry run
 * reports what it would do through `onStep` and hands back the master.
 */
export async function deriveClip(master, formatName, { dryRun = false, onStep = () => {} } = {}) {
  const format = FORMATS[formatName];
  const probe = await probeVideo(master);
  if (!probe) return { file: null, probe: null, converted: false };
  if (!format || !needsTranscode(probe, format)) return { file: master, probe, converted: false };

  const target = derivedPath(master, formatName);
  if (existsSync(target)) return { file: target, probe, converted: false };

  if (dryRun) {
    onStep(`would convert ${basename(master)} (${probe.width}x${probe.height}) to ${format.label}`);
    return { file: master, probe, converted: false };
  }

  await transcodeVideo(master, target, format);
  const after = await probeVideo(target);
  const cut = after && probe.duration - after.duration > 0.5 ? `, trimmed to ${format.maxSeconds}s` : '';
  onStep(`converted ${basename(master)} ${probe.width}x${probe.height} to ${format.label}${cut}`);
  return { file: target, probe, converted: true };
}

/** Where a derived copy lives: alongside the master, named after the format it was made for. */
export function derivedPath(input, formatName) {
  const dir = join(input, '..');
  const stem = basename(input, extname(input));
  const revision = FORMATS[formatName]?.revision;
  return join(dir, `${stem}--${formatName}${revision ? `-v${revision}` : ''}.mp4`);
}

/** ffprobe on the command line, for the odd one-off question. Throws on an unreadable file. */
export function probeSync(file) {
  return execFileSync(FFPROBE, ['-v', 'error', '-show_format', '-show_streams', '-print_format', 'json', file])
    .toString();
}
