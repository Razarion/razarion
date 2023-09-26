package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class BabylonRendererService {
    private static final Logger LOG = Logger.getLogger(BabylonRendererService.class.getName());

    @Inject
    private BabylonRenderServiceAccess babylonRenderServiceAccess;

    public ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfig) {
        return babylonRenderServiceAccess.createTerrainTile(terrainTile, defaultGroundConfig);
    }

    public BabylonBaseItem createSyncBaseItem(int id, BaseItemType baseItemType, Diplomacy diplomacy) {
        return babylonRenderServiceAccess.createBabylonBaseItem(id, baseItemType, diplomacy);
    }

    public BabylonResourceItem createBabylonResourceItem(int id, ResourceItemType baseItemType) {
        return babylonRenderServiceAccess.createBabylonResourceItem(id, baseItemType);
    }

    public void executeViewFieldConfig(ViewFieldConfig viewFieldConfig, Optional<Runnable> completionCallback) {
        if (viewFieldConfig.getToPosition() == null) {
            LOG.warning("Can only execute ViewFieldConfig with to-position");
            return;
        }
        babylonRenderServiceAccess.setViewFieldCenter(viewFieldConfig.getToPosition().getX(), viewFieldConfig.getToPosition().getY());
    }

    public void runRenderer(MeshContainer[] meshContainers) {
        babylonRenderServiceAccess.runRenderer(meshContainers);
    }
}
