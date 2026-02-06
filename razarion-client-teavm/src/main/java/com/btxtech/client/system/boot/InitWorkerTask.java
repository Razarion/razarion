package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

public class InitWorkerTask extends AbstractStartupTask {
    private final BootContext bootContext;

    public InitWorkerTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        bootContext.initGameEngineControl(bootContext.getGameUiControl().getColdGameUiContext(), deferredStartup);
    }
}
