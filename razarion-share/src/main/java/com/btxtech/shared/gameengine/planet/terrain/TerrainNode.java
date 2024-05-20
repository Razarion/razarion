package com.btxtech.shared.gameengine.planet.terrain;

/**
 * Created by Beat
 * on 30.06.2017.
 */
@Deprecated
public class TerrainNode {
    private TerrainSubNode[][] terrainSubNodes;
    private double height;
    private int terrainTypeOrdinal;

    public void initTerrainSubNodeField(int terrainSubNodeEdgeCount) {
        terrainSubNodes = new TerrainSubNode[terrainSubNodeEdgeCount][terrainSubNodeEdgeCount];
    }

    public TerrainSubNode[][] getTerrainSubNodes() {
        return terrainSubNodes;
    }

    public void setTerrainSubNodes(TerrainSubNode[][] terrainSubNodes) {
        this.terrainSubNodes = terrainSubNodes;
    }

    public void insertTerrainSubNode(int x, int y, TerrainSubNode terrainSubNode) {
        terrainSubNodes[x][y] = terrainSubNode;
    }

    public double getHeight() {
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
