import { createServer, request as httpRequest } from 'node:http';
import { request as httpsRequest } from 'node:https';
import { WebSocketServer } from 'ws';
import { packetName, packetPayload } from './connection.mjs';

// The server's tick (PlanetService.TICK_TIME_MILLI_SECONDS): the client's worker runs at it and
// holds back every tick info that is ahead of its own count.
const TICK_MS = 100;
const STREAM_EVERY_MS = 50;
// How long after the followed base's last shot the camera stays on its fight.
const FIGHT_MS = 6_000;
// A subject spread further than this (two builders, one still on the island) is framed on its
// middle at this size rather than zoomed out over both.
const SUBJECT_MAX_RADIUS = 25;
// How far inside the planet's edge the picture's edge stays, beyond the framed radius.
const EDGE_MARGIN = 40;

const median = (values) => {
  const sorted = [...values].sort((a, b) => a - b);
  return sorted[sorted.length >> 1];
};
// Requests the game client makes that are about the person in front of it: the replay is watched
// by nobody the tracking should count.
const SWALLOWED = [/^\/rest\/tracker\//, /^\/t\.gif/];
// The only write the client needs to start: loading its context is a POST. Every other one - a quest
// activated on start, a setting saved - would be the recorder's account playing, and it never plays.
const WRITES_PASSED = [/^\/rest\/game-ui-context-control\/cold$/];
// The page itself, whose query the server would count as a visit to the game (RequestInfoLoggingFilter).
// Only these: a dev server's module requests under /game carry a query they cannot do without.
const PAGE = /^\/game(\/(replay|director))?\/?$/;

/**
 * The world as it stood at t, in the shape the server sends on connect: the last keyframe before
 * t, with every message after it applied. Whole objects, not the recorder's slim ones - this goes
 * to a real game client.
 */
export function worldAt(tape, t) {
  const keyframe = [...tape.keyframes].reverse().find((k) => k.t <= t);
  if (!keyframe) return null;
  const world = { tickCount: 0, tickAt: keyframe.t, items: new Map(), bases: new Map(), resources: new Map(), boxes: new Map() };
  const take = (info, at) => {
    world.tickCount = info.tickCount;
    world.tickAt = at;
    world.items = new Map((info.syncBaseItemInfos || []).map((i) => [i.id, i]));
    world.bases = new Map((info.playerBaseInfos || []).map((b) => [b.baseId, b]));
    world.resources = new Map((info.syncResourceItemInfos || []).map((r) => [r.id, r]));
    world.boxes = new Map((info.syncBoxItemInfos || []).map((b) => [b.id, b]));
  };
  take(packetPayload(keyframe.text), keyframe.t);
  for (const entry of tape.range(keyframe.t + 1, t + 1)) {
    if (entry.name === 'HISTORY') continue;
    applyTo(world, entry);
  }
  return world;
}

function applyTo(world, entry) {
  const payload = packetPayload(entry.text);
  switch (entry.name) {
    case 'INITIAL_SLAVE_SYNC_INFO':
      world.tickCount = payload.tickCount;
      world.tickAt = entry.t;
      world.items = new Map((payload.syncBaseItemInfos || []).map((i) => [i.id, i]));
      world.bases = new Map((payload.playerBaseInfos || []).map((b) => [b.baseId, b]));
      world.resources = new Map((payload.syncResourceItemInfos || []).map((r) => [r.id, r]));
      world.boxes = new Map((payload.syncBoxItemInfos || []).map((b) => [b.id, b]));
      break;
    case 'TICK_INFO':
      world.tickCount = payload.tickCount;
      world.tickAt = entry.t;
      for (const info of payload.syncBaseItemInfos || []) world.items.set(info.id, info);
      break;
    case 'SYNC_ITEM_DELETED':
      world.items.delete(payload.id);
      world.resources.delete(payload.id);
      world.boxes.delete(payload.id);
      break;
    case 'BASE_CREATED':
      world.bases.set(payload.baseId, payload);
      break;
    case 'BASE_NAME_CHANGED': {
      const base = world.bases.get(payload.baseId);
      if (base) base.name = payload.name;
      break;
    }
    case 'BASE_DELETED':
      world.bases.delete(payload);
      for (const [id, item] of world.items) if (item.baseId === payload) world.items.delete(id);
      break;
    case 'SYNC_RESOURCE_ITEM_CHANGED':
      world.resources.set(payload.id, payload);
      break;
    case 'SYNC_BOX_ITEM_CHANGED':
      world.boxes.set(payload.id, payload);
      break;
    default:
  }
}

/** The tick the server had reached at t: the last one it named, and the time gone since. */
const tickAt = (world, t) => world.tickCount + Math.floor((t - world.tickAt) / TICK_MS);

function snapshotText(world, t, actualBaseId) {
  return 'INITIAL_SLAVE_SYNC_INFO#' + JSON.stringify({
    tickCount: tickAt(world, t),
    actualBaseId,
    syncBaseItemInfos: [...world.items.values()],
    playerBaseInfos: [...world.bases.values()],
    syncResourceItemInfos: [...world.resources.values()],
    syncBoxItemInfos: [...world.boxes.values()],
  });
}

/**
 * One replay, watched in a real game client through the proxy below. The client is told the
 * followed player's base is its own (actualBaseId): it opens on that base and shows it the way the
 * player saw it.
 *
 * Played at the speed it happened. The client's worker ticks in real time and holds back any tick
 * info ahead of its own count, so a faster tape would only pile up there. A jump is a new world
 * sent on the same connection, which the client takes as it takes a reconnect (PlanetService
 * .initialSlaveSyncItemInfo) - for the same base only: another base reloads the page.
 */
export class ReplaySession {
  /**
   * subject: { types: Set of item type ids, maxRadius? } - the units a moment is about (focus).
   * bounds: { width, height } of the planet, which the camera keeps inside of.
   */
  constructor({ tape, baseId, at, log, subject = null, bounds = null }) {
    this.tape = tape;
    this.baseId = baseId;
    this.log = log;
    this.subject = subject;
    this.bounds = bounds;
    this.socket = null;
    this.timer = null;
    this.startAt(at);
  }

  /** Where the playback is now, in tape time. */
  playhead() {
    return this.anchorT + (Date.now() - this.anchorWall);
  }

  status() {
    return { baseId: this.baseId, playhead: this.playhead(), connected: !!this.socket, newest: this.tape.entries.at(-1)?.t ?? null, focus: this.focus() };
  }

  /**
   * Where the camera should look, in game coordinates: where the followed base is fighting, or the
   * middle of its units. The client's renderer only knows the units in its view field, so it cannot
   * find a strike force forty metres outside the picture; the tape knows every one.
   *
   * With a subject (a moment's rule, camera.follow) the camera stays on those units whatever else
   * the base does: lock tells the client to ignore the fighting it sees itself. fight is set as well
   * for a client from before lock, which then at least looks at the subject.
   */
  focus() {
    const subject = this.subjectFocus();
    if (subject) return subject;
    const now = this.cursor;
    const shots = (this.shots || []).filter((shot) => now - shot.t < FIGHT_MS);
    if (shots.length) {
      return this.inBounds({ x: median(shots.map((s) => s.x)), y: median(shots.map((s) => s.y)), radius: 40, fight: true });
    }
    const units = [...(this.units?.values() || [])];
    if (!units.length) return null;
    const x = median(units.map((u) => u.x));
    const y = median(units.map((u) => u.y));
    const spread = units.map((u) => Math.hypot(u.x - x, u.y - y)).sort((a, b) => a - b);
    return this.inBounds({ x, y, radius: spread[Math.floor(spread.length * 0.8)] ?? 0, fight: false });
  }

  /** The subject's units that are out in the open - a builder in a transporter rides along unseen. */
  subjectFocus() {
    if (!this.subject?.types?.size) return null;
    const units = [...(this.units?.values() || [])].filter((u) => this.subject.types.has(u.type) && u.containedIn == null);
    if (!units.length) return null;
    const x = median(units.map((u) => u.x));
    const y = median(units.map((u) => u.y));
    const spread = Math.max(...units.map((u) => Math.hypot(u.x - x, u.y - y)));
    return this.inBounds({ x, y, radius: Math.min(spread, this.subject.maxRadius ?? SUBJECT_MAX_RADIUS), fight: true, lock: true });
  }

  /**
   * Keeps the camera's target this far inside the planet: closer to the edge, the picture shows
   * the void beyond it (the noob island lies along the south edge).
   */
  inBounds(focus) {
    if (!this.bounds) return focus;
    const margin = EDGE_MARGIN + focus.radius;
    const clampTo = (v, max) => (max > 2 * margin ? Math.min(Math.max(v, margin), max - margin) : max / 2);
    return { ...focus, x: clampTo(focus.x, this.bounds.width), y: clampTo(focus.y, this.bounds.height) };
  }

  track(info) {
    const position = info.syncPhysicalAreaInfo?.position;
    if (info.baseId === this.baseId && position) {
      this.units.set(info.id, { x: position.x, y: position.y, type: info.itemTypeId, containedIn: info.containedIn ?? null });
    }
  }

  /** Keeps the followed base's units and shots up to date as the tape goes out. */
  watch(name, payload, t) {
    if (name === 'TICK_INFO') {
      for (const info of payload.syncBaseItemInfos || []) this.track(info);
    } else if (name === 'SYNC_ITEM_DELETED') {
      this.units.delete(payload.id);
    } else if (name === 'PROJECTILE_FIRED' && this.units.has(payload.actorSyncBaseItemId) && payload.targetPosition) {
      this.shots.push({ t, x: payload.targetPosition.x, y: payload.targetPosition.y });
      if (this.shots.length > 50) this.shots.shift();
    }
  }

  /** The base must stand: without it the client would put up its base placer. */
  startAt(t) {
    const world = worldAt(this.tape, t);
    if (!world) throw new Error('no keyframe held before that time');
    if (!world.bases.has(this.baseId)) {
      const created = this.tape.range(t, Infinity).find((e) => e.name === 'BASE_CREATED' && packetPayload(e.text).baseId === this.baseId);
      if (!created) throw new Error(`base ${this.baseId} does not stand at that time, nor later`);
      t = created.t + 1_500;
    }
    this.anchorT = t;
    this.anchorWall = Date.now();
    this.cursor = t;
    return t;
  }

  attach(socket) {
    this.socket?.close();
    this.socket = socket;
    socket.on('message', (data) => this.onClientMessage(data.toString('utf8')));
    socket.on('close', () => {
      if (this.socket === socket) {
        this.socket = null;
        clearInterval(this.timer);
      }
    });
    this.sendWorld();
    this.log(`replay: client attached, base ${this.baseId} at ${new Date(this.anchorT).toLocaleTimeString()}`);
  }

  seek(t) {
    const at = this.startAt(t);
    if (this.socket) this.sendWorld();
    return at;
  }

  sendWorld() {
    clearInterval(this.timer);
    // The clock starts with the world on screen. Started when the replay was set up, it had run on
    // while the page loaded, and the client got its world and then seconds of tape in one burst,
    // ahead of its own tick: the units created in that burst were never built (local test, 2026-11-03).
    this.anchorWall = Date.now();
    const world = worldAt(this.tape, this.anchorT);
    this.lastTick = { tick: tickAt(world, this.anchorT), at: Date.now() };
    this.units = new Map();
    this.shots = [];
    for (const info of world.items.values()) this.track(info);
    this.socket.send(snapshotText(world, this.anchorT, this.baseId));
    this.cursor = this.anchorT;
    this.timer = setInterval(() => this.stream(), STREAM_EVERY_MS);
  }

  /** Everything the tape holds up to the playhead that has not gone out yet. */
  stream() {
    if (!this.socket) return;
    const until = this.playhead();
    for (const entry of this.tape.range(this.cursor + 1, until + 1)) {
      if (entry.name === 'HISTORY') continue;
      let text = entry.text;
      if (entry.name === 'INITIAL_SLAVE_SYNC_INFO') {
        // The recorder's own reconnect: the same world again, but as the followed player sees it
        const payload = packetPayload(text);
        payload.actualBaseId = this.baseId;
        text = 'INITIAL_SLAVE_SYNC_INFO#' + JSON.stringify(payload);
      } else if (entry.name === 'TICK_INFO' || entry.name === 'SYNC_ITEM_DELETED' || entry.name === 'PROJECTILE_FIRED') {
        const payload = packetPayload(text);
        if (entry.name === 'TICK_INFO') this.lastTick = { tick: payload.tickCount, at: Date.now() };
        this.watch(entry.name, payload, entry.t);
      }
      this.socket.send(text);
      this.cursor = entry.t;
    }
    this.cursor = Math.max(this.cursor, until);
  }

  onClientMessage(text) {
    // The client asks for the tick after every world; everything else it sends is an order, and
    // nobody plays here.
    if (packetName(text) === 'TICK_COUNT_REQUEST' && this.lastTick) {
      const tick = this.lastTick.tick + Math.floor((Date.now() - this.lastTick.at) / TICK_MS);
      this.socket?.send('TICK_COUNT_RESPONSE#' + JSON.stringify(tick));
    }
  }

  close() {
    clearInterval(this.timer);
    this.socket?.close();
  }
}

/**
 * A key for a replay of its own, in the client's token slot (localStorage app.token): the client
 * passes the token on to the game connection (?token=) by itself, so a headless client filming a
 * moment and a person watching another replay each get theirs. Shaped like a JWT because the client
 * decodes its token; unsigned, because nothing ever trusts it - the proxy replaces it on every
 * request with the recorder's own.
 */
export function sessionToken(key) {
  const part = (o) => Buffer.from(JSON.stringify(o)).toString('base64url');
  return `${part({ alg: 'none' })}.${part({ sub: 'replay', session: key })}.x`;
}

/** The session a token names; 'default' for none, the one a person opens from the 2D player. */
export function sessionKey(token) {
  try {
    return JSON.parse(Buffer.from(String(token).split('.')[1], 'base64url').toString('utf8')).session || 'default';
  } catch {
    return 'default';
  }
}

/**
 * The game client, served from the game server through this proxy, with the game connection
 * answered from the tape. The client's worker connects to /gameconnection on the page's own host,
 * so nothing in the client has to know it is watching a replay.
 *
 * - /gameconnection: the current ReplaySession
 * - /systemconnection: accepted and left silent - quests and chat are the watcher's, and the
 *   watcher is nobody
 * - REST: forwarded with the recorder's token, so the server sees its read-only account and never
 *   makes an anonymous player of the watcher; the tracking calls are answered here and go nowhere
 * - /game: forwarded without its query, which is what the server would count as a visit
 * - /replay/status: the session, for the client's ReplayService
 *
 * Several replays can play at once, one per session key (sessionToken): getSession(key).
 *
 * target is where the pages come from: the game server, or an Angular dev server in front of it.
 */
export function startReplayProxy({ port, target, auth, getSession, log }) {
  const targetUrl = new URL(target);
  const send = targetUrl.protocol === 'https:' ? httpsRequest : httpRequest;
  const games = new WebSocketServer({ noServer: true });
  const silent = new WebSocketServer({ noServer: true });

  const server = createServer((req, res) => {
    forward(req, res).catch((error) => {
      log(`replay proxy: ${req.method} ${req.url}: ${error.message}`);
      if (!res.headersSent) res.writeHead(500);
      res.end();
    });
  });

  async function forward(req, res) {
    const url = new URL(req.url, 'http://localhost');
    if (url.pathname === '/replay/status') {
      // For the client's ReplayService: which base to follow, and the moment on screen
      res.writeHead(200, { 'Content-Type': 'application/json', 'Cache-Control': 'no-store' });
      return res.end(JSON.stringify(getSession(sessionKey(url.searchParams.get('session')))?.status() ?? null));
    }
    const write = req.method !== 'GET' && req.method !== 'HEAD';
    if (SWALLOWED.some((pattern) => pattern.test(url.pathname)) || (write && !WRITES_PASSED.some((pattern) => pattern.test(url.pathname)))) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      return res.end('null');
    }
    const path = PAGE.test(url.pathname) ? url.pathname : url.pathname + url.search;
    const headers = { ...req.headers, host: targetUrl.host };
    delete headers.origin;
    delete headers.referer;
    delete headers.cookie;
    if (url.pathname.startsWith('/rest/')) {
      try {
        headers.authorization = 'Bearer ' + (await auth.token());
      } catch (error) {
        res.writeHead(502);
        return res.end(`recorder login failed: ${error.message}`);
      }
    }
    const upstream = send({ protocol: targetUrl.protocol, hostname: targetUrl.hostname, port: targetUrl.port, method: req.method, path, headers }, (answer) => {
      const out = { ...answer.headers };
      delete out['set-cookie'];
      res.writeHead(answer.statusCode, out);
      answer.pipe(res);
    });
    upstream.on('error', (error) => {
      res.writeHead(502);
      res.end(error.message);
    });
    req.pipe(upstream);
  }

  server.on('upgrade', (req, socket, head) => {
    const url = new URL(req.url, 'http://localhost');
    const path = url.pathname;
    if (path === '/gameconnection') {
      games.handleUpgrade(req, socket, head, (ws) => {
        const session = getSession(sessionKey(url.searchParams.get('token')));
        if (!session) {
          log('replay: a client connected but no replay is set up');
          ws.close(4000, 'no replay set up - start one from the recorder');
          return;
        }
        session.attach(ws);
      });
    } else if (path === '/systemconnection') {
      silent.handleUpgrade(req, socket, head, (ws) => ws.on('message', () => {}));
    } else {
      socket.destroy();
    }
  });

  server.listen(port, '127.0.0.1', () => log(`replay client on http://127.0.0.1:${port}, pages from ${target}`));
  server.on('error', (error) => log(`replay proxy: ${error.message}`));
  return server;
}
