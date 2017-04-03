package com.btxtech.shared.gameengine.planet.terrain;

/**
 * Created by Beat
 * 03.04.2017.
 */
public abstract class TerrainSlopeTile {
    public native void init(int slopeSkeletonConfigId, int vertexSize, int scalarSize);

    public native void setTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ, double normX, double normY, double normZ, double tangentX, double tangentY, double tangentZ, double slopeFactor, double splatting);

    public native int getSlopeSkeletonConfigId();

    public native void setSlopeVertexCount(int slopeVertexCount);

    public native int getSlopeVertexCount();

    public native double[] getVertices();

    public native double[] getNorms();

    public native double[] getTangents();

    public native double[] getSlopeFactors();

    public native double[] getGroundSplattings();

}
