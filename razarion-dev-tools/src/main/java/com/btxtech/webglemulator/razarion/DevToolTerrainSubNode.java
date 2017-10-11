package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;

/**
 * Created by Beat
 * on 03.07.2017.
 */
public class DevToolTerrainSubNode extends TerrainSubNode {
    private TerrainSubNode[][] terrainSubNodes;
    private Boolean land;
    private Double height;
    private Integer terrainTypeOrdinal;

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
    public Boolean isLand() {
        return land;
    }

    @Override
    public void setLand(Boolean land) {
        this.land = land;
    }

    @Override
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public double getHeight() {
        return height;
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
