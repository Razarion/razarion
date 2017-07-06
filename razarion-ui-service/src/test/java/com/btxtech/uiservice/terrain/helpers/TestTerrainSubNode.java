package com.btxtech.uiservice.terrain.helpers;

import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;

/**
 * Created by Beat
 * on 03.07.2017.
 */
public class TestTerrainSubNode extends TerrainSubNode {
    private TerrainSubNode[][] terrainSubNodes;
    private Boolean land;
    private Double height;

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
    public Boolean isLand() {
        return land;
    }

    @Override
    public void setLand(Boolean land) {
        this.land = land;
    }

    @Override
    public double getHeight() {
        return height;
    }
}
