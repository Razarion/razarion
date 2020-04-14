package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity_;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class SlopeCrudPersistence extends AbstractCrudPersistence<SlopeConfig, SlopeConfigEntity> {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ImagePersistence imagePersistence;

    public SlopeCrudPersistence() {
        super(SlopeConfigEntity.class, SlopeConfigEntity_.id, SlopeConfigEntity_.internalName);
    }

    @Override
    protected SlopeConfig toConfig(SlopeConfigEntity entity) {
        return entity.toSlopeConfig();
    }

    @Override
    protected void fromConfig(SlopeConfig config, SlopeConfigEntity entity) {
        entity.fromSlopeConfig(config, imagePersistence);
    }
}
