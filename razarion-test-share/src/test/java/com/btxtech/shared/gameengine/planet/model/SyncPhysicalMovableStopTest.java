package com.btxtech.shared.gameengine.planet.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The contract that {@code destinationUnreachable} carries, and the asymmetry that broke it.
 *
 * <p>Four abilities read this flag to tell "the movement layer gave up" from "I arrived and then
 * drifted out of range" — two situations that both leave {@code hasDestination()} false and need
 * opposite answers. {@link SyncPhysicalMovable#stop()} clears the flag, so anything that stops
 * *because it gave up* has to say so, and {@link SyncPhysicalMovable#stopUnreachable()} is the only
 * way to do that.
 *
 * <p>PathingService.replanStuckItems called plain {@code stop()} for a year. The give-up was
 * invisible, the owning ability re-issued the job in the same tick, and PROD logged the same Viper
 * giving up thirteen times at intervals of exactly 6.0 s with nobody clicking.
 *
 * <p>No world and no container needed: both methods only touch fields on this object, which is what
 * lets the contract be pinned without the integration harness.
 */
public class SyncPhysicalMovableStopTest {

    private static SyncPhysicalMovable movable() {
        return new SyncPhysicalMovable(null, null);
    }

    @Test
    public void ordinaryStopLeavesTheDestinationReachable() {
        SyncPhysicalMovable movable = movable();

        movable.stop();

        assertFalse("An ordinary stop is an arrival, not a give-up", movable.isDestinationUnreachable());
    }

    @Test
    public void givingUpSurvivesTheStopThatCarriesIt() {
        SyncPhysicalMovable movable = movable();

        movable.stopUnreachable();

        assertTrue("The ability has to see this in the next tick", movable.isDestinationUnreachable());
        assertFalse("...and the unit is stopped either way", movable.hasDestination());
    }

    @Test
    public void aFreshStopClearsAnEarlierGiveUp() {
        // The flag must not outlive the job it belongs to: a unit that gave up, then got a new
        // command and completed it, must not report the old give-up to the next ability.
        SyncPhysicalMovable movable = movable();
        movable.stopUnreachable();

        movable.stop();

        assertFalse(movable.isDestinationUnreachable());
    }
}
