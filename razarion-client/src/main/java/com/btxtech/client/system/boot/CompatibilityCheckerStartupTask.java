package com.btxtech.client.system.boot;

import com.btxtech.shared.Constants;
import com.btxtech.shared.rest.ServerMgmtControllerFactory;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.google.gwt.user.client.Window;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.03.2017.
 */

public class CompatibilityCheckerStartupTask extends AbstractStartupTask {
    private static final int RELOAD_DELAY = 2000;
    private static final Logger logger = Logger.getLogger(CompatibilityCheckerStartupTask.class.getName());
    private final BootContext bootContext;

    public CompatibilityCheckerStartupTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        ServerMgmtControllerFactory.INSTANCE.getInterfaceVersion().onSuccess(interfaceVersion -> {
            if (Constants.INTERFACE_VERSION == interfaceVersion) {
                deferredStartup.finished();
            } else {
                logger.log(Level.SEVERE, "CompatibilityCheckerStartupTask wrong client interface version: " + Constants.INTERFACE_VERSION + ". Server interface version: " + interfaceVersion);
                bootContext.getSimpleExecutorService().schedule(RELOAD_DELAY, Window.Location::reload, SimpleExecutorService.Type.RELOAD_CLIENT_WRONG_INTERFACE_VERSION);
            }
        }).onFailed(fail -> {
            logger.log(Level.SEVERE, "ServerMgmt.getInterfaceVersion failed: " + fail.getStatusText(), fail.getThrowable());
            deferredStartup.finished();
        }).send();
    }
}
