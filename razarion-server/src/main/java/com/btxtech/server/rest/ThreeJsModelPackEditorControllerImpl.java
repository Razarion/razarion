package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.ThreeJsModelPackConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelPackCrudPersistence;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;
import com.btxtech.shared.rest.ThreeJsModelPackEditorController;

import javax.inject.Inject;

public class ThreeJsModelPackEditorControllerImpl extends AbstractCrudController<ThreeJsModelPackConfig, ThreeJsModelPackConfigEntity> implements ThreeJsModelPackEditorController {
    @Inject
    private ThreeJsModelPackCrudPersistence threeJsModelPackCrudPersistence;

    @Override
    protected AbstractCrudPersistence<ThreeJsModelPackConfig, ThreeJsModelPackConfigEntity> getCrudPersistence() {
        return threeJsModelPackCrudPersistence;
    }
}
