package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.TerrainObjectGeneratorEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.TerrainObjectGeneratorService;
import com.btxtech.shared.CommonUrl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CommonUrl.TERRAIN_OBJECT_GENERATOR_EDITOR_PATH)
public class TerrainObjectGeneratorController extends AbstractBaseController<TerrainObjectGeneratorEntity> {
    private final TerrainObjectGeneratorService terrainObjectGeneratorPersistence;

    public TerrainObjectGeneratorController(TerrainObjectGeneratorService terrainObjectGeneratorPersistence) {
        this.terrainObjectGeneratorPersistence = terrainObjectGeneratorPersistence;
    }

    @Override
    protected AbstractBaseEntityCrudService<TerrainObjectGeneratorEntity> getEntityCrudPersistence() {
        return terrainObjectGeneratorPersistence;
    }
}
