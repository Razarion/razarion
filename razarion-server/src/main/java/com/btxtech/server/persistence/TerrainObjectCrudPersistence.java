package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity_;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class TerrainObjectCrudPersistence extends AbstractCrudPersistence<TerrainObjectConfig, TerrainObjectEntity> {
    @Inject
    private ThreeJsModelPackCrudPersistence threeJsModelPackCrudPersistence;
    @PersistenceContext
    private EntityManager entityManager;

    public TerrainObjectCrudPersistence() {
        super(TerrainObjectEntity.class, TerrainObjectEntity_.id, TerrainObjectEntity_.internalName);
    }

    @Override
    protected TerrainObjectConfig toConfig(TerrainObjectEntity entity) {
        return entity.toTerrainObjectConfig();
    }

    @Override
    protected void fromConfig(TerrainObjectConfig config, TerrainObjectEntity entity) {
        entity.fromTerrainObjectConfig(config, threeJsModelPackCrudPersistence);
    }

    @Transactional
    public void updateRadius(int terrainObjectId, double radius) {
        TerrainObjectEntity terrainObjectEntity = getEntity(terrainObjectId);
        terrainObjectEntity.setRadius(radius);
        entityManager.merge(terrainObjectEntity);
    }
}