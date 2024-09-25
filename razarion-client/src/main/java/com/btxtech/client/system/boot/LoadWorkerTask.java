package com.btxtech.client.system.boot;

import com.btxtech.client.ClientGameEngineControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */

public class LoadWorkerTask extends AbstractStartupTask {

    private ClientGameEngineControl clientGameEngineControl;

    @Inject
    public LoadWorkerTask(ClientGameEngineControl clientGameEngineControl) {
        this.clientGameEngineControl = clientGameEngineControl;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        clientGameEngineControl.loadWorker(deferredStartup);
    }
}
