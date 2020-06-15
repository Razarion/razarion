package com.btxtech.server.rest;

import com.btxtech.server.persistence.WaterCrudPersistence;
import com.btxtech.server.persistence.surface.WaterConfigEntity;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.rest.WaterEditorController;

import javax.inject.Inject;

public class WaterEditorControllerImpl extends AbstractCrudController<WaterConfig, WaterConfigEntity> implements WaterEditorController {
    @Inject
    private WaterCrudPersistence waterCrudPersistence;

    @Override
    protected WaterCrudPersistence getCrudPersistence() {
        return waterCrudPersistence;
    }
}
