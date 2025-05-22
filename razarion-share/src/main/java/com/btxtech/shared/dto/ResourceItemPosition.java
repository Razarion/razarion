package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;

/**
 * Created by Beat
 * 06.10.2016.
 */
public class ResourceItemPosition {
    private Integer resourceItemTypeId;
    private DecimalPosition position;
    private double rotationZ;

    public Integer getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public ResourceItemPosition setResourceItemTypeId(Integer resourceItemTypeId) {
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
