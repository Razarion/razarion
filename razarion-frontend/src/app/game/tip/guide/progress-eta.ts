/**
 * How long a wait still lasts, from how fast it went so far: the builder's distance to the spot
 * shrinking, a site's buildup growing. Measured rather than computed from item types - the client
 * knows neither the builder's speed nor how fast a building goes up, and what was measured already
 * includes whatever slows it down.
 */
export interface ProgressSample {
  /** Milliseconds. */
  t: number;
  value: number;
}

/** Long enough to smooth out the half-second steps, short enough to follow a change of pace. */
export const ETA_WINDOW_MILLIS = 4000;
/** Below this the rate is a guess from two neighbouring samples. */
export const ETA_MIN_SPAN_MILLIS = 1000;

/**
 * Seconds until value reaches target at the rate of the recent samples, or null while that cannot
 * be told yet: too short a history, or no progress toward the target.
 */
export function remainingSeconds(samples: ProgressSample[], target: number): number | null {
  if (samples.length < 2) {
    return null;
  }
  const last = samples[samples.length - 1];
  const first = samples.find(sample => last.t - sample.t <= ETA_WINDOW_MILLIS) ?? samples[0];
  const span = last.t - first.t;
  if (span < ETA_MIN_SPAN_MILLIS) {
    return null;
  }
  const rate = (last.value - first.value) / span;
  const remaining = target - last.value;
  if (rate === 0 || Math.sign(rate) !== Math.sign(remaining)) {
    return remaining === 0 ? 0 : null;
  }
  return remaining / rate / 1000;
}

/** The label of the wait: "On the way · 12 s", "Building · 4 s", or without a time while unknown. */
export function progressText(phase: 'driving' | 'building', seconds: number | null, buildup: number): string {
  if (phase === 'driving') {
    return seconds !== null ? `On the way · ${Math.max(1, Math.ceil(seconds))} s` : 'On the way';
  }
  return seconds !== null
    ? `Building · ${Math.max(1, Math.ceil(seconds))} s`
    : `Building · ${Math.round(Math.max(0, Math.min(1, buildup)) * 100)} %`;
}
