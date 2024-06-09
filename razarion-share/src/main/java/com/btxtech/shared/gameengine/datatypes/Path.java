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
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;


@Dependent
public class Path {
    @Inject
    private TerrainService terrainService;
    private List<DecimalPosition> wayPositions;
    private DecimalPosition currentWayPoint;

    /**
     * @param path the path
     */
    public void init(SimplePath path) {
        if (path.getWayPositions().size() < 2) {
            throw new IllegalArgumentException("At least two way points must be available");
        }
        wayPositions = path.getWayPositions();
    }

    public void setupCurrentWayPoint(SyncPhysicalArea syncPhysicalArea) {
        if (wayPositions.size() < 3) {
            currentWayPoint = wayPositions.get(1);
            return;
        }

        DecimalPosition terrainPosition = syncPhysicalArea.getPosition2d();
        DecimalPosition current = null;
        DecimalPosition next = null;
        DecimalPosition last = null;
        double shortestDistance = Double.MAX_VALUE;
        for (int i = 1; i < wayPositions.size() - 1; i++) {
            DecimalPosition tmpCurrent = wayPositions.get(i);

            if (current == null) {
                last = wayPositions.get(i - 1);
                current = tmpCurrent;
                next = wayPositions.get(i + 1);
            } else {
                double tmpDistance = terrainPosition.getDistance(tmpCurrent);
                if (shortestDistance > tmpDistance) {
                    last = wayPositions.get(i - 1);
                    current = tmpCurrent;
                    next = wayPositions.get(i + 1);
                    shortestDistance = tmpDistance;
                }
            }
        }
        if (current == null) {
            throw new IllegalArgumentException("At least two way points must be available");
        }

        double distanceLast = terrainPosition.getDistance(last);
        double distanceNext = terrainPosition.getDistance(next);

        if (distanceLast < distanceNext) {
            currentWayPoint = current;
        } else {
            currentWayPoint = next;
        }
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
