package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeShape {
    private DecimalPosition position;
    private double slopeFactor;

    public DecimalPosition getPosition() {
        return position;
    }

    public void setPosition(DecimalPosition position) {
        this.position = position;
    }

    public double getSlopeFactor() {
        return slopeFactor;
    }

    public void setSlopeFactor(double slopeFactor) {
        this.slopeFactor = slopeFactor;
    }

    public SlopeShape position(DecimalPosition position) {
        setPosition(position);
        return this;
    }

    public SlopeShape slopeFactor(double slopeFactor) {
        setSlopeFactor(slopeFactor);
        return this;
    }
}
