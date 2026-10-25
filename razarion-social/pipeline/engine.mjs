#!/usr/bin/env node
// The engine: the social pipeline as a service that runs on its own, in the background, on this PC.
// It makes posts, approves them, publishes them on a schedule and keeps the logins alive - and
// serves the dashboard where all of that is set and watched.
//
//   node engine.mjs                   # http://127.0.0.1:4711
//   scheduled\install-engine.ps1      # as a Windows task, started at logon, hidden
//
// Every minute it looks at the clock (lib/schedule.mjs, state/schedule.json):
//
//   - a slot is due        -> one post per network, the oldest approved first (publish*.mjs)
//   - the buffer is low    -> plan.mjs --auto makes the next post, lib/approve.mjs approves it
//   - twice a day          -> the Instagram token is renewed (refresh_token.mjs)
//   - every six hours      -> the Google login is tried, so an expired one shows before a slot
//
// One job runs at a time, each as the same scripts a person would type, so what the engine does can
// always be done by hand as well. Their output goes to state/engine.log, what came of each job to
// state/engine.json, which the dashboard shows as the history.
//
// A network whose login fails is held - the others go on - and a Windows notification links to the
// dashboard, where one click starts the login again (Google, X) or a new token is pasted
// (Instagram, Facebook).
//
// Local only: it listens on 127.0.0.1, and every write needs the X-Review header (lib/review-routes.mjs).

