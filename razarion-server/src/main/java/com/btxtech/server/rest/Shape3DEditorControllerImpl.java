package com.btxtech.server.rest;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.rest.Shape3DEditorController;

import javax.inject.Inject;

/**
 * Created by Beat
 * 16.08.2016.
 */
public class Shape3DEditorControllerImpl extends AbstractCrudController<Shape3DConfig, ColladaEntity> implements Shape3DEditorController {
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;

    @Override
    protected Shape3DCrudPersistence getCrudPersistence() {
        return shape3DPersistence;
    }
}
