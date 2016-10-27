package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 06.10.2016.
 */
public class BoxItemPosition {
    private int boxItemTypeId;
    private DecimalPosition position;
    private double rotationZ;

    public int getBoxItemTypeId() {
        return boxItemTypeId;
    }

    public BoxItemPosition setBoxItemTypeId(int boxItemTypeId) {
        this.boxItemTypeId = boxItemTypeId;
        return this;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public BoxItemPosition setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    public double getRotationZ() {
        return rotationZ;
    }

    public BoxItemPosition setRotationZ(double rotationZ) {
        this.rotationZ = rotationZ;
        return this;
    }
}
