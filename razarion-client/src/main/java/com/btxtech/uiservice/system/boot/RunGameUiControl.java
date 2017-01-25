package com.btxtech.uiservice.system.boot;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class RunGameUiControl extends AbstractStartupTask {
    // private Logger logger = Logger.getLogger(RunGameUiControl.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private GameEngineControl gameEngineControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameEngineControl.start();
        gameUiControl.start();
        gameCanvas.startRenderLoop();
    }
}
