package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalDirectionConfig;

/**
 * Created by Beat
 * 16.09.2016.
 */
public class SyncPhysicalDirection extends SyncPhysicalArea {
    private double angle;
    private double angleVelocity;

    public SyncPhysicalDirection(SyncItem syncItem, PhysicalDirectionConfig physicalDirectionConfig, Vertex position, Vertex norm, double angle) {
        super(syncItem, physicalDirectionConfig, position, norm);
        this.angle = angle;
        angleVelocity = physicalDirectionConfig.getAngularVelocity();
    }

    public double getAngle() {
        return angle;
    }

    void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngleVelocity() {
        return angleVelocity;
    }
}
