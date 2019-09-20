package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 10.04.2017.
 */
@JsType(isNative = true, name = "TerrainWaterTile", namespace = "com.btxtech.shared.nativejs")
public class TerrainWaterTile {
    private int slopeConfigId;
    private double[] vertices;
    private double[] shallowVertices;
    private double[] shallowUvs;

    public int getSlopeConfigId() {
        return slopeConfigId;
    }

    public void setSlopeConfigId(int slopeConfigId) {
        this.slopeConfigId = slopeConfigId;
    }

    public void setVertices(double[] vertices) {
        this.vertices = vertices;
    }

    public double[] getVertices() {
        return vertices;
    }

    public double[] getShallowVertices() {
        return shallowVertices;
    }

    public void setShallowVertices(double[] shallowVertices) {
        this.shallowVertices = shallowVertices;
    }

    public double[] getShallowUvs() {
        return shallowUvs;
    }

    public void setShallowUvs(double[] shallowUvs) {
        this.shallowUvs = shallowUvs;
    }
}
