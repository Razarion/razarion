package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

public class ServerGameEngineEditorControllerImpl extends AbstractCrudController<ServerGameEngineConfig, ServerGameEngineConfigEntity> implements ServerGameEngineEditorController {
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    protected AbstractConfigCrudPersistence<ServerGameEngineConfig, ServerGameEngineConfigEntity> getCrudPersistence() {
        return serverGameEngineCrudPersistence;
    }

    @Override
    @SecurityCheck
    public void updateResourceRegionConfig(int serverGameEngineConfigId, List<ResourceRegionConfig> resourceRegionConfigs) {
        try {
            serverGameEngineCrudPersistence.updateResourceRegionConfig(serverGameEngineConfigId, resourceRegionConfigs);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @SecurityCheck
    public void updateStartRegionConfig(int serverGameEngineConfigId, List<StartRegionConfig> startRegionConfigs) {
        try {
            serverGameEngineCrudPersistence.updateStartRegionConfig(serverGameEngineConfigId, startRegionConfigs);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @SecurityCheck
    public void updateBotConfig(int serverGameEngineConfigId, List<BotConfig> botConfigs) {
        try {
            serverGameEngineCrudPersistence.updateBotConfig(serverGameEngineConfigId, botConfigs);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @SecurityCheck
    public void updateServerLevelQuestConfig(int serverGameEngineConfigId, List<ServerLevelQuestConfig> serverLevelQuestConfigs) {
        try {
            serverGameEngineCrudPersistence.updateServerLevelQuestConfig(serverGameEngineConfigId, serverLevelQuestConfigs);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @SecurityCheck
    public void updateBoxRegionConfig(int serverGameEngineConfigId, List<BoxRegionConfig> boxRegionConfigs) {
        try {
            serverGameEngineCrudPersistence.updateBoxRegionConfig(serverGameEngineConfigId, boxRegionConfigs);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

}
