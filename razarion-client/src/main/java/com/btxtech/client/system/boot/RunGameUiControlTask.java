package com.btxtech.client.system.boot;

import com.btxtech.common.system.ClientPerformanceTrackerService;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.user.UserUiService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2016.
 */

public class RunGameUiControlTask extends AbstractStartupTask {

    // private Logger logger = Logger.getLogger(RunGameUiControlTask.class.getName());
    private BabylonRendererService babylonRendererService;

    private Boot boot;

    private GameUiControl gameUiControl;

    private GameEngineControl gameEngineControl;

    private PerfmonService perfmonService;

    private ClientPerformanceTrackerService clientPerformanceTrackerService;

    private UserUiService userUiService;

    @Inject
    public RunGameUiControlTask(UserUiService userUiService, ClientPerformanceTrackerService clientPerformanceTrackerService, PerfmonService perfmonService, GameEngineControl gameEngineControl, GameUiControl gameUiControl, Boot boot, BabylonRendererService babylonRendererService) {
        this.userUiService = userUiService;
        this.clientPerformanceTrackerService = clientPerformanceTrackerService;
        this.perfmonService = perfmonService;
        this.gameEngineControl = gameEngineControl;
        this.gameUiControl = gameUiControl;
        this.boot = boot;
        this.babylonRendererService = babylonRendererService;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameEngineControl.start();
        // gameEngineControl.enableTracking();
        gameUiControl.start();
        // Injection does not work here
        // babylonRendererService.startRenderLoop();
        perfmonService.start(boot.getGameSessionUuid());
        clientPerformanceTrackerService.start();
        userUiService.start();
    }
}
