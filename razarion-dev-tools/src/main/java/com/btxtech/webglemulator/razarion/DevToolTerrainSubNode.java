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

    @Override
    public TerrainSubNode[][] getTerrainSubNodes() {
        return terrainSubNodes;
    }

    @Override
    public void setTerrainSubNodes(TerrainSubNode[][] terrainSubNodes) {
        this.terrainSubNodes = terrainSubNodes;
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
