import {Vector3} from '@babylonjs/core';
import {CombatTracker} from './combat-tracker';

/**
 * The tracker decides where a filmed battle is, and the three things it has to get right are the
 * three that cannot be seen from a screenshot: that it averages rather than snaps, that it forgets,
 * and that it only counts fights the followed base is actually in.
 *
 * Tested here rather than in the live world because a fight cannot be staged on demand - the one
 * attempt hit the item limit of the base the staging endpoint spawns from, and a test that needs a
 * war to start is a test that runs once.
 */
describe('CombatTracker', () => {
  let now: number;

  beforeEach(() => {
    now = 1_000_000;
    spyOn(Date, 'now').and.callFake(() => now);
  });

  it('has no opinion before anything has happened', () => {
    expect(new CombatTracker().centre()).toBeNull();
  });

  it('sits between the fights rather than on the last one', () => {
    const tracker = new CombatTracker();
    tracker.record(new Vector3(0, 0, 0), [1, 2]);
    tracker.record(new Vector3(100, 0, 50), [1, 2]);

    const centre = tracker.centre()!;
    expect(centre.x).toBe(50);
    expect(centre.z).toBe(25);
  });

  it('forgets a battle that is over', () => {
    const tracker = new CombatTracker();
    tracker.record(new Vector3(10, 0, 10), [1]);

    now += CombatTracker.DEFAULT_WINDOW_MS + 1;

    expect(tracker.centre()).toBeNull();
  });

  it('follows the fighting as it moves, without jumping to it', () => {
    const tracker = new CombatTracker();
    tracker.record(new Vector3(0, 0, 0), [1]);
    now += 1000;
    tracker.record(new Vector3(60, 0, 0), [1]);

    // Both are still inside the window, so the answer is between them - not the newest one.
    expect(tracker.centre()!.x).toBe(30);
  });

  it('counts only the fights the followed base is in', () => {
    const tracker = new CombatTracker();
    tracker.record(new Vector3(0, 0, 0), [1, 2]);      // our base is in this one
    tracker.record(new Vector3(1000, 0, 1000), [3, 4]); // somebody else's war, far away

    expect(tracker.centre(CombatTracker.DEFAULT_WINDOW_MS, 1)!.x).toBe(0);
    expect(tracker.centre(CombatTracker.DEFAULT_WINDOW_MS, 3)!.x).toBe(1000);
    // Without a base, everything counts - which is why a follow camera must always pass one.
    expect(tracker.centre()!.x).toBe(500);
  });

  it('records both sides of a shot, so either camera counts it', () => {
    const tracker = new CombatTracker();
    tracker.record(new Vector3(42, 0, 0), [7, 8]);

    expect(tracker.centre(CombatTracker.DEFAULT_WINDOW_MS, 7)).not.toBeNull();
    expect(tracker.centre(CombatTracker.DEFAULT_WINDOW_MS, 8)).not.toBeNull();
    expect(tracker.centre(CombatTracker.DEFAULT_WINDOW_MS, 9)).toBeNull();
  });
});
