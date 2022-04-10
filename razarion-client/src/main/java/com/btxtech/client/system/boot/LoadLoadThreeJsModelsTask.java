package com.btxtech.client.system.boot;

import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.03.2016.
 */
@Dependent
public class LoadLoadThreeJsModelsTask extends AbstractStartupTask {
    @Inject
    private GwtAngularService gwtAngularService;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        ThreeJsModelConfig[] threeJsModelConfigs = gameUiControl.getColdGameUiContext()
                .getStaticGameConfig()
                .getThreeJsModelConfigs()
                .toArray(new ThreeJsModelConfig[0]);
        if(threeJsModelConfigs.length == 0) {
            deferredStartup.finished();
            return;
        }

        // Injection does not work here
        gwtAngularService.getGwtAngularBoot().loadThreeJsModels(threeJsModelConfigs)
                .then(ignore -> {
                    deferredStartup.finished();
                    return null;
                }).catch_(error -> {
                    deferredStartup.failed("LoadLoadThreeJsModelsTask failed: " + error);
                    return null;
                });
    }
}
