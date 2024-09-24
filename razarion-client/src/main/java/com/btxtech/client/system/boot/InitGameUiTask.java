package com.btxtech.client.system.boot;

import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */
@Dependent
public class InitGameUiTask extends AbstractStartupTask {

    private GameUiControl gameUiControl;

    @Inject
    public InitGameUiTask(GameUiControl gameUiControl) {
        this.gameUiControl = gameUiControl;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameUiControl.init();
    }
}
