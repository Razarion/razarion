package com.btxtech.server.rest;

import com.btxtech.server.persistence.CrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.rest.LevelEditorController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 22.08.2017.
 */
public class LevelEditorControllerImpl extends AbstractCrudController<LevelConfig, LevelEntity> implements LevelEditorController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private LevelPersistence levelPersistence;

    @Override
    protected CrudPersistence<LevelConfig, LevelEntity> getCrudPersistence() {
        return levelPersistence;
    }
}
