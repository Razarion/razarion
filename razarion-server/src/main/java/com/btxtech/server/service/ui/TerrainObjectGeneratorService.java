package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.TerrainObjectGeneratorEntity;
import com.btxtech.server.repository.ui.TerrainObjectGeneratorRepository;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;


@Service
public class TerrainObjectGeneratorService extends AbstractBaseEntityCrudService<TerrainObjectGeneratorEntity> {
    @Autowired
    private TerrainObjectGeneratorRepository terrainObjectGeneratorRepository;

    public TerrainObjectGeneratorService() {
        super(TerrainObjectGeneratorEntity.class);
    }

    @Override
    protected JpaRepository<TerrainObjectGeneratorEntity, Integer> getJpaRepository() {
        return terrainObjectGeneratorRepository;
    }
}
