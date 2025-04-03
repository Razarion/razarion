package com.btxtech.server;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.service.engine.ServerGameEngineCrudPersistence;
import com.btxtech.server.service.engine.ServerTerrainShapeService;
import com.btxtech.server.service.engine.StaticGameConfigService;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.system.alarm.AlarmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_PROPERTY;

@Component
public class RazarionServerEventListener {
    private final Logger logger = LoggerFactory.getLogger(RazarionServerEventListener.class);
    private final ServerTerrainShapeService serverTerrainShapeService;
    private final StaticGameConfigService staticGameConfigPersistence;
    private final ServerGameEngineControl gameEngineService;
    // TODO private final ChatPersistence chatPersistence;
    // TODO private ServerMgmt serverMgmt;
    private final AlarmService alarmService;
    private final InitializeService initializeService;
    private final ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

    public RazarionServerEventListener(ServerGameEngineCrudPersistence serverGameEngineCrudPersistence,
                                       InitializeService initializeService,
                                       AlarmService alarmService,
                                       // TODO ServerMgmt serverMgmt,
                                       // TODO ChatPersistence chatPersistence,
                                       ServerGameEngineControl gameEngineService,
                                       StaticGameConfigService staticGameConfigPersistence,
                                       ServerTerrainShapeService serverTerrainShapeService) {
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.initializeService = initializeService;
        this.alarmService = alarmService;
        // TODO this.serverMgmt = serverMgmt;
        // TODO this.chatPersistence = chatPersistence;
        this.gameEngineService = gameEngineService;
        this.staticGameConfigPersistence = staticGameConfigPersistence;
        this.serverTerrainShapeService = serverTerrainShapeService;
    }

    @EventListener
    public void handleApplicationStartedEvent(ApplicationStartedEvent event) {
        // TODO serverMgmt.setServerState(ServerState.STARTING);
        alarmService.addListener(alarm -> {
            // Temporarily suppress INVALID_PROPERTY
            if (alarm.getType() != INVALID_PROPERTY) {
                logger.error(alarm.toString());
            }
        });
        try {
            initializeService.setStaticGameConfig(staticGameConfigPersistence.loadStaticGameConfig());
        } catch (Exception e) {
            logger.error("setStaticGameConfig failed", e);
        }
        try {
            ServerGameEngineConfig serverGameEngineConfig = serverGameEngineCrudPersistence.read().get(0);
            serverTerrainShapeService.start(serverGameEngineConfig.getBotConfigs());
        } catch (Exception e) {
            logger.error("start failed ", e);
        }
        try {
            gameEngineService.start(null, true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
//   TODO     try {
//            chatPersistence.fillCacheFromDb();
//        } catch (Exception e) {
//            exceptionHandler.handleException(e);
//        }
//        serverMgmt.setServerState(ServerState.RUNNING);
    }
}
