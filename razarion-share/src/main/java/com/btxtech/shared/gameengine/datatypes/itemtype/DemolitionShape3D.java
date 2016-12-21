package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 20.12.2016.
 */
public class DemolitionShape3D {
    private int shape3DId;
    private Vertex position;

    public Integer getShape3DId() {
        return shape3DId;
    }

    public DemolitionShape3D setShape3DId(Integer shape3DId) {
        this.shape3DId = shape3DId;
        return this;
    }

    public Vertex getPosition() {
        return position;
    }

    public DemolitionShape3D setPosition(Vertex position) {
        this.position = position;
        return this;
    }
}
