package com.btxtech.uiservice.renderer;

import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class ThreeJsRendererService {
    private static final Logger LOG = Logger.getLogger(ThreeJsRendererService.class.getName());

    @Inject
    private ThreeJsRendererServiceAccess threeJsRendererServiceAccess;

    public ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile) {
        return threeJsRendererServiceAccess.createTerrainTile(terrainTile);
    }

    public void executeViewFieldConfig(ViewFieldConfig viewFieldConfig, Optional<Runnable> completionCallback) {
        if (viewFieldConfig.getToPosition() == null) {
            LOG.warning("Can only execute ViewFieldConfig with to-position");
            return;
        }
        threeJsRendererServiceAccess.setViewFieldCenter(viewFieldConfig.getToPosition().getX(), viewFieldConfig.getToPosition().getY());
    }

    public void startRenderLoop() {
        LOG.warning("startRenderLoop() Not implemented");
    }
}
