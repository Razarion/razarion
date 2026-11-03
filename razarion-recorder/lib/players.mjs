// Leaving the noob island: unloading the builder on the island beyond it. The same id local and on
// PROD (CommunityTickerService.LEFT_NOOB_ISLAND_QUEST_ID).
import { questConditionText } from './quest-text.mjs';

export const LEFT_NOOB_ISLAND_QUEST_ID = 392;

const MINUTE = 60_000;
const EVENTS_PER_PLAYER = 2_000;
// No sign of the player for this long: idle while the base stands, gone once it does not.
const IDLE_AFTER_MS = 3 * MINUTE;
// A pause in the story: this long without anything the player made happen.
const GAP_MS = 3 * MINUTE;
const FORGET_PLAYER_MS = 12 * 60 * MINUTE;

const time = (row, t) => (row.serverTime ? Date.parse(row.serverTime) || t : t);
const median = (values) => {
  if (!values.length) return null;
  const sorted = [...values].sort((a, b) => a - b);
  const mid = sorted.length >> 1;
  return sorted.length % 2 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
};
const minutes = (ms) => (ms == null ? null : Math.round(ms / 6_000) / 10);

/**
 * The players, one each, from two sources: the history feed (quests, levels, what was built and
 * lost - what only the player's own client is told) and the wire (where, and the moments units are
 * set going, which is the nearest the recorder comes to seeing the player act).
 *
 * Players are named by user id and base id only. Their names stay in the world model and on the
 * dashboard; nothing here, and so nothing an AI is handed, carries them.
 */
export class Players {
  constructor(world) {
    this.world = world;
    this.byUser = new Map();
    world.onEvent = (event, bases) => this.onWire(event, bases);
    world.onActivity = (base, t) => this.mark(this.player(base.userId), t, base.baseId);
    world.levelOf = (userId) => this.byUser.get(userId)?.level ?? null;
  }

  quest(id) {
    return this.world.names.quests.get(id);
  }

  questLabel(id) {
    const quest = this.quest(id);
    if (!quest) return `quest #${id}`;
    const what = quest.name?.trim() || questConditionText(quest.condition, this.world.names.baseItemTypes) || `#${id}`;
    return `L${quest.levelNumber} ${what}`;
  }

  player(userId) {
    let player = this.byUser.get(userId);
    if (!player) {
      player = {
        userId,
        baseIds: [],
        firstSeen: null,
        lastSeen: null,
        lastAction: null,
        startedAt: null, // the base created while the recorder watched; null when it was there before
        level: null,
        levels: [],
        activeQuest: null,
        passed: [],
        firstBuilt: {},
        built: {},
        lost: { toBot: 0, toPlayer: 0, removed: 0 },
        killed: { bot: 0, player: 0 },
        boxes: 0,
        leftIslandAt: null,
        actions: [], // minutes with something the player made happen, ascending
        events: [],
      };
      this.byUser.set(userId, player);
    }
    return player;
  }

  seen(player, t, baseId) {
    player.firstSeen = Math.min(player.firstSeen ?? t, t);
    player.lastSeen = Math.max(player.lastSeen ?? t, t);
    if (baseId != null && !player.baseIds.includes(baseId)) player.baseIds.push(baseId);
  }

  mark(player, t, baseId) {
    this.seen(player, t, baseId);
    player.lastAction = Math.max(player.lastAction ?? t, t);
    const minute = Math.floor(t / MINUTE);
    if (player.actions[player.actions.length - 1] !== minute) {
      player.actions.push(minute);
      player.actions.sort((a, b) => a - b);
    }
  }

  add(player, event) {
    player.events.push(event);
    if (player.events.length > EVENTS_PER_PLAYER) player.events.shift();
    // Into the planet's event list as well, beside what the wire says.
    const recent = this.world.recent;
    recent.push({ t: event.t, kind: 'player', userId: player.userId, baseId: event.baseId ?? player.baseIds[player.baseIds.length - 1], what: event.what });
    if (recent.length > 1000) recent.shift();
  }

  // ------------------------------------------------------------------ input

  /**
   * Where the players with a base stand, from /rest/recorder/players: level and active quest of
   * those who were playing before the recorder started. What the feed has said wins.
   */
  seed(states, now) {
    for (const state of states) {
      const player = this.player(state.userId);
      // Not "seen": a base standing says nothing about whether its player is there.
      player.firstSeen = player.firstSeen ?? now;
      if (!player.baseIds.includes(state.baseId)) player.baseIds.push(state.baseId);
      if (player.level == null && state.levelId != null) player.level = this.world.names.levels.get(state.levelId) ?? null;
      if (state.activeQuestId == null) continue;
      if (player.activeQuest?.id !== state.activeQuestId && !player.passed.some((p) => p.id === state.activeQuestId)) {
        player.activeQuest = { id: state.activeQuestId, t: now, assumed: true };
      }
    }
  }

