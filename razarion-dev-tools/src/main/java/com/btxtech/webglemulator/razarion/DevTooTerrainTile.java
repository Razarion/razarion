package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;

/**
 * Created by Beat
 * 31.03.2017.
 */
public class DevTooTerrainTile extends TerrainTile {
    private int indexX;
    private int indexY;
    private int groundVertexCount;
    private double[] groundVertices;
    private double[] groundNorms;
    private double[] groundTangents;
    private double[] groundSplattings;

    @Override
    public void init(int indexX, int indexY) {
        this.indexX = indexX;
        this.indexY = indexY;
    }

    @Override
    public void initGroundArrays(int groundSizeVec, int groundSizeScalar) {
        groundVertices = new double[groundSizeVec];
        groundNorms = new double[groundSizeVec];
        groundTangents = new double[groundSizeVec];
        groundSplattings = new double[groundSizeScalar];
    }

    @Override
    public void setGroundTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ, double normX, double normY, double normZ, double tangentX, double tangentY, double tangentZ, double splatting) {
        int cornerScalarIndex = triangleCornerIndex * 3;
        groundVertices[cornerScalarIndex] = vertexX;
        groundVertices[cornerScalarIndex + 1] = vertexY;
        groundVertices[cornerScalarIndex + 2] = vertexZ;
        groundNorms[cornerScalarIndex] = normX;
        groundNorms[cornerScalarIndex + 1] = normY;
        groundNorms[cornerScalarIndex + 2] = normZ;
        groundTangents[cornerScalarIndex] = tangentX;
        groundTangents[cornerScalarIndex + 1] = tangentY;
        groundTangents[cornerScalarIndex + 2] = tangentZ;
        groundSplattings[triangleCornerIndex] = splatting;
    }

    @Override
    public int getIndexX() {
        return indexX;
    }

    @Override
    public int getIndexY() {
        return indexY;
    }

    @Override
    public double[] getGroundVertices() {
        return groundVertices;
    }

    @Override
    public double[] getGroundNorms() {
        return groundNorms;
    }

    @Override
    public double[] getGroundTangents() {
        return groundTangents;
    }

    @Override
    public double[] getGroundSplattings() {
        return groundSplattings;
    }

    @Override
    public void setGroundVertexCount(int groundVertexCount) {
        this.groundVertexCount = groundVertexCount;
    }

    @Override
    public int getGroundVertexCount() {
        return groundVertexCount;
    }

    @Override
    public Object toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int fromArray(Object object) {
        throw new UnsupportedOperationException();
    }
}
