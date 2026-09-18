package com.btxtech.shared.gameengine.datatypes;


import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.pathing.BuildingBlockerOverlay.Blocker;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;


public class Path {
    /**
     * A way point farther away than this is not looked at. The sight test costs three rasterized
     * rays whose length is the distance, so an unbounded look ahead makes the first tick of a long
     * path scan the whole way point list with the most expensive rays there are - way points sit one
     * NODE_SIZE apart, so a cross map path holds hundreds of them.
     * <p>
     * Must stay above the longest braking distance v^2/(2*a) (~29m for the fastest unit), otherwise
     * the final way point would only become current after the point where the unit has to start
     * slowing down for it, and it would overshoot its destination.
     */
    private static final double MAX_LOOK_AHEAD_DISTANCE = 40;
    /**
     * Way points behind the current one are only revisited when the unit lost sight of its target,
     * which on a valid path means it was pushed aside by ORCA. Walking back the whole list would
     * reintroduce exactly the unbounded per tick scan this class avoids, and a unit that cannot see
     * eight way points back is a case for the stuck detection, not for a longer search.
     */
    private static final int MAX_RETREAT_STEPS = 8;
    private static final int UNSET_INDEX = -1;
    /**
     * How far a building centre may lie outside the rectangle of a sight segment and still reach into it:
     * the largest building radius plus the largest unit radius (PROD 09/2026: Refinery 3.45, units up
     * to 2.0), with room to spare. The cell grid files a building under its centre only.
     */
    private static final double BUILDING_REACH = 10;
    /**
     * A building footprint is kept this far below the distance of both ends of a sight segment, so
     * neither end ever counts as inside it - see {@link #isBuildingInTheWay}.
     */
    private static final double ENDPOINT_MARGIN = 0.05;

    private final TerrainService terrainService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    /**
     * Buildings around the unit, valid for one {@link #setupCurrentWayPoint} and collected only when a
     * sight test gets past the terrain. A tick of a moving unit usually runs one sight test, so the
     * first collection covers only the rectangle around that sight segment. A second test in the same
     * tick - the first tick of a path runs one per way point in the look ahead - collects the whole
     * look ahead once instead of querying per test.
     */
    private final List<Blocker> nearbyBuildings = new ArrayList<>();
    /** Where {@link #nearbyBuildings} is complete for a sight segment, null while nothing is collected. */
    private Rectangle2D nearbyBuildingsCover;
    private boolean nearbyBuildingsWholeLookAhead;
    private List<DecimalPosition> wayPositions;
    private DecimalPosition currentWayPoint;
    /**
     * Position of {@link #currentWayPoint} in {@link #wayPositions}, {@link #UNSET_INDEX} while it
     * has not been derived yet. Deliberately not part of {@link SyncPhysicalAreaInfo}: master and
     * slave must both re-derive it from the way points and the item position, otherwise the
     * prediction in the worker would depend on a value the server happens to send.
     */
    private int currentWayPointIndex = UNSET_INDEX;

    @Inject
    public Path(TerrainService terrainService, SyncItemContainerServiceImpl syncItemContainerService) {
        this.terrainService = terrainService;
        this.syncItemContainerService = syncItemContainerService;
    }

    /**
     * @param path the path
     */
    public void init(SimplePath path) {
        if (path.getWayPositions().isEmpty()) {
            throw new IllegalArgumentException("At least one way point must be available");
        }
        wayPositions = path.getWayPositions();
        currentWayPointIndex = UNSET_INDEX;
    }

    public void setupCurrentWayPoint(AbstractSyncPhysical abstractSyncPhysical) {
        DecimalPosition itemPosition = abstractSyncPhysical.getPosition();
        double radius = abstractSyncPhysical.getRadius();
        TerrainType terrainType = abstractSyncPhysical.getTerrainType();
        if (currentWayPointIndex == UNSET_INDEX) {
            currentWayPointIndex = nearestWayPointIndex(itemPosition);
        }
        nearbyBuildingsCover = null;
        nearbyBuildingsWholeLookAhead = false;
        // Either way the way point handed out was checked against the terrain and the buildings in
        // this tick, unless the walk back ran out of steps or hit the start of the path.
        if (!advanceToFarthestVisible(itemPosition, radius, terrainType)) {
            retreatToVisible(itemPosition, radius, terrainType);
        }
        nearbyBuildings.clear();
        currentWayPoint = wayPositions.get(currentWayPointIndex);
    }

    /**
     * Moves the index forward while the next way point is in sight, which is the same answer the
     * former full backwards scan gave whenever visibility is contiguous - and it is, on a path whose
     * way points are adjacent free nodes. Over the life of a path this costs one sight test per way
     * point in total instead of one per way point per tick.
     *
     * @return whether the index moved, in which case the new current way point is known to be in
     * sight and needs no further check.
     */
    private boolean advanceToFarthestVisible(DecimalPosition itemPosition, double radius, TerrainType terrainType) {
        boolean advanced = false;
        while (currentWayPointIndex < wayPositions.size() - 1) {
            DecimalPosition next = wayPositions.get(currentWayPointIndex + 1);
            if (itemPosition.getDistance(next) > MAX_LOOK_AHEAD_DISTANCE) {
                break;
            }
            if (!isInSight(itemPosition, radius, next, terrainType)) {
                break;
            }
            currentWayPointIndex++;
            advanced = true;
        }
        return advanced;
    }

