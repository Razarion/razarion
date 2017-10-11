package com.btxtech.shared;

import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;

/**
 * Created by Beat
 * on 03.07.2017.
 */
public class TestTerrainSubNode extends TerrainSubNode {
    private TestTerrainSubNode[][] terrainSubNodes;
    private Boolean land;
    private Double height;
    private Integer terrainTypeOrdinal;

    @Override
    public void initTerrainSubNodeField(int terrainSubNodeEdgeCount) {
        terrainSubNodes = new TestTerrainSubNode[terrainSubNodeEdgeCount][terrainSubNodeEdgeCount];
    }

    @Override
    public void insertTerrainSubNode(int x, int y, TerrainSubNode terrainSubNode) {
        terrainSubNodes[x][y] = (TestTerrainSubNode) terrainSubNode;
    }

    @Override
    public TerrainSubNode[][] getTerrainSubNodes() {
        return terrainSubNodes;
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

    @Override
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public Integer getTerrainType() {
        return terrainTypeOrdinal;
    }

    @Override
    public void setTerrainType(Integer terrainTypeOrdinal) {
        this.terrainTypeOrdinal = terrainTypeOrdinal;
    }
}
