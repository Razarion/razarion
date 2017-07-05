package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;

/**
 * Created by Beat
 * 31.03.2017.
 */
public class DevToolTerrainNode extends TerrainNode {
    private TerrainSubNode[][] terrainSubNodes;
    private boolean land;
    private double height;

    @Override
    public TerrainSubNode[][] getTerrainSubNodes() {
        return terrainSubNodes;
    }

    @Override
    public void setTerrainSubNode(TerrainSubNode[][] terrainSubNodes) {
        this.terrainSubNodes = terrainSubNodes;
    }

    @Override
    public void setLand(Boolean land) {
        this.land = land;
    }

    @Override
    public boolean isLand() {
        return land;
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