  /** A game_history row. Actor fields are userId/baseId/botId, the victim's targetUserId/targetBaseId. */
  onHistory(row, now) {
    const t = time(row, now);
    const actorIsPlayer = row.userId && row.botId == null && row.source === 'HUMAN';
    const actor = actorIsPlayer ? this.player(row.userId) : null;
    const victim = row.targetUserId ? this.player(row.targetUserId) : null;
    if (!actor && !victim) return;

    switch (row.type) {
      case 'BASE_CREATED':
        actor.startedAt = actor.startedAt ?? t;
        this.mark(actor, t, row.baseId);
        this.add(actor, { t, what: 'base created', baseId: row.baseId, at: at(row) });
        break;
      case 'LEVEL_UP':
        actor.level = row.levelNumber;
        actor.levels.push({ t, level: row.levelNumber });
        this.seen(actor, t);
        this.add(actor, { t, what: `level ${row.levelNumber}` });
        break;
      case 'QUEST_ACTIVATED':
        actor.activeQuest = { id: row.questId, t };
        this.seen(actor, t);
        this.add(actor, { t, what: `quest started: ${this.questLabel(row.questId)}`, questId: row.questId });
        break;
      case 'QUEST_PASSED':
        if (!actor.passed.some((p) => p.id === row.questId)) actor.passed.push({ id: row.questId, t });
        if (actor.activeQuest?.id === row.questId) actor.activeQuest = null;
        if (row.questId === LEFT_NOOB_ISLAND_QUEST_ID) actor.leftIslandAt = t;
        this.mark(actor, t);
        this.add(actor, { t, what: `quest passed: ${this.questLabel(row.questId)}`, questId: row.questId });
        break;
      case 'QUEST_DEACTIVATED':
        if (actor.activeQuest?.id === row.questId) actor.activeQuest = null;
        break;
      case 'ITEM_CREATED':
        if (!actor) break;
        actor.built[row.itemTypeName] = (actor.built[row.itemTypeName] || 0) + 1;
        if (!actor.firstBuilt[row.itemTypeName]) {
          actor.firstBuilt[row.itemTypeName] = t;
          this.add(actor, { t, what: `first ${row.itemTypeName}`, at: at(row) });
        }
        this.mark(actor, t, row.baseId);
        break;
      case 'ITEM_DESTROYED':
        if (victim) {
          const by = row.botId != null ? 'toBot' : row.userId ? 'toPlayer' : 'removed';
          victim.lost[by]++;
          this.seen(victim, t, row.targetBaseId);
          if (by !== 'removed') {
            this.add(victim, { t, what: `lost ${row.itemTypeName} to ${by === 'toBot' ? 'a bot' : 'a player'}`, at: at(row) });
          }
        }
        if (actor && victim !== actor) {
          actor.killed[row.targetUserId ? 'player' : 'bot']++;
          this.mark(actor, t, row.baseId);
        }
        break;
      case 'BASE_DEFEATED':
        if (victim) this.add(victim, { t, what: row.userId || row.botId ? 'base destroyed' : 'base given up', baseId: row.targetBaseId });
        if (actor && victim !== actor) this.add(actor, { t, what: 'destroyed a base', baseId: row.targetBaseId });
        break;
      case 'BOX_PICKED':
        actor.boxes++;
        this.mark(actor, t);
        break;
      case 'USER_LOGGED_IN':
      case 'ANONYMOUS_USER_RETURNED':
        this.seen(actor, t);
        this.add(actor, { t, what: row.type === 'USER_LOGGED_IN' ? 'logged in' : 'came back' });
        break;
      default:
        if (actor) this.seen(actor, t);
    }
  }

  /** Wire events of human bases: only where they add to the history - bases going. */
  onWire(event, bases) {
    if (event.kind !== 'base-deleted') return;
    const base = bases.find((b) => b.userId);
    if (!base) return;
    const player = this.player(base.userId);
    this.seen(player, event.t, base.baseId);
  }

  forget(now) {
    for (const [userId, player] of this.byUser) {
      if (now - (player.lastSeen ?? now) > FORGET_PLAYER_MS) this.byUser.delete(userId);
    }
  }

  // ------------------------------------------------------------------ views

  baseOf(player) {
    for (let i = player.baseIds.length - 1; i >= 0; i--) {
      const base = this.world.base(player.baseIds[i]);
      if (base) return base;
    }
    return null;
  }

  status(player, now) {
    const base = this.baseOf(player);
    if (!base || base.deletedAt) return 'gone';
    const last = Math.max(player.lastAction ?? 0, player.lastSeen ?? 0);
    return now - last < IDLE_AFTER_MS ? 'active' : 'idle';
  }

  summary(player, now) {
    const base = this.baseOf(player);
    const quest = player.activeQuest;
    return {
      userId: player.userId,
      baseId: base?.baseId ?? null,
      status: this.status(player, now),
      level: player.level,
      startedAt: player.startedAt,
      watchedFromStart: player.startedAt != null,
      minutesPlayed: player.startedAt ? minutes((player.lastSeen ?? now) - player.startedAt) : null,
      activeMinutes: player.actions.length,
      // startSeen false: the quest was already open when the recorder learned of it, so it has been
      // open at least openMinutes.
      currentQuest: quest ? { id: quest.id, name: this.questLabel(quest.id), openMinutes: minutes(now - quest.t), startSeen: !quest.assumed } : null,
      questsPassed: player.passed.length,
      leftNoobIsland: player.leftIslandAt != null,
      lost: player.lost,
      killed: player.killed,
      units: base ? this.world.baseView(base).itemCount : 0,
      lastActionMinutesAgo: player.lastAction ? minutes(now - player.lastAction) : null,
      where: base ? this.world.baseView(base).centre : null,
    };
  }

