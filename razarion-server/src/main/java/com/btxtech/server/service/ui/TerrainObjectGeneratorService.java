package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.TerrainObjectGeneratorEntity;
import com.btxtech.server.repository.ui.TerrainObjectGeneratorRepository;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import org.springframework.stereotype.Service;


@Service
public class TerrainObjectGeneratorService extends AbstractBaseEntityCrudService<TerrainObjectGeneratorEntity> {
    public TerrainObjectGeneratorService(TerrainObjectGeneratorRepository terrainObjectGeneratorRepository) {
        super(TerrainObjectGeneratorEntity.class, terrainObjectGeneratorRepository);
    }
}
