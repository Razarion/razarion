package com.btxtech.server.rest.editor;

import com.btxtech.server.model.engine.PlanetEntity;
import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudPersistence;
import com.btxtech.server.service.engine.PlanetCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/planet")
public class PlanetEditorController extends AbstractConfigController<PlanetConfig> {
    private final PlanetCrudPersistence planetCrudPersistence;

    public PlanetEditorController(PlanetCrudPersistence planetCrudPersistence) {
        this.planetCrudPersistence = planetCrudPersistence;
    }

    @Override
    protected AbstractConfigCrudPersistence<PlanetConfig, PlanetEntity> getConfigCrudPersistence() {
        return planetCrudPersistence;
    }
}
