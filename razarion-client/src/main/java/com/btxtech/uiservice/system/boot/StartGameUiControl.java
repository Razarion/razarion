package com.btxtech.uiservice.system.boot;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.uiservice.control.GameUiControl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class StartGameUiControl extends AbstractStartupTask {
    // private Logger logger = Logger.getLogger(StartGameUiControl.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameCanvas.init();
        gameUiControl.start();
        renderService.setup();
        gameCanvas.startRenderLoop();
    }
}
