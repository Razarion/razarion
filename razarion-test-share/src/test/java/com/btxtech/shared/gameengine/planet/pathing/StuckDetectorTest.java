package com.btxtech.shared.gameengine.planet.pathing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Pure tests for the stuck-detection state machine (no game objects / no mocks).
 */
public class StuckDetectorTest {

    private static final int THRESHOLD = 5;
    private static final int MAX_REPLANS = 3;

    @Test
    public void firesAfterThresholdOfCrowdedNoProgress() {
        StuckDetector detector = new StuckDetector(THRESHOLD, MAX_REPLANS);
        for (int i = 0; i < THRESHOLD - 1; i++) {
            assertFalse("Must not fire before threshold (tick " + i + ")", detector.onTick(true, true, false));
        }
        assertTrue("Must fire on the threshold-th crowded-no-progress tick", detector.onTick(true, true, false));
    }

    @Test
    public void progressResetsCounter() {
        StuckDetector detector = new StuckDetector(THRESHOLD, MAX_REPLANS);
        for (int i = 0; i < THRESHOLD - 1; i++) {
            detector.onTick(true, true, false);
        }
        // A tick with progress resets the counter...
        assertFalse(detector.onTick(true, true, true));
        // ...so it now takes another full threshold to fire.
        for (int i = 0; i < THRESHOLD - 1; i++) {
            assertFalse(detector.onTick(true, true, false));
        }
        assertTrue(detector.onTick(true, true, false));
    }

    @Test
    public void notCrowdedDoesNotAccumulate() {
        StuckDetector detector = new StuckDetector(THRESHOLD, MAX_REPLANS);
        for (int i = 0; i < THRESHOLD * 2; i++) {
            assertFalse("Moving (not crowded) must never be stuck", detector.onTick(true, false, false));
        }
    }

    @Test
    public void noDestinationNeverFiresAndResets() {
        StuckDetector detector = new StuckDetector(THRESHOLD, MAX_REPLANS);
        for (int i = 0; i < THRESHOLD - 1; i++) {
            detector.onTick(true, true, false);
        }
        assertFalse("No destination must reset and never fire", detector.onTick(false, true, false));
        // Counter was reset, so a fresh threshold is needed.
        for (int i = 0; i < THRESHOLD - 1; i++) {
            assertFalse(detector.onTick(true, true, false));
        }
        assertTrue(detector.onTick(true, true, false));
    }

    @Test
    public void replanBudgetIsConsumedAndExhausts() {
        StuckDetector detector = new StuckDetector(THRESHOLD, MAX_REPLANS);
        assertFalse(detector.replanBudgetExhausted());
        for (int i = 0; i < MAX_REPLANS; i++) {
            assertFalse("Budget must not be exhausted before " + MAX_REPLANS + " replans", detector.replanBudgetExhausted());
            detector.onReplan();
        }
        assertTrue("Budget exhausted after MAX_REPLANS", detector.replanBudgetExhausted());
        assertEquals(MAX_REPLANS, detector.getReplanCount());
    }

    @Test
    public void onReplanResetsStuckCounter() {
        StuckDetector detector = new StuckDetector(THRESHOLD, MAX_REPLANS);
        for (int i = 0; i < THRESHOLD - 1; i++) {
            detector.onTick(true, true, false);
        }
        detector.onReplan();
        // After a replan it takes a full threshold again before firing.
        for (int i = 0; i < THRESHOLD - 1; i++) {
            assertFalse(detector.onTick(true, true, false));
        }
        assertTrue(detector.onTick(true, true, false));
    }

    @Test
    public void freshCommandRefillsBudget() {
        StuckDetector detector = new StuckDetector(THRESHOLD, MAX_REPLANS);
        detector.onReplan();
        detector.onReplan();
        detector.onFreshCommand();
        assertEquals(0, detector.getReplanCount());
        assertFalse(detector.replanBudgetExhausted());
    }
}
