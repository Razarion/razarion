package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceCrudPersistence;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceEntity;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import com.btxtech.shared.rest.ParticleEmitterSequenceEditorController;

import javax.inject.Inject;

public class ParticleEmitterSequenceEditorControllerImpl extends AbstractCrudController<ParticleEmitterSequenceConfig, ParticleEmitterSequenceEntity> implements ParticleEmitterSequenceEditorController {
    @Inject
    private ParticleEmitterSequenceCrudPersistence particleEmitterSequenceCrudPersistence;

    @Override
    protected AbstractCrudPersistence<ParticleEmitterSequenceConfig, ParticleEmitterSequenceEntity> getCrudPersistence() {
        return particleEmitterSequenceCrudPersistence;
    }
}
