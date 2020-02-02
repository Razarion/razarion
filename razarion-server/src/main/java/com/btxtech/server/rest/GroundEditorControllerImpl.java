package com.btxtech.server.rest;

import com.btxtech.server.persistence.CrudPersistence;
import com.btxtech.server.persistence.GroundCrudPersistence;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.rest.GroundEditorController;

import javax.inject.Inject;

public class GroundEditorControllerImpl extends AbstractCrudController<GroundSkeletonConfig, GroundConfigEntity> implements GroundEditorController {
    @Inject
    private GroundCrudPersistence groundCrudPersistence;

    @Override
    protected CrudPersistence<GroundSkeletonConfig, GroundConfigEntity> getCrudPersistence() {
        return groundCrudPersistence;
    }
}
