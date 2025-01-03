package com.btxtech.client.system.boot;

import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.inject.Inject;

/**
 * Created by Beat
 * 07.03.2016.
 */

public class LoadLoadThreeJsModelsTask extends AbstractStartupTask {
    private final BootContext bootContext;

    public LoadLoadThreeJsModelsTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();

        ThreeJsModelConfig[] threeJsModelConfigs = bootContext.getGameUiControl().getColdGameUiContext()
                .getStaticGameConfig()
                .getThreeJsModelConfigs()
                .toArray(new ThreeJsModelConfig[0]);

        ParticleSystemConfig[] particleSystemConfigs = bootContext.getGameUiControl().getColdGameUiContext()
                .getStaticGameConfig()
                .getParticleSystemConfigs()
                .toArray(new ParticleSystemConfig[0]);

        if (threeJsModelConfigs.length == 0 && particleSystemConfigs.length == 0) {
            deferredStartup.finished();
            return;
        }

        // Injection does not work here
        bootContext.loadThreeJsModels(threeJsModelConfigs, particleSystemConfigs)
                .then(ignore -> {
                    deferredStartup.finished();
                    return null;
                }).catch_(error -> {
                    deferredStartup.failed("LoadLoadThreeJsModelsTask failed: " + error);
                    return null;
                });
    }
}
