package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.particle.ParticleShapeCrudPersistence;
import com.btxtech.server.persistence.particle.ParticleShapeEntity;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;
import com.btxtech.shared.rest.ParticleShapeEditorController;

import javax.inject.Inject;

public class ParticleShapeEditorControllerImpl extends AbstractCrudController<ParticleShapeConfig, ParticleShapeEntity> implements ParticleShapeEditorController {
    @Inject
    private ParticleShapeCrudPersistence particleShapeCrudPersistence;

    @Override
    protected AbstractCrudPersistence<ParticleShapeConfig, ParticleShapeEntity> getCrudPersistence() {
        return particleShapeCrudPersistence;
    }
}