  list(now) {
    return [...this.byUser.values()]
      .map((player) => this.summary(player, now))
      .sort((a, b) => (a.status === 'gone') - (b.status === 'gone') || (b.startedAt ?? 0) - (a.startedAt ?? 0));
  }

  /** One player's time on the planet, in order, with the pauses in it. */
  story(userId, now) {
    const player = this.byUser.get(userId);
    if (!player) return null;
    const start = player.startedAt ?? player.firstSeen;
    const lines = [...player.events].sort((a, b) => a.t - b.t).map((e) => ({ ...e, minute: minutes(e.t - start) }));
    const gaps = [];
    for (let i = 1; i < player.actions.length; i++) {
      const pause = (player.actions[i] - player.actions[i - 1]) * MINUTE;
      if (pause >= GAP_MS) gaps.push({ t: player.actions[i - 1] * MINUTE, minutes: minutes(pause) });
    }
    if (player.lastAction && now - player.lastAction >= GAP_MS && this.status(player, now) !== 'gone') {
      gaps.push({ t: player.lastAction, minutes: minutes(now - player.lastAction), open: true });
    }
    for (const gap of gaps) {
      lines.push({ t: gap.t, minute: minutes(gap.t - start), what: `${gap.open ? 'no action since' : 'pause of'} ${gap.minutes} min` });
    }
    lines.sort((a, b) => a.t - b.t);
    return {
      ...this.summary(player, now),
      levels: player.levels.map((l) => ({ level: l.level, minute: minutes(l.t - start) })),
      quests: player.passed.map((p) => ({ id: p.id, name: this.questLabel(p.id), minute: minutes(p.t - start) })),
      firstBuilt: Object.fromEntries(Object.entries(player.firstBuilt).map(([name, t]) => [name, minutes(t - start)])),
      built: player.built,
      boxes: player.boxes,
      story: lines.map(({ t, minute, what, at: where }) => ({ t, minute, what, ...(where ? { at: where } : {}) })),
    };
  }

  /**
   * Where the players go and where they stop. Only players whose base the recorder saw created
   * count: one that was already playing has steps the recorder never saw.
   */
  funnel(now) {
    const quests = [...this.world.names.quests.values()]
      .filter((q) => q.levelNumber >= 1)
      .sort((a, b) => a.levelNumber - b.levelNumber || a.orderInLevel - b.orderInLevel);
    const players = [...this.byUser.values()].filter((p) => p.startedAt != null);
    const steps = [{ key: 'base', label: 'base created', reachedAt: (p) => p.startedAt }].concat(
      quests.map((q) => ({
        key: q.id,
        label: this.questLabel(q.id),
        trigger: q.trigger,
        reachedAt: (p) => p.passed.find((x) => x.id === q.id)?.t ?? null,
      })),
    );
    const furthest = new Map(players.map((p) => [p.userId, -1]));
    const rows = steps.map((step, index) => {
      const times = [];
      for (const p of players) {
        const t = step.reachedAt(p);
        if (t == null) continue;
        times.push(t - p.startedAt);
        furthest.set(p.userId, Math.max(furthest.get(p.userId), index));
      }
      return { step: step.label, questId: step.key === 'base' ? null : step.key, trigger: step.trigger, reached: times.length, medianMinutes: minutes(median(times)) };
    });
    for (const p of players) {
      const row = rows[furthest.get(p.userId)];
      if (!row) continue;
      const status = this.status(p, now);
      row.stoppedHere = row.stoppedHere || { active: 0, idle: 0, gone: 0 };
      row.stoppedHere[status]++;
    }
    const last = rows.reduce((index, row, i) => (row.reached ? i : index), 0);
    return {
      players: players.length,
      joinedEarlier: this.byUser.size - players.length,
      leftNoobIsland: players.filter((p) => p.leftIslandAt).length,
      // Steps past the last one anybody reached say nothing but "nobody got here".
      steps: rows.slice(0, Math.min(rows.length, last + 2)),
    };
  }

  /** Players with a base on a quest open at least this long, longest first. */
  stalls(now, minMinutes = 5) {
    return [...this.byUser.values()]
      .filter((p) => p.activeQuest && this.status(p, now) !== 'gone' && now - p.activeQuest.t >= minMinutes * MINUTE)
      .map((p) => {
        const since = p.activeQuest.t;
        return {
          ...this.summary(p, now),
          sinceQuestStarted: p.events.filter((e) => e.t >= since).map((e) => ({ minute: minutes(e.t - since), what: e.what })),
        };
      })
      .sort((a, b) => b.currentQuest.openMinutes - a.currentQuest.openMinutes);
  }
}

function at(row) {
  return row.x != null && row.y != null ? { x: Math.round(row.x), y: Math.round(row.y) } : undefined;
}
