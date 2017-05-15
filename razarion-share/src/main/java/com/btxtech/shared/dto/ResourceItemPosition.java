package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 06.10.2016.
 */
public class ResourceItemPosition {
    private int resourceItemTypeId;
    private DecimalPosition position;
    private double rotationZ;

    public int getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public ResourceItemPosition setResourceItemTypeId(int resourceItemTypeId) {
        this.resourceItemTypeId = resourceItemTypeId;
        return this;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public ResourceItemPosition setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    public double getRotationZ() {
        return rotationZ;
    }

    public ResourceItemPosition setRotationZ(double rotationZ) {
        this.rotationZ = rotationZ;
        return this;
    }
}
