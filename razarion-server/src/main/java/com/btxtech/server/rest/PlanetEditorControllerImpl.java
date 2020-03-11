package com.btxtech.server.rest;

import com.btxtech.server.persistence.CrudPersistence;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.PlanetEditorController;

import javax.inject.Inject;

public class PlanetEditorControllerImpl extends AbstractCrudController<PlanetConfig, PlanetEntity> implements PlanetEditorController {
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;

    @Override
    protected CrudPersistence<PlanetConfig, PlanetEntity> getCrudPersistence() {
        return planetCrudPersistence;
    }
}
