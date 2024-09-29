package com.btxtech.server.web;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerTerrainShapeService;
import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.server.persistence.StaticGameConfigPersistence;
import com.btxtech.server.persistence.chat.ChatPersistence;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.shared.datatypes.ServerState;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.AlarmService;

import com.btxtech.shared.deprecated.Event;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_PROPERTY;

/**
 * Created by Beat
 * 19.04.2017.
 */
@WebListener
public class ServletContextMonitor implements ServletContextListener {

    private Logger logger;

    private ServerTerrainShapeService serverTerrainShapeService;

    private StaticGameConfigPersistence staticGameConfigPersistence;

    private ServerGameEngineControl gameEngineService;

    private ChatPersistence chatPersistence;

    private ExceptionHandler exceptionHandler;

    private ServerMgmt serverMgmt;

    private AlarmService alarmService;

    private Event<StaticGameInitEvent> gameEngineInitEvent;

    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

    @Inject
    public ServletContextMonitor(ServerGameEngineCrudPersistence serverGameEngineCrudPersistence, Event<com.btxtech.shared.gameengine.StaticGameInitEvent> gameEngineInitEvent, AlarmService alarmService, ServerMgmt serverMgmt, ExceptionHandler exceptionHandler, ChatPersistence chatPersistence, ServerGameEngineControl gameEngineService, StaticGameConfigPersistence staticGameConfigPersistence, ServerTerrainShapeService serverTerrainShapeService, Logger logger) {
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.gameEngineInitEvent = gameEngineInitEvent;
        this.alarmService = alarmService;
        this.serverMgmt = serverMgmt;
        this.exceptionHandler = exceptionHandler;
        this.chatPersistence = chatPersistence;
        this.gameEngineService = gameEngineService;
        this.staticGameConfigPersistence = staticGameConfigPersistence;
        this.serverTerrainShapeService = serverTerrainShapeService;
        this.logger = logger;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        serverMgmt.setServerState(ServerState.STARTING);
        alarmService.addListener(alarm -> {
            // Temporarily suppress INVALID_PROPERTY
            if(alarm.getType() != INVALID_PROPERTY) {
                logger.severe(alarm.toString());
            }
        });
        try {
            gameEngineInitEvent.fire(new StaticGameInitEvent(staticGameConfigPersistence.loadStaticGameConfig()));
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        try {
            ServerGameEngineConfig serverGameEngineConfig = serverGameEngineCrudPersistence.read().get(0);
            serverTerrainShapeService.start(serverGameEngineConfig.getBotConfigs());
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
