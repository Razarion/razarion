// Video the way each network wants it.
//
// The clips are recorded once, in whatever shape the game window had, and every network then wants
// a different one. Instagram and Facebook show reels at 9:16 and letterbox anything else into a
// sliver; X takes the landscape recording as it is; YouTube sorts a clip into Shorts or the main
// feed purely by its aspect ratio. So the source file is the master and each network gets its own
// derived copy, rather than one compromise file that looks wrong in three places.
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

// The game's own background, the same colour the image cards and the padded screenshots use. Bars
// in this colour read as part of the design; black bars read as a mistake.
export const PAD_COLOUR = '#1c1917';

/**
 * What each network actually accepts, as opposed to what it recommends.
 *
 * `ratio` is the target shape. `maxSeconds` is the hard limit that makes a publish fail rather than
 * look poor - the numbers are the documented API limits, not the in-app editor's. `fill` decides
 * what happens to the space when the source has a different shape: reels lose too much of the frame
 * to plain bars (a 2:1 recording inside 9:16 leaves 70 % of the screen empty), so they get the
 * blurred backdrop that every phone-first feed uses. The wider formats keep the flat colour, where
 * the bars are thin enough to pass for framing.
 */
export const FORMATS = {
  reel: {
    label: 'reel 9:16',
    width: 1080,
    height: 1920,
    maxSeconds: 90,
    fill: 'blur',
    needsAudio: true,
  },
  square: {
    label: 'square 1:1',
    width: 1080,
    height: 1080,
    maxSeconds: 90,
    fill: 'colour',
    needsAudio: true,
  },
  // X has a slot rather than a shape: anything from 1:3 to 3:1 is shown at its own proportions, so
  // there is no fixed size to convert to. Forcing 16:9 on a 1.19:1 recording would add bars to a
  // clip the timeline would have shown whole - the same reason the image path only pads what falls
  // outside the accepted range. `maxWidth`/`maxHeight` are the ceiling X will accept, not a target.
  native: {
    label: 'X native',
    maxWidth: 1920,
    maxHeight: 1200,
    minRatio: 1 / 3,
    maxRatio: 3,
    maxSeconds: 140,
    fill: 'colour',
  },
};

/**
 * The pixel size a clip of `sourceWidth`x`sourceHeight` ends up at in `format`.
 *
 * A fixed-shape format answers with its own numbers. A slot format keeps the source proportions,
 * shrinks them under the ceiling, and only reaches for padding when the shape itself is outside
 * what the network accepts. Everything is rounded to even numbers: H.264 4:2:0 cannot encode odd
 * dimensions and ffmpeg fails the run rather than rounding for you.
 */
export function targetSize(format, sourceWidth, sourceHeight) {
  const even = (n) => Math.max(2, Math.round(n / 2) * 2);
  if (format.width && format.height) {
    return { width: format.width, height: format.height, pad: true };
  }

  let w = sourceWidth;
  let h = sourceHeight;
  const scale = Math.min(format.maxWidth / w, format.maxHeight / h, 1);
  w = even(w * scale);
  h = even(h * scale);

  const ratio = w / h;
  if (ratio < format.minRatio) return { width: even(h * format.minRatio), height: h, pad: true };
  if (ratio > format.maxRatio) return { width: w, height: even(w / format.maxRatio), pad: true };
  return { width: w, height: h, pad: false };
}

/**
 * Which shape each network gets.
 *
 * Instagram and Facebook are phone-first and bury anything that is not a reel, so both get 9:16.
 * X shows video inline in a timeline read mostly on desktop - the numbers say 24 % of its desktop
 * visitors reach the game against 5 % on mobile - so it keeps the landscape recording. YouTube
 * decides Shorts by aspect ratio alone, which makes the choice there a content decision rather than
 * a technical one; `null` means "leave the master alone" and lets build_yt_posts.mjs label it.
 */
