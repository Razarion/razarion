package com.btxtech.client.system.boot.task;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.uiservice.storyboard.StoryboardService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class StartStoryboard extends AbstractStartupTask {
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private StoryboardService storyboardService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        storyboardService.start();
        gameCanvas.init();
        renderService.setup();
        gameCanvas.startRenderLoop();
    }
}
