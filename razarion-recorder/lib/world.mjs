import { packetPayload } from './connection.mjs';

// A hit counts as the kill when the victim explodes within this long after it.
const KILL_WINDOW_MS = 5_000;
const TIMELINE_PER_BASE = 1_000;
const RECENT_EVENTS = 1_000;
// Bases gone this long are forgotten, timeline and all; the tape still has them.
const FORGET_BASE_MS = 6 * 3_600_000;

const isBot = (base) => base && base.character !== 'HUMAN';

/**
 * What the recorder makes of the stream: who is on the planet, what each base holds, and the
 * events the wire does not name but implies - a unit that appears, one that explodes and who shot
 * it, a box that vanishes next to the unit that went for it.
 *
 * The positions are those of the last message about an item, not where it is now: the server
 * sends a unit's path once and the client moves it along by itself. Good enough to say where
 * something happened; a replay through the real engine is what shows it moving.
 */
export class World {
  constructor() {
    this.names = { baseItemTypes: new Map(), resourceItemTypes: new Map(), boxItemTypes: new Map(), quests: new Map(), levels: new Map() };
    this.bases = new Map(); // baseId -> base
    this.items = new Map(); // id -> base item
    this.resources = new Map();
    this.boxes = new Map();
    this.lastHit = new Map(); // victim id -> {actor, t}
    // Told of every event and of every unit set going by a human base; see lib/players.mjs.
    this.onEvent = () => {};
    this.onActivity = () => {};
    this.levelOf = () => null;
    this.recent = [];
    this.tickCount = null;
    this.snapshots = 0;
  }

  setConfig(config) {
    for (const key of ['baseItemTypes', 'resourceItemTypes', 'boxItemTypes']) {
      this.names[key] = new Map((config[key] || []).map((named) => [named.id, named.name]));
    }
    this.names.quests = new Map((config.quests || []).map((quest) => [quest.id, quest]));
    this.names.levels = new Map((config.levels || []).map((level) => [level.id, level.number]));
    // Size and speed per unit type, for the replay: a type without speed is a building.
    this.unitTypes = new Map((config.baseItemTypes || []).map((t) => [t.id, { name: t.name, radius: t.radius ?? 1, speed: t.speed ?? null }]));
    this.planet = config.planet ?? null;
  }

  typeName(itemTypeId) {
    return this.names.baseItemTypes.get(itemTypeId) || `#${itemTypeId}`;
  }

  apply(entry) {
    const handler = HANDLERS[entry.name];
    if (!handler) return;
    try {
      handler.call(this, packetPayload(entry.text), entry.t);
    } catch (error) {
      this.lastError = { at: entry.t, packet: entry.name, message: error.message };
    }
  }

  // ------------------------------------------------------------------ bases and events

  base(baseId) {
    return this.bases.get(baseId);
  }

  putBase(info, t) {
    const known = this.bases.get(info.baseId);
    const base = known || { baseId: info.baseId, createdAt: info.createdMillis || t, timeline: [], items: 0, lastActivity: null };
    Object.assign(base, {
      name: info.name ?? base.name,
      character: info.character ?? base.character,
      userId: info.userId ?? base.userId,
      botId: info.botId ?? base.botId,
      resources: info.resources ?? base.resources,
      deletedAt: null,
    });
    this.bases.set(info.baseId, base);
    return base;
  }

  event(t, kind, fields) {
    const event = { t, kind, ...fields };
    const bases = [fields.baseId, fields.killerBaseId].map((id) => this.bases.get(id)).filter(Boolean);
    for (const base of bases) {
      if (isBot(base)) continue;
      base.timeline.push(event);
      if (base.timeline.length > TIMELINE_PER_BASE) base.timeline.shift();
    }
    // The planet's own churn - bots rebuilding, bots shooting bots, a bot base respawning every
    // minute - stays out of the feed.
    if (bases.some((base) => !isBot(base))) {
      this.recent.push(event);
      if (this.recent.length > RECENT_EVENTS) this.recent.shift();
    }
    this.onEvent(event, bases);
    return event;
  }

  // ------------------------------------------------------------------ views

  forget(now) {
    for (const [baseId, base] of this.bases) {
      if (base.deletedAt && now - base.deletedAt > FORGET_BASE_MS) this.bases.delete(baseId);
    }
    for (const [victim, hit] of this.lastHit) {
      if (now - hit.t > KILL_WINDOW_MS) this.lastHit.delete(victim);
    }
  }

  baseList() {
    return [...this.bases.values()].map((base) => this.baseView(base));
  }

  baseView(base) {
    const types = {};
    let x = 0;
    let y = 0;
    let n = 0;
    for (const item of this.items.values()) {
      if (item.baseId !== base.baseId) continue;
      const name = this.typeName(item.itemTypeId);
      types[name] = (types[name] || 0) + 1;
      if (item.position) {
        x += item.position.x;
        y += item.position.y;
        n++;
      }
    }
    return {
      baseId: base.baseId,
      name: base.name,
      character: base.character,
      userId: base.userId,
      bot: isBot(base),
      createdAt: base.createdAt,
      deletedAt: base.deletedAt,
      lastActivity: base.lastActivity,
      resources: base.resources,
      level: base.userId ? this.levelOf(base.userId) : null,
      itemCount: Object.values(types).reduce((a, b) => a + b, 0),
      types,
      centre: n ? { x: Math.round(x / n), y: Math.round(y / n) } : null,
    };
  }
}

