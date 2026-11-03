const HOUR = 3_600_000;
const HOURS_OF_VOLUME = 48;
const keyframeSize = (keyframe) => (keyframe.source === 'connect' ? 0 : keyframe.text.length);

/**
 * Everything the server sent, as it was sent, with the time it arrived - in memory only.
 *
 * The raw text is kept rather than what the recorder made of it: a replay feeds exactly these
 * messages to a real game client, and whatever the analysis learns to read later can be read from
 * what is already here. Keyframes are whole worlds (INITIAL_SLAVE_SYNC_INFO) to start a replay at.
 *
 * Dropped oldest first when older than keepMs or when the budget is used up. Sizes are counted in
 * characters; the messages are ASCII JSON, which V8 holds at one byte per character.
 */
export class Tape {
  constructor({ keepMs, maxBytes }) {
    this.keepMs = keepMs;
    this.maxBytes = maxBytes;
    this.entries = [];
    this.head = 0; // entries before this index are dropped, compacted now and then
    this.keyframes = [];
    this.bytes = 0;
    this.dropped = { entries: 0, bytes: 0 };
    this.startedAt = Date.now();
    this.total = { count: 0, bytes: 0 };
    this.byName = new Map(); // name -> {count, bytes, maxBytes}
    this.hours = []; // [{hour, count, bytes, byName: {name: bytes}}], newest last
  }

  add(entry) {
    const size = entry.text.length;
    this.entries.push(entry);
    this.bytes += size;
    this.count(entry.t, entry.name, size);
    this.trim(entry.t);
  }

  /** A keyframe that is also a message on the tape (a connect) shares its text and costs nothing. */
  addKeyframe(keyframe) {
    this.keyframes.push(keyframe);
    this.bytes += keyframeSize(keyframe);
    this.trim(keyframe.t);
  }

  count(t, name, size) {
    this.total.count++;
    this.total.bytes += size;
    const stat = this.byName.get(name) || { count: 0, bytes: 0, maxBytes: 0 };
    stat.count++;
    stat.bytes += size;
    stat.maxBytes = Math.max(stat.maxBytes, size);
    this.byName.set(name, stat);

    const hour = Math.floor(t / HOUR) * HOUR;
    let bucket = this.hours[this.hours.length - 1];
    if (!bucket || bucket.hour !== hour) {
      bucket = { hour, count: 0, bytes: 0, byName: {} };
      this.hours.push(bucket);
      if (this.hours.length > HOURS_OF_VOLUME) this.hours.shift();
    }
    bucket.count++;
    bucket.bytes += size;
    bucket.byName[name] = (bucket.byName[name] || 0) + size;
  }

  trim(now) {
    const cutoff = now - this.keepMs;
    while (this.head < this.entries.length) {
      const oldest = this.entries[this.head];
      if (oldest.t >= cutoff && this.bytes <= this.maxBytes) break;
      this.bytes -= oldest.text.length;
      this.dropped.entries++;
      this.dropped.bytes += oldest.text.length;
      this.entries[this.head++] = undefined;
    }
    // Keyframes older than the oldest message held start nothing that can still be played.
    const oldestT = this.head < this.entries.length ? this.entries[this.head].t : now;
    while (this.keyframes.length && this.keyframes[0].t < oldestT) {
      this.bytes -= keyframeSize(this.keyframes.shift());
    }
    if (this.head > 10_000 && this.head * 2 > this.entries.length) {
      this.entries = this.entries.slice(this.head);
      this.head = 0;
    }
  }

  /** Messages with from <= t < to, oldest first. */
  range(from, to) {
    const out = [];
    for (let i = this.indexOf(from); i < this.entries.length; i++) {
      const entry = this.entries[i];
      if (entry.t >= to) break;
      if (entry.t >= from) out.push(entry);
    }
    return out;
  }

  /** The index of the first message with t >= from. The tape is in arrival order, so t never falls. */
  indexOf(from) {
    let lo = this.head;
    let hi = this.entries.length;
    while (lo < hi) {
      const mid = (lo + hi) >> 1;
      if (this.entries[mid].t < from) lo = mid + 1;
      else hi = mid;
    }
    return lo;
  }

  get held() {
    return this.entries.length - this.head;
  }

  get oldest() {
    return this.held ? this.entries[this.head].t : null;
  }

  summary() {
    const hours = (Date.now() - this.startedAt) / HOUR;
    return {
      startedAt: this.startedAt,
      held: { entries: this.held, keyframes: this.keyframes.length, bytes: this.bytes, oldest: this.oldest },
      budget: { keepMs: this.keepMs, maxBytes: this.maxBytes },
      dropped: this.dropped,
      total: this.total,
      // The first connect's world makes any shorter average meaningless.
      bytesPerHour: hours >= 1 / 6 ? Math.round(this.total.bytes / hours) : null,
      byName: Object.fromEntries([...this.byName].sort((a, b) => b[1].bytes - a[1].bytes)),
      hours: this.hours,
      keyframeSizes: this.keyframes.slice(-12).map((k) => ({ t: k.t, bytes: k.text.length, source: k.source })),
    };
  }
}
