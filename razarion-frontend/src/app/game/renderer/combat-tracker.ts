import {Vector3} from '@babylonjs/core';

/**
 * Where fighting is happening right now.
 *
 * A camera that follows a base sits on the centre of everything that base owns, which is a
 * perfectly steady shot of a factory while the battle is at the far edge of the screen. What is
 * worth filming is where the shooting is - and the renderer is already told: every shot fired
 * raises onProjectileFired, and every unit destroyed raises onExplode. This keeps the last few
 * seconds of those and hands back their centre.
 *
 * Deliberately not a list of everything that ever happened: entries older than the window are
 * dropped on read, so the memory is bounded by the rate of fire and the camera stops looking at a
 * battle that is over.
 */
export class CombatTracker {
  /** How long a hit keeps drawing the camera. Long enough to bridge a reload, short enough that
   *  the camera leaves when the shooting does. */
  static readonly DEFAULT_WINDOW_MS = 4000;
  /** A burst of fire is many events a second; this is plenty to average a position from. */
  private static readonly MAX_EVENTS = 200;

  private readonly events: {x: number; y: number; z: number; at: number; baseIds: number[]}[] = [];

  /**
   * Record something violent at a place. `baseIds` are everyone involved (attacker and target),
   * so a camera following one base can ask for the fights that base is actually in rather than
   * every fight on the planet.
   */
  record(position: Vector3, baseIds: number[]): void {
    this.events.push({x: position.x, y: position.y, z: position.z, at: Date.now(), baseIds});
    if (this.events.length > CombatTracker.MAX_EVENTS) {
      this.events.splice(0, this.events.length - CombatTracker.MAX_EVENTS);
    }
  }

  /**
   * Centre of the recent fighting, or null when there has been none.
   *
   * @param baseId  only count fights this base is part of; undefined = anywhere.
   */
  centre(windowMs = CombatTracker.DEFAULT_WINDOW_MS, baseId?: number): Vector3 | null {
    const cutoff = Date.now() - windowMs;
    // Dropping the expired ones here rather than on a timer: the only thing that cares is this
    // call, and a tracker nobody reads should not be doing work.
    while (this.events.length && this.events[0].at < cutoff) {
      this.events.shift();
    }
    let x = 0, y = 0, z = 0, n = 0;
    for (const e of this.events) {
      if (baseId !== undefined && !e.baseIds.includes(baseId)) {
        continue;
      }
      x += e.x;
      y += e.y;
      z += e.z;
      n++;
    }
    return n === 0 ? null : new Vector3(x / n, y / n, z / n);
  }
}
