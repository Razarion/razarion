package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.DrivewayCrudPersistence;
import com.btxtech.server.persistence.surface.DrivewayConfigEntity;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.rest.DrivewayEditorController;

import javax.inject.Inject;

public class DrivewayEditorControllerImpl extends AbstractCrudController<DrivewayConfig, DrivewayConfigEntity> implements DrivewayEditorController {
    @Inject
    private DrivewayCrudPersistence drivewayCrudPersistence;

    @Override
    protected AbstractConfigCrudPersistence<DrivewayConfig, DrivewayConfigEntity> getCrudPersistence() {
        return drivewayCrudPersistence;
    }
}
