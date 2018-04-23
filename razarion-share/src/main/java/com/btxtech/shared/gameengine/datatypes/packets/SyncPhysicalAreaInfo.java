package com.btxtech.shared.gameengine.datatypes.packets;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.List;

/**
 * Created by Beat
 * 16.04.2017.
 */
public class SyncPhysicalAreaInfo {
    private DecimalPosition position;
    private double angle;
    private DecimalPosition velocity;
    private List<DecimalPosition> wayPositions;
    private Integer currentWayPointIndex;
    private Double totalRange;

    public DecimalPosition getPosition() {
        return position;
    }

    public SyncPhysicalAreaInfo setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    public double getAngle() {
        return angle;
    }

    public SyncPhysicalAreaInfo setAngle(double angle) {
        this.angle = angle;
        return this;
    }

    public DecimalPosition getVelocity() {
        return velocity;
    }

    public SyncPhysicalAreaInfo setVelocity(DecimalPosition velocity) {
        this.velocity = velocity;
        return this;
    }

    public List<DecimalPosition> getWayPositions() {
        return wayPositions;
    }

    public SyncPhysicalAreaInfo setWayPositions(List<DecimalPosition> wayPositions) {
        this.wayPositions = wayPositions;
        return this;
    }

    public Integer getCurrentWayPointIndex() {
        return currentWayPointIndex;
    }

    public SyncPhysicalAreaInfo setCurrentWayPointIndex(Integer currentWayPointIndex) {
        this.currentWayPointIndex = currentWayPointIndex;
        return this;
    }

    public Double getTotalRange() {
        return totalRange;
    }

    public SyncPhysicalAreaInfo setTotalRange(Double totalRange) {
        this.totalRange = totalRange;
        return this;
    }
}
