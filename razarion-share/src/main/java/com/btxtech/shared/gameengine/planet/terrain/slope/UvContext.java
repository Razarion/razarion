package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;

public class UvContext {
    private double uvY;
    private boolean terminationBorder;
    private boolean terminationSegment;

    public void addToUv(double uvYLength) {
        uvY += uvYLength;
    }

    public double getUvY() {
        return uvY;
    }

    public void setTerminationBorder(boolean terminationBorder) {
        this.terminationBorder = terminationBorder;
    }

    public void setTerminationSegment(boolean terminationSegment) {
        this.terminationSegment = terminationSegment;
    }

    public Double getUvYTermination(double uvYLength) {
        if (terminationBorder && terminationSegment) {
            return uvY + uvYLength;
        } else {
            return null;
        }
    }
}