import { createServer } from 'node:http';
import { spawn } from 'node:child_process';
import { appendFileSync, existsSync, renameSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { PIPELINE_ROOT, STATE_DIR, INSTAGRAM_TOKEN_FILE, ensureDir, readJson, writeJson } from './lib/paths.mjs';
import { handleReview, sendJson, sendError, readBody, httpError } from './lib/review-routes.mjs';
import { loadSchedule, saveSchedule, nextSlots, dueSlot, mayProduce, postsPerWeek } from './lib/schedule.mjs';
import { loadQueue, bufferLevel, REVIEW_TARGETS } from './lib/queue.mjs';
import { autoApprove } from './lib/approve.mjs';
import { loadLedger } from './lib/ledger.mjs';
import { notify } from './lib/notify.mjs';
import { authoriseGoogle, googleAccessToken } from '../src/auth/google.mjs';
import { authoriseX } from '../src/auth/x.mjs';
import { parseArgs } from './lib/args.mjs';

const args = parseArgs(process.argv.slice(2));
const PORT = Number(args.port || 4711);
const HOST = '127.0.0.1';
const ADDRESS = `http://${HOST}:${PORT}`;

const ENGINE_FILE = join(STATE_DIR, 'engine.json');
const LOG_FILE = join(STATE_DIR, 'engine.log');
const LOG_MAX_BYTES = 5_000_000;

const TICK_MS = 60_000;
const HOUR = 3_600_000;
const REFRESH_EVERY = 12 * HOUR;
const AUTH_CHECK_EVERY = 6 * HOUR;
// After a production run, the next waits this long - a battle that found no fight is not retried
// every minute.
const PRODUCE_EVERY = HOUR / 2;
// No filming in the last minutes before a slot, so the slot is not kept waiting behind it.
const QUIET_BEFORE_SLOT = HOUR / 3;
const HISTORY = 200;

const NETS = ['ig', 'fb', 'x', 'yt'];
const PUBLISHER = { ig: 'publish.mjs', fb: 'publish_fb.mjs', x: 'publish_x.mjs', yt: 'publish_youtube.mjs' };

// What a failed publish says when the login is the problem rather than the post. The Meta hints are
// the ones lib/instagram.mjs and lib/facebook.mjs print for error 190.
const AUTH_FAILURE = {
  yt: /invalid_grant|Not authorised for "google"|oauth2\.googleapis\.com\/token/,
  x: /api\.x\.com\/2\/oauth2\/token|Not authorised for "x"|invalid_request|unauthorized_client/,
  ig: /Access token invalid or expired|OAuthException/,
  fb: /Access token invalid or expired|OAuthException/,
};

// Where the login is renewed. Google and X go through the dashboard's own flow; Meta's tokens are
// made in its dashboards and pasted in.
const RECONNECT = {
  yt: { kind: 'oauth', href: '/auth/google' },
  x: { kind: 'oauth', href: '/auth/x' },
  ig: { kind: 'paste', href: 'https://developers.facebook.com/apps/', hint: 'App → Instagram → API setup with Instagram login → Generate token. Paste the token here.' },
  fb: { kind: 'paste', href: 'https://developers.facebook.com/tools/explorer/', hint: 'Graph API Explorer: user token with pages_show_list, pages_manage_posts, pages_read_engagement. Paste it here; it becomes a Page token that does not expire.' },
};

// ---------------------------------------------------------------------------------------------
// State and log

function loadState() {
  const s = readJson(ENGINE_FILE, {}) || {};
  return {
    lastSlot: s.lastSlot ?? null,
    lastRefresh: s.lastRefresh ?? null,
    lastAuthCheck: s.lastAuthCheck ?? null,
    lastProduce: s.lastProduce ?? null,
    health: Object.fromEntries(NETS.map((n) => [n, s.health?.[n] ?? { status: 'unknown', at: null, message: null }])),
    runs: s.runs ?? [],
  };
}

const state = loadState();
const saveState = () => writeJson(ENGINE_FILE, state);

function log(line) {
  ensureDir(STATE_DIR);
  try {
    if (existsSync(LOG_FILE) && statSync(LOG_FILE).size > LOG_MAX_BYTES) renameSync(LOG_FILE, LOG_FILE + '.1');
  } catch { /* a full log is no reason to stop */ }
  const stamp = new Date().toLocaleString('sv-SE');
  appendFileSync(LOG_FILE, line.split('\n').map((l) => `${stamp}  ${l}`).join('\n') + '\n', 'utf8');
}

function remember(run) {
  state.runs.unshift({ at: new Date().toISOString(), ...run });
  state.runs.length = Math.min(state.runs.length, HISTORY);
  saveState();
}

function setHealth(net, status, message = null) {
  const before = state.health[net]?.status;
  state.health[net] = { status, message: message ? String(message).slice(0, 400) : null, at: new Date().toISOString() };
  saveState();
  if (status === 'auth' && before !== 'auth') {
    log(`!!! ${REVIEW_TARGETS[net].label}: login needed - ${message}`);
    notify(`Razarion Social: ${REVIEW_TARGETS[net].label} needs a new login`,
      'Posting there is paused until you reconnect. Click to open the dashboard.', `${ADDRESS}/#engine`);
  }
}

// ---------------------------------------------------------------------------------------------
// Jobs

let job = null;

/** Runs one pipeline script; its output goes to the log. */
function runNode(script, scriptArgs = [], extraEnv = {}) {
  return new Promise((resolve) => {
    if (job) job.step = [script, ...scriptArgs].join(' ');
    log(`--- node ${[script, ...scriptArgs].join(' ')}`);
    const child = spawn(process.execPath, [join(PIPELINE_ROOT, script), ...scriptArgs], {
      cwd: PIPELINE_ROOT, windowsHide: true, env: { ...process.env, ...extraEnv }, stdio: ['ignore', 'pipe', 'pipe'],
    });
    let output = '';
    child.stdout.on('data', (d) => { output += d; });
    child.stderr.on('data', (d) => { output += d; });
    child.on('error', (err) => { output += `\n${err.message}`; });
    child.on('close', (code) => {
      // eslint-disable-next-line no-control-regex
      output = output.replace(/\x1b\[[0-9;]*m/g, '');
      if (output.trim()) log(output.trimEnd().split('\n').map((l) => '    ' + l).join('\n'));
      if (code !== 0) log(`    exit ${code}`);
      resolve({ code, output });
    });
  });
}

/** Starts `fn` as the one job, unless one is running. Resolves to false when it did not start. */
async function runJob(kind, fn) {
  if (job) return false;
  job = { kind, step: null, startedAt: new Date().toISOString() };
  log(`=== ${kind}`);
  try {
    await fn();
  } catch (err) {
    log(`!!! ${kind} failed: ${err.stack || err.message}`);
    remember({ kind, error: err.message });
  } finally {
    job = null;
  }
  return true;
}

/** The line a publisher prints for the post it is on: "  > <first words of the text>". */
function previewOf(output) {
  const m = /^\s+> (?!waiting|X:|YouTube:|composed|converted|data\/)(.+)$/m.exec(output);
  return m ? m[1].trim() : null;
}

function failureOf(output) {
  const lines = output.split('\n').map((l) => l.trim()).filter(Boolean);
  const fail = lines.find((l) => /\[fail\]|error_description|Error:/.test(l));
  return fail ?? lines.at(-1) ?? 'failed';
}

/** One post on one network. Returns what came of it, for the history. */
async function publishOne(net) {
  const r = await runNode(PUBLISHER[net], ['--live', '--limit', '1']);
  const preview = previewOf(r.output);
  if (r.code !== 0) {
    if (AUTH_FAILURE[net].test(r.output)) {
      setHealth(net, 'auth', failureOf(r.output));
      return { net, outcome: 'login', preview, message: failureOf(r.output) };
    }
    setHealth(net, 'error', failureOf(r.output));
    return { net, outcome: 'failed', preview, message: failureOf(r.output) };
  }
  setHealth(net, 'ok');
  if (/published as|X: posted|uploaded as/.test(r.output)) return { net, outcome: 'published', preview };
  return { net, outcome: 'nothing' };
}

/** A slot: media up, then one post per enabled network that has a working login. */
async function publishSlot(slot, only = null) {
  const settings = loadSchedule();
  const nets = (only ? [only] : NETS).filter((n) => settings.networks[n]);
  const results = [];
  if (nets.includes('ig')) await runNode('upload_media.mjs');
  if (nets.includes('fb')) await runNode('upload_media.mjs', ['--source', 'fb']);
  for (const net of nets) {
    if (state.health[net].status === 'auth') {
      results.push({ net, outcome: 'login', message: 'waiting for a new login' });
      continue;
    }
    results.push(await publishOne(net));
  }
  // How far the last posts got. Asks only about posts younger than seven days.
  await runNode('metrics.mjs');
  remember({ kind: only ? 'publish-now' : 'publish', slot: slot?.toISOString() ?? null, results });
}

/** The planner makes one post if the buffer wants one; the approval runs right after. */
async function produce() {
  const r = await runNode('plan.mjs', ['--auto', '--max', '1']);
  state.lastProduce = new Date().toISOString();
  const made = /post\(s\) made/.test(r.output);
  const approval = autoApprove(loadSchedule());
  logApproval(approval);
  const last = made ? loadLedger().items.at(-1) : null;
  remember({
    kind: 'produce',
    made: last ? { id: last.id, format: last.format, subject: last.subject, tone: last.tone ?? null } : null,
    note: made ? null : (/Every format ran within its gap|No format had anything|full enough/.exec(r.output)?.[0] ?? (r.code ? failureOf(r.output) : 'nothing made')),
    approved: approval.approved.length,
    held: approval.held,
  });
}

function logApproval({ approved, held }) {
  for (const a of approved) log(`    approved ${a.id} on ${a.net}`);
  for (const h of held) log(`    held ${h.id} on ${h.net}: ${h.reason}`);
}

async function refreshTokens() {
  const r = await runNode('refresh_token.mjs');
  state.lastRefresh = new Date().toISOString();
  const stored = readJson(INSTAGRAM_TOKEN_FILE);
  const daysLeft = stored ? Math.floor((Date.parse(stored.expires_at) - Date.now()) / (24 * HOUR)) : null;
  if (r.code !== 0 && AUTH_FAILURE.ig.test(r.output)) setHealth('ig', 'auth', failureOf(r.output));
  saveState();
  if (r.code !== 0) remember({ kind: 'refresh', error: failureOf(r.output), daysLeft });
}

async function checkGoogle() {
  state.lastAuthCheck = new Date().toISOString();
  try {
    await googleAccessToken();
    if (state.health.yt.status !== 'ok') setHealth('yt', 'ok');
  } catch (err) {
    const text = `${err.message} ${JSON.stringify(err.body ?? '')}`;
    if (AUTH_FAILURE.yt.test(text)) setHealth('yt', 'auth', text);
    else log(`Google check: ${text}`);
  }
  saveState();
}

// ---------------------------------------------------------------------------------------------
// The clock

async function tick() {
  const settings = loadSchedule();
  if (settings.paused || job) return;
  const now = new Date();
  const since = (iso) => (iso ? now - Date.parse(iso) : Infinity);

  // What came from elsewhere - a post made by hand and approved, a setting turned on - is approved
  // here too, not only right after the planner.
  logApproval(autoApprove(settings));

  if (since(state.lastRefresh) > REFRESH_EVERY) return void runJob('refresh tokens', refreshTokens);

  const slot = dueSlot(settings, now);
  if (slot && (!state.lastSlot || slot > Date.parse(state.lastSlot))) {
    // Recorded before the slot runs, so a crash in the middle cannot post the same slot twice.
    state.lastSlot = slot.toISOString();
    saveState();
    return void runJob('publish', () => publishSlot(slot));
  }

  if (settings.networks.yt && since(state.lastAuthCheck) > AUTH_CHECK_EVERY) return void runJob('check Google login', checkGoogle);

  const next = nextSlots(settings, 1, now)[0];
  if (next && next - now < QUIET_BEFORE_SLOT) return;
  if (!mayProduce(settings, now) || since(state.lastProduce) < PRODUCE_EVERY) return;
  if (bufferLevel(settings.networks).level >= settings.buffer) return;
  return void runJob('produce', produce);
}

// ---------------------------------------------------------------------------------------------
// What the dashboard reads

function firstLine(entry, net) {
  const text = net === 'yt' ? entry.title : net === 'ig' ? entry.caption : net === 'fb' ? entry.message : entry.text;
  return String(text ?? '').split('\n')[0].slice(0, 140);
}

/**
 * Per network the posts still to go out, in the order the publishers take them (oldest first),
 * each with the slot it will go out in. A held one and one waiting for a person have no time.
 */
function upcoming(settings) {
  const slots = nextSlots(settings, 40);
  const ledger = new Map(loadLedger().items.map((e) => [e.id, e]));
  const out = {};
  for (const net of NETS) {
    const list = loadQueue()
      .filter((p) => p.entries[net] && (p.entries[net].state === 'ok' || p.entries[net].state === 'review'))
      .sort((a, b) => Date.parse(a.date) - Date.parse(b.date));
    let i = 0;
    out[net] = list.map((p) => {
      const e = p.entries[net];
      const scheduled = e.state === 'ok' && settings.networks[net] && state.health[net].status !== 'auth';
      return {
        id: p.id,
        state: e.state,
        held: e.entry.held ?? null,
        format: ledger.get(p.id)?.format ?? null,
        auto: !!ledger.get(p.id)?.auto,
        text: firstLine(e.entry, net),
        at: scheduled ? (slots[i++]?.toISOString() ?? null) : null,
      };
    });
  }
  return out;
}

function engineView() {
  const settings = loadSchedule();
  const stored = readJson(INSTAGRAM_TOKEN_FILE);
  return {
    settings,
    postsPerWeek: postsPerWeek(settings),
    next: nextSlots(settings, 8).map((d) => d.toISOString()),
    buffer: bufferLevel(settings.networks),
    job,
    health: state.health,
    reconnect: RECONNECT,
    instagramTokenExpires: stored?.expires_at ?? null,
    lastSlot: state.lastSlot,
    lastProduce: state.lastProduce,
    upcoming: upcoming(settings),
    runs: state.runs.slice(0, 60),
  };
}

// ---------------------------------------------------------------------------------------------
// Logins from the dashboard

// Per network, a login started from the dashboard and not finished yet (the flow waits 5 minutes).
const authInFlight = new Set();

/** Starts Google's or X's flow and answers the click with a redirect to the provider's page. */
function startOAuth(net, res) {
  if (authInFlight.has(net)) throw httpError(409, `A ${REVIEW_TARGETS[net].label} login is still open - finish it in the other tab, or wait 5 minutes for it to time out.`);
  const authorise = net === 'yt' ? authoriseGoogle : authoriseX;
  authInFlight.add(net);
  let answered = false;
  authorise({
    doneUrl: `${ADDRESS}/#engine`,
    onAuthUrl: (url) => {
      answered = true;
      res.writeHead(302, { Location: url }).end();
    },
  }).then(() => {
    log(`${REVIEW_TARGETS[net].label}: login renewed from the dashboard`);
    setHealth(net, 'ok');
    remember({ kind: 'login', net, ok: true });
  }).catch((err) => {
    log(`${REVIEW_TARGETS[net].label}: login failed - ${err.message}`);
    remember({ kind: 'login', net, ok: false, error: err.message });
    if (!answered) sendJson(res, 500, { error: err.message });
  }).finally(() => {
    authInFlight.delete(net);
  });
}

async function pasteToken(net, token) {
  token = String(token ?? '').trim();
  if (token.length < 20) throw httpError(422, 'That does not look like a token.');
  if (net === 'ig') {
    // A token generated in the Meta dashboard for Instagram login is long-lived: 60 days, and from
    // then on refresh_token.mjs renews it.
    writeJson(INSTAGRAM_TOKEN_FILE, {
      access_token: token,
      refreshed_at: new Date().toISOString(),
      expires_at: new Date(Date.now() + 59 * 24 * HOUR).toISOString(),
    });
  } else if (net === 'fb') {
    const r = await runNode('fb_token.mjs', [], { FB_USER_TOKEN: token });
    if (r.code !== 0) throw httpError(422, `fb_token.mjs: ${failureOf(r.output)}`);
  } else {
    throw httpError(400, `No token to paste for ${net}.`);
  }
  setHealth(net, 'ok');
  remember({ kind: 'login', net, ok: true });
}

// ---------------------------------------------------------------------------------------------
// The server

async function handleEngine(req, res, url) {
  const write = req.method === 'POST';
  if (write && req.headers['x-review'] !== '1') throw httpError(403, 'Missing X-Review header.');

  if (req.method === 'GET' && url.pathname === '/api/engine') return sendJson(res, 200, engineView());

  if (write && url.pathname === '/api/settings') {
    saveSchedule(await readBody(req));
    log('settings changed on the dashboard');
    return sendJson(res, 200, engineView());
  }

  if (write && url.pathname === '/api/run') {
    const { action, net } = await readBody(req);
    let started;
    if (action === 'produce') started = runJob('produce (by hand)', produce);
    else if (action === 'publish' && NETS.includes(net)) started = runJob(`publish ${net} (by hand)`, () => publishSlot(null, net));
    else throw httpError(400, 'Unknown action.');
    if (!(await Promise.race([started, new Promise((r) => setTimeout(() => r(true), 50))]))) {
      throw httpError(409, `Busy: ${job?.kind}. Try again when it is done.`);
    }
    return sendJson(res, 200, engineView());
  }

  if (write && url.pathname === '/api/health/reset') {
    const { net } = await readBody(req);
    if (!NETS.includes(net)) throw httpError(400, 'Unknown network.');
    setHealth(net, 'unknown', 'reset by hand - the next post shows whether it works');
    return sendJson(res, 200, engineView());
  }

  if (write && url.pathname === '/api/token') {
    const { net, token } = await readBody(req);
    await pasteToken(net, token);
    return sendJson(res, 200, engineView());
  }

  if (req.method === 'GET' && (url.pathname === '/auth/google' || url.pathname === '/auth/x')) {
    return startOAuth(url.pathname === '/auth/google' ? 'yt' : 'x', res);
  }

  res.writeHead(404).end();
}

const server = createServer(async (req, res) => {
  const url = new URL(req.url, ADDRESS);
  if (await handleReview(req, res, url)) return;
  try {
    await handleEngine(req, res, url);
  } catch (err) {
    sendError(res, err);
  }
});

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    // Most likely the engine already runs; a second one would publish the same slot twice.
    log(`Port ${PORT} is taken - the engine (or review.mjs) is already running. This one stops.`);
    console.error(`Port ${PORT} is taken - is the engine already running? ${ADDRESS}`);
    process.exit(0);
  }
  log(`!!! server: ${err.message}`);
  process.exit(1);
});

server.listen(PORT, HOST, () => {
  log(`Engine started at ${ADDRESS} (pid ${process.pid})`);
  console.log(`Razarion Social engine at ${ADDRESS}`);
  if (args.open && process.platform === 'win32') spawn('cmd', ['/c', 'start', '', ADDRESS], { detached: true, stdio: 'ignore' }).unref();
  const loop = () => tick().catch((err) => log(`!!! tick: ${err.stack || err.message}`));
  setTimeout(loop, 5_000);
  setInterval(loop, TICK_MS);
});

process.on('uncaughtException', (err) => log(`!!! uncaught: ${err.stack || err.message}`));
process.on('unhandledRejection', (err) => log(`!!! unhandled: ${err?.stack || err}`));
