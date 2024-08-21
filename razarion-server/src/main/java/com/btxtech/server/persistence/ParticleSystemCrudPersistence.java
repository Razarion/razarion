package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParticleSystemCrudPersistence extends AbstractConfigCrudPersistence<ParticleSystemConfig, ParticleSystemEntity> {
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;
    @Inject
    private ImagePersistence imagePersistence;

    public ParticleSystemCrudPersistence() {
        super(ParticleSystemEntity.class, ParticleSystemEntity_.id, ParticleSystemEntity_.internalName);
    }

    @Override
    protected ParticleSystemConfig toConfig(ParticleSystemEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(ParticleSystemConfig config, ParticleSystemEntity entity) {
        entity.fromConfig(config, threeJsModelCrudPersistence, imagePersistence);
    }
}
