package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 10.04.2017.
 */
@JsType(isNative = true, name = "TerrainWaterTile", namespace = "com.btxtech.shared.nativejs")
public class TerrainWaterTile {
    public native void initArray(int sizeVec);

    public native void setTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ);

    public native double[] getVertices();

    public native void setVertexCount(int vertexCount);

    public native int getVertexCount();
}
