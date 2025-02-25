package com.btxtech.server.rest.crud;

import com.btxtech.server.persistence.AbstractEntityCrudPersistence;
import com.btxtech.server.persistence.TerrainObjectGeneratorPersistence;
import com.btxtech.server.persistence.ui.TerrainObjectGeneratorEntity;
import com.btxtech.shared.CommonUrl;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path(CommonUrl.TERRAIN_OBJECT_GENERATOR_EDITOR_PATH)
public class TerrainObjectGeneratorController extends BaseEntityController<TerrainObjectGeneratorEntity> {
    @Inject
    private TerrainObjectGeneratorPersistence terrainObjectGeneratorPersistence;

    @Override
    protected AbstractEntityCrudPersistence<TerrainObjectGeneratorEntity> getEntityCrudPersistence() {
        return terrainObjectGeneratorPersistence;
    }
}
