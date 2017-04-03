package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class DevToolTerrainSlopeTile extends TerrainSlopeTile {
    private int slopeSkeletonConfigId;
    private int slopeVertexCount;
    private double[] vertices;
    private double[] norms;
    private double[] tangents;
    private double[] slopeFactors;
    private double[] groundSplattings;

    @Override
    public void init(int slopeSkeletonConfigId, int vertexSize, int scalarSize) {
        this.slopeSkeletonConfigId = slopeSkeletonConfigId;
        vertices = new double[vertexSize];
        norms = new double[vertexSize];
        tangents = new double[vertexSize];
        slopeFactors = new double[scalarSize];
        groundSplattings = new double[scalarSize];
    }

    @Override
    public void setTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ, double normX, double normY, double normZ, double tangentX, double tangentY, double tangentZ, double slopeFactor, double splatting) {
        int cornerScalarIndex = triangleCornerIndex * 3;
        vertices[cornerScalarIndex] = vertexX;
        vertices[cornerScalarIndex + 1] = vertexY;
        vertices[cornerScalarIndex + 2] = vertexZ;
        norms[cornerScalarIndex] = normX;
        norms[cornerScalarIndex + 1] = normY;
        norms[cornerScalarIndex + 2] = normZ;
        tangents[cornerScalarIndex] = tangentX;
        tangents[cornerScalarIndex + 1] = tangentY;
        tangents[cornerScalarIndex + 2] = tangentZ;
        slopeFactors[triangleCornerIndex] = slopeFactor;
        groundSplattings[triangleCornerIndex] = splatting;
    }

    @Override
    public int getSlopeSkeletonConfigId() {
        return slopeSkeletonConfigId;
    }

    @Override
    public void setSlopeVertexCount(int slopeVertexCount) {
        this.slopeVertexCount = slopeVertexCount;
    }

    @Override
    public int getSlopeVertexCount() {
        return slopeVertexCount;
    }

    @Override
    public double[] getVertices() {
        return vertices;
    }

    @Override
    public double[] getNorms() {
        return norms;
    }

    @Override
    public double[] getTangents() {
        return tangents;
    }

    @Override
    public double[] getSlopeFactors() {
        return slopeFactors;
    }

    @Override
    public double[] getGroundSplattings() {
        return groundSplattings;
    }
}
