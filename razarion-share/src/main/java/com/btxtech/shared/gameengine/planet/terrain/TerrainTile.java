package com.btxtech.shared.gameengine.planet.terrain;

/**
 * Created by Beat
 * 28.03.2017.
 */
public abstract class TerrainTile {
    private int indexX;
    private int indexY;
    private double[] groundVertices;
    private double[] groundNorms;
    private double[] groundTangents;
    private double[] groundSplattings;

    public void init(int indexX, int indexY) {
        this.indexX = indexX;
        this.indexY = indexY;
    }

    public void initGroundArrays(int groundSizeVec, int groundSizeScalar) {
        groundVertices = new double[groundSizeVec];
        groundNorms = new double[groundSizeVec];
        groundTangents = new double[groundSizeVec];
        groundSplattings = new double[groundSizeScalar];
    }

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

    public int getIndexX() {
        return indexX;
    }

    public int getIndexY() {
        return indexY;
    }

    public double[] getGroundVertices() {
        return groundVertices;
    }

    public double[] getGroundNorms() {
        return groundNorms;
    }

    public double[] getGroundTangents() {
        return groundTangents;
    }

    public double[] getGroundSplattings() {
        return groundSplattings;
    }

    public int getGroundVertexCount() {
        return groundVertices.length / 3;
    }
}