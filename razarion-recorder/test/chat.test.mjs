import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { ChatWatch } from '../lib/chat.mjs';
import { answerChat, checkAnswer } from '../lib/assistant.mjs';

const players = { byUser: new Map([['u1', { userId: 'u1' }]]), summary: () => ({ level: 2, currentQuest: { name: 'L2 Build 1 Viper', openMinutes: 3 } }) };

function watch({ answer, posted = [], answering = true } = {}) {
  return new ChatWatch({
    baseUrl: 'http://x', auth: null, players, everyMs: 5000, answering, log: () => {},
    stateFile: join(mkdtempSync(join(tmpdir(), 'chat-')), 'chat.json'),
    answerChat: async () => answer ?? { kind: 'howto', answer: 'Guest-1234: select the Factory and build a Viper.' },
    post: async (text) => posted.push(text),
  });
}

const entry = (id, over = {}) => ({ id, timestamp: new Date().toISOString(), userId: 'u1', userName: 'Guest-1234', message: 'how do i play', ...over });

test('a fresh recorder answers nothing from before it started', async () => {
  const posted = [];
  const chat = watch({ posted });
  chat.receive(entry(26), true);
  await chat.work();
  assert.equal(chat.messages[0].outcome.status, 'before-start');
  assert.equal(posted.length, 0);
});

test('a question is answered once, with the player context kept', async () => {
  const posted = [];
  const chat = watch({ posted });
  chat.lastId = 26;
  chat.receive(entry(27), false);
  chat.receive(entry(27), false);
  await chat.work();
  assert.deepEqual(posted, ['Guest-1234: select the Factory and build a Viper.']);
  assert.equal(chat.messages.length, 1);
  assert.equal(chat.messages[0].outcome.status, 'answered');
  assert.equal(chat.messages[0].player.level, 2);
});

test('the assistant does not answer itself, old messages, or when switched off', async () => {
  const posted = [];
  const chat = watch({ posted });
  chat.lastId = 1;
  chat.receive(entry(2, { userId: null, userName: 'Razarion AI' }), false);
  chat.receive(entry(3, { timestamp: new Date(Date.now() - 3_600_000).toISOString() }), false);
  await chat.work();
  assert.deepEqual(chat.messages.map((m) => m.outcome.status), ['assistant', 'stale']);
  const off = watch({ posted, answering: false });
  off.lastId = 1;
  off.receive(entry(2), false);
  assert.equal(off.messages[0].outcome.status, 'answering-off');
  assert.equal(posted.length, 0);
});

test('the same question sent twice is answered once', async () => {
  const posted = [];
  const chat = watch({ posted });
  chat.lastId = 1;
  chat.receive(entry(2, { message: 'how to level up?' }), false);
  chat.receive(entry(3, { message: 'How to level up? ' }), false);
  await chat.work();
  assert.deepEqual(chat.messages.map((m) => m.outcome.status), ['answered', 'repeat']);
  assert.equal(posted.length, 1);
});

test('no answer for chatter', async () => {
  const posted = [];
  const chat = watch({ posted, answer: { kind: 'none', answer: null } });
  chat.lastId = 1;
  chat.receive(entry(2, { message: 'game from temu' }), false);
  await chat.work();
  assert.equal(chat.messages[0].outcome.status, 'not-a-question');
  assert.equal(posted.length, 0);
});

test('checks refuse links, promises, other players and length', () => {
  assert.deepEqual(checkAnswer('Guest-1: close other tabs.'), []);
  assert.ok(checkAnswer('see https://razarion.com').length);
  assert.ok(checkAnswer('this will be fixed soon').length);
  assert.ok(checkAnswer('ask WarMind', ['WarMind']).length);
  assert.ok(checkAnswer('x'.repeat(500)).length);
});

test('answerChat reads the reply and refuses a bad one', async () => {
  const run = (reply) => async () => ({ text: JSON.stringify(reply) });
  const message = { userName: 'Guest-1', message: 'laggy' };
  assert.deepEqual(await answerChat({ message, player: null, recent: [], otherNames: [] }, run({ kind: 'technical', answer: 'Guest-1: close other tabs.' })),
    { kind: 'technical', answer: 'Guest-1: close other tabs.' });
  assert.deepEqual(await answerChat({ message, player: null, recent: [], otherNames: [] }, run({ kind: 'none', answer: null })), { kind: 'none', answer: null });
  assert.ok((await answerChat({ message, player: null, recent: [], otherNames: [] }, run({ kind: 'technical', answer: 'fixed soon' }))).error);
});
