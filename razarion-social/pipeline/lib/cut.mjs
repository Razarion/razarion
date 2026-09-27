// Cuts the part of a take where something happens.
//
// A staged battle is filmed for 40 s: the camera settles, the strike force drives in, the fight
// takes a dozen seconds, and then there is a smoking base. The posts that did well were 13-16 s
// of the fight alone, cut by hand. This finds that window without a person.
//
// How: ffmpeg's scene score - how different each frame is from the one before - sampled ten times
// a second on a small copy. Explosions, fire and units crossing the frame score high; a still base
// scores near nothing. The window with the highest total wins. The follow camera moves too, but it
// moves because the fighting does, so it points the same way.
//
// The same number is the quality check: a take whose best window is nearly still shows an empty
// square (a crashed worker, a strike force that never arrived), and is refused rather than posted.

import { execFile } from 'node:child_process';
import { promisify } from 'node:util';
import { FFMPEG, probeVideo } from './video.mjs';

const execFileAsync = promisify(execFile);

const SAMPLES_PER_SECOND = 10;

/** Per-sample scene scores as [{ t, score }], t in seconds. */
export async function motionProfile(file) {
  const { stdout } = await execFileAsync(FFMPEG, [
    '-v', 'error',
    '-i', file,
    '-vf', `fps=${SAMPLES_PER_SECOND},scale=240:-2,select='gte(scene,0)',metadata=print:file=-`,
    '-an', '-f', 'null', '-',
  ], { maxBuffer: 64 * 1024 * 1024 });

  const samples = [];
  let t = null;
  for (const line of stdout.split('\n')) {
    const time = line.match(/pts_time:([\d.]+)/);
    if (time) {
      t = Number(time[1]);
      continue;
    }
    const score = line.match(/lavfi\.scene_score=([\d.]+)/);
    if (score && t !== null) samples.push({ t, score: Number(score[1]) });
  }
  return samples;
}

/**
 * The `length`-second window with the most motion. `motion` is its mean scene score, the number
 * the quality check compares; `peak` is the busiest second inside it.
 */
export function bestWindow(samples, length) {
  if (!samples.length) return null;
  const duration = samples[samples.length - 1].t;
  if (duration <= length) {
    return { start: 0, length: duration, motion: mean(samples.map((s) => s.score)) };
  }
  let best = null;
  // Half-second steps are fine enough: nobody sees where a 15 s cut starts to the tenth.
  for (let start = 0; start + length <= duration; start += 0.5) {
    const inside = samples.filter((s) => s.t >= start && s.t < start + length).map((s) => s.score);
    const motion = mean(inside);
    if (!best || motion > best.motion) best = { start, length, motion };
  }
  return best;
}

// Measured on the September takes: a still base scores 0.000-0.002 a second, a fight 0.01-0.04.
const ACTIVE_SECOND = 0.005;
/** A cut whose mean is below this shows next to nothing happening. */
export const MIN_MOTION = 0.008;

/**
 * The cut: the busiest `around` seconds, trimmed to the seconds in them that move - half a second
 * before the first, a second and a half after the last, so an explosion is not cut off mid-cloud -
 * and held between `min` and `max`. Null when nothing in the take moves enough to be worth a post.
 */
export function actionWindow(samples, { min = 8, max = 18, around = 15 } = {}) {
  const region = bestWindow(samples, around);
  if (!region) return null;
  const duration = samples[samples.length - 1].t;

  const seconds = new Map();
  for (const s of samples) {
    if (s.t < region.start || s.t >= region.start + region.length) continue;
    const k = Math.floor(s.t);
    seconds.set(k, [...(seconds.get(k) ?? []), s.score]);
  }
  const active = [...seconds].filter(([, v]) => mean(v) >= ACTIVE_SECOND).map(([k]) => k);
  if (!active.length) return null;

  let start = Math.max(0, Math.min(...active) - 0.5);
  let end = Math.min(duration, Math.max(...active) + 1 + 1.5);
  while (end - start < min && (start > 0 || end < duration)) {
    start = Math.max(0, start - 0.5);
    end = Math.min(duration, end + 0.5);
  }
  if (end - start > max) end = start + max;

  const motion = mean(samples.filter((s) => s.t >= start && s.t < end).map((s) => s.score));
  if (motion < MIN_MOTION) return null;
  return { start, length: end - start, motion };
}

/** Where record_director.mjs puts the tally of a take: next to it, as <take>.fight.json. */
export function fightFile(take) {
  return take.replace(/\.mp4$/i, '.fight.json');
}

/** Fewer things destroyed than this in a whole take is not a fight worth a post. */
export const MIN_KILLS = 3;

/**
 * The cut from the tally record_director.mjs kept while filming: from `lead` seconds before the
 * first thing destroyed - the strike force arriving - to `tail` after the last, between `min` and
 * `max` long. When the killing spreads wider than `max`, the stretch of it with the most losses.
 *
 * Losses on both sides count: a strike force shot to pieces by turrets is a fight too. A base the
 * bots rebuild only goes up, which is never counted. Null when fewer than MIN_KILLS died.
 */
export function fightWindow(tally, duration, { min = 8, max = 18, lead = 4, tail = 2 } = {}) {
  const events = [];
  const s = tally?.samples ?? [];
  for (let i = 1; i < s.length; i++) {
    const kills = Math.max(0, s[i - 1].target - s[i].target) + Math.max(0, s[i - 1].own - s[i].own);
    if (kills) events.push({ t: s[i].t, kills });
  }
  const total = events.reduce((n, e) => n + e.kills, 0);
  if (total < MIN_KILLS) return null;

  let start = events[0].t - lead;
  let end = events[events.length - 1].t + tail;
  if (end - start > max) {
    // The densest stretch: each event in turn opens a window of `max`, the one holding most wins.
    let best = null;
    for (const e of events) {
      const from = e.t - lead;
      const kills = events.filter((x) => x.t >= e.t && x.t <= from + max - tail).reduce((n, x) => n + x.kills, 0);
      if (!best || kills > best.kills) best = { from, kills };
    }
    start = best.from;
    end = start + max;
  }
  start = Math.max(0, start);
  end = Math.min(duration, end);
  while (end - start < min && (start > 0 || end < duration)) {
    start = Math.max(0, start - 0.5);
    end = Math.min(duration, end + 0.5);
  }
  const inside = events.filter((e) => e.t >= start && e.t <= end).reduce((n, e) => n + e.kills, 0);
  return { start, length: end - start, kills: inside, total };
}

function mean(values) {
  return values.length ? values.reduce((a, b) => a + b, 0) / values.length : 0;
}

/**
 * Re-encodes [start, start + length) of `input` into `output`. Re-encoded rather than copied: a
 * stream copy can only start on a keyframe, which in a browser recording can be seconds away.
 */
export async function cutClip(input, output, { start, length }) {
  await execFileAsync(FFMPEG, [
    '-y', '-v', 'error',
    '-ss', start.toFixed(2),
    '-i', input,
    '-t', length.toFixed(2),
    '-an',
    '-c:v', 'libx264', '-preset', 'medium', '-crf', '18', '-pix_fmt', 'yuv420p',
    '-movflags', '+faststart',
    output,
  ], { maxBuffer: 16 * 1024 * 1024 });
  const probe = await probeVideo(output);
  if (!probe || probe.duration < length * 0.9) throw new Error(`The cut of ${input} came out broken.`);
  return probe;
}
