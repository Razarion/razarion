package com.btxtech.uiservice.system.boot;

import com.btxtech.client.ClientGameEngineControl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */
@Dependent
public class LoadWorkerTask extends AbstractStartupTask {
    @Inject
    private ClientGameEngineControl clientGameEngineControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        clientGameEngineControl.loadWorker(deferredStartup);
    }
}
