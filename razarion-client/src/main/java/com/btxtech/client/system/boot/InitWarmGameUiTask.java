package com.btxtech.client.system.boot;

import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import jakarta.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */

public class InitWarmGameUiTask extends AbstractStartupTask {

    private GameUiControl gameUiControl;

    @Inject
    public InitWarmGameUiTask(GameUiControl gameUiControl) {
        this.gameUiControl = gameUiControl;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameUiControl.initWarm();
    }
}
