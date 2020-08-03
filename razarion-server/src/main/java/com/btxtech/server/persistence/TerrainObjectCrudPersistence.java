package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity_;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class TerrainObjectCrudPersistence extends AbstractCrudPersistence<TerrainObjectConfig, TerrainObjectEntity> {
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;

    public TerrainObjectCrudPersistence() {
        super(TerrainObjectEntity.class, TerrainObjectEntity_.id, TerrainObjectEntity_.internalName);
    }

    @Override
    protected TerrainObjectConfig toConfig(TerrainObjectEntity entity) {
        return entity.toTerrainObjectConfig();
    }

    @Override
    protected void fromConfig(TerrainObjectConfig config, TerrainObjectEntity entity) {
        entity.fromTerrainObjectConfig(config, shape3DPersistence.getEntity(config.getShape3DId()));
    }
}