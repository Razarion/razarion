package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 28.03.2017.
 */
@JsType(isNative = true, name = "TerrainTile", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainTile {
    public native void init(int indexX, int indexY);

    public native void initGroundArrays(int groundSizeVec, int groundSizeScalar);

    public native void setGroundTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ, double normX, double normY, double normZ, double tangentX, double tangentY, double tangentZ, double splatting);

    public native int getIndexX();

    public native int getIndexY();

    public native double[] getGroundVertices();

    public native double[] getGroundNorms();

    public native double[] getGroundTangents();

    public native double[] getGroundSplattings();

    public native int getGroundVertexCount();

    public native Object toArray();

    public native int fromArray(Object object);
}
