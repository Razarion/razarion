package com.btxtech.uiservice.mock;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.renderer.BabylonTerrainTile;

public class BabylonTerrainTileMock implements BabylonTerrainTile {
    private final TerrainTile terrainTile;

    public BabylonTerrainTileMock(TerrainTile terrainTile) {
        this.terrainTile = terrainTile;
    }

    @Override
    public void addToScene() {

    }

    @Override
    public void removeFromScene() {

    }

    public TerrainTile getTerrainTile() {
        return terrainTile;
    }
}
