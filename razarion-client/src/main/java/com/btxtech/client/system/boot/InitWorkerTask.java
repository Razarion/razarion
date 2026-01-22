package com.btxtech.client.system.boot;

import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import jakarta.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */

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
