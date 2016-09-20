package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeNode {
    private Vertex position;
    private double slopeFactor;

    public SlopeNode setSlopeFactor(double slopeFactor) {
        this.slopeFactor = slopeFactor;
        return this;
    }

    public double getSlopeFactor() {
        return slopeFactor;
    }

    public SlopeNode setPosition(Vertex position) {
        this.position = position;
        return this;
    }

    public Vertex getPosition() {
        return position;
    }
}
