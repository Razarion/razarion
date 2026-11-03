import { execFile } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { extname, join } from 'node:path';
import { chromium } from 'playwright-core';
import ffprobe from 'ffprobe-static';
import { ReplaySession, sessionToken } from './replay-server.mjs';

// DirectorService.RECORD_PRE_ROLL_MS: the take begins this long after recordStart, on a camera
// that has held still while the ground streamed in.
const PRE_ROLL_MS = 4_000;
// The take is filmed once the tape holds all of it, and a little more.
const SETTLE_MS = 3_000;
const READY_TIMEOUT_MS = 90_000;
const HANDOFF_RETRY_MS = 10 * 60_000;
const KEEP = 100;
const SHAPES = { portrait: [1080, 1920], landscape: [1920, 1080] };
// What razarion-social's record_director.mjs refuses as well: a take below this is a slide show.
const MIN_FPS = 20;
const MIN_SHARE = 0.8;
// The social pipeline's flags (lib/browser.mjs): no throttled timers, and a viewport of exactly
// the size asked for.
const LAUNCH_ARGS = [
  '--disable-backgrounding-occluded-windows',
  '--disable-renderer-backgrounding',
  '--disable-background-timer-throttling',
  '--force-device-scale-factor=1',
  '--hide-scrollbars',
];

/** Length and frame rate of a take, from ffprobe. */
function probe(file) {
  return new Promise((resolve, reject) => {
    execFile(ffprobe.path, ['-v', 'error', '-select_streams', 'v:0', '-show_entries', 'format=duration:stream=nb_read_packets', '-count_packets', '-of', 'json', file], (error, stdout) => {
      if (error) return reject(error);
      const out = JSON.parse(stdout);
      const duration = Number(out.format.duration);
      const frames = Number(out.streams?.[0]?.nb_read_packets ?? 0);
      resolve({ duration, fps: duration > 0 ? frames / duration : 0 });
    });
  });
}
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const at = (row, now) => (row.serverTime ? Date.parse(row.serverTime) || now : now);
const stamp = (t) => new Date(t).toISOString().replace(/[-:T]/g, '').slice(0, 14);

/** Whether a history row is what a rule waits for. actor/target: 'human' or 'bot'; the rest must equal. */
export function matches(when, row) {
  for (const [key, wanted] of Object.entries(when || {})) {
    if (key === 'actor') {
      const human = row.source === 'HUMAN' && !!row.userId && row.botId == null;
      if (wanted === 'human' ? !human : row.botId == null) return false;
    } else if (key === 'target') {
      const human = !!row.targetUserId;
      if (wanted === 'human' ? !human : human || row.targetBaseId == null) return false;
    } else if (row[key] !== wanted) {
      return false;
    }
  }
  return true;
}

/** The base a moment follows: the human side of the row. */
export function followedBase(when, row) {
  if (when?.target === 'human' && when?.actor !== 'human') return { baseId: row.targetBaseId ?? null, userId: row.targetUserId ?? null };
  return { baseId: row.baseId ?? row.targetBaseId ?? null, userId: row.userId ?? row.targetUserId ?? null };
}

/**
 * Moments that become shorts. A history row that a rule (rules.json) waits for marks one; once the
 * tape holds all of it, it is filmed - the stretch replayed in a headless game client through the
 * replay proxy, the camera on the player's base, the canvas recorded by the director's recorder -
 * and handed to the social engine (POST /api/moments on 127.0.0.1:4711), which cuts, words and
 * posts it like its own clips.
 *
 * One at a time: a take is filmed in real time, on the GPU of this machine.
 */
export class Moments {
  constructor({ dir, rulesFile, tape, players, sessions, settings, log }) {
    this.dir = dir;
    this.rulesFile = rulesFile;
    this.tape = tape;
    this.players = players;
    this.sessions = sessions;
    this.settings = settings;
    this.log = log;
    this.file = join(dir, 'moments.json');
    mkdirSync(dir, { recursive: true });
    this.items = existsSync(this.file) ? JSON.parse(readFileSync(this.file, 'utf8')).items || [] : [];
    this.busy = false;
  }

