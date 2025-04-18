package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

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


        // Injection does not work here
        bootContext.loadThreeJsModels()
                .then(ignore -> {
                    deferredStartup.finished();
                    return null;
                }).catch_(error -> {
                    deferredStartup.failed("LoadLoadThreeJsModelsTask failed: " + error);
                    return null;
                });
    }
}
