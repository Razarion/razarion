package com.btxtech.server.rest;

import com.btxtech.server.persistence.TerrainObjectCrudPersistence;
import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.rest.TerrainObjectEditorController;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class TerrainObjectEditorControllerImpl extends AbstractCrudController<TerrainObjectConfig, TerrainObjectEntity> implements TerrainObjectEditorController {
    @Inject
    private TerrainObjectCrudPersistence persistenceService;

    @Override
    protected TerrainObjectCrudPersistence getCrudPersistence() {
        return persistenceService;
    }
}
