package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

/**
 * Created by Beat
 * 25.01.2017.
 */

public class InitRendererTask extends AbstractStartupTask {
    private final BootContext bootContext;

    public InitRendererTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        bootContext.runRenderer();
    }
}
