package com.btxtech.server.web;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by Beat
 * 19.04.2017.
 */
@WebListener
public class ServletContextMonitor implements ServletContextListener {
    @Inject
    private ServerGameEngineControl gameEngineService;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            gameEngineService.start(null, true);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        gameEngineService.stop();
    }
}
