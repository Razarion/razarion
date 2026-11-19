package com.btxtech.uiservice.renderer;

import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
public class BabylonRendererService {
    private static final Logger LOG = Logger.getLogger(BabylonRendererService.class.getName());
    private final ItemTypeService itemTypeService;
    private final BabylonRenderServiceAccess babylonRenderServiceAccess;

    @Inject
    public BabylonRendererService(BabylonRenderServiceAccess babylonRenderServiceAccess, ItemTypeService itemTypeService) {
        this.babylonRenderServiceAccess = babylonRenderServiceAccess;
        this.itemTypeService = itemTypeService;
    }

    public BabylonTerrainTile createTerrainTile(TerrainTile terrainTile) {
        return babylonRenderServiceAccess.createTerrainTile(terrainTile);
    }

    public BabylonBaseItem createSyncBaseItem(int id, BaseItemType baseItemType, int baseId, Diplomacy diplomacy, String userName) {
        return babylonRenderServiceAccess.createBabylonBaseItem(id, baseItemType, baseId, diplomacy, userName);
    }

    /**
     * @param playerItem only a player's spawn (own or another one's) is shown and heard: bots
     *                   respawn over and over next to him (the extractor bot's rage Tesla, a garrison
     *                   refilling) and the spawn then read as something happening to him (phone test,
     *                   2026-11-17). A player spawning is news - someone new arrived.
     */
    public void startSpawn(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, boolean playerItem) {
        if (!playerItem) {
            return;
        }
        BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId);
        Integer particleSystemId = baseItemType.getSpawnParticleSystemId();
        Integer spawnAudioId = baseItemType.getSpawnAudioId();
        if (particleSystemId != null || spawnAudioId != null) {
            babylonRenderServiceAccess.startSpawn(particleSystemId, spawnAudioId, nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y, nativeSyncBaseItemTickInfo.z);
        }
    }

    public BabylonResourceItem createBabylonResourceItem(int id, ResourceItemType resourceItemType) {
        return babylonRenderServiceAccess.createBabylonResourceItem(id, resourceItemType);
    }

    public BabylonBoxItem createBabylonBoxItem(int id, BoxItemType boxItemType) {
        return babylonRenderServiceAccess.createBabylonBoxItem(id, boxItemType);
    }

    public void executeViewFieldConfig(ViewFieldConfig viewFieldConfig, Optional<Runnable> completionCallback) {
        if (viewFieldConfig.getToPosition() == null) {
            LOG.warning("Can only execute ViewFieldConfig with to-position");
            return;
        }
        babylonRenderServiceAccess.setViewFieldCenter(viewFieldConfig.getToPosition().getX(), viewFieldConfig.getToPosition().getY());
    }

    public void showOutOfViewMarker(MarkerConfig markerConfig, double angle) {
        babylonRenderServiceAccess.showOutOfViewMarker(markerConfig, angle);
    }

    public void showPlaceMarker(PlaceConfig placeConfig, MarkerConfig markerConfig) {
        babylonRenderServiceAccess.showPlaceMarker(placeConfig, markerConfig);
    }

    public void runRenderer() {
        babylonRenderServiceAccess.runRenderer();
    }

    public void disposeOutOfViewItem(int id) {
        babylonRenderServiceAccess.disposeOutOfViewItem(id);
    }

    public void reportEngineError(String reason) {
        babylonRenderServiceAccess.reportEngineError(reason);
    }

    public void reportStartupStage(String stage, String detail) {
        babylonRenderServiceAccess.reportStartupStage(stage, detail);
    }

    public void onGameEngineTick(double clientTickMs) {
        babylonRenderServiceAccess.onGameEngineTick(clientTickMs);
    }

    public void onTerrainTileBuilt(double workerMs, double clientMs) {
        babylonRenderServiceAccess.onTerrainTileBuilt(workerMs, clientMs);
    }
}
