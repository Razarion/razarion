package com.btxtech.client;

import com.btxtech.client.jso.JsWindow;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.btxtech.client.system.boot.PageBootGlobals;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.system.boot.StartupSeq;
import com.btxtech.uiservice.user.UserUiService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.function.Consumer;

@Singleton
public class TeaVMBootImpl extends Boot {
    private final Provider<TeaVMClientGameEngineControl> clientGameEngineControl;
    private final Provider<TeaVMGwtAngularService> gwtAngularService;
    private final GameEngineControl gameEngineControl;
    private final GameUiControl gameUiControl;
    private final BabylonRendererService threeJsRendererService;
    private final SimpleExecutorService simpleExecutorService;
    private final UserUiService userUiService;

    @Inject
    public TeaVMBootImpl(AlarmService alarmService,
                         Provider<TeaVMClientGameEngineControl> clientGameEngineControl,
                         Provider<TeaVMGwtAngularService> gwtAngularService,
                         GameEngineControl gameEngineControl,
                         GameUiControl gameUiControl,
                         BabylonRendererService threeJsRendererService,
                         SimpleExecutorService simpleExecutorService,
                         UserUiService userUiService) {
        super(alarmService);
        this.clientGameEngineControl = clientGameEngineControl;
        this.gwtAngularService = gwtAngularService;
        this.gameEngineControl = gameEngineControl;
        this.gameUiControl = gameUiControl;
        this.threeJsRendererService = threeJsRendererService;
        this.simpleExecutorService = simpleExecutorService;
        this.userUiService = userUiService;
    }

    @Override
    protected StartupSeq getWarm() {
        return GameStartupSeq.WARM;
    }

    @Override
    protected void scheduleDeferredTimeout(long delayMillis, Runnable runnable) {
        simpleExecutorService.schedule(delayMillis, runnable, SimpleExecutorService.Type.BOOT_TASK_TIMEOUT);
    }

    /**
     * The page already opened a session before the WebAssembly module was loaded and reported
     * its first tasks under that id. Adopting it here keeps everything from the page load to the
     * running engine in one session; without it the page-side tasks would look like an orphaned
     * session that never booted.
     * <p>
     * Taken once and cleared: a warm restart is a new startup and gets a fresh id.
     */
    @Override
    protected String createGameSessionUuid() {
        String fromPage = JsWindow.getString(PageBootGlobals.SESSION_UUID);
        if (fromPage == null || fromPage.isEmpty()) {
            return super.createGameSessionUuid();
        }
        JsWindow.setString(PageBootGlobals.SESSION_UUID, null);
        return fromPage;
    }

    @Override
    protected BootContext createBootContext() {
        return new BootContext(simpleExecutorService, gameEngineControl, gameUiControl, userUiService) {
            @Override
            public void loadWorker(DeferredStartup deferredStartup) {
                clientGameEngineControl.get().loadWorker(deferredStartup);
            }

            @Override
            public void loadThreeJsModels(Runnable onSuccess, Consumer<String> onError) {
                gwtAngularService.get().getGwtAngularBoot().loadThreeJsModels(onSuccess, onError);
            }

            @Override
            public void initGameEngineControl(ColdGameUiContext coldGameUiContext, DeferredStartup deferredStartup) {
                gameEngineControl.init(coldGameUiContext, deferredStartup);
            }

            @Override
            public void runRenderer() {
                threeJsRendererService.runRenderer();
            }
        };
    }
}
