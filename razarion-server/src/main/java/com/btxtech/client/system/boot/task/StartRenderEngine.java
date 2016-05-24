package com.btxtech.client.system.boot.task;

import com.btxtech.client.ItemServiceRunner;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class StartRenderEngine extends AbstractStartupTask {
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private RenderService renderService;
    @Inject
    private ItemServiceRunner itemServiceRunner;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        itemServiceRunner.start();
        gameCanvas.init();
        renderService.setupRenderers();
        renderService.fillBuffers();
        gameCanvas.startRenderLoop();
    }
}
