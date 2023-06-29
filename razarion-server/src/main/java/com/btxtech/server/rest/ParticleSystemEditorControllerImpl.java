package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.ParticleSystemCrudPersistence;
import com.btxtech.server.persistence.ParticleSystemEntity;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;
import com.btxtech.shared.rest.ParticleSystemEditorController;

import javax.inject.Inject;

public class ParticleSystemEditorControllerImpl extends AbstractCrudController<ParticleSystemConfig, ParticleSystemEntity> implements ParticleSystemEditorController {
    @Inject
    private ParticleSystemCrudPersistence particleSystemCrudPersistence;

    @Override
    protected AbstractCrudPersistence<ParticleSystemConfig, ParticleSystemEntity> getCrudPersistence() {
        return particleSystemCrudPersistence;
    }
}
