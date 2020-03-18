package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity_;
import com.btxtech.shared.dto.GroundConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class GroundCrudPersistence extends AbstractCrudPersistence<GroundConfig, GroundConfigEntity> {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ImagePersistence imagePersistence;

    public GroundCrudPersistence() {
        super(GroundConfigEntity.class, GroundConfigEntity_.id, GroundConfigEntity_.internalName);
    }

    @Override
    protected GroundConfig toConfig(GroundConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(GroundConfig config, GroundConfigEntity entity) {
        entity.fromGroundConfig(config, imagePersistence);
    }
}
