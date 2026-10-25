// The engine's settings and its clock: when posts go out, how many wait in the buffer, which
// formats the planner may make. Stored in state/schedule.json, changed on the dashboard.
//
// A post goes out at a slot - every listed time on every listed day, in this PC's local time.
// Each slot publishes one post per network, the oldest approved one first, so the number of slots
// a week is the number of posts a week. A slot missed by more than GRACE_MS (the PC was off) is
// dropped rather than made up, so a day away does not end in a burst of posts.

import { join } from 'node:path';
import { STATE_DIR, readJson, writeJson } from './paths.mjs';

export const SCHEDULE_FILE = join(STATE_DIR, 'schedule.json');

export const DAYS = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
export const GRACE_MS = 3 * 3_600_000;

export const DEFAULTS = {
  // Nothing is produced or published while paused; the dashboard keeps working.
  paused: false,
  // Off: planned posts wait on "review" as before, for a person to approve.
  autoApprove: true,
  // A post whose text is the format's template because the writer failed - no login, or rejected
  // twice. Those read the same every time, which is what cost the reach on X in August.
  approveTemplate: false,
  days: ['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun'],
  times: ['18:00'],
  networks: { x: true, ig: true, fb: true, yt: true },
  // Posts made and not yet out that the planner keeps ready. Below it, the next one is made.
  buffer: 3,
  // When the planner may make something. A battle is filmed on the live planet for minutes.
  produceFrom: '00:00',
  produceTo: '24:00',
  // Days a format waits after its last post; the formats' own rules apply on top.
  formats: {
    battle: { enabled: true, gapDays: 1 },
    'week-in-numbers': { enabled: true, gapDays: 6 },
    devlog: { enabled: true, gapDays: 6 },
    duel: { enabled: true, gapDays: 2 },
  },
};

// A post time is 00:00 to 23:59; the end of the production hours may also be 24:00.
const TIME = /^([01]?\d|2[0-4]):([0-5]\d)$/;
const SLOT_TIME = /^([01]?\d|2[0-3]):([0-5]\d)$/;

/** The settings with every missing key taken from DEFAULTS. */
export function loadSchedule() {
  const stored = readJson(SCHEDULE_FILE, {}) || {};
  return {
    ...DEFAULTS,
    ...stored,
    networks: { ...DEFAULTS.networks, ...(stored.networks || {}) },
    formats: Object.fromEntries(Object.entries(DEFAULTS.formats).map(([name, d]) => [name, { ...d, ...(stored.formats?.[name] || {}) }])),
  };
}

/** Checks and stores changed settings; throws with a readable message on a bad value. */
export function saveSchedule(changes) {
  const next = { ...loadSchedule(), ...changes };
  const problems = [];
  if (!Array.isArray(next.days) || next.days.some((d) => !DAYS.includes(d))) problems.push('days must be from ' + DAYS.join(', '));
  if (!Array.isArray(next.times) || next.times.some((t) => !SLOT_TIME.test(t))) problems.push('times must be HH:MM');
  next.times = [...new Set(next.times)].sort();
  if (!Number.isInteger(next.buffer) || next.buffer < 0 || next.buffer > 20) problems.push('buffer must be 0 to 20');
  if (!TIME.test(next.produceFrom) || !TIME.test(next.produceTo)) problems.push('production hours must be HH:MM');
  for (const [name, f] of Object.entries(next.formats)) {
    if (!DEFAULTS.formats[name]) problems.push(`unknown format ${name}`);
    else if (!(f.gapDays >= 0 && f.gapDays <= 60)) problems.push(`${name}: gap must be 0 to 60 days`);
  }
  for (const key of ['paused', 'autoApprove', 'approveTemplate']) next[key] = !!next[key];
  if (problems.length) throw Object.assign(new Error(problems.join('; ')), { code: 422 });
  writeJson(SCHEDULE_FILE, next);
  return next;
}

const minutesOf = (hhmm) => {
  const [, h, m] = TIME.exec(hhmm);
  return Number(h) * 60 + Number(m);
};

function slotsOfDay(day, settings) {
  if (!settings.days.includes(DAYS[day.getDay()])) return [];
  return settings.times.map((t) => {
    const at = new Date(day);
    at.setHours(0, minutesOf(t), 0, 0);
    return at;
  });
}

/** The next `count` slots after `after`. Empty when no day or no time is set. */
export function nextSlots(settings, count, after = new Date()) {
  const out = [];
  if (!settings.days.length || !settings.times.length) return out;
  const day = new Date(after);
  day.setHours(0, 0, 0, 0);
  for (let i = 0; i < 400 && out.length < count; i++) {
    for (const slot of slotsOfDay(day, settings)) if (slot > after && out.length < count) out.push(slot);
    day.setDate(day.getDate() + 1);
  }
  return out;
}

/** The latest slot at or before `now` that is still within the grace period, or null. */
export function dueSlot(settings, now = new Date()) {
  const day = new Date(now);
  day.setHours(0, 0, 0, 0);
  let best = null;
  for (let i = 0; i < 2; i++) {
    for (const slot of slotsOfDay(day, settings)) {
      if (slot <= now && now - slot <= GRACE_MS && (!best || slot > best)) best = slot;
    }
    day.setDate(day.getDate() - 1);
  }
  return best;
}

/** Slots a week, which is posts a week per network. */
export function postsPerWeek(settings) {
  return settings.days.length * settings.times.length;
}

/** Whether `now` is inside the production hours. From after to means across midnight. */
export function mayProduce(settings, now = new Date()) {
  const m = now.getHours() * 60 + now.getMinutes();
  const from = minutesOf(settings.produceFrom);
  const to = minutesOf(settings.produceTo);
  return from <= to ? m >= from && m < to : m >= from || m < to;
}
