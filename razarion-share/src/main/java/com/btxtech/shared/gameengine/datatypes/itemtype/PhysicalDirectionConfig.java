package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;

/**
 * Created by Beat
 * 16.09.2016.
 */
public class PhysicalDirectionConfig extends PhysicalAreaConfig {
    private double angularVelocity; //Grad per second

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public PhysicalDirectionConfig setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
        return this;
    }
}
