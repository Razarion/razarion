import {Injectable} from '@angular/core';
import {FirstInteractionTrackerService} from './first-interaction-tracker.service';

/**
 * What a game start actually downloads, and when.
 *
 * {@link StartupTiming} splits the seconds before the game into downloading, parsing and booting.
 * It cannot say what was being downloaded, and that turns out to be the larger half of the
 * question: of roughly 18.8 MB per start, only 2.4 MB of JavaScript and 1.3 MB of WebAssembly are
 * accounted for by anything we can weigh in the repository. The remaining four fifths are models,
 * textures, audio and materials, all of which live in the database and none of which has ever been
 * measured.
 *
 * The reason it matters: an hour spent on the JavaScript is an hour spent on 13% of the payload.
 * Converting the fifty-four Babylon barrel imports to deep imports - the largest single saving
 * available on the code side - is worth about 1 MB, which is 5% of a start. If a third of the bytes
 * turn out to be textures nobody looks at in the first minute, that is a far cheaper repair, and
 * nothing so far could tell the two apart.
 *
 * Three snapshots, because "how much" and "how much before the player can act" are different
 * numbers:
 * <ul>
 * <li><b>playable</b> - the first engine tick. Everything counted here had to arrive before the
 *     game could run, and is therefore blocking by definition.</li>
 * <li><b>t20</b>, <b>t60</b> - twenty and sixty seconds after Angular started, on a plain timer.
 *     What arrives after "playable" is streaming, not startup, and the gap between the two is the
 *     budget a progressive start could work with.</li>
 * </ul>
 *
 * The timers deliberately do not depend on the engine: the sessions this exists for are the ones
 * where the engine never runs, and those still download. A missing <b>playable</b> next to a
 * present <b>t20</b> is itself the finding.
 *
 * Read the later snapshots as survivors' numbers. Someone who leaves at seventeen seconds reports
 * t20 and never t60, so t60 describes the patient, not the average.
 */
export interface StartupPayload {
  /** Milliseconds since navigation start, so the three snapshots can be told apart. */
  ms: number;
  fileCount: number;
  /** What crossed the wire, headers included. */
  transferredKb: number;
  /** Bodies that cost nothing because they came from the cache. Not part of transferredKb. */
  cachedKb: number;
  /** Bytes and file count per category, in the order of {@link CATEGORIES}. Empty ones are left out. */
  byCategory: Map<string, {kb: number, files: number}>;
  /** The page was in the background when this was taken - it is a download, not an experience. */
  hidden: boolean;
  /**
   * The heaviest resource that matched no category, and what it cost.
   * <p>
   * `other` was 2.5 MB of the 15 MB a player waits for - 16% of the blocking payload, and the
   * measurement could not say what any of it was. A category list is a guess about what a start
   * fetches; this is the part of the answer that checks the guess. If one path accounts for most
   * of `other`, it belongs in {@link CATEGORIES}; if nothing does, `other` is genuinely many small
   * things and can be left alone.
   */
  largestOther: string | null;
  largestOtherKb: number;
  /**
   * Resource timing dropped entries, so every number here is a floor rather than a total.
   * <p>
   * The buffer holds 250 entries by default and a start fetches 675 chunks of JavaScript alone.
   * The first version of this measurement had no such flag and reported 1.1 MB with nothing
   * arriving after the game was playable - wrong, and indistinguishable from a real answer.
   * index.html raises the limit before anything is fetched; this says whether that was enough.
   */
  truncated: boolean;
}

/**
 * First match wins, so the order is the meaning: a `.js` under `/rest` is still JavaScript, and a
 * texture is a texture whether it is served from `/rest/image` or lands with a `.webp` on the end.
 * Anything unrecognised is counted rather than dropped - a category that stays at zero while
 * `other` grows is a mistake in this list, and one that can be seen.
 */
const CATEGORIES: { key: string, test: RegExp }[] = [
  {key: 'js', test: /\.js(\?|$)/},
  {key: 'css', test: /\.css(\?|$)/},
  {key: 'wasm', test: /\.wasm(\?|$)/},
  {key: 'gltf', test: /\/rest\/gltf/},
  {key: 'mat', test: /\/rest\/babylon-material/},
  {key: 'audio', test: /\/rest\/audio/},
  {key: 'img', test: /\/rest\/image|\.(webp|png|jpe?g|svg|gif|ico)(\?|$)/}
];

function categorise(url: string): string {
  for (const category of CATEGORIES) {
    if (category.test.test(url)) {
      return category.key;
    }
  }
  return 'other';
}

/**
 * A resource URL reduced to something that fits in a telemetry detail and names the thing.
 * <p>
 * The query string goes: it carries cache busters and ids that would make every session's answer
 * look different. Commas and equals signs go because the detail is read back by splitting on them.
 * The host is kept only when it is not ours - a third party in the blocking path is worth seeing.
 */