export const PLATFORM_FORMAT = {
  instagram: 'reel',
  facebook: 'reel',
  x: 'native',
  youtube: null,
};

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
 * 9:16 keeps the bars, and they end up as hard black edges in the middle of the blurred backdrop -
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
 * Nothing is ever cropped. The clips show a game UI and a battlefield, and cutting the edges off to
 * fill a phone screen removes the part worth watching. The frame is filled instead - blurred copy
 * of the clip behind for reels, flat colour for the rest - and the whole recording stays visible in
 * the middle at its own shape.
 *
 * The even-width rounding is not cosmetic: H.264 4:2:0 cannot encode odd dimensions, and ffmpeg
 * fails the run rather than rounding for you.
 */
function filterFor(format, crop = null, size = null) {
  const { width: w, height: h } = size || format;
  const fill = format.fill;
  const fit = `scale=${w}:${h}:force_original_aspect_ratio=decrease,scale=trunc(iw/2)*2:trunc(ih/2)*2`;
  // Strip the bars the source already carries before anything is measured or scaled.
  const source = crop
    ? `[0:v]crop=${crop.width}:${crop.height}:${crop.x}:${crop.y}[src]`
    : null;
  const input = crop ? '[src]' : '[0:v]';

  if (fill === 'blur') {
    return [
      source,
      `${input}split=2[bg][fg]`,
      `[bg]scale=${w}:${h}:force_original_aspect_ratio=increase,crop=${w}:${h},gblur=sigma=24[bgb]`,
      `[fg]${fit}[fgs]`,
      `[bgb][fgs]overlay=(W-w)/2:(H-h)/2,setsar=1[v]`,
    ].filter(Boolean).join(';');
  }

  return [
    source,
    `${input}${fit},pad=${w}:${h}:(ow-iw)/2:(oh-ih)/2:${PAD_COLOUR},setsar=1[v]`,
  ].filter(Boolean).join(';');
}

/**
 * Does this clip already satisfy the format, or does it need a pass through ffmpeg?
 *
 * Re-encoding a clip that is already right costs quality for nothing, so the check is deliberately
 * about what the networks reject: the shape, the container, the codecs. A 1080x1920 H.264 file goes
 * up untouched.
 */
export function needsTranscode(probe, format, crop = null) {
  if (!probe) return true;
  if (!format) return false;
  const w = crop ? crop.width : probe.width;
  const h = crop ? crop.height : probe.height;
  const size = targetSize(format, w, h);
  const shapeOff = w !== size.width || h !== size.height;
  const codecOff = probe.videoCodec !== 'h264';
  // A missing audio track is as much a reason to re-encode as a wrong one. A clip recorded off a
  // canvas has no audio at all, and a reel without an audio stream is taken by the API and then
  // plays as a black frame on some clients - so the silent track transcodeVideo adds is not a
  // nicety. Everything else here would have said "already fine" and sent it straight up.
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
 *
 * A silent AAC track is added when the source has none. A reel without an audio stream is accepted
 * by the API and then plays as a black frame on some clients - the recordings come from a browser
 * tab and frequently have no audio at all.
 */
export async function transcodeVideo(input, output, format, { maxSeconds = null, crop = undefined } = {}) {
  const limit = maxSeconds ?? format.maxSeconds ?? null;
  const args = ['-y', '-i', input];

  const probe = await probeVideo(input);
  // `crop: null` is an explicit "leave the bars alone"; undefined means "work it out".
  const bars = crop === undefined ? await detectContentCrop(input, probe) : crop;
  const size = probe
    ? targetSize(format, bars ? bars.width : probe.width, bars ? bars.height : probe.height)
    : null;
  const silent = probe && !probe.hasAudio;
  if (silent) args.push('-f', 'lavfi', '-i', 'anullsrc=channel_layout=stereo:sample_rate=44100');

  args.push(
    '-filter_complex', filterFor(format, bars, size),
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

/** Where a derived copy lives: alongside the master, named after the format it was made for. */
export function derivedPath(input, formatName) {
  const dir = join(input, '..');
  const stem = basename(input, extname(input));
  return join(dir, `${stem}--${formatName}.mp4`);
}

/** ffprobe on the command line, for the odd one-off question. Throws on an unreadable file. */
export function probeSync(file) {
  return execFileSync(FFPROBE, ['-v', 'error', '-show_format', '-show_streams', '-print_format', 'json', file])
    .toString();
}
