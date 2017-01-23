package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 06.01.2017.
 */
public abstract class SyncItemSimpleDto { // Rename to Snapshot
    protected int id;
    private int itemTypeId;
    private DecimalPosition position2d;
    private Vertex position3d;
    private Matrix4 model;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public DecimalPosition getPosition2d() {
        return position2d;
    }

    public void setPosition2d(DecimalPosition position2d) {
        this.position2d = position2d;
    }

    public Vertex getPosition3d() {
        return position3d;
    }

    public void setPosition3d(Vertex position3d) {
        this.position3d = position3d;
    }

    public Matrix4 getModel() {
        return model;
    }

    public void setModel(Matrix4 model) {
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SyncBaseItemSimpleDto simpleDto = (SyncBaseItemSimpleDto) o;

        return id == simpleDto.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
