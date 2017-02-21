package com.btxtech.client.system.boot;

import com.btxtech.uiservice.control.GameEngineControl;
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
public class InitWorkerTask extends AbstractStartupTask {
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();

        gameEngineControl.init(gameUiControl.getGameUiControlConfig().getGameEngineConfig(), deferredStartup);
    }
}
