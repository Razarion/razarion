package com.btxtech.server.rest.editor;

import com.btxtech.server.model.ui.TerrainObjectGeneratorEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.TerrainObjectGeneratorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/editor/terrain-object-generator")
public class TerrainObjectGeneratorController extends AbstractBaseController<TerrainObjectGeneratorEntity> {
    private final TerrainObjectGeneratorService terrainObjectGeneratorPersistence;

    public TerrainObjectGeneratorController(TerrainObjectGeneratorService terrainObjectGeneratorPersistence) {
        this.terrainObjectGeneratorPersistence = terrainObjectGeneratorPersistence;
    }

    @Override
    protected AbstractBaseEntityCrudService<TerrainObjectGeneratorEntity> getBaseEntityCrudService() {
        return terrainObjectGeneratorPersistence;
    }
}
