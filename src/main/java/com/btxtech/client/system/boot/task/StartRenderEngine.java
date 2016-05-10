package com.btxtech.client.system.boot.task;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.client.terrain.TerrainSurface;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class StartRenderEngine extends AbstractStartupTask{
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private RenderService renderService;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private TerrainObjectService terrainObjectService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        terrainSurface.init();
        terrainObjectService.init();
        gameCanvas.init();
        renderService.setupRenderers();
        renderService.fillBuffers();
        gameCanvas.startRenderLoop();
    }
}
