package com.btxtech.server.persistence.particle;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParticleShapeCrudPersistence extends AbstractCrudPersistence<ParticleShapeConfig, ParticleShapeEntity> {
    @Inject
    private ImagePersistence imagePersistence;

    public ParticleShapeCrudPersistence() {
        super(ParticleShapeEntity.class, ParticleShapeEntity_.id, ParticleShapeEntity_.internalName);
    }

    @Override
    protected ParticleShapeConfig toConfig(ParticleShapeEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(ParticleShapeConfig config, ParticleShapeEntity entity) {
        entity.fromConfig(config, imagePersistence);
    }
}
