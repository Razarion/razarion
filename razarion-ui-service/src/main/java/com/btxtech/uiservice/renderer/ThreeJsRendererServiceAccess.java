package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface ThreeJsRendererServiceAccess {
    ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfigId);

    BabylonBaseItem createSyncBaseItem(int id, BaseItemType baseItemType, Diplomacy diplomacy);

    void setViewFieldCenter(double x, double y);

    void initMeshContainers(MeshContainer[] meshContainers);
}
