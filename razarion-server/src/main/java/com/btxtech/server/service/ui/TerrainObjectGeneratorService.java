package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.TerrainObjectGeneratorEntity;
import com.btxtech.server.repository.ui.TerrainObjectGeneratorRepository;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;


@Service
public class TerrainObjectGeneratorService extends AbstractBaseEntityCrudService<TerrainObjectGeneratorEntity> {
    private final TerrainObjectGeneratorRepository terrainObjectGeneratorRepository;

    public TerrainObjectGeneratorService(TerrainObjectGeneratorRepository terrainObjectGeneratorRepository) {
        super(TerrainObjectGeneratorEntity.class);
        this.terrainObjectGeneratorRepository = terrainObjectGeneratorRepository;
    }

    @Override
    protected JpaRepository<TerrainObjectGeneratorEntity, Integer> getJpaRepository() {
        return terrainObjectGeneratorRepository;
    }
}
