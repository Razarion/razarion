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
    public void buildingHoldingTheDestinationIsNotBlocked() {
        // The thing the unit walks up to (build / attack / dock) must stay reachable: if the goal
        // point sits inside the footprint, blocking it would make the goal unreachable and A* would
        // stop the unit at whatever best-fit node it found, possibly out of range.
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(199, 199), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertTrue("Building holding the destination must not be blocked", blocked.isEmpty());
    }

    /**
     * The regression this class was rewritten for. Before, a building within
     * {@code radius + unitRadius + clearance} of the start was dropped from the overlay entirely -
     * so A* routed straight through the factory a unit had just left, and every stuck-replan (which
     * recomputes from the unit's current, wedged position) reproduced the identical path.
     */
    @Test
    public void buildingAtStartIsBlockedEverywhereExceptRightAtTheUnit() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        // 6m from the start: with unitRadius 2 the old skip radius was 3 + 2 + 1 = 6, so this
        // building used to disappear completely.
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(16, 10), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertFalse("Overlay must not be empty — A* has to see the building", blocked.isEmpty());
        assertTrue("The far side of the building must still block", blocked.contains(new Index(20, 10)));
    }

    @Test
    public void theUnitCanAlwaysStepOffWhereItStands() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        // A unit wedged right against a building: 5m apart is exactly footprint (3 + 2), i.e.
        // touching. Its own node and everything within the clearance disc must stay free.
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(15, 10), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertFalse("The unit's own node must be free", blocked.contains(new Index(10, 10)));
        assertFalse("Stepping away must be free", blocked.contains(new Index(8, 10)));
        assertFalse("Stepping sideways must be free", blocked.contains(new Index(10, 12)));
        assertTrue("...but the building itself is still an obstacle", blocked.contains(new Index(15, 10)));
    }

    @Test
    public void arrivingNextToABuildingStaysPossible() {
        // Destination beside — not inside — a building: the whole-building exemption does not apply,
        // so the clearance disc is what keeps the last metre reachable.
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(206, 200), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertFalse("The destination node itself must be free", blocked.contains(new Index(200, 200)));
        assertTrue("The building is still an obstacle", blocked.contains(new Index(206, 200)));
    }

    /**
     * A unit that is genuinely *inside* a footprint — a building raised on top of it, a spawn that
     * landed badly — has to cross its own penetration depth before it is clear of the wall, and the
     * ordinary clearance disc does not reach that far. The disc grows to suit, so it can still get
     * out.
     */
    @Test
    public void aUnitTrappedInsideAFootprintCanStillLeave() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        // Footprint is 3 + 2 = 5; the unit centre is 1m from the building centre, i.e. 4m inside.
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(11, 10), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertFalse("The unit's own node must be free", blocked.contains(new Index(10, 10)));
        assertFalse("...and so must a way out through the wall it is inside",
                blocked.contains(new Index(7, 10)));
    }

    /**
     * The counterpart, and the distinction the whole rewrite turns on: a unit merely wedged against
     * a wall sits at penetration zero and must NOT get an enlarged disc, or the building disappears
     * from the overlay again and the replan goes back to reproducing the path it is stuck on.
     */
    @Test
    public void wedgedAgainstAWallIsNotMistakenForTrappedInside() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        // Exactly touching: distance 5 == footprint, penetration 0.
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(15, 10), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertTrue("Wedged against it — the building stays an obstacle", blocked.contains(new Index(18, 10)));
    }

    /**
     * No cliff between the two cases above. A unit that ORCA squeezed a few centimetres into a wall
     * is physically indistinguishable from one touching it, and must be treated the same way — the
     * disc grows by those centimetres, it does not jump to exempting the building.
     */
    @Test
    public void aHairInsideTheWallIsStillTreatedAsWedged() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        // Penetration 0.2m.
        buildings.add(new BuildingBlockerOverlay.Blocker(new DecimalPosition(14.8, 10), 3.0));

        Set<Index> blocked = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);

        assertTrue("Still an obstacle", blocked.contains(new Index(18, 10)));
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

    /**
     * The property that makes the stuck-replan able to do anything at all: a unit wedged against a
     * building must get a materially different overlay than the one that produced the path it is
     * stuck on. Under the old rule both overlays were empty and therefore identical.
     */
    @Test
    public void replanFromAWedgedPositionSeesTheBuilding() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        DecimalPosition factory = new DecimalPosition(15, 10);
        buildings.add(new BuildingBlockerOverlay.Blocker(factory, 3.0));

        Set<Index> firstPath = BuildingBlockerOverlay.compute(buildings, START, DESTINATION, UNIT_RADIUS);
        // The unit crawled a little and is now wedged at the factory wall.
        Set<Index> replan = BuildingBlockerOverlay.compute(buildings, new DecimalPosition(11, 10), DESTINATION, UNIT_RADIUS);

        assertFalse(firstPath.isEmpty());
        assertFalse(replan.isEmpty());
        // (18,10) is 3m from the factory centre — well inside the 5m footprint — and 7m from the
        // wedged unit, so no clearance disc reaches it. Not (20,10): that sits exactly on the
        // footprint boundary, where rasterizeCircle's inclusion is an implementation detail rather
        // than the property under test.
        assertTrue("The building must stay visible to A* from the wedged position too",
                replan.contains(new Index(18, 10)));
    }
}
