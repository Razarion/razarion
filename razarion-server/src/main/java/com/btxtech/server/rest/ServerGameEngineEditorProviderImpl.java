package com.btxtech.server.rest;

import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
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
    private ServerGameEnginePersistence serverGameEnginePersistence;

    @Override
    public List<ObjectNameId> readStartRegionObjectNameIds() {
        try {
            return serverGameEnginePersistence.readStartRegionObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public StartRegionConfig readStartRegionConfig(int id) {
        try {
            return serverGameEnginePersistence.readStartRegionConfig(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public StartRegionConfig createStartRegionConfig() {
        try {
            return serverGameEnginePersistence.createStartRegionConfig();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateStartRegionConfig(StartRegionConfig startRegionConfig) {
        try {
            serverGameEnginePersistence.updateStartRegionConfig(startRegionConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteStartRegionConfig(int id) {
        try {
            serverGameEnginePersistence.deleteStartRegion(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readLevelQuestConfigObjectNameIds() {
        try {
            return serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public ServerLevelQuestConfig readLevelQuestConfig(int id) {
        try {
            return serverGameEnginePersistence.getServerLevelQuestCrud().read(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public ServerLevelQuestConfig createLevelQuestConfig() {
        try {
            return serverGameEnginePersistence.getServerLevelQuestCrud().create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateLevelQuestConfig(ServerLevelQuestConfig serverLevelQuestConfig) {
        try {
            serverGameEnginePersistence.getServerLevelQuestCrud().update(serverLevelQuestConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteLevelQuestConfig(int id) {
        try {
            serverGameEnginePersistence.getServerLevelQuestCrud().delete(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readQuestConfigObjectNameIds(int levelQuestId) {
        try {
            return serverGameEnginePersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public QuestConfig createQuestConfig(int levelQuestId) {
        try {
            return serverGameEnginePersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public QuestConfig readQuestConfig(int levelQuestId, int questId) {
        try {
            return serverGameEnginePersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).read(questId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateQuestConfig(int levelQuestId, QuestConfig questConfig) {
        try {
            serverGameEnginePersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).update(questConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteQuestConfig(int levelQuestId, int questId) {
        System.out.println("deleteQuestConfig levelQuestId: " + levelQuestId + " questId: " + questId);
        try {
            serverGameEnginePersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).delete(questId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void swapQuestConfig(int levelQuestId, int index1, int index2) {
        try {
            serverGameEnginePersistence.getServerQuestCrud(levelQuestId, Locale.ENGLISH).swap(index1, index2);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<ObjectNameId> readResourceRegionObjectNameIds() {
        try {
            return serverGameEnginePersistence.getResourceRegionConfigCrud().readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public ResourceRegionConfig createResourceRegionConfig() {
        try {
            return serverGameEnginePersistence.getResourceRegionConfigCrud().create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteResourceRegionConfig(int resourceRegionConfigId) {
        try {
            serverGameEnginePersistence.getResourceRegionConfigCrud().delete(resourceRegionConfigId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateResourceRegionConfig(ResourceRegionConfig resourceRegionConfig) {
        try {
            serverGameEnginePersistence.getResourceRegionConfigCrud().update(resourceRegionConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public ResourceRegionConfig readResourceRegionConfig(int resourceRegionConfigId) {
        try {
            return serverGameEnginePersistence.getResourceRegionConfigCrud().read(resourceRegionConfigId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
