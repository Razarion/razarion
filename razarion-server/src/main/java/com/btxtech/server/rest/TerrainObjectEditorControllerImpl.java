package com.btxtech.server.rest;

import com.btxtech.server.persistence.TerrainObjectCrudPersistence;
import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.user.SecurityCheck;
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

    @Override
    @SecurityCheck
    public void updateRadius(int terrainObjectId, double radius) {
        persistenceService.updateRadius(terrainObjectId, radius);
    }
}
