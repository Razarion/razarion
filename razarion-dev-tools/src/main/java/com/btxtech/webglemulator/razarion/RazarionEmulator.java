package com.btxtech.webglemulator.razarion;

import com.btxtech.persistence.GameUiControlProviderEmulator;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.webglemulator.WebGlEmulatorController;
import com.btxtech.webglemulator.WebGlEmulatorSceneController;
import javafx.application.Platform;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 22.05.2016.
 */
@ApplicationScoped
public class RazarionEmulator {
    private static final long RENDER_DELAY = 400;
    @Inject
    private WebGlEmulatorController controller;
    @Inject
    private WebGlEmulatorSceneController sceneController;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private DevToolsRenderServiceImpl renderService;
    @Inject
    private GameUiControlProviderEmulator gameUiControlProviderEmulator;
    @Inject
    private GameEngineControl gameEngineControl;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean showRenderTime;

    public void run() {
        try {
            gameUiControl.setGameUiControlConfig(gameUiControlProviderEmulator.readFromFile());
            gameEngineControl.init(gameUiControl.getGameUiControlConfig().getGameEngineConfig(), gameUiControl.getUserContext(), null);
            gameUiControl.init();
            gameEngineControl.start();
            gameUiControl.start();
            renderService.setup();
            controller.onEngineInitialized();
            startRenderLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isShowRenderTime() {
        return showRenderTime;
    }

    public void setShowRenderTime(boolean showRenderTime) {
        this.showRenderTime = showRenderTime;
    }

    private void startRenderLoop() {
        scheduler.scheduleWithFixedDelay(() -> Platform.runLater(() -> {
            try {
                long time = System.currentTimeMillis();
                renderService.render();
                sceneController.update();
                if (showRenderTime) {
                    System.out.println("Time for render: " + (System.currentTimeMillis() - time));
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }), RENDER_DELAY, RENDER_DELAY, TimeUnit.MILLISECONDS);
    }
}
