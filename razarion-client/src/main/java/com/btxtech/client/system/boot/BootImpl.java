package com.btxtech.client.system.boot;

import com.btxtech.client.ClientGameEngineControl;
import com.btxtech.client.gwtangular.GwtAngularService;
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
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Singleton
public class BootImpl extends Boot {
    private final Provider<ClientGameEngineControl> clientGameEngineControl;
    private final Provider<GwtAngularService> gwtAngularService;
    private final GameEngineControl gameEngineControl;
    private final GameUiControl gameUiControl;
    private final BabylonRendererService threeJsRendererService;
    private final SimpleExecutorService simpleExecutorService;
    private final UserUiService userUiService;

    @Inject
    public BootImpl(AlarmService alarmService,
                    Provider<ClientGameEngineControl> clientGameEngineControl,
                    Provider<GwtAngularService> gwtAngularService,
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
    protected BootContext createBootContext() {
        return new BootContext(simpleExecutorService, gameEngineControl, gameUiControl, userUiService) {
            @Override
            public void loadWorker(DeferredStartup deferredStartup) {
                clientGameEngineControl.get().loadWorker(deferredStartup);
            }

            @Override
            public Promise<Void> loadThreeJsModels() {
                return gwtAngularService.get().getGwtAngularBoot().loadThreeJsModels();
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
