package com.btxtech.uiservice.terrain.helpers;

import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;

/**
 * Created by Beat
 * 31.03.2017.
 */
public class TestToolTerrainTile extends TerrainTile {
    private double landWaterProportion;
    private TerrainNode[][] terrainNodes;
    private double height;

    @Override
    public void initTerrainNodeField(int terrainTileNodesEdgeCount) {
        terrainNodes = new TerrainNode[terrainTileNodesEdgeCount][terrainTileNodesEdgeCount];
    }

    @Override
    public void insertTerrainNode(int x, int y, TerrainNode terrainNode) {
        terrainNodes[x][y] = terrainNode;
    }

    @Override
    public TerrainNode[][] getTerrainNodes() {
        return terrainNodes;
    }

    @Override
    public double getLandWaterProportion() {
        return landWaterProportion;
    }

    @Override
    public void setLandWaterProportion(double landWaterProportion) {
        this.landWaterProportion = landWaterProportion;
    }

    @Override
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public double getHeight() {
        return height;
    }
}