function label(url: string, origin: string): string {
  let rest = url.split('?')[0].split('#')[0];
  rest = rest.startsWith(origin) ? rest.slice(origin.length) : rest.replace(/^https?:\/\//, '');
  return rest.replace(/[,=]/g, '_').slice(0, 80) || '/';
}

export function collectStartupPayload(perf: Performance = performance,
                                      doc: Document = document,
                                      origin: string = location.origin): StartupPayload | null {
  try {
    const entries = perf.getEntriesByType('resource') as PerformanceResourceTiming[];
    const byCategory = new Map<string, { kb: number, files: number }>();
    let transferred = 0;
    let cached = 0;
    let largestOther: string | null = null;
    let largestOtherBytes = 0;

    for (const entry of entries) {
      // A body that cost nothing on the wire came from the cache. A response with no body at all -
      // a 204, or a cross-origin one without Timing-Allow-Origin - reports zero for both and is
      // counted as a file with no bytes rather than guessed at.
      const wire = entry.transferSize || 0;
      const body = entry.encodedBodySize || 0;
      const fromCache = !wire && body > 0;

      transferred += wire;
      if (fromCache) {
        cached += body;
      }

      const key = categorise(entry.name);
      const bytes = fromCache ? body : wire;
      const bucket = byCategory.get(key) ?? {kb: 0, files: 0};
      bucket.kb += bytes;
      bucket.files++;
      byCategory.set(key, bucket);

      if (key === 'other' && bytes > largestOtherBytes) {
        largestOtherBytes = bytes;
        largestOther = entry.name;
      }
    }

    for (const [key, bucket] of byCategory) {
      bucket.kb = Math.round(bucket.kb / 1024);
      if (!bucket.kb && !bucket.files) {
        byCategory.delete(key);
      }
    }

    return {
      ms: Math.round(perf.now()),
      fileCount: entries.length,
      transferredKb: Math.round(transferred / 1024),
      cachedKb: Math.round(cached / 1024),
      byCategory,
      hidden: doc.visibilityState === 'hidden',
      truncated: !!(window as any).RAZ_resourceBufferFull,
      largestOther: largestOther ? label(largestOther, origin) : null,
      largestOtherKb: Math.round(largestOtherBytes / 1024)
    };
  } catch (ignored) {
    // Resource timing is not worth a failed start.
    return null;
  }
}

/** The pairs are read back split on commas and equals signs, so no value may contain either. */
export function formatStartupPayload(at: string, payload: StartupPayload): string {
  const pairs = [
    'at=' + at,
    'ms=' + payload.ms,
    'files=' + payload.fileCount,
    'kb=' + payload.transferredKb,
    'cachedKb=' + payload.cachedKb
  ];
  if (payload.hidden) {
    pairs.push('hidden=1');
  }
  // A floor, not a total. Read before the category numbers, or they will be believed.
  if (payload.truncated) {
    pairs.push('truncated=1');
  }
  // Only the categories that carried something, so the pairs stay readable by eye. A category that
  // is genuinely empty says nothing that its absence does not.
  for (const category of CATEGORIES.concat({key: 'other', test: /(?:)/})) {
    const bucket = payload.byCategory.get(category.key);
    if (bucket && bucket.files) {
      pairs.push(category.key + 'Kb=' + bucket.kb, category.key + 'N=' + bucket.files);
    }
  }
  // What the biggest unrecognised resource was, so `other` is a question with an answer rather
  // than a number nobody can act on.
  if (payload.largestOther) {
    pairs.push('otherTop=' + payload.largestOther, 'otherTopKb=' + payload.largestOtherKb);
  }
  return pairs.join(',');
}

/**
 * Takes the three snapshots. Root-provided because two unrelated places drive it: the game
 * component starts the timers, and the renderer reports the first engine tick.
 */
@Injectable({
  providedIn: 'root'
})
export class StartupPayloadProbe {
  /** The tick fires sixty times a second; the first one is the only one that means anything here. */
  private playableReported = false;
  private started = false;

  constructor(private readonly tracker: FirstInteractionTrackerService) {
  }

  /**
   * Schedules the two timed snapshots. Called once, from the earliest point Angular can speak -
   * deliberately not from the renderer, because a session whose engine never starts is exactly the
   * one whose downloads we want to see.
   */
  public start(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.snapshotIn(20000, 't20');
    this.snapshotIn(60000, 't60');
  }

  /** The engine produced its first tick: everything downloaded so far was blocking. */
  public onGamePlayable(): void {
    if (this.playableReported) {
      return;
    }
    this.playableReported = true;
    this.report('playable');
  }

  private snapshotIn(delayMs: number, at: string): void {
    try {
      window.setTimeout(() => this.report(at), delayMs);
    } catch (ignored) {
      // Telemetry is never a reason for a game not to start.
    }
  }

  private report(at: string): void {
    try {
      const payload = collectStartupPayload();
      if (payload) {
        this.tracker.report('STARTUP_PAYLOAD', formatStartupPayload(at, payload));
      }
    } catch (ignored) {
      // Telemetry is never a reason for a game not to start.
    }
  }
}
