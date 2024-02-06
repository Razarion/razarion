package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface BabylonRenderServiceAccess {
    BabylonTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfigId);

    BabylonBaseItem createBabylonBaseItem(int id, BaseItemType baseItemType, Diplomacy diplomacy);

    BabylonResourceItem createBabylonResourceItem(int id, ResourceItemType baseItemType);

    BabylonBoxItem createBabylonBoxItem(int id, BoxItemType boxItemType);

    void setViewFieldCenter(double x, double y);

    void runRenderer(MeshContainer[] meshContainers);

    void showOutOfViewMarker(MarkerConfig markerConfig, double angle);

    void showPlaceMarker(PlaceConfig placeConfig, MarkerConfig markerConfig);
}
