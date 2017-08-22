package com.btxtech.server.rest;

import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.rest.LevelEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 22.08.2017.
 */
public class LevelEditorProviderImpl implements LevelEditorProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private LevelPersistence levelPersistence;

    @Override
    public LevelConfig create() {
        try {
            return levelPersistence.create();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<ObjectNameId> readObjectNameIds() {
        try {
            return levelPersistence.readObjectNameIds();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public LevelConfig reads(int id) {
        try {
            return levelPersistence.readLevelConfig(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void update(LevelConfig levelConfig) {
        try {
            levelPersistence.update(levelConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void delete(int id) {
        try {
            levelPersistence.delete(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
