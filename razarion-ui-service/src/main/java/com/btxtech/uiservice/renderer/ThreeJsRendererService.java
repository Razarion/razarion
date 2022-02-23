package com.btxtech.uiservice.renderer;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;

public interface ThreeJsRendererService {
    // Control
    void init();

    void startRenderLoop();

    // Content
    ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile);
}
