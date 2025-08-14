package com.btxtech.uiservice.renderer;

import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
public class BabylonRendererService {
    private static final Logger LOG = Logger.getLogger(BabylonRendererService.class.getName());


    private BabylonRenderServiceAccess babylonRenderServiceAccess;

    @Inject
    public BabylonRendererService(BabylonRenderServiceAccess babylonRenderServiceAccess) {
        this.babylonRenderServiceAccess = babylonRenderServiceAccess;
    }

    public BabylonTerrainTile createTerrainTile(TerrainTile terrainTile) {
        return babylonRenderServiceAccess.createTerrainTile(terrainTile);
    }

    public BabylonBaseItem createSyncBaseItem(int id, BaseItemType baseItemType, int baseId, Diplomacy diplomacy, String userName) {
        return babylonRenderServiceAccess.createBabylonBaseItem(id, baseItemType, baseId, diplomacy, userName);
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
}
