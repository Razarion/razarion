package com.btxtech.shared.gameengine.planet.terrain;

/**
 * Created by Beat
 * on 30.06.2017.
 */
public class TerrainSubNode {
    private TerrainSubNode[][] terrainSubNodes;
    private Double height;
    private int terrainTypeOrdinal;

    public void initTerrainSubNodeField(int terrainSubNodeEdgeCount) {
        terrainSubNodes = new TerrainSubNode[terrainSubNodeEdgeCount][terrainSubNodeEdgeCount];
    }

    public void insertTerrainSubNode(int x, int y, TerrainSubNode terrainSubNode) {
        terrainSubNodes[x][y] = terrainSubNode;
    }

    public TerrainSubNode[][] getTerrainSubNodes() {
        return terrainSubNodes;
    }

    public void setTerrainSubNodes(TerrainSubNode[][] terrainSubNodes) {
        this.terrainSubNodes = terrainSubNodes;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getTerrainType() {
        return terrainTypeOrdinal;
    }

    public void setTerrainType(int terrainTypeOrdinal) {
        this.terrainTypeOrdinal = terrainTypeOrdinal;
    }
}
