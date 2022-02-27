package com.btxtech.uiservice.renderer;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface ThreeJsRendererServiceAccess {
    ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile);

    void setViewFieldCenter(double x, double y);
}
