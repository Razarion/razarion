package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class VerticalSegment {
    private Slope slope;
    private int index;
    private DecimalPosition inner;
    private DecimalPosition outer;
    private double drivewayHeightFactor;
    private double uvY;

    public VerticalSegment(Slope slope, int index, DecimalPosition inner, DecimalPosition outer, double uvY, double drivewayHeightFactor) {
        this.slope = slope;
        this.index = index;
        this.inner = inner;
        this.outer = outer;
        this.uvY = uvY;
        this.drivewayHeightFactor = drivewayHeightFactor;
    }

    public Slope getSlope() {
        return slope;
    }

    public DecimalPosition getInner() {
        return inner;
    }

    public DecimalPosition getOuter() {
        return outer;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getDrivewayHeightFactor() {
        return drivewayHeightFactor;
    }

    public double getUvY() {
        return uvY;
    }
}
