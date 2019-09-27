package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
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
    private double[] uvs;
    private double[] slopeFactors;
    private double[] groundSplattings;

    @Override
    public void init(int slopeConfigId, int vertexSize, int decimalPositionSize, int scalarSize) {
        this.slopeSkeletonConfigId = slopeConfigId;
        vertices = new double[vertexSize];
        norms = new double[vertexSize];
        uvs = new double[decimalPositionSize];
        slopeFactors = new double[scalarSize];
        groundSplattings = new double[scalarSize];
    }

    @Override
    public void setTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ, double normX, double normY, double normZ, double vwX, double vwY, double slopeFactor, double splatting) {
        int cornerVertexIndex = triangleCornerIndex * Vertex.getComponentsPerVertex();
        vertices[cornerVertexIndex] = vertexX;
        vertices[cornerVertexIndex + 1] = vertexY;
        vertices[cornerVertexIndex + 2] = vertexZ;
        norms[cornerVertexIndex] = normX;
        norms[cornerVertexIndex + 1] = normY;
        norms[cornerVertexIndex + 2] = normZ;
        int cornerDecimalPositionIndex = triangleCornerIndex * DecimalPosition.getComponentsPerDecimalPosition();
        uvs[cornerDecimalPositionIndex] = vwX;
        uvs[cornerDecimalPositionIndex + 1] = vwY;
        slopeFactors[triangleCornerIndex] = slopeFactor;
        groundSplattings[triangleCornerIndex] = splatting;
    }

    @Override
    public int getSlopeConfigId() {
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
    public double[] getUvs() {
        return uvs;
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
