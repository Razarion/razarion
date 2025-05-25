package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 06.01.2017.
 */
public abstract class SyncItemSimpleDto { // Rename to Snapshot or volatile
    protected int id;
    private int itemTypeId;
    private Vertex position;

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

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
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
