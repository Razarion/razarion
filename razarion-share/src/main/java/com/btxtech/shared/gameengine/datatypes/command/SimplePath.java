package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.List;

/**
 * Created by Beat
 * 21.04.2017.
 */
public class SimplePath {
    private List<DecimalPosition> wayPositions;
    private double totalRange;

    public List<DecimalPosition> getWayPositions() {
        return wayPositions;
    }

    public void setWayPositions(List<DecimalPosition> wayPositions) {
        this.wayPositions = wayPositions;
    }

    public double getTotalRange() {
        return totalRange;
    }

    public void setTotalRange(double totalRange) {
        this.totalRange = totalRange;
    }
}
