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
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;


@Dependent
public class Path {
    @Inject
    private ObstacleContainer obstacleContainer;
    private List<DecimalPosition> wayPositions;
    private int currentWayPointIndex;
    private double totalRange;

    /**
     * @param path       the path
     * @param totalRange radius SyncItem + radius target + weapon range
     */
    public void init(List<DecimalPosition> path, double totalRange) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("At least one way point must be available");
        }
        this.totalRange = totalRange;
        wayPositions = path;
        currentWayPointIndex = 0;
    }

    public DecimalPosition getCurrentWayPoint() {
        return wayPositions.get(currentWayPointIndex);
    }

    public void setupCurrentWayPoint(SyncPhysicalArea syncPhysicalArea) {
        if (obstacleContainer.isInSight(syncPhysicalArea, wayPositions.get(currentWayPointIndex))) {
            aheadTrack(syncPhysicalArea);
        } else {
            backtrack(syncPhysicalArea);
        }
    }

    private void aheadTrack(SyncPhysicalArea syncPhysicalArea) {
        int tmpCurrentWayPointIndex = currentWayPointIndex + 1;
        while (tmpCurrentWayPointIndex < wayPositions.size()) {
            if (obstacleContainer.isInSight(syncPhysicalArea, new DecimalPosition(wayPositions.get(tmpCurrentWayPointIndex)))) {
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
            if (obstacleContainer.isInSight(syncPhysicalArea, new DecimalPosition(wayPositions.get(tmpCurrentWayPointIndex)))) {
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
}
