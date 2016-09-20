package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;

/**
 * Created by Beat
 * 16.09.2016.
 */
public class PhysicalAreaConfig {
    private double radius;

    public double getRadius() {
        return radius;
    }

    public PhysicalAreaConfig setRadius(double radius) {
        this.radius = radius;
        return this;
    }
}
