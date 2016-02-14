package com.btxtech.client.terrain.slope;

import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 13.02.2016.
 */
public class ShapeTemplateEntry {
    private Vertex position;
    private float slopeFactor;

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public float getSlopeFactor() {
        return slopeFactor;
    }

    public void setSlopeFactor(float slopeFactor) {
        this.slopeFactor = slopeFactor;
    }
}
