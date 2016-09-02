package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeNode {
    private Vertex position;
    private double slopeFactor;

    public void setSlopeFactor(double slopeFactor) {
        this.slopeFactor = slopeFactor;
    }

    public double getSlopeFactor() {
        return slopeFactor;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public Vertex getPosition() {
        return position;
    }
}
