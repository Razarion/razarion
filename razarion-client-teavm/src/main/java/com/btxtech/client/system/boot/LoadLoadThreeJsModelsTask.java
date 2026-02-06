package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

public class LoadLoadThreeJsModelsTask extends AbstractStartupTask {
    private final BootContext bootContext;

    public LoadLoadThreeJsModelsTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();

        bootContext.loadThreeJsModels(
                () -> deferredStartup.finished(),
                error -> deferredStartup.failed("LoadLoadThreeJsModelsTask failed: " + error)
        );
    }
}
