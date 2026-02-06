package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

public class InitGameUiTask extends AbstractStartupTask {
    private final BootContext bootContext;

    public InitGameUiTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        bootContext.getGameUiControl().init();
    }
}
