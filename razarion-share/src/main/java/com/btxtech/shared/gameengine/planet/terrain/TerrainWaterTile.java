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
    private double[] slopeVertices;
    private double[] slopeUvs;

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

    public double[] getSlopeVertices() {
        return slopeVertices;
    }

    public void setSlopeVertices(double[] slopeVertices) {
        this.slopeVertices = slopeVertices;
    }

    public double[] getSlopeUvs() {
        return slopeUvs;
    }

    public void setSlopeUvs(double[] slopeUvs) {
        this.slopeUvs = slopeUvs;
    }
}
