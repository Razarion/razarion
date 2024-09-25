package com.btxtech.client.system.boot;

import com.btxtech.client.Caller;
import com.btxtech.client.RemoteCallback;
import com.btxtech.shared.Constants;
import com.btxtech.shared.rest.ServerMgmtProvider;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.google.gwt.user.client.Window;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.03.2017.
 */

public class CompatibilityCheckerStartupTask extends AbstractStartupTask {
    private static final int RELOAD_DELAY = 2000;
    private static final Logger logger = Logger.getLogger(CompatibilityCheckerStartupTask.class.getName());

    private Caller<ServerMgmtProvider> serverMgmt;

    private SimpleExecutorService simpleExecutorService;

    @Inject
    public CompatibilityCheckerStartupTask(SimpleExecutorService simpleExecutorService, Caller<com.btxtech.shared.rest.ServerMgmtProvider> serverMgmt) {
        this.simpleExecutorService = simpleExecutorService;
        this.serverMgmt = serverMgmt;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        serverMgmt.call((RemoteCallback<Integer>) interfaceVersion -> {
            if (Constants.INTERFACE_VERSION == interfaceVersion) {
                deferredStartup.finished();
            } else {
                logger.log(Level.SEVERE, "CompatibilityCheckerStartupTask wrong client interface version: " + Constants.INTERFACE_VERSION + ". Server interface version: " + interfaceVersion);
                simpleExecutorService.schedule(RELOAD_DELAY, Window.Location::reload, SimpleExecutorService.Type.RELOAD_CLIENT_WRONG_INTERFACE_VERSION);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerMgmt.getInterfaceVersion failed: " + message, throwable);
            deferredStartup.finished();
            return false;
        }).getInterfaceVersion();
    }
}
