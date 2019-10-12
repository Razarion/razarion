package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 03.04.2017.
 */
@JsType(isNative = true, name = "TerrainSlopeTile", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainSlopeTile {
    public native void init(int slopeConfigId, int vertexSize, int decimalPositionSize, int scalarSize);

    public native void setTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ, double normX, double normY, double normZ, double vwX, double vwY, double slopeFactor);

    public native int getSlopeConfigId();

    public native void setSlopeVertexCount(int slopeVertexCount);

    public native int getSlopeVertexCount();

    public native double[] getVertices();

    public native double[] getNorms();

    public native double[] getUvs();

    public native double[] getSlopeFactors();
}
