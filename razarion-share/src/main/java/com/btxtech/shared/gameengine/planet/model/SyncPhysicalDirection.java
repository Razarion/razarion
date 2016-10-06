package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 16.09.2016.
 */
public class SyncPhysicalDirection extends SyncPhysicalArea {
    private double angleVelocity;

    public SyncPhysicalDirection(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, Vertex position, Vertex norm, double angle) {
        super(syncItem, physicalAreaConfig, position, norm, angle);
        angleVelocity = physicalAreaConfig.getAngularVelocity();
    }

    public double getAngleVelocity() {
        return angleVelocity;
    }
}
