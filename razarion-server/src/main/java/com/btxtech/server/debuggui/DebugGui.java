package com.btxtech.server.debuggui;


import com.btxtech.shared.system.ExceptionHandler;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

//**************************************************************************************
// Add in WEB-INF jboss-deployment-structure.xml
//
//
// https://developer.jboss.org/thread/238426
// https://docs.jboss.org/author/display/WFLY10/Class+Loading+in+WildFly
//**************************************************************************************

/**
 * Created by Beat
 * on 07.08.2018.
 */
@ApplicationScoped
public class DebugGui {
    @Resource(name = "DefaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService scheduleExecutor;
    @Inject
    private ServerGuiController serverGuiController;
    @Inject
    private Logger logger;
    @Inject
    private ExceptionHandler exceptionHandler;

    public void display() {
        try {
            logger.severe("************************ DebugGui starting... This should not happen in production!");
            scheduleExecutor.submit(() -> {
                try {
                    // Add in WEB-INF jboss-deployment-structure.xml
                    StaticApplication.doLaunch(serverGuiController);
                } catch (Throwable e) {
                    exceptionHandler.handleException(e);
                }
            });
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
        }

    }

}
