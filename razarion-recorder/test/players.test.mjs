// node --test test/
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { World } from '../lib/world.mjs';
import { Players, LEFT_NOOB_ISLAND_QUEST_ID } from '../lib/players.mjs';

const T0 = Date.parse('2026-11-03T10:00:00Z');
const at = (minute) => new Date(T0 + minute * 60_000).toISOString();

function setup() {
  const world = new World();
  world.setConfig({
    baseItemTypes: [{ id: 1, name: 'Builder' }, { id: 2, name: 'Harvester' }],
    quests: [
      { id: 10, name: 'Build factory', levelNumber: 1, orderInLevel: 0, trigger: 'SYNC_ITEM_CREATED' },
      { id: 11, name: 'Build harvester', levelNumber: 1, orderInLevel: 1, trigger: 'SYNC_ITEM_CREATED' },
      { id: LEFT_NOOB_ISLAND_QUEST_ID, name: 'Unload builder', levelNumber: 8, orderInLevel: 3, trigger: 'UNLOAD' },
      { id: 99, name: '', levelNumber: 9, orderInLevel: 0, condition: { conditionTrigger: 'SYNC_ITEM_CREATED', comparisonConfig: { typeCount: { 1: 2 } } } },
    ],
  });
  const players = new Players(world);
  // The base on the wire, so the player has one standing
  world.apply({ t: T0, name: 'BASE_CREATED', text: 'BASE_CREATED#' + JSON.stringify({ baseId: 5, character: 'HUMAN', userId: 'u1', createdMillis: T0 }) });
  return { world, players };
}

const human = (type, minute, fields = {}) => ({ type, source: 'HUMAN', userId: 'u1', baseId: 5, serverTime: at(minute), ...fields });

test('a player through the funnel: steps, times, where they stopped', () => {
  const { players } = setup();
  players.onHistory(human('BASE_CREATED', 0), T0);
  players.onHistory(human('QUEST_ACTIVATED', 0, { questId: 10 }), T0);
  players.onHistory(human('ITEM_CREATED', 2, { itemTypeName: 'Factory' }), T0);
  players.onHistory(human('QUEST_PASSED', 2, { questId: 10, levelNumber: 1 }), T0);
  players.onHistory(human('LEVEL_UP', 2, { levelNumber: 2 }), T0);
  players.onHistory(human('QUEST_ACTIVATED', 2, { questId: 11 }), T0);

  const now = T0 + 20 * 60_000;
  const funnel = players.funnel(now);
  assert.equal(funnel.players, 1);
  assert.deepEqual(funnel.steps.map((s) => [s.step, s.reached]), [
    ['base created', 1], ['L1 Build factory', 1], ['L1 Build harvester', 0],
  ]);
  assert.equal(funnel.steps[1].medianMinutes, 2);
  // Stopped after the factory, nothing for 18 minutes while the base stands: idle
  assert.deepEqual(funnel.steps[1].stoppedHere, { active: 0, idle: 1, gone: 0 });

  const stalls = players.stalls(now, 5);
  assert.equal(stalls.length, 1);
  assert.equal(stalls[0].currentQuest.name, 'L1 Build harvester');
  assert.equal(stalls[0].currentQuest.openMinutes, 18);
  assert.equal(stalls[0].level, 2);
});

test('the story names losses to bots and the pauses', () => {
  const { players } = setup();
  players.onHistory(human('BASE_CREATED', 0), T0);
  players.onHistory(human('ITEM_CREATED', 1, { itemTypeName: 'Viper' }), T0);
  // A bot kills the player's Viper: the bot is the actor, the player the target
  players.onHistory({ type: 'ITEM_DESTROYED', source: 'BOT', botId: 7, baseId: 3, targetUserId: 'u1', targetBaseId: 5, itemTypeName: 'Viper', serverTime: at(9), x: 100.4, y: 50 }, T0);
  players.onHistory(human('ITEM_CREATED', 10, { itemTypeName: 'Viper' }), T0);

  const story = players.story('u1', T0 + 11 * 60_000);
  assert.deepEqual(story.lost, { toBot: 1, toPlayer: 0, removed: 0 });
  assert.deepEqual(story.firstBuilt, { Viper: 1 });
  assert.deepEqual(story.built, { Viper: 2 });
  assert.deepEqual(story.story.map((s) => [s.minute, s.what]), [
    [0, 'base created'],
    [1, 'first Viper'],
    [1, 'pause of 9 min'],
    [9, 'lost Viper to a bot'],
  ]);
  assert.deepEqual(story.story[3].at, { x: 100, y: 50 });
});

test('leaving the noob island, and players who were there before the recorder', () => {
  const { players } = setup();
  players.onHistory(human('QUEST_PASSED', 30, { questId: LEFT_NOOB_ISLAND_QUEST_ID, levelNumber: 8 }), T0);
  players.onHistory({ ...human('QUEST_PASSED', 31, { questId: 10 }), userId: 'u2' }, T0);

  const list = players.list(T0 + 31 * 60_000);
  assert.equal(list.find((p) => p.userId === 'u1').leftNoobIsland, true);
  // Neither base creation was seen: both count as joined earlier, not in the funnel
  const funnel = players.funnel(T0 + 31 * 60_000);
  assert.equal(funnel.players, 0);
  assert.equal(funnel.joinedEarlier, 2);
});

test('a quest without a name is called by what it asks for', () => {
  const { players } = setup();
  assert.equal(players.questLabel(99), 'L9 Build 2 Builder');
  assert.equal(players.questLabel(10), 'L1 Build factory');
  assert.equal(players.questLabel(12345), 'quest #12345');
});

test('a player who was there before the recorder gets the quest the server says is open', () => {
  const { world, players } = setup();
  world.setConfig({ ...{ quests: [...world.names.quests.values()] }, levels: [{ id: 272, number: 1 }] });
  players.seed([{ userId: 'u1', baseId: 5, levelId: 272, activeQuestId: 11 }], T0);

  const stalls = players.stalls(T0 + 6 * 60_000, 5);
  assert.equal(stalls.length, 1);
  assert.deepEqual(stalls[0].currentQuest, { id: 11, name: 'L1 Build harvester', openMinutes: 6, startSeen: false });
  assert.equal(stalls[0].level, 1);
  assert.equal(stalls[0].status, 'idle');
  // The feed wins: once it says the quest passed, the seed does not bring it back
  players.onHistory(human('QUEST_PASSED', 7, { questId: 11 }), T0);
  players.seed([{ userId: 'u1', baseId: 5, levelId: 272, activeQuestId: 11 }], T0 + 8 * 60_000);
  assert.equal(players.list(T0 + 8 * 60_000)[0].currentQuest, null);
});

test('bot rows that touch no player are ignored', () => {
  const { players } = setup();
  players.onHistory({ type: 'ITEM_CREATED', source: 'BOT', botId: 7, baseId: 3, itemTypeName: 'Tower', serverTime: at(1) }, T0);
  players.onHistory({ type: 'BASE_CREATED', source: 'BOT', botId: 7, baseId: 3, serverTime: at(1) }, T0);
  assert.equal(players.byUser.size, 0);
});
