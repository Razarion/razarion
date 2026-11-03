// node --test test/
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { Moments, followedBase, matches } from '../lib/moments.mjs';

const T0 = Date.parse('2026-11-03T12:00:00Z');
const leftIsland = { type: 'QUEST_PASSED', source: 'HUMAN', userId: 'u1', baseId: 5, questId: 392, serverTime: new Date(T0).toISOString() };
const lostToBot = { type: 'ITEM_DESTROYED', source: 'BOT', botId: 7, baseId: 3, targetUserId: 'u1', targetBaseId: 5, itemTypeName: 'Viper' };
const botBaseDown = { type: 'BASE_DEFEATED', source: 'HUMAN', userId: 'u1', baseId: 5, targetBaseId: 3 };

test('rules match on the row, actor and target by side', () => {
  assert.equal(matches({ type: 'QUEST_PASSED', questId: 392 }, leftIsland), true);
  assert.equal(matches({ type: 'QUEST_PASSED', questId: 391 }, leftIsland), false);
  assert.equal(matches({ type: 'ITEM_DESTROYED', actor: 'bot', target: 'human' }, lostToBot), true);
  assert.equal(matches({ type: 'ITEM_DESTROYED', actor: 'human' }, lostToBot), false);
  assert.equal(matches({ type: 'BASE_DEFEATED', actor: 'human', target: 'bot' }, botBaseDown), true);
  assert.equal(matches({ type: 'BASE_DEFEATED', target: 'human' }, botBaseDown), false);
  // The followed base is the player's, whichever side of the row it is on
  assert.deepEqual(followedBase({ actor: 'bot', target: 'human' }, lostToBot), { baseId: 5, userId: 'u1' });
  assert.deepEqual(followedBase({ type: 'QUEST_PASSED' }, leftIsland), { baseId: 5, userId: 'u1' });
});

function setup(oldest) {
  const dir = mkdtempSync(join(tmpdir(), 'moments-'));
  const rulesFile = join(dir, 'rules.json');
  writeFileSync(rulesFile, JSON.stringify({ rules: [
    { id: 'left-noob-island', enabled: true, title: 'Leaves', when: { type: 'QUEST_PASSED', questId: 392 }, before: 40, after: 8, cooldownMinutes: 60, oncePerBase: true },
    { id: 'off', enabled: false, title: 'Off', when: { type: 'QUEST_PASSED' }, before: 5, after: 5 },
  ] }));
  const tape = { oldest, entries: [] };
  return new Moments({ dir, rulesFile, tape, players: { byUser: new Map() }, sessions: new Map(), settings: {}, log: () => {} });
}

test('a matching row marks a moment around it; cooldown, once per base and the tape gate it', () => {
  const moments = setup(T0 - 10 * 60_000);
  moments.onHistory(leftIsland, T0);
  assert.equal(moments.items.length, 1);
  const m = moments.items[0];
  assert.deepEqual([m.rule, m.baseId, m.from, m.to, m.status], ['left-noob-island', 5, T0 - 40_000, T0 + 8_000, 'waiting']);
  // The same base again, and another base within the cooldown: nothing
  moments.onHistory({ ...leftIsland, serverTime: new Date(T0 + 120 * 60_000).toISOString() }, T0);
  moments.onHistory({ ...leftIsland, baseId: 6, userId: 'u2', serverTime: new Date(T0 + 5 * 60_000).toISOString() }, T0);
  assert.equal(moments.items.length, 1);
  // Another base after the cooldown: yes
  moments.onHistory({ ...leftIsland, baseId: 6, userId: 'u2', serverTime: new Date(T0 + 61 * 60_000).toISOString() }, T0);
  assert.equal(moments.items.length, 2);
  // A list for the dashboard carries no user id
  assert.equal('userId' in moments.list()[0], false);
});

test('a moment whose start the tape does not hold is not marked', () => {
  const moments = setup(T0 - 20_000);
  moments.onHistory(leftIsland, T0);
  assert.equal(moments.items.length, 0);
});

test('the settings name the social engine', async () => {
  const { settings } = await import('../lib/config.mjs');
  assert.match(settings.engineUrl, /^http:\/\//);
});
