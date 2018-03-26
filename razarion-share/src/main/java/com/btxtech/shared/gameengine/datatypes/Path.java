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
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;


@Dependent
public class Path {
    @Inject
    private TerrainService terrainService;
    private List<DecimalPosition> wayPositions;
    private int currentWayPointIndex;
    private double totalRange;

    /**
     * @param path the path
     */
    public void init(SimplePath path) {
        if (path.getWayPositions().isEmpty()) {
            throw new IllegalArgumentException("At least one way point must be available");
        }
        this.totalRange = path.getTotalRange();
        wayPositions = path.getWayPositions();
        currentWayPointIndex = 0;
    }

    public DecimalPosition getCurrentWayPoint() {
        return wayPositions.get(currentWayPointIndex);
    }

    public void setupCurrentWayPoint(SyncPhysicalArea syncPhysicalArea) {
        if (currentWayPointIndex + 1 < wayPositions.size() && MathHelper.compareWithPrecision(syncPhysicalArea.getPosition2d().getDistance(getCurrentWayPoint()), 0.0)) {
            currentWayPointIndex++;
            return;
        }
        if (terrainService.getPathingAccess().isInSight(syncPhysicalArea.getPosition2d(), syncPhysicalArea.getRadius(), wayPositions.get(currentWayPointIndex))) {
            aheadTrack(syncPhysicalArea);
        } else {
            backtrack(syncPhysicalArea);
        }
    }

    private void aheadTrack(SyncPhysicalArea syncPhysicalArea) {
        int tmpCurrentWayPointIndex = currentWayPointIndex + 1;
        while (tmpCurrentWayPointIndex < wayPositions.size()) {
            if (terrainService.getPathingAccess().isInSight(syncPhysicalArea.getPosition2d(), syncPhysicalArea.getRadius(), new DecimalPosition(wayPositions.get(tmpCurrentWayPointIndex)))) {
                currentWayPointIndex = tmpCurrentWayPointIndex;
                tmpCurrentWayPointIndex++;
            } else {
                break;
            }
        }
    }

    private void backtrack(SyncPhysicalArea syncPhysicalArea) {
        int tmpCurrentWayPointIndex = currentWayPointIndex - 1;
        while (tmpCurrentWayPointIndex >= 0) {
            if (terrainService.getPathingAccess().isInSight(syncPhysicalArea.getPosition2d(), syncPhysicalArea.getRadius(), new DecimalPosition(wayPositions.get(tmpCurrentWayPointIndex)))) {
                currentWayPointIndex = tmpCurrentWayPointIndex;
                return;
            }
            tmpCurrentWayPointIndex--;
        }
        currentWayPointIndex = 0;
    }

    public boolean isLastWayPoint() {
        return currentWayPointIndex >= wayPositions.size() - 1;
    }

    public double getTotalRange() {
        return totalRange;
    }

    public List<DecimalPosition> getWayPositions() {
        return wayPositions;
    }

    public void synchronize(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        wayPositions = syncPhysicalAreaInfo.getWayPositions();
        currentWayPointIndex = syncPhysicalAreaInfo.getCurrentWayPointIndex();
        totalRange = syncPhysicalAreaInfo.getTotalRange();
    }

    public void fillSyncPhysicalAreaInfo(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        syncPhysicalAreaInfo.setWayPositions(wayPositions);
        syncPhysicalAreaInfo.setCurrentWayPointIndex(currentWayPointIndex);
        syncPhysicalAreaInfo.setTotalRange(totalRange);
    }
}
