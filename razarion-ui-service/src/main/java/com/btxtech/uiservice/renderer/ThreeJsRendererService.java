package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.Diplomacy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class ThreeJsRendererService {
    private static final Logger LOG = Logger.getLogger(ThreeJsRendererService.class.getName());

    @Inject
    private ThreeJsRendererServiceAccess threeJsRendererServiceAccess;

    public ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfig) {
        return threeJsRendererServiceAccess.createTerrainTile(terrainTile, defaultGroundConfig);
    }

    public BabylonBaseItem createBaseItem(int baseItemId, Diplomacy diplomacy, double radius) {
        return threeJsRendererServiceAccess.createBaseItem(baseItemId, diplomacy, radius);
    }

    public void executeViewFieldConfig(ViewFieldConfig viewFieldConfig, Optional<Runnable> completionCallback) {
        if (viewFieldConfig.getToPosition() == null) {
            LOG.warning("Can only execute ViewFieldConfig with to-position");
            return;
        }
        threeJsRendererServiceAccess.setViewFieldCenter(viewFieldConfig.getToPosition().getX(), viewFieldConfig.getToPosition().getY());
    }

    public void initMeshContainers(MeshContainer[] meshContainers) {
        threeJsRendererServiceAccess.initMeshContainers(meshContainers);
    }

    public void startRenderLoop() {
        LOG.warning("startRenderLoop() Not implemented");
    }
}
