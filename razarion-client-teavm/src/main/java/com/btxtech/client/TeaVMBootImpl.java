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
     * Adopted once - a warm restart is a new startup and gets a fresh id. The page global always
     * names the session that is booting right now: clearing it would leave the abort beacon
     * without a session, and an abort that names none can neither be dropped when the session
     * comes back and finishes nor recognised as the session the tasks already describe. That
     * showed up as two aborted rows for one abandoned startup.
     */
    @Override
    protected String createGameSessionUuid() {
        String fromPage = JsWindow.getString(PageBootGlobals.SESSION_UUID);
        boolean adopted = JsWindow.getString(PageBootGlobals.SESSION_UUID_ADOPTED) != null;
        if (adopted || fromPage == null || fromPage.isEmpty()) {
            String uuid = super.createGameSessionUuid();
            JsWindow.setString(PageBootGlobals.SESSION_UUID, uuid);
            return uuid;
        }
        JsWindow.setString(PageBootGlobals.SESSION_UUID_ADOPTED, "true");
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
