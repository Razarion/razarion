package com.btxtech.uiservice.renderer;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface ThreeJsRendererService {
    // Control
    void init();

    void startRenderLoop();

    // Content
    ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile);
}
