package com.btxtech.client.system.boot;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class RunGameUiControlTask extends AbstractStartupTask {
    // private Logger logger = Logger.getLogger(RunGameUiControlTask.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Boot boot;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private ClientPerformanceTrackerService clientPerformanceTrackerService;
    @Inject
    private UserUiService userUiService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameEngineControl.start();
        // gameEngineControl.enableTracking();
        gameUiControl.start();
        gameCanvas.startRenderLoop();
        perfmonService.start(boot.getGameSessionUuid());
        clientPerformanceTrackerService.start();
        userUiService.start();
    }
}
