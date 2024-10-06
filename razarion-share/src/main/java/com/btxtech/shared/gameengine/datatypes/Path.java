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
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.inject.Inject;
import java.util.List;



public class Path {

    private TerrainService terrainService;
    private List<DecimalPosition> wayPositions;
    private DecimalPosition currentWayPoint;

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
    }

    public void setupCurrentWayPoint(AbstractSyncPhysical abstractSyncPhysical) {
        DecimalPosition itemPosition = abstractSyncPhysical.getPosition();
        for (int i = wayPositions.size() - 1; i >= 0; i--) {
            DecimalPosition wayPosition = wayPositions.get(i);
            // Attention due to performance!! isInSight() surface data (Obstacle-Model) is not based on the AStar surface data -> AStar model must overlap Obstacle-Model
            if (terrainService.getPathingAccess().isInSight(itemPosition, abstractSyncPhysical.getRadius(), wayPosition)) {
                currentWayPoint = wayPosition;
                return;
            }

        }
        currentWayPoint = wayPositions.get(0);
    }

    public DecimalPosition getCurrentWayPoint() {
        return currentWayPoint;
    }

    public boolean isLastWayPoint() {
        return currentWayPoint == wayPositions.get(wayPositions.size() - 1);
    }

    public List<DecimalPosition> getWayPositions() {
        return wayPositions;
    }

    public void synchronize(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        wayPositions = syncPhysicalAreaInfo.getWayPositions();
    }

    public void fillSyncPhysicalAreaInfo(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        syncPhysicalAreaInfo.setWayPositions(wayPositions);
    }
}
