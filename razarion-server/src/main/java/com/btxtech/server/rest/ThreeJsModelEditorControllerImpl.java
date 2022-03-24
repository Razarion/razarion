package com.btxtech.server.rest;

import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.rest.ThreeJsModelEditorController;

import javax.inject.Inject;

public class ThreeJsModelEditorControllerImpl extends AbstractCrudController<ThreeJsModelConfig, ThreeJsModelConfigEntity> implements ThreeJsModelEditorController {
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;

    @Override
    protected ThreeJsModelCrudPersistence getCrudPersistence() {
        return threeJsModelCrudPersistence;
    }
}
