package com.btxtech.server.web;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerTerrainShapeService;
import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.server.persistence.chat.ChatPersistence;
import com.btxtech.shared.datatypes.ServerState;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 19.04.2017.
 */
@WebListener
public class ServletContextMonitor implements ServletContextListener {
    @Inject
    private Logger logger;
    @Inject
    private ServerTerrainShapeService serverTerrainShapeService;
    @Inject
    private ServerGameEngineControl gameEngineService;
    @Inject
    private ChatPersistence chatPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerMgmt serverMgmt;
    @Inject
    private AlarmService alarmService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        serverMgmt.setServerState(ServerState.STARTING);
        alarmService.addListener(alarm -> logger.severe(alarm.toString()));
        try {
            serverTerrainShapeService.start();
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        try {
            gameEngineService.start(null, true);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        try {
            chatPersistence.fillCacheFromDb();
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        serverMgmt.setServerState(ServerState.RUNNING);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        gameEngineService.shutdown();
    }

}