    /**
     * Steps back until the current way point is in sight again. Driving towards a way point behind an
     * obstacle makes the unit push into that obstacle instead of following its path.
     */
    private void retreatToVisible(DecimalPosition itemPosition, double radius, TerrainType terrainType) {
        for (int step = 0; step < MAX_RETREAT_STEPS && currentWayPointIndex > 0; step++) {
            if (isInSight(itemPosition, radius, wayPositions.get(currentWayPointIndex), terrainType)) {
                return;
            }
            currentWayPointIndex--;
        }
    }

    /**
     * Whether the unit can drive straight to {@code target}. Has to see what A* saw: the terrain, and
     * the buildings of {@code BuildingBlockerOverlay}. A follower that only knows the terrain looks
     * past the detour A* made around a building, steers through the building towards the destination,
     * and ORCA pins the unit to the wall. A replan gets the same detour and the follower skips it
     * again - the "replan returned the identical path" at the own base in PROD
     * (docs/architecture/pathing-flowfield-analysis.md).
     */
    private boolean isInSight(DecimalPosition itemPosition, double radius, DecimalPosition target, TerrainType terrainType) {
        return terrainService.getTerrainAnalyzer().isInSight(itemPosition, radius, target, terrainType)
                && !isBuildingInTheWay(itemPosition, radius, target);
    }

    /**
     * Tests the sight segment against each building footprint inflated by the unit radius, as A* does.
     * <p>
     * The footprint never reaches the unit or the target. A* frees a clearance disc around the start,
     * so a unit touching a building - or squeezed a few centimetres into it - gets its first way points
     * inside that footprint, and a full footprint would hide every one of them: the unit would stand
     * still in front of the path meant to lead it away.
     */
    private boolean isBuildingInTheWay(DecimalPosition itemPosition, double radius, DecimalPosition target) {
        if (nearbyBuildingsCover == null) {
            collectNearbyBuildings(itemPosition, radius, Rectangle2D.generateRectangleFromAnyPoints(itemPosition, target));
        } else if (!nearbyBuildingsWholeLookAhead && !(nearbyBuildingsCover.contains(itemPosition) && nearbyBuildingsCover.contains(target))) {
            nearbyBuildingsWholeLookAhead = true;
            collectNearbyBuildings(itemPosition, radius, Rectangle2D.generateRectangleFromMiddlePoint(itemPosition, 2.0 * MAX_LOOK_AHEAD_DISTANCE, 2.0 * MAX_LOOK_AHEAD_DISTANCE));
        }
        Line segment = null;
        for (Blocker building : nearbyBuildings) {
            double footprint = Math.min(building.radius + radius,
                    Math.min(building.position.getDistance(itemPosition), building.position.getDistance(target))) - ENDPOINT_MARGIN;
            if (footprint <= 0) {
                continue;
            }
            if (segment == null) {
                segment = new Line(itemPosition, target);
            }
            if (new Circle2D(building.position, footprint).doesLineCut(segment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Same selection as {@code PathingService.collectBuildings()}, plus the one exemption of the
     * overlay: a building the destination lies in is the goal (attack, build, dock), and A* led the
     * path into it. A way point behind a building further away than the look ahead reaches (a unit
     * pushed far off its path) is tested against the terrain only, as before.
     *
     * @param cover where the list has to be complete for sight segments, before growing it by the
     *              reach of a building
     */
    private void collectNearbyBuildings(DecimalPosition itemPosition, double radius, Rectangle2D cover) {
        nearbyBuildingsCover = cover;
        nearbyBuildings.clear();
        DecimalPosition destination = wayPositions.get(wayPositions.size() - 1);
        Rectangle2D scan = new Rectangle2D(cover.startX() - BUILDING_REACH, cover.startY() - BUILDING_REACH,
                cover.width() + 2.0 * BUILDING_REACH, cover.height() + 2.0 * BUILDING_REACH);
        syncItemContainerService.iterateCellRectangleBaseItem(scan, other -> {
            AbstractSyncPhysical physical = other.getAbstractSyncPhysical();
            if (physical.canMove() || !physical.hasPosition()) {
                return;
            }
            DecimalPosition centre = physical.getPosition();
            if (centre.getDistance(destination) <= physical.getRadius() + radius) {
                return;
            }
            nearbyBuildings.add(new Blocker(centre, physical.getRadius()));
        });
    }

    /**
     * Entry point into the way point list when no index is known - after a fresh path, or after the
     * slave replaced the list from a sync packet. Pure distance arithmetic, no sight test: this runs
     * over the whole list, and the raycasts are what has to be kept out of it.
     */
    private int nearestWayPointIndex(DecimalPosition itemPosition) {
        int nearest = 0;
        double nearestDistanceSq = Double.MAX_VALUE;
        for (int i = 0; i < wayPositions.size(); i++) {
            double distanceSq = itemPosition.getDistanceSq(wayPositions.get(i));
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearest = i;
            }
        }
        return nearest;
    }

    public DecimalPosition getCurrentWayPoint() {
        return currentWayPoint;
    }

    public boolean isLastWayPoint() {
        return currentWayPointIndex == wayPositions.size() - 1;
    }

    public List<DecimalPosition> getWayPositions() {
        return wayPositions;
    }

    public void synchronize(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        wayPositions = syncPhysicalAreaInfo.getWayPositions();
        // The incoming list is a different one - any index into the old list points at an unrelated
        // way point now.
        currentWayPointIndex = UNSET_INDEX;
    }

    public void fillSyncPhysicalAreaInfo(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        syncPhysicalAreaInfo.setWayPositions(wayPositions);
    }
}
