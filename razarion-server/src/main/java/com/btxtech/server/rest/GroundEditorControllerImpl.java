package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.GroundCrudPersistence;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.rest.GroundEditorController;

import javax.inject.Inject;

public class GroundEditorControllerImpl extends AbstractCrudController<GroundConfig, GroundConfigEntity> implements GroundEditorController {
    @Inject
    private GroundCrudPersistence groundCrudPersistence;

    @Override
    protected AbstractConfigCrudPersistence<GroundConfig, GroundConfigEntity> getCrudPersistence() {
        return groundCrudPersistence;
    }
}
