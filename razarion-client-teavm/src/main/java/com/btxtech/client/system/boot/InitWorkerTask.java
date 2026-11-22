package com.btxtech.client.system.boot;

import com.btxtech.client.JwtHelper;
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
        // Right behind INITIALIZE: the worker opens the game connection while it builds the planet,
        // instead of at RUN_GAME behind the download burst - see GameEngineWorker#preconnect.
        bootContext.getGameEngineControl().preconnect(JwtHelper.getBearerTokenFromLocalStorage());
    }
}
