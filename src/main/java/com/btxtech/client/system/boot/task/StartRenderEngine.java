package com.btxtech.client.system.boot.task;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.units.ItemService;

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
    @Inject
    private ItemService itemService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        itemService.init();
        terrainSurface.init();
        terrainObjectService.init();
        gameCanvas.init();
        renderService.setupRenderers();
        renderService.fillBuffers();
        gameCanvas.startRenderLoop();
    }
}
