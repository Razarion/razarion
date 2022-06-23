package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.renderer.ThreeJsRendererServiceAccess;
import com.btxtech.uiservice.renderer.ThreeJsTerrainTile;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@ApplicationScoped
public class ThreeJsRendererServiceAccessMock implements ThreeJsRendererServiceAccess {
    private final Logger logger = Logger.getLogger(ThreeJsRendererServiceAccessMock.class.getName());

    @Override
    public ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile, Integer defaultGroundConfigId) {
        logger.warning("createTerrainTile()");
        return null;
    }

    @Override
    public void setViewFieldCenter(double x, double y) {
        logger.warning("setViewFieldCenter()");
    }
}
