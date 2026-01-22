package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import jakarta.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */

public class InitGameUiTask extends AbstractStartupTask {
    private final BootContext context;

    @Inject
    public InitGameUiTask(BootContext bootContext) {
        this.context = bootContext;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        context.getGameUiControl().init();
    }
}
