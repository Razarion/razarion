package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;

/**
 * Created by Beat
 * 10.04.2017.
 */
public class DevToolTerrainWaterTile extends TerrainWaterTile {
    private int vertexCount;
    private double[] vertices;

    @Override
    public void initArray(int sizeVec) {
        vertices = new double[sizeVec];
    }

    @Override
    public void setTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ) {
        int cornerScalarIndex = triangleCornerIndex * 3;
        vertices[cornerScalarIndex] = vertexX;
        vertices[cornerScalarIndex + 1] = vertexY;
        vertices[cornerScalarIndex + 2] = vertexZ;
    }

    @Override
    public double[] getVertices() {
        return vertices;
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
