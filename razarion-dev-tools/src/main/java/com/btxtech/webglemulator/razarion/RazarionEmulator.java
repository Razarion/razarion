package com.btxtech.webglemulator.razarion;

import com.btxtech.persistence.JsonProviderEmulator;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.i18n.I18nConstants;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.webglemulator.ClientViewController;
import com.btxtech.webglemulator.WebGlEmulatorController;
import javafx.application.Platform;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 22.05.2016.
 */
@ApplicationScoped
public class RazarionEmulator {
    private static final long RENDER_DELAY = 100;
    @Inject
    private WebGlEmulatorController controller;
    @Inject
    private ClientViewController sceneController;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private DevToolsRenderServiceImpl renderService;
    @Inject
    private JsonProviderEmulator jsonProviderEmulator;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private DevToolShape3DUiService devToolShape3DUiService;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean showRenderTime;

    public void run() {
        try {
            gameUiControl.setColdGameUiControlConfig(jsonProviderEmulator.readColdGameUiControlConfig());
            gameUiControl.init();
            gameEngineControl.init(gameUiControl.getColdGameUiControlConfig(), null);
            devToolShape3DUiService.loadBuffer();
            gameEngineControl.start();
            gameUiControl.start();
            renderService.setup();
            controller.onEngineInitialized();
            I18nHelper.setConstants((I18nConstants) Proxy.newProxyInstance(I18nConstants.class.getClassLoader(), new Class[]{I18nConstants.class}, (proxy, method, args) -> method.getName()));
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
