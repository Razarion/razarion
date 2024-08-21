package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.BrushConfigEntity;
import com.btxtech.server.persistence.BrushCrudPersistence;
import com.btxtech.shared.datatypes.BrushConfig;
import com.btxtech.shared.rest.BrushEditorController;

import javax.inject.Inject;

public class BrushEditorControllerImpl extends AbstractCrudController<BrushConfig, BrushConfigEntity> implements BrushEditorController {
    @Inject
    private BrushCrudPersistence persistenceService;

    @Override
    protected AbstractConfigCrudPersistence<BrushConfig, BrushConfigEntity> getCrudPersistence() {
        return this.persistenceService;
    }
}