  save() {
    this.items = this.items.slice(-KEEP);
    writeFileSync(this.file, JSON.stringify({ items: this.items }, null, 1));
  }

  rules() {
    try {
      return JSON.parse(readFileSync(this.rulesFile, 'utf8')).rules || [];
    } catch (error) {
      this.log(`moments: rules not readable (${error.message})`);
      return [];
    }
  }

  /** Every history row the feed brings: does a rule want it? */
  onHistory(row, now) {
    for (const rule of this.rules()) {
      if (!rule.enabled || !matches(rule.when, row)) continue;
      const { baseId, userId } = followedBase(rule.when, row);
      if (baseId == null) continue;
      const t = at(row, now);
      // The feed hands over two hours of rows on start; a moment from before the tape is lost.
      if (this.tape.oldest == null || t - rule.before * 1000 < this.tape.oldest) continue;
      const recent = this.items.filter((m) => m.rule === rule.id);
      if (rule.cooldownMinutes && recent.some((m) => t - m.at < rule.cooldownMinutes * 60_000)) continue;
      if (rule.oncePerBase && recent.some((m) => m.baseId === baseId)) continue;
      this.add(rule, { baseId, userId, at: t, by: 'rule' });
    }
  }

  /**
   * A moment picked by hand (2D player, "Short from here"): a rule by id, or the plain one. Its
   * text is a placeholder - moments wait on the engine's review page unless it is told otherwise.
   */
  manual({ baseId, userId = null, at: t, rule: ruleId, before = 20, after = 10 }) {
    const rule = this.rules().find((r) => r.id === ruleId) ?? {
      id: 'manual', title: 'A moment picked by hand', before, after, shapes: ['portrait', 'landscape'],
      text: 'A moment from the live server, replayed in the game\'s own engine. Open-source RTS in a browser tab, no download and no account.',
      shows: 'a real player\'s units on the live server', event: 'a moment picked from the recording',
    };
    return this.add(rule, { baseId, userId, at: t, by: 'hand' });
  }

  add(rule, { baseId, userId, at: t, by }) {
    const moment = {
      id: `${rule.id}-${baseId}-${stamp(t)}`,
      rule: rule.id, title: rule.title, by, baseId, userId, at: t,
      from: t - rule.before * 1000, to: t + rule.after * 1000,
      shapes: rule.shapes?.length ? rule.shapes : ['portrait'],
      status: 'waiting', created: Date.now(), takes: {},
    };
    if (this.items.some((m) => m.id === moment.id)) return null;
    this.items.push(moment);
    this.save();
    this.log(`moment ${moment.id}: ${rule.title} (${by})`);
    return moment;
  }

  /** Called every few seconds: films what the tape is ready for, retries a handover. */
  async tick() {
    if (this.busy) return;
    const newest = this.tape.entries.at(-1)?.t ?? 0;
    const ready = this.items.find((m) => m.status === 'waiting' && newest >= m.to + SETTLE_MS);
    if (ready) return this.run(() => this.film(ready));
    const retry = this.items.find((m) => m.status === 'not-handed-over' && Date.now() >= (m.retryAt ?? 0));
    if (retry) return this.run(() => this.handOver(retry));
  }

  async run(fn) {
    this.busy = true;
    try {
      await fn();
    } finally {
      this.busy = false;
      this.save();
    }
  }

  async film(moment) {
    moment.status = 'filming';
    this.save();
    if (this.tape.oldest == null || this.tape.oldest > moment.from) {
      moment.status = 'failed';
      moment.error = 'the tape no longer holds the start of it';
      return;
    }
    const browser = await chromium.launch({ channel: 'chrome', headless: true, args: LAUNCH_ARGS });
    try {
      for (const shape of moment.shapes) moment.takes[shape] = await this.filmShape(browser, moment, shape);
      moment.status = 'filmed';
      this.log(`moment ${moment.id}: filmed ${Object.keys(moment.takes).join(', ')}`);
    } catch (error) {
      moment.status = 'failed';
      moment.error = error.message;
      this.log(`moment ${moment.id}: filming failed: ${error.message}`);
      return;
    } finally {
      await browser.close();
    }
    await this.handOver(moment);
  }

