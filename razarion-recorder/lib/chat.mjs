import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { getJson, postText } from './auth.mjs';

export const ASSISTANT_NAME = 'Razarion AI';
const KEEP = 300;
// A question older than this when it is seen is not answered any more: the player has moved on,
// and a recorder that was off for a night must not answer the whole night at once.
const STALE_MS = 10 * 60_000;
const PER_HOUR = 30;
const RECENT = 8;

/**
 * The planet's chat, read from /rest/recorder/chat, and the assistant's answers to it.
 *
 * Every message is kept with the player's context at the time it was written and what became of
 * it: answered (and what), not a question for the assistant, or why not. That is the place to read
 * what players ask and complain about - the reason the chat is watched at all.
 *
 * The last id seen is kept on disk (state/chat.json), so a restart goes on where it stopped and
 * never answers a message twice. Without it the recorder starts from the newest message.
 */
export class ChatWatch {
  constructor({ baseUrl, auth, players, stateFile, everyMs, answering, log, answerChat, post }) {
    this.baseUrl = baseUrl;
    this.auth = auth;
    this.players = players;
    this.stateFile = stateFile;
    this.everyMs = everyMs;
    this.answering = answering;
    this.log = log;
    this.answerChat = answerChat;
    this.post = post ?? ((text) => postText(baseUrl, auth, '/rest/recorder/chat/answer', text));
    this.lastId = null;
    this.messages = [];
    this.queue = [];
    this.busy = false;
    this.answeredAt = [];
    this.state = 'idle';
    this.lastError = null;
    this.load();
  }

  load() {
    try {
      if (!existsSync(this.stateFile)) return;
      const saved = JSON.parse(readFileSync(this.stateFile, 'utf8'));
      this.lastId = saved.lastId ?? null;
      this.messages = saved.messages ?? [];
    } catch (error) {
      this.log(`chat: state not read (${error.message}), starting from the newest message`);
    }
  }

  save() {
    try {
      writeFileSync(this.stateFile, JSON.stringify({ lastId: this.lastId, messages: this.messages.slice(-KEEP) }, null, 1));
    } catch (error) {
      this.log(`chat: state not written (${error.message})`);
    }
  }

  start() {
    this.poll();
  }

  async poll() {
    try {
      const fresh = this.lastId == null;
      const entries = await getJson(this.baseUrl, this.auth, '/rest/recorder/chat' + (fresh ? '' : `?after=${this.lastId}`));
      for (const entry of entries) this.receive(entry, fresh);
      if (entries.length) this.save();
      this.state = 'ok';
    } catch (error) {
      this.state = error.status === 404 ? 'not-on-server' : error.status === 403 ? 'no-role' : 'error';
      this.lastError = { at: Date.now(), message: error.message };
    }
    this.work();
    this.timer = setTimeout(() => this.poll(), this.state === 'ok' ? this.everyMs : Math.max(this.everyMs, 60_000));
  }

  /** One message from the server. `fresh`: the recorder had seen nothing, this is the newest one. */
  receive(entry, fresh, now = Date.now()) {
    if (this.lastId != null && entry.id <= this.lastId) return;
    this.lastId = entry.id;
    const player = entry.userId ? this.playerOf(entry.userId, now) : null;
    const message = {
      id: entry.id,
      t: new Date(entry.timestamp).getTime() || now,
      userId: entry.userId,
      userName: entry.userName,
      message: entry.message,
      player,
      outcome: null,
    };
    // Players send the same question twice when no answer comes at once; it is answered once.
    const same = (m) => m.userId === entry.userId && m.message.trim().toLowerCase() === entry.message.trim().toLowerCase() && message.t - m.t < STALE_MS;
    const repeat = entry.userId && this.messages.some(same);
    this.messages.push(message);
    if (this.messages.length > KEEP) this.messages.splice(0, this.messages.length - KEEP);
    this.log(`chat #${entry.id} ${entry.userName}: ${entry.message}`);
    // No user id: the server's own voices, the assistant and the planet's news (a player left the noob island)
    if (!entry.userId || entry.userName === ASSISTANT_NAME) message.outcome = { status: entry.userName === ASSISTANT_NAME ? 'assistant' : 'news' };
    else if (fresh) message.outcome = { status: 'before-start' };
    else if (repeat) message.outcome = { status: 'repeat' };
    else if (!this.answering) message.outcome = { status: 'answering-off' };
    else if (now - message.t > STALE_MS) message.outcome = { status: 'stale' };
    else this.queue.push(message);
  }

  playerOf(userId, now) {
    const player = this.players.byUser.get(userId);
    return player ? this.players.summary(player, now) : null;
  }

  async work(now = Date.now()) {
    if (this.busy || !this.queue.length) return;
    this.busy = true;
    try {
      while (this.queue.length) {
        const message = this.queue.shift();
        this.answeredAt = this.answeredAt.filter((t) => t > now - 3_600_000);
        if (this.answeredAt.length >= PER_HOUR) {
          message.outcome = { status: 'hourly-limit' };
          continue;
        }
        const before = this.messages.filter((m) => m.id < message.id).slice(-RECENT);
        const otherNames = [...new Set(before.map((m) => m.userName))].filter((n) => n !== message.userName && n !== ASSISTANT_NAME);
        const result = await this.answerChat({ message, player: message.player, recent: before, otherNames });
        if (result.error) {
          message.outcome = { status: 'error', error: result.error };
          this.log(`chat #${message.id}: no answer, ${result.error}`);
        } else if (!result.answer) {
          message.outcome = { status: 'not-a-question' };
        } else {
          try {
            await this.post(result.answer);
          } catch (error) {
            message.outcome = { status: 'error', error: `not posted: ${error.message}`, answer: result.answer };
            this.log(`chat #${message.id}: answer not posted, ${error.message}`);
            this.save();
            continue;
          }
          this.answeredAt.push(Date.now());
          message.outcome = { status: 'answered', kind: result.kind, answer: result.answer, at: Date.now() };
          this.log(`chat #${message.id}: answered (${result.kind}) ${result.answer}`);
        }
        this.save();
      }
    } catch (error) {
      this.lastError = { at: Date.now(), message: error.message };
      this.log(`chat: answering failed, ${error.message}`);
    } finally {
      this.busy = false;
    }
  }

  /** Newest first, for the dashboard and the MCP. */
  list(limit = 100) {
    return this.messages.slice(-limit).reverse();
  }

  status() {
    return { state: this.state, lastId: this.lastId, answering: this.answering, queued: this.queue.length, held: this.messages.length, answeredLastHour: this.answeredAt.length, lastError: this.lastError };
  }
}
