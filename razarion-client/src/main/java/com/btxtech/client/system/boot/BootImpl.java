package com.btxtech.client.system.boot;

import com.btxtech.client.ClientGameEngineControl;
import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.user.FacebookService;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.system.boot.StartupSeq;
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
    private final FacebookService facebookService;
    private final GwtAngularService gwtAngularService;
    private final GameEngineControl gameEngineControl;
    private final BabylonRendererService threeJsRendererService;

    @Inject
    public BootImpl(AlarmService alarmService,
                    Provider<ClientGameEngineControl> clientGameEngineControl,
                    FacebookService facebookService,
                    GwtAngularService gwtAngularService,
                    GameEngineControl gameEngineControl,
                    BabylonRendererService threeJsRendererService) {
        super(alarmService);
        this.clientGameEngineControl = clientGameEngineControl;
        this.facebookService = facebookService;
        this.gwtAngularService = gwtAngularService;
        this.gameEngineControl = gameEngineControl;
        this.threeJsRendererService = threeJsRendererService;
    }

    @Override
    protected StartupSeq getWarm() {
        return GameStartupSeq.WARM;
    }

    @Override
    protected BootContext createBootContext() {
        return new BootContext() {
            @Override
            public void loadWorker(DeferredStartup deferredStartup) {
                clientGameEngineControl.get().loadWorker(deferredStartup);
            }

            @Override
            public void activateFacebookAppStartLogin() {
                facebookService.activateFacebookAppStartLogin();
            }

            @Override
            public Promise<Void> loadThreeJsModels(ThreeJsModelConfig[] threeJsModelConfigs, ParticleSystemConfig[] particleSystemConfigs) {
                return gwtAngularService.getGwtAngularBoot().loadThreeJsModels(threeJsModelConfigs, particleSystemConfigs);
            }

            @Override
            public void initGameEngineControl(ColdGameUiContext coldGameUiContext, DeferredStartup deferredStartup) {
                gameEngineControl.init(coldGameUiContext, deferredStartup);
            }

            @Override
            public void runRenderer(MeshContainer[] meshContainers) {
                threeJsRendererService.runRenderer(meshContainers);
            }
        };
    }
}
