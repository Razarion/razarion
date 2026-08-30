import {BabylonTerrainTileImpl} from './babylon-terrain-tile.impl';

/**
 * The serialized build queue and the settle counter that hangs off it.
 *
 * Both are static and shared by every tile on the page, which is what makes a single failure
 * expensive: before this was guarded, one tile throwing during its build skipped the reschedule at
 * the end of the drain and left every tile still queued behind it stuck on the green placeholder -
 * and left the counter raised, so whenBuildsSettled() could never resolve again for the life of the
 * page. In the studio that showed as "Building ground..." over a scene where nothing was building.
 *
 * Reaching the private statics on purpose: they are the surface the failure lives on, and a test
 * that built real tiles would need most of Babylon to say the same thing.
 */
describe('Terrain build queue', () => {
  const impl = BabylonTerrainTileImpl as any;

  /** Waits for the queue to drain; it hands control back through requestAnimationFrame. */
  function settle(): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, 120));
  }

  beforeEach(() => {
    impl.buildQueue = [];
    impl.buildQueueScheduled = false;
    impl.pendingBuilds = 0;
    impl.settleWaiters = [];
  });

  it('keeps draining after a task throws', async () => {
    const ran: string[] = [];
    impl.enqueueHeavyBuild(() => {
      ran.push('first');
      throw new Error('shader build failed');
    });
    impl.enqueueHeavyBuild(() => {
      ran.push('second');
      return true;
    });

    await settle();

    // Before the guard the second tile never ran: the throw escaped the drain and the reschedule
    // at the end of it was skipped.
    expect(ran).toEqual(['first', 'second']);
  });

  it('does not spin on a task that throws', async () => {
    let calls = 0;
    impl.enqueueHeavyBuild(() => {
      calls++;
      throw new Error('again');
    });

    await settle();

    // A throw counts as a frame spent. Treating it as a no-op would re-enter the while loop and
    // burn the whole queue in one frame - which is the freeze the queue exists to prevent.
    expect(calls).toBe(1);
  });

  /**
   * The counter is what tells the studio the ground is finished. A tile that gives up owes nothing
   * any more, and must say so - otherwise the wait never ends.
   */
  it('resolves the settle promise even when every build failed', async () => {
    impl.pendingBuilds = 2;
    let settled = false;
    BabylonTerrainTileImpl.whenBuildsSettled().then(() => settled = true);

    const tile = Object.create(BabylonTerrainTileImpl.prototype);
    impl.prototype.markBuildSettled.call(tile);
    expect(settled).withContext('one of two still owes work').toBeFalse();

    const other = Object.create(BabylonTerrainTileImpl.prototype);
    impl.prototype.markBuildSettled.call(other);
    await Promise.resolve();

    expect(settled).toBeTrue();
    expect(BabylonTerrainTileImpl.pendingTileBuilds).toBe(0);
  });

  /** One tile, one decrement - however many ways it is told it is done. */
  it('counts a tile once even if it settles twice', () => {
    impl.pendingBuilds = 1;
    const tile = Object.create(BabylonTerrainTileImpl.prototype);

    impl.prototype.markBuildSettled.call(tile);
    impl.prototype.markBuildSettled.call(tile);

    expect(BabylonTerrainTileImpl.pendingTileBuilds).toBe(0);
  });

  it('is already settled when nothing is building', async () => {
    let settled = false;
    BabylonTerrainTileImpl.whenBuildsSettled().then(() => settled = true);

    await Promise.resolve();

    expect(settled).toBeTrue();
  });
});
