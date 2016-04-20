package com.btxtech.client.terrain.slope;

import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 13.02.2016.
 */
public class SlopeSkeletonEntry {
    private Vertex position;
    private float slopeFactor;
    private double normShift;

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

    public double getNormShift() {
        return normShift;
    }

    public void setNormShift(double normShift) {
        this.normShift = normShift;
    }
}
