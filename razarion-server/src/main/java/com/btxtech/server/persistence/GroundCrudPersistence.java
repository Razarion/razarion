package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity_;
import com.btxtech.shared.dto.GroundConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class GroundCrudPersistence extends CrudPersistence<GroundConfig, GroundConfigEntity> {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ImagePersistence imagePersistence;

    public GroundCrudPersistence() {
        super(GroundConfigEntity.class, GroundConfigEntity_.id, GroundConfigEntity_.internalName, GroundConfig::getId);
    }

    @Override
    protected GroundConfig toConfig(GroundConfigEntity entity) {
        return entity.toGroundConfig();
    }

    @Override
    protected void fromConfig(GroundConfig config, GroundConfigEntity entity) {
        entity.fromGroundConfig(config, imagePersistence);
    }

    public GroundConfigEntity getPlanetConfig(Integer groundConfigId) {
        return entityManager.find(GroundConfigEntity.class, groundConfigId);
    }
}
