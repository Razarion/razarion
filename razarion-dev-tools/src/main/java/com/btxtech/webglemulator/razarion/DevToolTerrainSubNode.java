package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;

/**
 * Created by Beat
 * on 03.07.2017.
 */
public class DevToolTerrainSubNode extends TerrainSubNode {
    private TerrainSubNode[][] terrainSubNodes;
    private Double height;
    private int terrainTypeOrdinal;

    @Override
    public TerrainSubNode[][] getTerrainSubNodes() {
        return terrainSubNodes;
    }

    @Override
    public void initTerrainSubNodeField(int terrainSubNodeEdgeCount) {
        terrainSubNodes = new TerrainSubNode[terrainSubNodeEdgeCount][terrainSubNodeEdgeCount];
    }

    @Override
    public void insertTerrainSubNode(int x, int y, TerrainSubNode terrainSubNode) {
        terrainSubNodes[x][y] = terrainSubNode;
    }

    @Override
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public Double getHeight() {
        return height;
    }

    @Override
    public int getTerrainType() {
        return terrainTypeOrdinal;
    }

    @Override
    public void setTerrainType(int terrainTypeOrdinal) {
        this.terrainTypeOrdinal = terrainTypeOrdinal;
    }
}