const HANDLERS = {
  INITIAL_SLAVE_SYNC_INFO(info, t) {
    // The whole world: it replaces what the recorder had, the way a reconnecting client does
    // (PlanetService.initialSlaveSyncItemInfo). Bases it no longer lists ended while nobody watched.
    this.snapshots++;
    this.tickCount = info.tickCount;
    const listed = new Set((info.playerBaseInfos || []).map((b) => b.baseId));
    for (const base of this.bases.values()) {
      if (!listed.has(base.baseId) && !base.deletedAt) base.deletedAt = t;
    }
    for (const b of info.playerBaseInfos || []) this.putBase(b, t);
    this.items.clear();
    for (const item of info.syncBaseItemInfos || []) this.items.set(item.id, slim(item, t));
    this.resources = new Map((info.syncResourceItemInfos || []).map((r) => [r.id, r]));
    this.boxes = new Map((info.syncBoxItemInfos || []).map((b) => [b.id, b]));
    this.event(t, 'snapshot', { items: this.items.size, bases: listed.size });
  },

  BASE_CREATED(info, t) {
    const base = this.putBase(info, t);
    this.event(t, 'base-created', { baseId: base.baseId, bot: isBot(base), userId: base.userId });
  },

  BASE_NAME_CHANGED(info, t) {
    const base = this.bases.get(info.baseId);
    if (base) base.name = info.name;
  },

  BASE_DELETED(baseId, t) {
    const base = this.bases.get(baseId);
    if (!base) return;
    base.deletedAt = t;
    this.event(t, 'base-deleted', { baseId, bot: isBot(base), userId: base.userId, lived: t - base.createdAt });
  },

  TICK_INFO(tickInfo, t) {
    this.tickCount = tickInfo.tickCount;
    for (const info of tickInfo.syncBaseItemInfos || []) {
      const known = this.items.get(info.id);
      const item = slim(info, t);
      this.items.set(info.id, item);
      const base = this.bases.get(item.baseId);
      if (base) base.lastActivity = t;
      if (!known) {
        this.event(t, 'item-created', { baseId: item.baseId, itemId: item.id, type: this.typeName(item.itemTypeId), at: item.position });
      } else if (base && !isBot(base) && setGoing(known, item) && !/harvester/i.test(this.typeName(item.itemTypeId))) {
        // Commands are not on the wire, only what they do: a new destination, a new building, a
        // longer factory queue. A harvester goes back and forth by itself, so it says nothing.
        this.onActivity(base, t);
      }
    }
  },

  PROJECTILE_FIRED(info, t) {
    this.lastHit.set(info.targetSyncBaseItemId, { actor: info.actorSyncBaseItemId, t });
  },

  SYNC_ITEM_DELETED(info, t) {
    const item = this.items.get(info.id);
    if (item) {
      this.items.delete(info.id);
      const hit = this.lastHit.get(info.id);
      this.lastHit.delete(info.id);
      const killer = hit && t - hit.t <= KILL_WINDOW_MS ? this.items.get(hit.actor) : null;
      this.event(t, info.explode ? 'item-destroyed' : 'item-removed', {
        baseId: item.baseId,
        itemId: item.id,
        type: this.typeName(item.itemTypeId),
        at: item.position,
        ...(killer ? { killerBaseId: killer.baseId, killerType: this.typeName(killer.itemTypeId) } : {}),
      });
      return;
    }
    const box = this.boxes.get(info.id);
    if (box) {
      this.boxes.delete(info.id);
      const picker = [...this.items.values()].find((i) => i.syncBoxItemId === info.id);
      if (picker) {
        this.event(t, 'box-picked', { baseId: picker.baseId, boxType: this.names.boxItemTypes.get(box.boxItemTypeId) || box.boxItemTypeId });
      }
      return;
    }
    this.resources.delete(info.id);
  },

  SYNC_RESOURCE_ITEM_CHANGED(info) {
    this.resources.set(info.id, info);
  },

  SYNC_BOX_ITEM_CHANGED(info) {
    this.boxes.set(info.id, info);
  },
};

/** Whether an item was given something new to do between two messages about it. */
function setGoing(before, after) {
  return (after.wayEnd && after.wayEnd !== before.wayEnd)
    || (after.toBeBuiltTypeId != null && after.toBeBuiltTypeId !== before.toBeBuiltTypeId)
    || after.queue > before.queue;
}

/** What the recorder needs of an item; the full message stays on the tape. */
function slim(info, t) {
  const position = info.syncPhysicalAreaInfo?.position;
  const way = info.syncPhysicalAreaInfo?.wayPositions;
  const end = way && way.length ? way[way.length - 1] : null;
  return {
    wayEnd: end ? `${Math.round(end.x)},${Math.round(end.y)}` : null,
    toBeBuiltTypeId: info.toBeBuiltTypeId ?? null,
    queue: info.factoryBuildQueue?.length ?? 0,
    id: info.id,
    itemTypeId: info.itemTypeId,
    baseId: info.baseId,
    position: position ? { x: Math.round(position.x), y: Math.round(position.y) } : null,
    health: info.health,
    buildup: info.buildup,
    target: info.target ?? null,
    containedIn: info.containedIn ?? null,
    syncBoxItemId: info.syncBoxItemId ?? null,
    seenAt: t,
  };
}
