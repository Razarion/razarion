package com.btxtech.server.rest.editor;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.engine.ServerGameEngineConfigEntity;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/rest/editor/server-game-engine")
public class ServerGameEngineEditorController {
    private final Logger logger = LoggerFactory.getLogger(ServerGameEngineEditorController.class);
    private final ServerGameEngineService serverGameEngineService;

    public ServerGameEngineEditorController(ServerGameEngineService serverGameEngineService) {
        this.serverGameEngineService = serverGameEngineService;
    }

    @GetMapping(value = "read/{id}", produces = APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public ServerGameEngineConfigEntity read(@PathVariable("id") int id) {
        return serverGameEngineService.getBaseEntity(id);
    }


    @PostMapping(value = "update/resourceRegionConfig/{serverGameEngineConfigId}", consumes = APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public void updateResourceRegionConfig(@PathVariable("serverGameEngineConfigId") int serverGameEngineConfigId, @RequestBody List<ResourceRegionConfig> resourceRegionConfigs) {
        try {
            serverGameEngineService.updateResourceRegionConfig(serverGameEngineConfigId, resourceRegionConfigs);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "update/startRegionConfig/{serverGameEngineConfigId}", consumes = APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public void updateStartRegionConfig(@PathVariable("serverGameEngineConfigId") int serverGameEngineConfigId, @RequestBody List<StartRegionConfig> startRegionConfigs) {
        try {
            serverGameEngineService.updateStartRegionConfig(serverGameEngineConfigId, startRegionConfigs);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "update/botConfig/{serverGameEngineConfigId}", consumes = APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public void updateBotConfig(@PathVariable("serverGameEngineConfigId") int serverGameEngineConfigId, @RequestBody List<BotConfig> botConfigs) {
        try {
            serverGameEngineService.updateBotConfig(serverGameEngineConfigId, botConfigs);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "update/serverLevelQuestConfig/{serverGameEngineConfigId}", consumes = APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public void updateServerLevelQuestConfig(@PathVariable("serverGameEngineConfigId") int serverGameEngineConfigId, @RequestBody List<ServerLevelQuestConfig> serverLevelQuestConfigs) {
        try {
            serverGameEngineService.updateServerLevelQuestConfig(serverGameEngineConfigId, serverLevelQuestConfigs);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "update/boxRegionConfig/{serverGameEngineConfigId}", consumes = APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public void updateBoxRegionConfig(@PathVariable("serverGameEngineConfigId") int serverGameEngineConfigId, @RequestBody List<BoxRegionConfig> boxRegionConfigs) {
        try {
            serverGameEngineService.updateBoxRegionConfig(serverGameEngineConfigId, boxRegionConfigs);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }
}
