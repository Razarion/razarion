package com.btxtech.client.system.boot;

import com.btxtech.shared.Constants;
import com.btxtech.shared.rest.ServerMgmtProvider;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.google.gwt.user.client.Window;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.03.2017.
 */
@Dependent
public class CompatibilityCheckerStartupTask extends AbstractStartupTask {
    private static final int RELOAD_DELAY = 2000;
    private static final Logger logger = Logger.getLogger(CompatibilityCheckerStartupTask.class.getName());
    @Inject
    private Caller<ServerMgmtProvider> serverMgmt;
    @Inject
    private SimpleExecutorService simpleExecutorService;

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
