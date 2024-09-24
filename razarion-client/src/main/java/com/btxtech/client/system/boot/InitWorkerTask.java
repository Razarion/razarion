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

    private GameEngineControl gameEngineControl;

    private GameUiControl gameUiControl;

    @Inject
    public InitWorkerTask(GameUiControl gameUiControl, GameEngineControl gameEngineControl) {
        this.gameUiControl = gameUiControl;
        this.gameEngineControl = gameEngineControl;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();

        gameEngineControl.init(gameUiControl.getColdGameUiContext(), deferredStartup);
    }
}
