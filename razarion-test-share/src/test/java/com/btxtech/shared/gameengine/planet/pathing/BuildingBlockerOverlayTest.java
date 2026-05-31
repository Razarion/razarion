package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Pure (no terrain / no mocks) tests for the A* building blocker overlay.
 */
public class BuildingBlockerOverlayTest {

    private static final DecimalPosition START = new DecimalPosition(10, 10);
    private static final DecimalPosition DESTINATION = new DecimalPosition(200, 200);
    private static final double UNIT_RADIUS = 2.0;

    @Test
    public void noBuildingsYieldsEmptyOverlay() {
        Set<Index> blocked = BuildingBlockerOverlay.compute(Collections.emptyList(), START, DESTINATION, UNIT_RADIUS);
        assertTrue(blocked.isEmpty());
    }

    @Test
    public void buildingInTheCorridorIsBlocked() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(100, 100), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        // Centre node of the building must be blocked.
        assertTrue("Building centre node must be blocked", blocked.contains(new Index(100, 100)));
        // Inflated by the unit radius (3 + 2 = 5), so a node ~4m out is still blocked...
        assertTrue(blocked.contains(new Index(104, 100)));
        // ...but a node well beyond building radius + unit radius is free.
        assertFalse(blocked.contains(new Index(120, 100)));
    }

    @Test
    public void buildingAtDestinationIsNotBlocked() {
        // The thing the unit walks up to (build / attack / harvest target) must stay reachable.
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(198, 198), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertTrue("Building at the destination must not be blocked", blocked.isEmpty());
    }

    @Test
    public void buildingAtStartIsNotBlocked() {
        // A unit exiting/standing next to its own building (e.g. factory) must be able to leave.
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(12, 12), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertTrue("Building at the start must not be blocked", blocked.isEmpty());
    }

    @Test
    public void multipleBuildingsAccumulate() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(80, 80), 3.0));
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(120, 120), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertTrue(blocked.contains(new Index(80, 80)));
        assertTrue(blocked.contains(new Index(120, 120)));
    }
}
