package com.btxtech.uiservice.renderer;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;
public interface BabylonRenderServiceAccess {
    BabylonTerrainTile createTerrainTile(TerrainTile terrainTile);

    BabylonBaseItem createBabylonBaseItem(int id, BaseItemType baseItemType, int baseId, Diplomacy diplomacy, String userName);

    void startSpawn(int particleSystemId, double x, double y, double z);

    BabylonResourceItem createBabylonResourceItem(int id, ResourceItemType baseItemType);

    BabylonBoxItem createBabylonBoxItem(int id, BoxItemType boxItemType);

    void setViewFieldCenter(double x, double y);

    void runRenderer();

    void showOutOfViewMarker(MarkerConfig markerConfig, double angle);

    void showPlaceMarker(PlaceConfig placeConfig, MarkerConfig markerConfig);
}
