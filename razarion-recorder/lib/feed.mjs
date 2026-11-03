import { getJson } from './auth.mjs';

const UNAVAILABLE_RETRY_MS = 60_000;

/**
 * The history feed (/rest/recorder/events): quests, levels, XP, boxes - what the game connection
 * tells only the player it concerns. Polled with the last number seen; a new epoch means the server
 * restarted and starts over, which can repeat rows the recorder already has, never lose any it held.
 *
 * A server without the endpoint (404) or an account without the role (403) is shown, not fatal:
 * the recorder still records the world.
 */
export class HistoryFeed {
  constructor(baseUrl, auth, everyMs, onRow) {
    this.baseUrl = baseUrl;
    this.auth = auth;
    this.everyMs = everyMs;
    this.onRow = onRow;
    this.epoch = null;
    this.after = 0;
    this.state = 'idle';
    this.lastError = null;
    this.rows = 0;
    this.lastPollAt = null;
  }

  start() {
    this.poll();
  }

  stop() {
    clearTimeout(this.timer);
  }

  async poll() {
    let wait = this.everyMs;
    try {
      const query = `?after=${this.after}` + (this.epoch ? `&epoch=${encodeURIComponent(this.epoch)}` : '');
      const page = await getJson(this.baseUrl, this.auth, `/rest/recorder/events${query}`);
      this.epoch = page.epoch;
      for (const event of page.events) {
        this.rows++;
        this.onRow(event.history, Date.now());
        this.after = event.seq;
      }
      if (!page.events.length) this.after = page.lastSeq;
      this.state = 'ok';
      this.lastPollAt = Date.now();
      // A full page means more is waiting.
      if (page.events.length >= 2000) wait = 0;
    } catch (error) {
      this.state = error.status === 404 ? 'not-on-server' : error.status === 403 ? 'no-role' : 'error';
      this.lastError = { at: Date.now(), message: error.message };
      if (error.status === 404 || error.status === 403) wait = UNAVAILABLE_RETRY_MS;
    }
    this.timer = setTimeout(() => this.poll(), wait);
  }
}
