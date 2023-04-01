package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface ThreeJsRendererServiceAccess {
    ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfigId);

    BabylonBaseItem createBaseItem(int id, Diplomacy diplomacy, double radius);

    void setViewFieldCenter(double x, double y);

    void initMeshContainers(MeshContainer[] meshContainers);
}
