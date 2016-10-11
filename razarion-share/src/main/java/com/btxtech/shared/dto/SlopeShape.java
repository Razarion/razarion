package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;

/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeShape {
    private DecimalPosition position;
    private float slopeFactor;

    /**
     * Used by errai
     */
    public SlopeShape() {
    }

    public SlopeShape(DecimalPosition position, float slopeFactor) {
        this.position = position;
        this.slopeFactor = slopeFactor;
    }

    public void setPosition(DecimalPosition position) {
        this.position = position;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public void setSlopeFactor(float slopeFactor) {
        this.slopeFactor = slopeFactor;
    }

    public float getSlopeFactor() {
        return slopeFactor;
    }
}
