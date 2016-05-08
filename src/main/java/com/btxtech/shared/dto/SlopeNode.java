package com.btxtech.shared.dto;

import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 08.05.2016.
 */
@Portable
public class SlopeNode {
    private Vertex position;
    private double slopeFactor;
    private double normShift;

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

    public void setNormShift(double normShift) {
        this.normShift = normShift;
    }

    public double getNormShift() {
        return normShift;
    }
}
