#!/usr/bin/env node
// The recorder: watches the live planet the way a player's browser does, and keeps what it sees.
//
//   node recorder.mjs                    # http://127.0.0.1:4712
//   scheduled\install-recorder.ps1       # as a Windows task, started at logon, hidden
//
// It holds one game connection to razarion.com with its own account (RAZARION_RECORDER_EMAIL),
// which the server sends the whole world and from which it accepts nothing. Every message goes on
// the tape (lib/tape.mjs) as it came; every few minutes a second connection fetches a whole world
// as a keyframe; the history feed adds what only a player's own client is told (lib/feed.mjs).
// lib/world.mjs reads the stream into bases, items and events for the dashboard.
//
// In memory only, for now: a restart starts an empty tape. On disk are only the log and the
// volume measurement (state/), which hold sizes and counts and nothing of the game.
//
// Local only: it listens on 127.0.0.1, and nothing it serves changes anything. Its one write to the
// game is the chat assistant's answer (lib/chat.mjs).

import { createServer } from 'node:http';
import { appendFileSync, existsSync, readFileSync, renameSync, statSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { settings, ensureStateDir, ROOT } from './lib/config.mjs';
import { createAuth, getJson, postBytes } from './lib/auth.mjs';
import { GameStream, takeKeyframe } from './lib/connection.mjs';
import { Tape } from './lib/tape.mjs';
import { World } from './lib/world.mjs';
import { Players } from './lib/players.mjs';
import { buildTrack, MAX_RANGE_MS } from './lib/replay.mjs';
import { ReplaySession, startReplayProxy } from './lib/replay-server.mjs';
import { Moments } from './lib/moments.mjs';
import { HistoryFeed } from './lib/feed.mjs';
import { ChatWatch } from './lib/chat.mjs';
import { answerChat } from './lib/assistant.mjs';
import { momentToChat } from './lib/chat-video.mjs';

const HOUR = 3_600_000;
const STATE_DIR = ensureStateDir();
const LOG_FILE = join(STATE_DIR, 'recorder.log');
const VOLUME_FILE = join(STATE_DIR, 'volume.json');
const LOG_MAX_BYTES = 5_000_000;
const VOLUME_EVERY_MS = 10 * 60_000;
const CONFIG_EVERY_MS = HOUR;
// Catches a quest the feed missed, e.g. while the recorder was reconnecting.
const PLAYERS_EVERY_MS = 10 * 60_000;
const PAGE = join(ROOT, 'dashboard', 'page.html');
const PLAYER_PAGE = join(ROOT, 'dashboard', 'player.html');

function log(line) {
  const text = `${new Date().toISOString()} ${line}\n`;
  process.stdout.write(text);
  try {
    if (existsSync(LOG_FILE) && statSync(LOG_FILE).size > LOG_MAX_BYTES) renameSync(LOG_FILE, LOG_FILE + '.1');
    appendFileSync(LOG_FILE, text);
  } catch {
    // the log is a convenience; recording goes on without it
  }
}

const auth = createAuth(settings.baseUrl);
const tape = new Tape({ keepMs: settings.keepHours * HOUR, maxBytes: settings.maxMegabytes * 1024 * 1024 });
const world = new World();
const players = new Players(world);
const stream = new GameStream(settings.baseUrl, auth);
const feed = new HistoryFeed(settings.baseUrl, auth, settings.feedSeconds * 1000, (row, t) => {
  tape.add({ t, segment: stream.segment, name: 'HISTORY', text: 'HISTORY#' + JSON.stringify(row) });
  players.onHistory(row, t);
  moments?.onHistory(row, t);
});
// The chat, and the assistant answering questions in it (lib/chat.mjs, lib/assistant.mjs).
const chat = new ChatWatch({ baseUrl: settings.baseUrl, auth, players, stateFile: join(STATE_DIR, 'chat.json'), everyMs: settings.chatSeconds * 1000, answering: settings.chatAnswer, log, answerChat });
const keyframes = { taken: 0, failed: 0, lastError: null };
let configLoadedAt = null;

stream.on('packet', (entry) => {
  tape.add(entry);
  world.apply(entry);
  if (entry.name === 'INITIAL_SLAVE_SYNC_INFO') {
    tape.addKeyframe({ t: entry.t, text: entry.text, source: 'connect', segment: entry.segment });
    log(`segment ${entry.segment}: world of ${entry.text.length} characters, ${world.items.size} items, ${world.bases.size} bases`);
  }
});
stream.on('state', (state) => {
  log(`game connection ${state}${stream.lastError && state !== 'connected' ? ': ' + stream.lastError.message : ''}`);
  if (state === 'connected') log(`roles of the account: ${auth.roles.join(' ') || '(none)'}`);
});

async function loadConfig() {
  try {
    world.setConfig(await getJson(settings.baseUrl, auth, '/rest/recorder/config'));
    configLoadedAt = Date.now();
  } catch (error) {
    log(`names not loaded (${error.message}); ids are shown instead`);
  }
}

async function loadPlayers() {
  try {
    players.seed(await getJson(settings.baseUrl, auth, '/rest/recorder/players'), Date.now());
  } catch (error) {
    log(`player states not loaded (${error.message})`);
  }
}

async function keyframe() {
  if (stream.state !== 'connected') return;
  try {
    const frame = await takeKeyframe(settings.baseUrl, auth);
    tape.addKeyframe({ ...frame, source: 'keyframe', segment: stream.segment });
    keyframes.taken++;
  } catch (error) {
    keyframes.failed++;
    keyframes.lastError = { at: Date.now(), message: error.message };
    log(`keyframe failed: ${error.message}`);
  }
}

/** What an hour of the planet costs, written down so the measurement survives a restart. */
function writeVolume() {
  const summary = tape.summary();
  let history = [];
  try {
    history = JSON.parse(readFileSync(VOLUME_FILE, 'utf8')).hours || [];
  } catch {
    // first run
  }
  const byHour = new Map(history.map((h) => [h.hour, h]));
  for (const hour of summary.hours) byHour.set(hour.hour, { ...hour, at: new Date(hour.hour).toISOString() });
  const hours = [...byHour.values()].sort((a, b) => a.hour - b.hour).slice(-24 * 14);
  writeFileSync(VOLUME_FILE, JSON.stringify({ updated: new Date().toISOString(), hours }, null, 1));
  const mb = (bytes) => (bytes / 1024 / 1024).toFixed(1);
  log(`volume: ${summary.bytesPerHour == null ? "-" : mb(summary.bytesPerHour)} MB/h on average, tape ${mb(summary.held.bytes)} MB, ` +
    `${summary.held.entries} messages, ${summary.held.keyframes} keyframes, heap ${mb(process.memoryUsage().heapUsed)} MB`);
}

function status() {
  const memory = process.memoryUsage();
  return {
    now: Date.now(),
    server: settings.baseUrl,
    connection: {
      state: stream.state,
      segment: stream.segment,
      connectedSince: stream.connectedSince,
      lastMessageAt: stream.lastMessageAt,
      lastError: stream.lastError,
      roles: auth.roles,
      tokenExpiresAt: auth.expiresAt,
    },
    feed: { state: feed.state, rows: feed.rows, lastPollAt: feed.lastPollAt, lastError: feed.lastError },
    chat: chat.status(),
    keyframes: { ...keyframes, everyMinutes: settings.keyframeMinutes },
    names: { loadedAt: configLoadedAt, baseItemTypes: world.names.baseItemTypes.size },
    world: { bases: world.bases.size, items: world.items.size, tickCount: world.tickCount, snapshots: world.snapshots, lastError: world.lastError },
    memory: { rss: memory.rss, heapUsed: memory.heapUsed, heapTotal: memory.heapTotal },
    tape: tape.summary(),
  };
}

/**
 * The stretch a replay asks for: ?from=&to= (ms), ?minutes=N up to now, or ?userId= for one
 * player's time on the planet, a minute either side.
 */
function replayRange(params) {
  const now = Date.now();
  const userId = params.get('userId');
  if (userId) {
    const player = players.byUser.get(userId);
    if (!player) return null;
    const start = (player.startedAt ?? player.firstSeen ?? now) - 60_000;
    const end = Math.min(now, (player.lastSeen ?? now) + 60_000);
    return { from: Math.max(start, end - MAX_RANGE_MS), to: end };
  }
  if (params.get('from')) return { from: Number(params.get('from')), to: Number(params.get('to') || now) };
  return { from: now - Number(params.get('minutes') || 10) * 60_000, to: now };
}

let minimap = null;
async function minimapImage() {
  if (minimap?.planetId === world.planet?.id) return minimap;
  const res = await fetch(`${settings.baseUrl}/rest/image/minimap/${world.planet.id}`);
  if (!res.ok) throw new Error(`minimap: HTTP ${res.status}`);
  minimap = { planetId: world.planet.id, type: res.headers.get('content-type'), data: Buffer.from(await res.arrayBuffer()) };
  return minimap;
}

// The replays in the real game client, by session key (lib/replay-server.mjs sessionToken): the one
// a person sets up from the 2D player is 'default', the moments film in their own.
const sessions = new Map();
let replaySession = null;
// Shorts from players' moments (rules.json), filmed from the tape and handed to the social engine.
const moments = new Moments({
  dir: join(STATE_DIR, 'moments'), rulesFile: join(ROOT, 'rules.json'), tape, world, players, sessions, settings, log,
  toChat: (moment) => momentToChat(moment, {
    log,
    post: (userId, bytes) => postBytes(settings.baseUrl, auth, `/rest/recorder/chat/video?userId=${encodeURIComponent(userId)}`, bytes, 'video/mp4'),
  }),
});

async function readJson(req) {
  let body = '';
  for await (const chunk of req) body += chunk;
  return body ? JSON.parse(body) : {};
}

/**
 * POST /api/replay3d {baseId | userId, at}   set up a replay, answers the url to open
 * POST /api/replay3d/seek {at}               jump (same base: same page, no reload)
 * GET  /api/replay3d                         where it is
 * Changes nothing in the game, only what the recorder plays to its own client.
 */
async function replay3d(req, res, url) {
  try {
    if (req.method === 'GET') return send(res, 200, replaySession ? replaySession.status() : null);
    const body = await readJson(req);
    if (url.pathname === '/api/replay3d/seek') {
      if (!replaySession) return send(res, 409, { error: 'no replay set up' });
      return send(res, 200, { at: replaySession.seek(Number(body.at)) });
    }
    let baseId = body.baseId != null ? Number(body.baseId) : null;
    if (baseId == null && body.userId) baseId = players.baseOf(players.byUser.get(body.userId) ?? { baseIds: [] })?.baseId ?? null;
    if (baseId == null) return send(res, 400, { error: 'which base? give baseId or userId' });
    replaySession?.close();
    replaySession = new ReplaySession({ tape, baseId, at: Number(body.at || Date.now() - 60_000), log, bounds: world.planet });
    sessions.set('default', replaySession);
    return send(res, 200, { url: `http://127.0.0.1:${settings.replayPort}/game/replay`, ...replaySession.status() });
  } catch (error) {
    return send(res, 400, { error: error.message });
  }
}

/**
 * GET  /api/moments                          the moments, newest first
 * POST /api/moments {baseId | userId, at, rule?}   a moment picked by hand, filmed like the others
 */
async function momentsApi(req, res) {
  try {
    if (req.method === 'GET') return send(res, 200, moments.list());
    const body = await readJson(req);
    let baseId = body.baseId != null ? Number(body.baseId) : null;
    if (baseId == null && body.userId) baseId = players.baseOf(players.byUser.get(body.userId) ?? { baseIds: [] })?.baseId ?? null;
    if (baseId == null || !body.at) return send(res, 400, { error: 'which base and when? give baseId (or userId) and at' });
    const owner = [...players.byUser.values()].find((p) => p.baseIds.includes(baseId));
    const moment = moments.manual({ baseId, userId: owner?.userId ?? null, at: Number(body.at), rule: body.rule });
    return moment ? send(res, 200, moment) : send(res, 409, { error: 'that moment is already there' });
  } catch (error) {
    return send(res, 400, { error: error.message });
  }
}

function send(res, code, body, type = 'application/json; charset=utf-8') {
  res.writeHead(code, { 'Content-Type': type, 'Cache-Control': 'no-store' });
  res.end(typeof body === 'string' ? body : JSON.stringify(body));
}

const server = createServer((req, res) => {
  const url = new URL(req.url, 'http://localhost');
  if (url.pathname.startsWith('/api/replay3d')) return replay3d(req, res, url);
  if (url.pathname === '/api/moments') return momentsApi(req, res);
  if (req.method !== 'GET') return send(res, 405, { error: 'read-only' });
  if (url.pathname === '/') return send(res, 200, readFileSync(PAGE, 'utf8'), 'text/html; charset=utf-8');
  if (url.pathname === '/player') return send(res, 200, readFileSync(PLAYER_PAGE, 'utf8'), 'text/html; charset=utf-8');
  if (url.pathname === '/api/replay') {
    const range = replayRange(url.searchParams);
    if (!range) return send(res, 404, { error: 'unknown player' });
    return send(res, 200, buildTrack(tape, world, range.from, range.to, (id) => players.questLabel(id)));
  }
  if (url.pathname === '/api/minimap') {
    if (!world.planet) return send(res, 404, { error: 'planet not known yet' });
    minimapImage()
      .then((image) => {
        res.writeHead(200, { 'Content-Type': image.type, 'Cache-Control': 'max-age=3600' });
        res.end(image.data);
      })
      .catch((error) => send(res, 502, { error: error.message }));
    return;
  }
  if (url.pathname === '/api/status') return send(res, 200, status());
  if (url.pathname === '/api/bases') return send(res, 200, world.baseList());
  if (url.pathname === '/api/events') {
    const limit = Math.min(Number(url.searchParams.get('limit') || 200), 1000);
    // History rows arrive a few seconds after the wire; ordered by when things happened, not by arrival.
    return send(res, 200, [...world.recent].sort((a, b) => b.t - a.t).slice(0, limit));
  }
  const baseMatch = url.pathname.match(/^\/api\/base\/(\d+)$/);
  if (baseMatch) {
    const base = world.base(Number(baseMatch[1]));
    if (!base) return send(res, 404, { error: 'unknown base' });
    return send(res, 200, { ...world.baseView(base), timeline: base.timeline, player: base.userId ? players.story(base.userId, Date.now()) : null });
  }
  // What the analysis reads; see lib/players.mjs and mcp.mjs.
  if (url.pathname === '/api/players') return send(res, 200, players.list(Date.now()));
  if (url.pathname === '/api/chat') return send(res, 200, chat.list(Math.min(Number(url.searchParams.get('limit') || 100), 300)));
  if (url.pathname === '/api/funnel') return send(res, 200, players.funnel(Date.now()));
  if (url.pathname === '/api/stalls') return send(res, 200, players.stalls(Date.now(), Number(url.searchParams.get('min') || 5)));
  const playerMatch = url.pathname.match(/^\/api\/player\/([\w-]+)$/);
  if (playerMatch) {
    const story = players.story(playerMatch[1], Date.now());
    return story ? send(res, 200, story) : send(res, 404, { error: 'unknown player' });
  }
  send(res, 404, { error: 'not found' });
});

server.on('error', (error) => {
  log(`dashboard: ${error.message}`);
  process.exit(1);
});
startReplayProxy({ port: settings.replayPort, target: settings.replayUi || settings.baseUrl, auth, getSession: (key) => sessions.get(key) ?? null, log });

server.listen(settings.port, '127.0.0.1', () => {
  log(`recorder for ${settings.baseUrl}, dashboard http://127.0.0.1:${settings.port}, ` +
    `keeps ${settings.keepHours} h / ${settings.maxMegabytes} MB`);
  loadConfig().then(loadPlayers).then(() => {
    stream.start();
    feed.start();
    chat.start();
  });
});

setInterval(keyframe, settings.keyframeMinutes * 60_000);
setInterval(writeVolume, VOLUME_EVERY_MS);
setInterval(loadConfig, CONFIG_EVERY_MS);
setInterval(() => moments.tick().catch((error) => log(`moments: ${error.message}`)), 5_000);
// A server just started has no planet yet; the replay needs it for the map.
setInterval(() => world.planet || loadConfig(), 60_000);
setInterval(loadPlayers, PLAYERS_EVERY_MS);
setInterval(() => {
  world.forget(Date.now());
  players.forget(Date.now());
}, 60_000);

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => {
    writeVolume();
    log(`stopped (${signal})`);
    process.exit(0);
  });
}
