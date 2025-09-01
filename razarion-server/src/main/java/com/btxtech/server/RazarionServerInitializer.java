package com.btxtech.server;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.ServerTerrainShapeService;
import com.btxtech.server.service.engine.StaticGameConfigService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.system.alarm.AlarmService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_PROPERTY;

@Component
@Order(1)
public class RazarionServerInitializer implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(RazarionServerInitializer.class);
    private final ServerTerrainShapeService serverTerrainShapeService;
    private final StaticGameConfigService staticGameConfigService;
    private final ServerGameEngineControl gameEngineService;
    // TODO private ServerMgmt serverMgmt;
    private final AlarmService alarmService;
    private final InitializeService initializeService;
    private final ServerGameEngineService serverGameEngineService;
    private final UserService userService;

    public RazarionServerInitializer(ServerGameEngineService serverGameEngineService,
                                     InitializeService initializeService,
                                     AlarmService alarmService,
                                     // TODO ServerMgmt serverMgmt,
                                     ServerGameEngineControl gameEngineService,
                                     StaticGameConfigService staticGameConfigService,
                                     ServerTerrainShapeService serverTerrainShapeService, UserService userService) {
        this.serverGameEngineService = serverGameEngineService;
        this.initializeService = initializeService;
        this.alarmService = alarmService;
        // TODO this.serverMgmt = serverMgmt;
        this.gameEngineService = gameEngineService;
        this.staticGameConfigService = staticGameConfigService;
        this.serverTerrainShapeService = serverTerrainShapeService;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // TODO serverMgmt.setServerState(ServerState.STARTING);
        alarmService.addListener(alarm -> {
            // Temporarily suppress INVALID_PROPERTY
            if (alarm.getType() != INVALID_PROPERTY) {
                logger.error(alarm.toString());
            }
        });
        try {
            userService.cleanupUnregisteredUsersStartup();
        } catch (Exception e) {
            logger.error("User connection cleanup failed", e);
        }
        try {
            initializeService.setStaticGameConfig(staticGameConfigService.loadStaticGameConfig());
        } catch (Exception e) {
            logger.error("setStaticGameConfig failed", e);
        }
        try {
            ServerGameEngineConfig serverGameEngineConfig = serverGameEngineService.read().get(0);
            serverTerrainShapeService.start(serverGameEngineConfig.getBotConfigs());
        } catch (Exception e) {
            logger.error("start failed ", e);
        }
        try {
            gameEngineService.start(null, true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @PreDestroy
    public void onShutdown() {
        gameEngineService.shutdown();
    }
}