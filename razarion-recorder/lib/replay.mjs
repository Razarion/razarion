import { packetPayload } from './connection.mjs';

// One replay is at most this long: an hour of the planet is a few MB of track.
export const MAX_RANGE_MS = 90 * 60_000;

const round = (v) => Math.round(v * 10) / 10;

/**
 * A path as flat [x, y, ...] without the points in between on a straight stretch. The pathing
 * hands out one point per grid cell, a metre apart, and a long walk is hundreds of them.
 */
export function straighten(way) {
  const out = [round(way[0].x), round(way[0].y)];
  for (let i = 1; i < way.length; i++) {
    const p = way[i];
    const next = way[i + 1];
    if (next) {
      const ax = p.x - way[i - 1].x, ay = p.y - way[i - 1].y;
      const bx = next.x - p.x, by = next.y - p.y;
      if (Math.abs(ax * by - ay * bx) < 1e-6 && ax * bx + ay * by > 0) continue;
    }
    out.push(round(p.x), round(p.y));
  }
  return out;
}

/**
 * A stretch of the tape as a track the 2D player draws (dashboard/player.html): for every item the
 * positions the server sent with the path it was on, when it appeared and when it went, plus the
 * shots and what the players achieved on the way.
 *
 * The server sends a unit's path once and the client walks it; the track does the same with the
 * advantage of knowing where the unit was next, so the player moves it along the path between two
 * known positions instead of guessing a speed.
 *
 * The world at `from` comes from the last keyframe before it, wound forward through the tape.
 * questLabel names a quest (Players.questLabel).
 */
export function buildTrack(tape, world, from, to, questLabel = (id) => `quest #${id}`) {
  const keyframe = [...tape.keyframes].reverse().find((k) => k.t <= from) ?? tape.keyframes[0];
  if (!keyframe) return { error: 'no keyframe held yet' };
  from = Math.max(from, keyframe.t);
  to = Math.min(to, from + MAX_RANGE_MS);

  const state = new Map(); // id -> {id, type, base, x, y, way, hidden}
  const bases = new Map(); // baseId -> {name, human, userId}
  const tracks = new Map(); // id -> output item
  const shots = [];
  const markers = [];
  let recording = false;

  const point = (s, t) => [t, round(s.x), round(s.y), s.way, s.hidden ? 1 : 0];
  const track = (s, t) => {
    let item = tracks.get(s.id);
    if (!item) {
      item = { id: s.id, type: s.type, base: s.base, born: t > from ? t : null, died: null, explode: false, p: [] };
      tracks.set(s.id, item);
    }
    return item;
  };
  const startRecording = () => {
    recording = true;
    for (const s of state.values()) track(s, from).p.push(point(s, from));
  };

  const putBase = (info) => bases.set(info.baseId, {
    name: info.name ?? bases.get(info.baseId)?.name ?? null,
    human: info.character === 'HUMAN',
    userId: info.userId ?? null,
  });

  const snapshot = (info, t) => {
    const present = new Set();
    for (const b of info.playerBaseInfos || []) putBase(b);
    for (const i of info.syncBaseItemInfos || []) {
      present.add(i.id);
      update(i, t);
    }
    for (const [id, s] of state) {
      if (present.has(id)) continue;
      state.delete(id);
      if (recording) track(s, t).died = t;
    }
  };

  const update = (info, t) => {
    const area = info.syncPhysicalAreaInfo;
    const known = state.get(info.id);
    const s = known || { id: info.id, type: info.itemTypeId, base: info.baseId };
    if (area?.position) {
      s.x = area.position.x;
      s.y = area.position.y;
    }
    const way = area?.wayPositions;
    s.way = way && way.length ? straighten(way) : null;
    s.hidden = info.containedIn != null;
    s.base = info.baseId ?? s.base;
    state.set(info.id, s);
    if (recording && s.x != null) track(s, t).p.push(point(s, t));
  };

  apply(keyframe.text, keyframe.t);
  for (const entry of tape.range(keyframe.t + 1, to)) {
    if (!recording && entry.t >= from) startRecording();
    apply(entry.text, entry.t, entry.name);
  }
  if (!recording) startRecording();

  function apply(text, t, name = 'INITIAL_SLAVE_SYNC_INFO') {
    let payload;
    try {
      payload = packetPayload(text);
    } catch {
      return;
    }
    switch (name) {
      case 'INITIAL_SLAVE_SYNC_INFO':
        snapshot(payload, t);
        break;
      case 'TICK_INFO':
        for (const info of payload.syncBaseItemInfos || []) update(info, t);
        break;
      case 'SYNC_ITEM_DELETED': {
        const s = state.get(payload.id);
        if (!s) break;
        state.delete(payload.id);
        if (recording) Object.assign(track(s, t), { died: t, explode: !!payload.explode });
        break;
      }
      case 'PROJECTILE_FIRED':
        if (recording && payload.targetPosition) {
          shots.push([t, payload.actorSyncBaseItemId, round(payload.targetPosition.x), round(payload.targetPosition.y)]);
        }
        break;
      case 'BASE_CREATED':
      case 'BASE_NAME_CHANGED':
        putBase(payload);
        break;
      case 'HISTORY':
        if (recording) marker(payload, t);
        break;
      default:
    }
  }

  // What is worth a mark on the time line: what players achieve and lose, not the firehose.
  function marker(row, t) {
    const at = row.serverTime ? Date.parse(row.serverTime) || t : t;
    const human = row.source === 'HUMAN' && row.userId && row.botId == null;
    const base = row.baseId ?? row.targetBaseId ?? null;
    if (human && row.type === 'QUEST_PASSED') {
      markers.push({ t: at, kind: 'quest', base, what: `quest passed: ${questLabel(row.questId)}`, questId: row.questId });
    } else if (human && row.type === 'LEVEL_UP') {
      markers.push({ t: at, kind: 'level', base, what: `level ${row.levelNumber}` });
    } else if (human && row.type === 'BASE_CREATED') {
      markers.push({ t: at, kind: 'base', base, what: 'base created' });
    } else if (row.type === 'ITEM_DESTROYED' && row.targetUserId && (row.userId || row.botId != null)) {
      markers.push({ t: at, kind: 'loss', base: row.targetBaseId, what: `lost ${row.itemTypeName} to ${row.botId != null ? 'a bot' : 'a player'}` });
    } else if (row.type === 'BASE_DEFEATED' && row.targetUserId) {
      // Without an actor the base was removed or given up, not beaten
      const beaten = row.userId || row.botId != null;
      markers.push({ t: at, kind: beaten ? 'loss' : 'base', base: row.targetBaseId, what: beaten ? 'base destroyed' : 'base removed' });
    }
  }

  const types = {};
  for (const item of tracks.values()) {
    if (types[item.type]) continue;
    const type = world.unitTypes?.get(item.type);
    types[item.type] = { name: type?.name ?? `#${item.type}`, radius: type?.radius ?? 1, speed: type?.speed ?? null };
  }
  return {
    from,
    to,
    planet: world.planet,
    types,
    bases: Object.fromEntries(bases),
    items: [...tracks.values()].filter((item) => item.p.length),
    shots,
    markers: markers.sort((a, b) => a.t - b.t),
  };
}