  /** One take: the stretch played in its own replay session, recorded from the canvas. */
  async filmShape(browser, moment, shape) {
    const [width, height] = SHAPES[shape];
    const key = `moment-${moment.id}-${shape}`;
    // Started a little early: the client loads and the camera finds the base before the take
    const session = new ReplaySession({ tape: this.tape, baseId: moment.baseId, at: moment.from - 15_000, log: this.log });
    this.sessions.set(key, session);
    // The window in the shape of the take: a portrait take drawn in a landscape window came out at
    // 8 fps and a third of its length (local test, 2026-11-03), the same as record_director found.
    const context = await browser.newContext({ viewport: { width, height }, acceptDownloads: true });
    try {
      const page = await context.newPage();
      await page.addInitScript((token) => localStorage.setItem('app.token', token), sessionToken(key));
      await page.goto(`http://127.0.0.1:${this.settings.replayPort}/game/replay`);
      await page.waitForFunction(() => window.razReplay?.ready(), null, { timeout: READY_TIMEOUT_MS });
      // The pre-roll ends on the moment's first second - or, for a base younger than the moment's
      // lead, on the base's first second: a replay cannot start before the base stands
      // (ReplaySession.startAt), and the take is then that much shorter.
      const begin = session.seek(moment.from - PRE_ROLL_MS);
      moment.from = Math.max(moment.from, begin + PRE_ROLL_MS);
      await sleep(500);
      const download = page.waitForEvent('download', { timeout: moment.to - moment.from + 120_000 });
      await page.evaluate(([name, w, h]) => window.razReplay.record(name, w, h), [`${moment.id}-${shape}`, width, height]);
      while (session.playhead() < moment.to) await sleep(250);
      await page.evaluate(() => window.razReplay.stop());
      const file = await download;
      const out = join(this.dir, `${moment.id}-${shape}${extname(file.suggestedFilename()) || '.mp4'}`);
      await file.saveAs(out);
      const { duration, fps } = await probe(out);
      const wanted = (moment.to - moment.from) / 1000;
      if (fps < MIN_FPS || duration < wanted * MIN_SHARE) {
        throw new Error(`${shape} take broken: ${duration.toFixed(1)} s of ${wanted} s at ${fps.toFixed(1)} fps`);
      }
      return out;
    } finally {
      this.sessions.delete(key);
      session.close();
      await context.close();
    }
  }

  /** To the social engine, which makes the post of it. Kept and retried while it is not reachable. */
  async handOver(moment) {
    const rule = this.rules().find((r) => r.id === moment.rule) ?? {};
    const player = moment.userId ? this.players.byUser.get(moment.userId) : null;
    const body = {
      id: moment.id,
      rule: moment.rule,
      title: moment.title,
      text: rule.text ?? 'A moment from the live server, replayed in the game\'s own engine.',
      shows: rule.shows ?? 'a real player\'s units on the live server',
      facts: {
        event: rule.event ?? moment.title,
        ...(player?.level != null ? { player_level: player.level } : {}),
        ...(player?.startedAt ? { minutes_since_the_player_placed_their_base: Math.round((moment.at - player.startedAt) / 60_000) } : {}),
      },
      takes: moment.takes,
      cut: { start: 0, length: (moment.to - moment.from) / 1000 },
    };
    try {
      const res = await fetch(`${this.settings.engineUrl}/api/moments`, {
        method: 'POST', headers: { 'Content-Type': 'application/json', 'X-Review': '1' }, body: JSON.stringify(body),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status} ${(await res.text()).slice(0, 200)}`);
      moment.status = 'handed-over';
      moment.error = null;
      this.log(`moment ${moment.id}: handed to the social engine`);
    } catch (error) {
      moment.status = 'not-handed-over';
      moment.error = `social engine: ${error.message}`;
      moment.retryAt = Date.now() + HANDOFF_RETRY_MS;
      this.log(`moment ${moment.id}: not handed over (${error.message}), again in 10 min`);
    }
  }

  list() {
    return [...this.items].reverse().map(({ userId, ...rest }) => rest);
  }
}
