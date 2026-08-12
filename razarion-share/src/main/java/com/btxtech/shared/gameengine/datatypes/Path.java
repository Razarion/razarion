/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.datatypes;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import jakarta.inject.Inject;
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

    private final TerrainService terrainService;
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
    public Path(TerrainService terrainService) {
        this.terrainService = terrainService;
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
        // Either way the way point handed out was checked against the terrain in this tick, unless
        // the walk back ran out of steps or hit the start of the path.
        if (!advanceToFarthestVisible(itemPosition, radius, terrainType)) {
            retreatToVisible(itemPosition, radius, terrainType);
        }
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
            // Attention due to performance!! isInSight() surface data (Obstacle-Model) is not based on the AStar surface data -> AStar model must overlap Obstacle-Model
            if (!terrainService.getTerrainAnalyzer().isInSight(itemPosition, radius, next, terrainType)) {
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
            if (terrainService.getTerrainAnalyzer().isInSight(itemPosition, radius, wayPositions.get(currentWayPointIndex), terrainType)) {
                return;
            }
            currentWayPointIndex--;
        }
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
