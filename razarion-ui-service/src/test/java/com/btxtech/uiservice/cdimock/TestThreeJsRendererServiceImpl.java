package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.renderer.ThreeJsRendererService;
import com.btxtech.uiservice.renderer.ThreeJsTerrainTile;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@ApplicationScoped
public class TestThreeJsRendererServiceImpl implements ThreeJsRendererService {
    private final Logger logger = Logger.getLogger(TestThreeJsRendererServiceImpl.class.getName());

    @Override
    public void init() {
        logger.warning("init()");
    }

    @Override
    public void startRenderLoop() {
        logger.warning("startRenderLoop()");
    }

    @Override
    public ThreeJsTerrainTile createTerrainTile(TerrainTile terrainTile) {
        logger.warning("createTerrainTile()");
        return null;
    }
}
