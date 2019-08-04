package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;

public class UvContext {
    private double uvY;
    private DecimalPosition lastPosition;

    public void addToUv(DecimalPosition lastPosition) {
        if (this.lastPosition != null) {
            uvY += this.lastPosition.getDistance(lastPosition);
        }
        this.lastPosition = lastPosition;
    }

    public double getUvY() {
        return uvY;
    }
}
