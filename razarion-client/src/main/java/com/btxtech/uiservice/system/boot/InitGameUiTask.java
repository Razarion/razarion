package com.btxtech.uiservice.system.boot;

import com.btxtech.uiservice.control.GameUiControl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */
@Dependent
public class InitGameUiTask extends AbstractStartupTask {
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        gameUiControl.init();
    }
}
