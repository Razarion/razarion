import { EventEmitter } from 'node:events';
import WebSocket from 'ws';

const RETRY_MIN_MS = 2_000;
const RETRY_MAX_MS = 60_000;
// The server sends nothing while nothing changes, which on a quiet planet can be minutes. Pings
// tell a dead socket from a quiet world.
const PING_EVERY_MS = 30_000;
const KEYFRAME_TIMEOUT_MS = 30_000;

/** Every message is `PACKET_NAME#json` (ConnectionMarshaller). */
export function packetName(text) {
  const hash = text.indexOf('#');
  return hash < 0 ? '?' : text.slice(0, hash);
}

export function packetPayload(text) {
  return JSON.parse(text.slice(text.indexOf('#') + 1));
}

/**
 * Sent with every socket handshake, so the backend's connection table can tell the recorder from a
 * player instead of showing a row with no device at all (frontend backend/device-text.ts).
 */
export const USER_AGENT = 'RazarionRecorder/1.0';
const SOCKET_OPTIONS = { headers: { 'User-Agent': USER_AGENT } };

function gameUrl(baseUrl, token) {
  return `${baseUrl.replace(/^http/, 'ws')}/gameconnection?token=${encodeURIComponent(token)}`;
}

/**
 * The game connection, as a player's browser holds it, except that nothing is ever sent: the
 * recorder has no base and gives no orders, and the server ignores anything it might send anyway.
 *
 * Emits `packet` {t, segment, name, text} for every message and `state` on every change. A segment
 * is one connection; each begins with INITIAL_SLAVE_SYNC_INFO, the whole world as it stood then.
 */
export class GameStream extends EventEmitter {
  constructor(baseUrl, auth) {
    super();
    this.baseUrl = baseUrl;
    this.auth = auth;
    this.segment = 0;
    this.state = 'idle';
    this.lastError = null;
    this.connectedSince = null;
    this.lastMessageAt = null;
    this.retryMs = RETRY_MIN_MS;
    this.stopped = false;
  }

  start() {
    this.stopped = false;
    this.connect();
  }

  stop() {
    this.stopped = true;
    clearTimeout(this.retryTimer);
    this.socket?.terminate();
  }

  setState(state, error) {
    this.state = state;
    if (error) this.lastError = { at: Date.now(), message: String(error.message || error) };
    this.emit('state', state);
  }

  async connect() {
    let token;
    try {
      token = await this.auth.token();
    } catch (error) {
      this.setState('login-failed', error);
      return this.retry();
    }
    this.setState('connecting');
    const socket = new WebSocket(gameUrl(this.baseUrl, token), SOCKET_OPTIONS);
    this.socket = socket;
    let alive = true;
    let ping;

    socket.on('open', () => {
      this.segment++;
      this.connectedSince = Date.now();
      this.retryMs = RETRY_MIN_MS;
      this.setState('connected');
      ping = setInterval(() => {
        if (!alive) return socket.terminate();
        alive = false;
        socket.ping();
      }, PING_EVERY_MS);
    });
    socket.on('pong', () => (alive = true));
    socket.on('message', (data) => {
      alive = true;
      const t = Date.now();
      this.lastMessageAt = t;
      const text = data.toString('utf8');
      this.emit('packet', { t, segment: this.segment, name: packetName(text), text });
    });
    socket.on('unexpected-response', (req, res) => {
      if (res.statusCode === 401 || res.statusCode === 403) this.auth.invalidate();
      this.setState('rejected', new Error(`Handshake answered HTTP ${res.statusCode}`));
    });
    socket.on('error', (error) => this.setState('error', error));
    socket.on('close', (code, reason) => {
      clearInterval(ping);
      this.connectedSince = null;
      if (this.state === 'connected') this.setState('closed', new Error(`Closed ${code} ${reason || ''}`.trim()));
      if (!this.stopped) this.retry();
    });
  }

  retry() {
    clearTimeout(this.retryTimer);
    this.retryTimer = setTimeout(() => this.connect(), this.retryMs);
    this.retryMs = Math.min(this.retryMs * 2, RETRY_MAX_MS);
  }
}

/**
 * A second connection that is opened only to be sent the world and closed again. The server sends
 * the whole planet on every connect and otherwise only what changed, so this is the one way to get
 * a picture of everything mid-stream. One user may hold several connections; closing this one
 * leaves the live stream alone (ClientGameConnectionService.afterConnectionClosed).
 */
export async function takeKeyframe(baseUrl, auth) {
  const token = await auth.token();
  return new Promise((resolve, reject) => {
    const socket = new WebSocket(gameUrl(baseUrl, token), SOCKET_OPTIONS);
    const timer = setTimeout(() => {
      socket.terminate();
      reject(new Error('No world within 30 s'));
    }, KEYFRAME_TIMEOUT_MS);
    socket.on('message', (data) => {
      const text = data.toString('utf8');
      if (packetName(text) !== 'INITIAL_SLAVE_SYNC_INFO') return;
      clearTimeout(timer);
      socket.close();
      resolve({ t: Date.now(), text });
    });
    socket.on('error', (error) => {
      clearTimeout(timer);
      reject(error);
    });
  });
}
