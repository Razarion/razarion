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

    void startSpawn(Integer particleSystemId, Integer spawnAudioId, double x, double y, double z);

    BabylonResourceItem createBabylonResourceItem(int id, ResourceItemType baseItemType);

    BabylonBoxItem createBabylonBoxItem(int id, BoxItemType boxItemType);

    void setViewFieldCenter(double x, double y);

    void runRenderer();

    void showOutOfViewMarker(MarkerConfig markerConfig, double angle);

    void showPlaceMarker(PlaceConfig placeConfig, MarkerConfig markerConfig);

    void disposeOutOfViewItem(int id);

    /**
     * Notifies the renderer that a game-engine simulation tick was applied (fires every tick,
     * ~100ms, even when nothing moved). Feeds the F8 perf overlay's authoritative tick-rate curve.
     *
     * @param clientTickMs main-thread time (ms) spent applying this tick
     */
    void onGameEngineTick(double clientTickMs);

    /**
     * Reports terrain-tile timing to the F8 perf overlay: how long the worker took to generate the
     * tile, and how long the main thread took to build its Babylon mesh (the part that can stutter
     * while scrolling into new terrain).
     */
    void onTerrainTileBuilt(double workerMs, double clientMs);
}
