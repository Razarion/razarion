package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.rest.LevelEditorController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 22.08.2017.
 */
public class LevelEditorControllerImpl extends AbstractCrudController<LevelEditConfig, LevelEntity> implements LevelEditorController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;

    @Override
    protected AbstractCrudPersistence<LevelEditConfig, LevelEntity> getCrudPersistence() {
        return levelCrudPersistence;
    }
}
