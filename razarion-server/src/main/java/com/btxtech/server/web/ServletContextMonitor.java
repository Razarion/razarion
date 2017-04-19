package com.btxtech.server.web;

import com.btxtech.server.gameengine.GameEngineService;

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
    private GameEngineService gameEngineService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        gameEngineService.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        gameEngineService.stop();
    }
}
