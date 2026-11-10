#!/usr/bin/env node
// The recorder for an AI: an MCP server over stdio that asks the running recorder (recorder.mjs,
// http://127.0.0.1:4712) and hands back what it knows about the players.
//
// It holds nothing itself - Claude Code starts it per session (.mcp.json), the recorder runs on as
// the Windows task. Players are user ids and base ids here, never names.

import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';

const RECORDER = (process.env.RECORDER_URL || 'http://127.0.0.1:4712').replace(/\/$/, '');

async function get(path) {
  let res;
  try {
    res = await fetch(RECORDER + path);
  } catch {
    throw new Error(`The recorder is not running at ${RECORDER} (node razarion-recorder/recorder.mjs).`);
  }
  if (!res.ok) throw new Error(`${path}: HTTP ${res.status} ${await res.text()}`);
  return res.json();
}

const ok = (data) => ({ content: [{ type: 'text', text: JSON.stringify(data, null, 1) }] });

const server = new McpServer({ name: 'razarion-recorder', version: '1.0.0' });

server.tool(
  'recorder_status',
  'Whether the recorder is connected to the game, which server it watches, since when, and how much it holds. ' +
    'Call first: every other answer only covers the time since the recorder started (at most its keep window).',
  {},
  async () => {
    const s = await get('/api/status');
    return ok({
      server: s.server,
      connection: s.connection.state,
      historyFeed: s.feed.state,
      recordingSince: new Date(s.tape.startedAt).toISOString(),
      oldestHeld: s.tape.held.oldest ? new Date(s.tape.held.oldest).toISOString() : null,
      world: s.world,
      tapeMegabytes: Math.round(s.tape.held.bytes / 1048576),
    });
  },
);

server.tool(
  'list_players',
  'Every human player the recorder has seen: status (active / idle / gone), level, current quest and how long it is open, ' +
    'quests passed, units lost to bots or players, whether they left the noob island. watchedFromStart=false means the ' +
    'player was already playing when the recorder started, so their early steps are missing.',
  { status: z.enum(['active', 'idle', 'gone', 'all']).optional().describe('Filter by status; default all') },
  async ({ status }) => {
    const list = await get('/api/players');
    return ok(status && status !== 'all' ? list.filter((p) => p.status === status) : list);
  },
);

server.tool(
  'player_story',
  "One player's time on the planet in order: base placed, quests started and passed, first unit of each type, " +
    'units lost, level-ups, and pauses of 3+ minutes without any action. minute = minutes since their base was placed.',
  { userId: z.string().describe('User id from list_players') },
  async ({ userId }) => ok(await get('/api/player/' + encodeURIComponent(userId))),
);

server.tool(
  'funnel',
  'The quest funnel over the players whose start the recorder saw: per step how many reached it, the median minutes from ' +
    'placing the base, and how many stopped there (still active, idle, or gone). Steps are in the order the game runs them.',
  {},
  async () => ok(await get('/api/funnel')),
);

server.tool(
  'stalls',
  'Players with a base whose current quest has been open at least minMinutes, longest first, with what they did since ' +
    'the quest started. The place to look for where players get stuck.',
  { minMinutes: z.number().optional().describe('Default 5') },
  async ({ minMinutes }) => ok(await get('/api/stalls?min=' + (minMinutes ?? 5))),
);

server.tool(
  'recent_events',
  'The latest events on the planet involving human players, newest first: bases created and gone, units built and ' +
    'destroyed (with the killer type), boxes picked, history rows (quests, levels).',
  { limit: z.number().optional().describe('Default 100, at most 1000') },
  async ({ limit }) => {
    const events = await get('/api/events?limit=' + (limit ?? 100));
    // Base names are the players' names; the events carry ids only already, userIds too.
    return ok(events.map((e) => ({ ...e, t: new Date(e.t).toISOString() })));
  },
);

server.tool(
  'chat_log',
  'The planet chat, newest first: what players wrote, with their level and open quest at that moment, and what the ' +
    'chat assistant (Razarion AI) made of it - answered (kind technical/howto and the answer), not-a-question, error, ' +
    'stale, before-start; news = the server announcing a player who left the noob island. The place to read what players ask and complain about.',
  { limit: z.number().optional().describe('Default 100, at most 300') },
  async ({ limit }) => {
    const messages = await get('/api/chat?limit=' + (limit ?? 100));
    // Players by user id, as everywhere here; only the assistant is named.
    return ok(messages.map((m) => ({
      id: m.id,
      t: new Date(m.t).toISOString(),
      from: m.userId ? m.userId : m.userName,
      message: m.message,
      level: m.player?.level ?? null,
      quest: m.player?.currentQuest?.name ?? null,
      outcome: m.outcome,
    })));
  },
);

await server.connect(new StdioServerTransport());
