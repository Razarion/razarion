package com.btxtech.server.rest;

import com.btxtech.server.persistence.SlopeCrudPersistence;
import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.rest.SlopeEditorController;

import javax.inject.Inject;

public class SlopeEditorControllerImpl extends AbstractCrudController<SlopeConfig, SlopeConfigEntity> implements SlopeEditorController {
    @Inject
    private SlopeCrudPersistence slopeCrudPersistence;

    @Override
    protected SlopeCrudPersistence getCrudPersistence() {
        return slopeCrudPersistence;
    }
}
