package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Way point selection of {@link Path} against a counting terrain analyzer.
 * <p>
 * The point of these tests is the cost, not only the result: {@code setupCurrentWayPoint()} runs for
 * every moving unit in every tick and used to test the sight to every way point of the path each
 * time, which made it 97% of the pathing tick in production. A regression here is invisible in any
 * assertion about positions, so the sight tests are counted.
 */
public class PathWayPointTest {
    private static final double RADIUS = 1;
    private static final double LOOK_AHEAD_DISTANCE = 40;
    private static final int MAX_RETREAT_STEPS = 8;

    private CountingTerrainAnalyzer terrainAnalyzer;
    private TerrainService terrainService;

    @Before
    public void before() {
        terrainAnalyzer = new CountingTerrainAnalyzer();
        terrainService = new StubTerrainService(terrainAnalyzer);
    }

    @Test
    public void walkingALongPathCostsFarLessThanOneScanPerTick() {
        List<DecimalPosition> wayPositions = straightLine(300);
        Path path = createPath(wayPositions);
        TestPhysical unit = new TestPhysical(new DecimalPosition(0, 0));

        path.setupCurrentWayPoint(unit);
        int firstTickCalls = terrainAnalyzer.isInSightCount;
        int ticks = 20;
        for (int tick = 1; tick < ticks; tick++) {
            unit.moveTo(new DecimalPosition(tick, 0));
            path.setupCurrentWayPoint(unit);
        }

        int totalCalls = terrainAnalyzer.isInSightCount;
        Assert.assertTrue("isInSight() calls for " + ticks + " ticks: " + totalCalls
                        + ", way points: " + wayPositions.size()
                        + " (the former implementation needed up to " + ticks * wayPositions.size() + ")",
                totalCalls < wayPositions.size());
        int followUpCalls = totalCalls - firstTickCalls;
        Assert.assertTrue("isInSight() calls per follow up tick: " + followUpCalls / (double) (ticks - 1),
                followUpCalls <= 2 * (ticks - 1));
    }

    @Test
    public void theCurrentWayPointStaysWithinTheLookAheadDistance() {
        Path path = createPath(straightLine(300));
        TestPhysical unit = new TestPhysical(new DecimalPosition(0, 0));

        for (int tick = 0; tick < 20; tick++) {
            unit.moveTo(new DecimalPosition(tick, 0));
            path.setupCurrentWayPoint(unit);
            Assert.assertEquals("tick " + tick, LOOK_AHEAD_DISTANCE,
                    unit.getPosition().getDistance(path.getCurrentWayPoint()), 0.001);
        }
    }

    @Test
    public void aBlockedSightStopsTheAdvance() {
        Path path = createPath(straightLine(300));
        terrainAnalyzer.visible = target -> target.getX() <= 10;
        TestPhysical unit = new TestPhysical(new DecimalPosition(0, 0));

        path.setupCurrentWayPoint(unit);

        Assert.assertEquals(new DecimalPosition(10, 0), path.getCurrentWayPoint());
        Assert.assertFalse(path.isLastWayPoint());
    }

    /**
     * The braking in {@code SyncPhysicalMovable.setupPreferredVelocity()} and the arrival check in
     * {@code stopIfDestinationReached()} both hang off this flag - a unit whose path never reports
     * its last way point never slows down and never stops.
     */
    @Test
    public void theLastWayPointIsRecognized() {
        Path path = createPath(straightLine(5));
        TestPhysical unit = new TestPhysical(new DecimalPosition(0, 0));

        path.setupCurrentWayPoint(unit);

        Assert.assertEquals(new DecimalPosition(5, 0), path.getCurrentWayPoint());
        Assert.assertTrue(path.isLastWayPoint());

        Path longPath = createPath(straightLine(300));
        longPath.setupCurrentWayPoint(unit);
        Assert.assertFalse(longPath.isLastWayPoint());
    }

    @Test
    public void synchronizeDropsTheCachedIndex() {
        Path path = createPath(straightLine(300));
        TestPhysical unit = new TestPhysical(new DecimalPosition(0, 0));
        path.setupCurrentWayPoint(unit);

        List<DecimalPosition> newWayPositions = Arrays.asList(new DecimalPosition(100, 100),
                new DecimalPosition(101, 100),
                new DecimalPosition(102, 100));
        path.synchronize(new SyncPhysicalAreaInfo().setWayPositions(newWayPositions));
        unit.moveTo(new DecimalPosition(99, 100));
        path.setupCurrentWayPoint(unit);

        Assert.assertEquals(new DecimalPosition(102, 100), path.getCurrentWayPoint());
        Assert.assertTrue(path.isLastWayPoint());
    }

    /**
     * A unit pushed aside by ORCA can end up with its way point behind an obstacle. Without the walk
     * back it would drive into that obstacle instead of following its path.
     */
    @Test
    public void aWayPointOutOfSightIsGivenUp() {
        Path path = createPath(straightLine(20));
        TestPhysical unit = new TestPhysical(new DecimalPosition(0, 0));
        path.setupCurrentWayPoint(unit);
        Assert.assertEquals(new DecimalPosition(20, 0), path.getCurrentWayPoint());

        terrainAnalyzer.visible = target -> target.getX() <= 5;
        for (int tick = 0; tick < 3; tick++) {
            int before = terrainAnalyzer.isInSightCount;
            path.setupCurrentWayPoint(unit);
            Assert.assertTrue("isInSight() calls in one tick: " + (terrainAnalyzer.isInSightCount - before),
                    terrainAnalyzer.isInSightCount - before <= MAX_RETREAT_STEPS + 1);
        }

        Assert.assertTrue("way point still out of sight: " + path.getCurrentWayPoint(),
                path.getCurrentWayPoint().getX() <= 5);
    }

    private Path createPath(List<DecimalPosition> wayPositions) {
        SimplePath simplePath = new SimplePath();
        simplePath.setWayPositions(wayPositions);
        Path path = new Path(terrainService);
        path.init(simplePath);
        return path;
    }

    /** Way points one meter apart, like the node centers an A* path is made of. */
    private static List<DecimalPosition> straightLine(int count) {
        List<DecimalPosition> wayPositions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            wayPositions.add(new DecimalPosition(i, 0));
        }
        return wayPositions;
    }

    private static class CountingTerrainAnalyzer extends TerrainAnalyzer {
        private int isInSightCount;
        private Predicate<DecimalPosition> visible = target -> true;

        CountingTerrainAnalyzer() {
            super(null, null);
        }

        @Override
        public boolean isInSight(DecimalPosition start, double radius, DecimalPosition target, TerrainType terrainType) {
            isInSightCount++;
            return visible.test(target);
        }
    }

    private static class StubTerrainService extends TerrainService {
        private final TerrainAnalyzer terrainAnalyzer;

        StubTerrainService(TerrainAnalyzer terrainAnalyzer) {
            super(null, null, null);
            this.terrainAnalyzer = terrainAnalyzer;
        }

        @Override
        public TerrainAnalyzer getTerrainAnalyzer() {
            return terrainAnalyzer;
        }
    }

    private static class TestPhysical extends AbstractSyncPhysical {
        private DecimalPosition position;

        TestPhysical(DecimalPosition position) {
            super(null);
            this.position = position;
        }

        void moveTo(DecimalPosition position) {
            this.position = position;
        }

        @Override
        public DecimalPosition getPosition() {
            return position;
        }

        @Override
        public double getRadius() {
            return RADIUS;
        }

        @Override
        public TerrainType getTerrainType() {
            return TerrainType.LAND;
        }

        @Override
        public boolean canMove() {
            return true;
        }
    }
}
