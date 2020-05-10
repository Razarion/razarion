package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 10.04.2017.
 */
@JsType(isNative = true, name = "TerrainWaterTile", namespace = "com.btxtech.shared.json")
public class TerrainWaterTile {
//    private int slopeConfigId;
//    private double[] vertices;
//    private double[] shallowVertices;
//    private double[] shallowUvs;

    public native int getSlopeConfigId();

    public native void setSlopeConfigId(int slopeConfigId);

    public native void setVertices(double[] vertices);

    public native double[] getVertices();

    public native double[] getShallowVertices();

    public native void setShallowVertices(double[] shallowVertices);

    public native double[] getShallowUvs();

    public native void setShallowUvs(double[] shallowUvs);
}
