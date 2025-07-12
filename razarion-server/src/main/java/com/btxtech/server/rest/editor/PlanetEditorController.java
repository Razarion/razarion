package com.btxtech.server.rest.editor;

import com.btxtech.server.model.engine.PlanetEntity;
import com.btxtech.server.rest.AbstractConfigController;
import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.server.service.engine.PlanetCrudService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/planet")
public class PlanetEditorController extends AbstractConfigController<PlanetConfig> {
    private final PlanetCrudService planetCrudService;

    public PlanetEditorController(PlanetCrudService planetCrudService) {
        this.planetCrudService = planetCrudService;
    }

    @Override
    protected AbstractConfigCrudService<PlanetConfig, PlanetEntity> getConfigCrudService() {
        return planetCrudService;
    }
}
