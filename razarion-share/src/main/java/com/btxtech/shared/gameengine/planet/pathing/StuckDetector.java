package com.btxtech.shared.gameengine.planet.pathing;

/**
 * Per-unit, server-side stuck detection used to trigger an A* replan when ORCA alone cannot free a
 * unit (e.g. wedged against a building or in a dense crowd).
 *
 * <p>Pure state machine — no game objects — so it is deterministic and unit-testable in isolation.
 * The owning {@code SyncPhysicalMovable} feeds it the per-tick facts (has a destination, is crowded,
 * made progress). It is NOT serialized: replanning is a MASTER-only concern and the resulting path
 * propagates to clients via the normal TickInfo channel.
 */
public class StuckDetector {

    private final int tickThreshold;
    private final int maxReplans;
    private int stuckTicks;
    private int replanCount;

    public StuckDetector(int tickThreshold, int maxReplans) {
        this.tickThreshold = tickThreshold;
        this.maxReplans = maxReplans;
    }

    /**
     * Advance one tick.
     *
     * @return true when the unit has been crowded with no progress for {@code tickThreshold}
     * consecutive ticks while still having a destination — i.e. a replan is due.
     */
    public boolean onTick(boolean hasDestination, boolean crowded, boolean madeProgress) {
        if (!hasDestination) {
            stuckTicks = 0;
            return false;
        }
        if (crowded && !madeProgress) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
        return stuckTicks >= tickThreshold;
    }

    /** True once the replan budget for the current movement is used up — the caller should give up
     * (stop the unit) instead of replanning forever against an unreachable goal. */
    public boolean replanBudgetExhausted() {
        return replanCount >= maxReplans;
    }

    /** A replan was performed: reset the stuck counter and consume one replan from the budget. */
    public void onReplan() {
        stuckTicks = 0;
        replanCount++;
    }

    /** A fresh command (or stop) reset: clear the stuck counter and refill the replan budget. */
    public void onFreshCommand() {
        stuckTicks = 0;
        replanCount = 0;
    }

    public int getReplanCount() {
        return replanCount;
    }
}
