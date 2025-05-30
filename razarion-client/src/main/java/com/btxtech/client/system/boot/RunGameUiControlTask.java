package com.btxtech.client.system.boot;

import com.btxtech.common.JwtHelper;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

/**
 * Created by Beat
 * 07.02.2016.
 */

public class RunGameUiControlTask extends AbstractStartupTask {
    private final BootContext bootContext;

    public RunGameUiControlTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        bootContext.getGameEngineControl().start(JwtHelper.getBearerTokenFromLocalStorage());
        bootContext.getGameUiControl().start();
        bootContext.getUserUiService().start();
    }
}
