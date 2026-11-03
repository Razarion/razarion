// node --test test/
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Tape } from '../lib/tape.mjs';
import { World } from '../lib/world.mjs';
import { buildTrack } from '../lib/replay.mjs';

const T0 = 1_000_000;
const msg = (t, name, payload) => ({ t, segment: 1, name, text: `${name}#${JSON.stringify(payload)}` });
const unit = (id, x, y, fields = {}) => ({
  id, itemTypeId: 3, baseId: 5, syncPhysicalAreaInfo: { position: { x, y }, ...fields.area }, ...fields.item,
});

function setup() {
  const tape = new Tape({ keepMs: 3_600_000, maxBytes: 1e9 });
  const world = new World();
  world.setConfig({ planet: { id: 1, width: 1000, height: 800 }, baseItemTypes: [{ id: 3, name: 'Viper', radius: 2, speed: 10 }] });
  const keyframe = msg(T0, 'INITIAL_SLAVE_SYNC_INFO', {
    playerBaseInfos: [{ baseId: 5, character: 'HUMAN', userId: 'u1', name: 'n' }, { baseId: 9, character: 'BOT' }],
    syncBaseItemInfos: [unit(1, 10, 10), unit(2, 50, 50, { item: { baseId: 9 } })],
  });
  tape.add(keyframe);
  tape.addKeyframe({ t: T0, text: keyframe.text, source: 'connect' });
  return { tape, world };
}

test('the world at the start, then what moved, fired and died', () => {
  const { tape, world } = setup();
  // Before the stretch: unit 1 is sent off on a path - the track must start from there
  tape.add(msg(T0 + 1_000, 'TICK_INFO', { syncBaseItemInfos: [unit(1, 10, 10, { area: { wayPositions: [{ x: 30, y: 10 }] } })] }));
  tape.add(msg(T0 + 5_000, 'TICK_INFO', { syncBaseItemInfos: [unit(1, 30, 10), unit(3, 70, 70)] }));
  tape.add(msg(T0 + 6_000, 'PROJECTILE_FIRED', { actorSyncBaseItemId: 1, targetSyncBaseItemId: 2, targetPosition: { x: 50, y: 50 } }));
  tape.add(msg(T0 + 7_000, 'SYNC_ITEM_DELETED', { id: 2, explode: true }));
  tape.add(msg(T0 + 8_000, 'HISTORY', { type: 'QUEST_PASSED', source: 'HUMAN', userId: 'u1', baseId: 5, questId: 358, serverTime: new Date(T0 + 7_900).toISOString() }));

  const track = buildTrack(tape, world, T0 + 2_000, T0 + 10_000, (id) => `Q${id}`);
  assert.equal(track.from, T0 + 2_000);
  assert.deepEqual(track.planet, { id: 1, width: 1000, height: 800 });
  assert.deepEqual(track.types[3], { name: 'Viper', radius: 2, speed: 10 });
  assert.equal(track.bases[5].human, true);
  assert.equal(track.bases[9].human, false);

  const byId = Object.fromEntries(track.items.map((item) => [item.id, item]));
  // Unit 1: at the start on its way (the path is kept), then where it arrived
  assert.deepEqual(byId[1].p, [[T0 + 2_000, 10, 10, [30, 10], 0], [T0 + 5_000, 30, 10, null, 0]]);
  assert.equal(byId[1].born, null);
  // Unit 2: there from the start, blown up
  assert.equal(byId[2].died, T0 + 7_000);
  assert.equal(byId[2].explode, true);
  // Unit 3: born in the stretch
  assert.equal(byId[3].born, T0 + 5_000);
  assert.deepEqual(track.shots, [[T0 + 6_000, 1, 50, 50]]);
  assert.deepEqual(track.markers, [{ t: T0 + 7_900, kind: 'quest', base: 5, what: 'quest passed: Q358', questId: 358 }]);
});

test('a reconnect replaces the world: what it no longer lists ended unseen', () => {
  const { tape, world } = setup();
  tape.add(msg(T0 + 3_000, 'INITIAL_SLAVE_SYNC_INFO', {
    playerBaseInfos: [{ baseId: 5, character: 'HUMAN', userId: 'u1' }],
    syncBaseItemInfos: [unit(1, 12, 12)],
  }));
  const track = buildTrack(tape, world, T0, T0 + 5_000);
  const byId = Object.fromEntries(track.items.map((item) => [item.id, item]));
  assert.equal(byId[2].died, T0 + 3_000);
  assert.equal(byId[2].explode, false);
  assert.deepEqual(byId[1].p.at(-1), [T0 + 3_000, 12, 12, null, 0]);
});

test('no keyframe, no track', () => {
  const tape = new Tape({ keepMs: 1, maxBytes: 1 });
  assert.deepEqual(buildTrack(tape, new World(), 0, 1), { error: 'no keyframe held yet' });
});

test('a path keeps its corners and drops the points on straight stretches', async () => {
  const { straighten } = await import('../lib/replay.mjs');
  const way = [{ x: 0, y: 0 }, { x: 1, y: 0 }, { x: 2, y: 0 }, { x: 3, y: 0 }, { x: 3, y: 1 }, { x: 3, y: 2 }];
  assert.deepEqual(straighten(way), [0, 0, 3, 0, 3, 2]);
  assert.deepEqual(straighten([{ x: 5, y: 5 }]), [5, 5]);
});
