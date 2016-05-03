package com.btxtech.client.system.boot.task;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
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

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        terrainSurface.init();
        gameCanvas.init();
        renderService.init();
        renderService.fillBuffers();
        gameCanvas.startRenderLoop();
    }
}
