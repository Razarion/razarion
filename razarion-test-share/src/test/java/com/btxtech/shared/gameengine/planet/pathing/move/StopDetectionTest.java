package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * Reproduces the orbit-bug.
 *
 * MOVING_TEST item: speed=17 m/s, angularVelocity=π rad/s, radius=2.
 * STOP_DETECTION_DISTANCE in SyncPhysicalMovable is 0.5 m.
 * → steady-state turning radius ≈ 5.4 m, dwarfing the 0.5 m stop circle. If the
 * vehicle ever sweeps a tangent around the destination it can orbit indefinitely.
 *
 * Negative cases tried (DO stop cleanly — for future regression coverage):
 *   - 180° behind, no obstacle: decel-on-large-angle settles the unit.
 *   - Perpendicular at the steady-state turning radius: settles.
 *   - 40° off heading, 3 m away: settles.
 * So the orbit is NOT caused by tangential arrival alone — it requires the
 * destination to sit in a region the unit physically cannot stand on (inside
 * an obstacle's vehicle-radius + grow clearance zone).
 */
public class StopDetectionTest extends ScenarioBaseTest {

    private static final int MAX_TICKS = 300; // 30 s game time
    private static final int SPEEDUP_MAX_TICKS = 60;

    /**
     * Interactive reproducer (manual). Spawns a unit at (132, 19), gives it a path
     * straight to (136, 62), then opens the JavaFX debug display before ticking the
     * planet. Close the display to let the test run; ticks then drain in CPU mode.
     * Tune start/destination here to find new orbit geometries by eye.
     */
    @Test
    public void interactiveOrbitDebug() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(
                0, userContext.getLevelId(), Collections.emptyMap(),
                userContext.getUserId(), userContext.getName());
        playerBase.setResources(Double.MAX_VALUE);

        DecimalPosition start = new DecimalPosition(132, 19);
        DecimalPosition destination = new DecimalPosition(136, 62);

        SyncBaseItem unit;
        SimplePath path;
        try {
            unit = getBaseItemService().spawnSyncBaseItem(
                    getTestShareDagger().itemTypeService().getBaseItemType(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID),
                    start, 0, playerBase, true);
            path = getPathingService().setupPathToDestination(unit, destination);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SyncPhysicalMovable movable = (SyncPhysicalMovable) unit.getAbstractSyncPhysical();
        movable.setPath(path);

        //showDisplay(new PositionMarker().addPosition(start).addPosition(destination));

        int ticks = 0;
        while (movable.hasDestination() && ticks < MAX_TICKS) {
            tickPlanetService();
            ticks++;
        }

        if (movable.hasDestination()) {
            DecimalPosition pos = unit.getAbstractSyncPhysical().getPosition();
            Assert.fail("Unit did not stop within " + MAX_TICKS + " ticks. "
                    + "Position: " + pos
                    + " distance-to-destination: " + pos.getDistance(destination)
                    + " destination: " + destination);
        }
    }

    /**
     * Automated reproducer #1 — fresh path to a destination inside an obstacle's
     * clearance zone.
     *
     * Obstacle radius 10 at (420, 115); destination at (431, 115) — only 1m past
     * the obstacle's edge, inside the vehicle-radius (2) + grow (1) = 3m clearance.
     * A* hands out a path whose final waypoint isn't actually reachable. Once the
     * vehicle reaches the second-to-last waypoint, line-of-sight to the destination
     * flickers between "visible" and "blocked", flipping the heading each tick →
     * orbit at 30–55 m distance forever.
     */
    @Test
    public void orbitsWhenDestinationInsideObstacleClearance() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(
                0, userContext.getLevelId(), Collections.emptyMap(),
                userContext.getUserId(), userContext.getName());
        playerBase.setResources(Double.MAX_VALUE);

        DecimalPosition start = new DecimalPosition(370, 115);
        DecimalPosition destination = new DecimalPosition(431, 115);

        SyncBaseItem unit;
        SimplePath path;
        try {
            unit = getBaseItemService().spawnSyncBaseItem(
                    getTestShareDagger().itemTypeService().getBaseItemType(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID),
                    start, 0, playerBase, true);
            path = getPathingService().setupPathToDestination(unit, destination);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SyncPhysicalMovable movable = (SyncPhysicalMovable) unit.getAbstractSyncPhysical();
        movable.setPath(path);

        int ticks = 0;
        while (movable.hasDestination() && ticks < MAX_TICKS) {
            tickPlanetService();
            ticks++;
        }

        if (movable.hasDestination()) {
            DecimalPosition pos = unit.getAbstractSyncPhysical().getPosition();
            Assert.fail("Orbit-bug (fresh path): unit did not stop within " + MAX_TICKS + " ticks. "
                    + "Position: " + pos
                    + " distance-to-destination: " + pos.getDistance(destination)
                    + " destination: " + destination);
        }
    }

    /**
     * Smooth arrival: the vehicle must decelerate as it approaches the final waypoint
     * (no sudden full-speed → zero drop) and must NOT teleport onto the exact waypoint
     * (no visible "jump"). With max speed 17 m/s, accel 5 m/s², braking distance from
     * full speed is ~29 m, so a clear ~140 m run leaves plenty of room.
     */
    @Test
    public void vehicleDeceleratesAndStopsWithoutTeleport() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(
                0, userContext.getLevelId(), Collections.emptyMap(),
                userContext.getUserId(), userContext.getName());
        playerBase.setResources(Double.MAX_VALUE);

        DecimalPosition start = new DecimalPosition(370, 115);
        DecimalPosition destination = new DecimalPosition(290, 115);

        SyncBaseItem unit;
        try {
            unit = getBaseItemService().spawnSyncBaseItem(
                    getTestShareDagger().itemTypeService().getBaseItemType(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID),
                    start, 0, playerBase, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SyncPhysicalMovable movable = (SyncPhysicalMovable) unit.getAbstractSyncPhysical();
        // Single-waypoint path → the destination is always the "last waypoint" from
        // the first tick, so the braking logic engages as soon as the vehicle is
        // within braking distance (no multi-waypoint A* artefacts confusing the test).
        SimplePath path = new SimplePath();
        path.setWayPositions(java.util.Collections.singletonList(destination));
        movable.setPath(path);

        double maxSpeed = 0;
        double speedBeforeStop = 0;
        int ticks = 0;
        while (movable.hasDestination() && ticks < MAX_TICKS) {
            if (movable.getVelocity() != null) {
                double v = movable.getVelocity().magnitude();
                maxSpeed = Math.max(maxSpeed, v);
                speedBeforeStop = v;
            }
            tickPlanetService();
            ticks++;
        }

        Assert.assertFalse("Vehicle never stopped (ticks=" + ticks + ")", movable.hasDestination());
        // Vehicle should have reached top speed on the long straight path.
        Assert.assertTrue("Vehicle never accelerated to near top speed (max=" + maxSpeed + " m/s)",
                maxSpeed > 16);
        // Without smooth braking, last velocity = ~full speed = ~17 m/s (abrupt stop).
        // With braking, last velocity should be well below max — vehicle entered the
        // 0.5 m stop circle at ≈ sqrt(2·a·0.5) = 2.24 m/s and was decelerating.
        Assert.assertTrue("Vehicle should brake before stopping but last v=" + speedBeforeStop + " m/s",
                speedBeforeStop < 5);
        DecimalPosition finalPos = unit.getAbstractSyncPhysical().getPosition();
        double distToDest = finalPos.getDistance(destination);
        Assert.assertTrue("Vehicle stopped too far from destination: " + distToDest + "m",
                distToDest < 1.5);
        // Without the setPosition2d teleport, the stop happens wherever the vehicle
        // actually was when its motion line crossed the stop circle — not snapped to
        // the exact waypoint. Distance should be non-zero (typically 0.1–0.5 m).
        Assert.assertTrue("Vehicle teleported exactly onto destination (dist=" + distToDest + "m) — "
                        + "expected natural stop without setPosition2d snap",
                distToDest > 0.001);
    }

    /**
     * Automated reproducer #2 — prod-system sequence:
     *   1. MoveCommand A drives the unit east; it reaches full speed.
     *   2. MoveCommand B redirects mid-flight to a destination inside the clearance
     *      zone of an obstacle the arc must pass near.
     *
     * Confirms the orbit happens regardless of whether the path was set fresh
     * at spawn or via a mid-flight redirect.
     */
    @Test
    public void orbitsAfterMidFlightRedirectIntoObstacleClearance() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(
                0, userContext.getLevelId(), Collections.emptyMap(),
                userContext.getUserId(), userContext.getName());
        playerBase.setResources(Double.MAX_VALUE);

        SyncBaseItem unit;
        try {
            unit = getBaseItemService().spawnSyncBaseItem(
                    getTestShareDagger().itemTypeService().getBaseItemType(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID),
                    new DecimalPosition(370, 115), 0, playerBase, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SyncPhysicalMovable movable = (SyncPhysicalMovable) unit.getAbstractSyncPhysical();

        // 1. Drive east past the obstacle area.
        getCommandService().move(unit, new DecimalPosition(500, 115));

        // 2. Tick until full speed (~17 m/s).
        int speedupTicks = 0;
        while (movable.getVelocity() == null || movable.getVelocity().magnitude() < 16.0) {
            tickPlanetService();
            speedupTicks++;
            if (speedupTicks > SPEEDUP_MAX_TICKS) {
                Assert.fail("Unit didn't reach full speed in " + SPEEDUP_MAX_TICKS + " ticks. velocity="
                        + (movable.getVelocity() == null ? "null" : movable.getVelocity().magnitude()));
            }
        }
        DecimalPosition fullSpeedPos = unit.getAbstractSyncPhysical().getPosition();

        // 3. Redirect to a point inside the obstacle's clearance (the same unreachable
        // target as the fresh-path reproducer above).
        DecimalPosition redirect = new DecimalPosition(431, 115);
        getCommandService().move(unit, redirect);

        int ticks = 0;
        while (movable.hasDestination() && ticks < MAX_TICKS) {
            tickPlanetService();
            ticks++;
        }

        if (movable.hasDestination()) {
            DecimalPosition finalPos = unit.getAbstractSyncPhysical().getPosition();
            Assert.fail("Orbit-bug (mid-flight redirect): unit did not stop within " + MAX_TICKS + " ticks. "
                    + "Final position: " + finalPos
                    + " distance-to-redirect: " + finalPos.getDistance(redirect)
                    + " redirect: " + redirect
                    + " full-speed pos: " + fullSpeedPos);
        }
    }
}
