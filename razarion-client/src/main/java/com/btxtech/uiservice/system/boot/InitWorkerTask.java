package com.btxtech.uiservice.system.boot;

import com.btxtech.client.ClientGameEngineControl;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;

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

        gameEngineControl.init(gameUiControl.getGameUiControlConfig().getGameEngineConfig(), gameUiControl.getUserContext(), deferredStartup);
    }
}
