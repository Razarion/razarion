package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.ThreeJsModelPackConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelPackCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;
import com.btxtech.shared.rest.ThreeJsModelPackEditorController;

import javax.inject.Inject;
import java.util.List;

public class ThreeJsModelPackEditorControllerImpl extends AbstractCrudController<ThreeJsModelPackConfig, ThreeJsModelPackConfigEntity> implements ThreeJsModelPackEditorController {
    @Inject
    private ThreeJsModelPackCrudPersistence threeJsModelPackCrudPersistence;

    @Override
    protected AbstractCrudPersistence<ThreeJsModelPackConfig, ThreeJsModelPackConfigEntity> getCrudPersistence() {
        return threeJsModelPackCrudPersistence;
    }

    @Override
    @SecurityCheck
    public List<ThreeJsModelPackConfig> findByThreeJsModelId(int threeJsModelId) {
        return threeJsModelPackCrudPersistence.findByThreeJsModelId(threeJsModelId);
    }
}
