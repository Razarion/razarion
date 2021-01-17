package com.btxtech.server.persistence.particle;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.AudioPersistence;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ParticleEmitterSequenceCrudPersistence extends AbstractCrudPersistence<ParticleEmitterSequenceConfig, ParticleEmitterSequenceEntity> {
    @Inject
    private AudioPersistence audioPersistence;
    @Inject
    private ParticleShapeCrudPersistence particleShapeCrudPersistence;

    public ParticleEmitterSequenceCrudPersistence() {
        super(ParticleEmitterSequenceEntity.class, ParticleEmitterSequenceEntity_.id, ParticleEmitterSequenceEntity_.internalName);
    }

    @Override
    protected ParticleEmitterSequenceConfig toConfig(ParticleEmitterSequenceEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(ParticleEmitterSequenceConfig config, ParticleEmitterSequenceEntity entity) {
        entity.fromConfig(config, audioPersistence, particleShapeCrudPersistence);
    }
}
