package com.btxtech.client.system.boot;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.webgl.WebGLTextureContainer;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */
@Dependent
public class InitRendererTask extends AbstractStartupTask {
    @Inject
    private WebGLTextureContainer textureContainer;
    @Inject
    private RenderService renderService;
    @Inject
    private GameCanvas gameCanvas;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameCanvas.init();
        textureContainer.setupTextures();
        renderService.setup();
    }
}
