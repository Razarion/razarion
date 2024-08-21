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

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.List;
import java.util.logging.Logger;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_PROPERTY;

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
    private StaticGameConfigPersistence staticGameConfigPersistence;
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
    @Inject
    private Event<StaticGameInitEvent> gameEngineInitEvent;
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

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
