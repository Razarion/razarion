// node --test test/
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Tape } from '../lib/tape.mjs';
import { ReplaySession, worldAt } from '../lib/replay-server.mjs';

const NOW = Date.now();
const T0 = NOW - 60_000;
const msg = (t, name, payload) => ({ t, segment: 1, name, text: `${name}#${JSON.stringify(payload)}` });
const item = (id, baseId, x) => ({ id, itemTypeId: 3, baseId, health: 1, syncPhysicalAreaInfo: { position: { x, y: 0 } } });

function tape() {
  const tape = new Tape({ keepMs: 3_600_000, maxBytes: 1e9 });
  const keyframe = msg(T0, 'INITIAL_SLAVE_SYNC_INFO', {
    tickCount: 1000, actualBaseId: null,
    playerBaseInfos: [{ baseId: 9, character: 'BOT' }],
    syncBaseItemInfos: [item(1, 9, 10)], syncResourceItemInfos: [{ id: 50, amount: 3 }], syncBoxItemInfos: [],
  });
  tape.add(keyframe);
  tape.addKeyframe({ t: T0, text: keyframe.text, source: 'connect' });
  tape.add(msg(T0 + 1_000, 'TICK_INFO', { tickCount: 1010, syncBaseItemInfos: [item(1, 9, 12)] }));
  tape.add(msg(T0 + 2_000, 'BASE_CREATED', { baseId: 5, character: 'HUMAN', userId: 'u1' }));
  tape.add(msg(T0 + 2_100, 'TICK_INFO', { tickCount: 1021, syncBaseItemInfos: [item(2, 5, 100)] }));
  tape.add(msg(T0 + 3_000, 'SYNC_ITEM_DELETED', { id: 50, explode: false }));
  tape.add(msg(T0 + 3_500, 'HISTORY', { type: 'LEVEL_UP' }));
  return tape;
}

test('the world at a time: keyframe plus everything after it', () => {
  const world = worldAt(tape(), T0 + 3_200);
  assert.equal(world.items.get(1).syncPhysicalAreaInfo.position.x, 12);
  assert.equal(world.items.get(2).baseId, 5);
  assert.ok(world.bases.has(5));
  assert.equal(world.resources.size, 0);
  assert.equal(world.tickCount, 1021);
  assert.equal(worldAt(tape(), T0 - 1), null);
});

test('a session sends the world as the followed player sees it, then the tape as it happened', async () => {
  const sent = [];
  const socket = { send: (text) => sent.push(text), on() {}, close() {} };
  const session = new ReplaySession({ tape: tape(), baseId: 5, at: T0, log: () => {} });
  // The base does not stand at T0: the replay starts once it does
  assert.equal(session.anchorT, T0 + 3_500);
  session.attach(socket);
  const world = JSON.parse(sent[0].slice(sent[0].indexOf('#') + 1));
  assert.equal(sent[0].split('#')[0], 'INITIAL_SLAVE_SYNC_INFO');
  assert.equal(world.actualBaseId, 5);
  // 1.4 s after the last tick info at 1021
  assert.equal(world.tickCount, 1035);
  assert.deepEqual(world.syncBaseItemInfos.map((i) => i.id).sort(), [1, 2]);

  // Asked for the tick, it answers the one it last sent
  session.onClientMessage('TICK_COUNT_REQUEST#null');
  assert.match(sent.at(-1), /^TICK_COUNT_RESPONSE#103\d$/);

  // A jump back sends a new world and the history rows never go to the client
  sent.length = 0;
  session.seek(T0 + 2_050);
  session.stream();
  assert.equal(sent[0].split('#')[0], 'INITIAL_SLAVE_SYNC_INFO');
  assert.ok(sent.every((text) => !text.startsWith('HISTORY#')));
  session.close();
});
