package com.btxtech.server.rest;

import com.btxtech.server.persistence.TerrainObjectCrudPersistence;
import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class TerrainObjectEditorControllerImpl extends AbstractCrudController<TerrainObjectConfig, TerrainObjectEntity> implements TerrainObjectEditorController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainObjectCrudPersistence persistenceService;

    @Override
    protected TerrainObjectCrudPersistence getCrudPersistence() {
        return persistenceService;
    }

    @Override
    public List<ObjectNameId> readDrivewayObjectNameIds() {
        try {
            return persistenceService.readDrivewayObjectNameIds();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
