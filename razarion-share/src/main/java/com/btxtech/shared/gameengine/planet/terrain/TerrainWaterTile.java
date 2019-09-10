package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 10.04.2017.
 */
@JsType(isNative = true, name = "TerrainWaterTile", namespace = "com.btxtech.shared.nativejs")
public class TerrainWaterTile {
    private int slopeId;
    private double[] vertices;
    private double[] shallowVertices;
    private double[] shallowUvs;

    public int getSlopeId() {
        return slopeId;
    }

    public void setSlopeId(int slopeId) {
        this.slopeId = slopeId;
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
