package com.btxtech.shared;

import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;

/**
 * Created by Beat
 * 10.04.2017.
 */
public class TestTerrainWaterTile extends TerrainWaterTile {
    private int vertexCount;
    private double[] vertices;
    private Double[] offsetToOuters;

    @Override
    public void initArray(int sizeVertex, int sizeScalar) {
        vertices = new double[sizeVertex];
        offsetToOuters = new Double[sizeScalar];
    }

    @Override
    public void setTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ, Double offsetToOuter) {
        int cornerScalarIndex = triangleCornerIndex * 3;
        vertices[cornerScalarIndex] = vertexX;
        vertices[cornerScalarIndex + 1] = vertexY;
        vertices[cornerScalarIndex + 2] = vertexZ;
        offsetToOuters[triangleCornerIndex] = offsetToOuter;
    }

    @Override
    public double[] getVertices() {
        return vertices;
    }

    @Override
    public Double[] getOffsetToOuters() {
        return offsetToOuters;
    }

    @Override
    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }
}
