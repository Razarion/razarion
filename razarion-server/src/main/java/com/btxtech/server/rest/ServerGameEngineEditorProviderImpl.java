package com.btxtech.server.rest;

import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;

/**
 * Created by Beat
 * on 28.07.2017.
 */
public class ServerGameEngineEditorProviderImpl implements ServerGameEngineEditorProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

    @Override
    public List<ObjectNameId> readStartRegionObjectNameIds() {
        try {
            return serverGameEngineCrudPersistence.readStartRegionObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public StartRegionConfig readStartRegionConfig(int id) {
        try {
            return serverGameEngineCrudPersistence.readStartRegionConfig(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public StartRegionConfig createStartRegionConfig() {
        try {
            return serverGameEngineCrudPersistence.createStartRegionConfig();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateStartRegionConfig(StartRegionConfig startRegionConfig) {
        try {
            serverGameEngineCrudPersistence.updateStartRegionConfig(startRegionConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteStartRegionConfig(int id) {
        try {
            serverGameEngineCrudPersistence.deleteStartRegion(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readLevelQuestConfigObjectNameIds() {
        try {
            return serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public ServerLevelQuestConfig readLevelQuestConfig(int id) {
        try {
            return serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public ServerLevelQuestConfig createLevelQuestConfig() {
        try {
            return serverGameEngineCrudPersistence.getServerLevelQuestCrud().create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateLevelQuestConfig(ServerLevelQuestConfig serverLevelQuestConfig) {
        try {
            serverGameEngineCrudPersistence.getServerLevelQuestCrud().update(serverLevelQuestConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteLevelQuestConfig(int id) {
        try {
            serverGameEngineCrudPersistence.getServerLevelQuestCrud().delete(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readQuestConfigObjectNameIds(int levelQuestId) {
        try {
            return serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public QuestConfig createQuestConfig(int levelQuestId) {
        try {
            return serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public QuestConfig readQuestConfig(int levelQuestId, int questId) {
        try {
            return serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).read(questId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateQuestConfig(int levelQuestId, QuestConfig questConfig) {
        try {
            serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).update(questConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteQuestConfig(int levelQuestId, int questId) {
        System.out.println("deleteQuestConfig levelQuestId: " + levelQuestId + " questId: " + questId);
        try {
            serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).delete(questId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void swapQuestConfig(int levelQuestId, int index1, int index2) {
        try {
            serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).swap(index1, index2);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readResourceRegionObjectNameIds() {
        try {
            return serverGameEngineCrudPersistence.getResourceRegionConfigCrud().readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public ResourceRegionConfig createResourceRegionConfig() {
        try {
            return serverGameEngineCrudPersistence.getResourceRegionConfigCrud().create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteResourceRegionConfig(int resourceRegionConfigId) {
        try {
            serverGameEngineCrudPersistence.getResourceRegionConfigCrud().delete(resourceRegionConfigId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateResourceRegionConfig(ResourceRegionConfig resourceRegionConfig) {
        try {
            serverGameEngineCrudPersistence.getResourceRegionConfigCrud().update(resourceRegionConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public ResourceRegionConfig readResourceRegionConfig(int resourceRegionConfigId) {
        try {
            return serverGameEngineCrudPersistence.getResourceRegionConfigCrud().read(resourceRegionConfigId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readBotConfigObjectNameIds() {
        try {
            return serverGameEngineCrudPersistence.getBotConfigCrud().readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public BotConfig createBotConfig() {
        try {
            return serverGameEngineCrudPersistence.getBotConfigCrud().create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteBotConfigConfig(int id) {
        try {
            serverGameEngineCrudPersistence.getBotConfigCrud().delete(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateBotConfig(BotConfig botConfig) {
        try {
            serverGameEngineCrudPersistence.getBotConfigCrud().update(botConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public BotConfig readBotConfig(int id) {
        try {
            return serverGameEngineCrudPersistence.getBotConfigCrud().read(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readBotSceneConfigObjectNameIds() {
        try {
            return serverGameEngineCrudPersistence.getBotSceneConfigCrud().readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public BotSceneConfig createBotSceneConfig() {
        try {
            return serverGameEngineCrudPersistence.getBotSceneConfigCrud().create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteBotSceneConfigConfig(int id) {
        try {
            serverGameEngineCrudPersistence.getBotSceneConfigCrud().delete(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateBotSceneConfig(BotSceneConfig botSceneConfig) {
        try {
            serverGameEngineCrudPersistence.getBotSceneConfigCrud().update(botSceneConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public BotSceneConfig readBotSceneConfig(int id) {
        try {
            return serverGameEngineCrudPersistence.getBotSceneConfigCrud().read(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public BoxRegionConfig createBoxRegionConfig() {
        try {
            return serverGameEngineCrudPersistence.getBoxRegionConfigCrud().create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateBoxRegionConfig(BoxRegionConfig resourceRegionConfig) {
        try {
            serverGameEngineCrudPersistence.getBoxRegionConfigCrud().update(resourceRegionConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteBoxRegionConfig(int id) {
        try {
            serverGameEngineCrudPersistence.getBoxRegionConfigCrud().delete(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public BoxRegionConfig readBoxRegionConfig(int id) {
        try {
            return serverGameEngineCrudPersistence.getBoxRegionConfigCrud().read(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readBoxRegionObjectNameIds() {
        try {
            return serverGameEngineCrudPersistence.getBoxRegionConfigCrud().readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
