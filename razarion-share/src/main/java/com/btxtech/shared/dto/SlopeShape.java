package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Index;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 08.05.2016.
 */
@Portable
public class SlopeShape {
    private Index position;
    private float slopeFactor;

    /**
     * Used by errai
     */
    public SlopeShape() {
    }

    public SlopeShape(Index position, float slopeFactor) {
        this.position = position;
        this.slopeFactor = slopeFactor;
    }

    public void setPosition(Index position) {
        this.position = position;
    }

    public Index getPosition() {
        return position;
    }

    public void setSlopeFactor(float slopeFactor) {
        this.slopeFactor = slopeFactor;
    }

    public float getSlopeFactor() {
        return slopeFactor;
    }
}
