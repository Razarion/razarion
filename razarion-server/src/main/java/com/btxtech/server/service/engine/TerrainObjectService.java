package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.TerrainObjectEntity;
import com.btxtech.server.repository.engine.TerrainObjectRepository;
import com.btxtech.server.service.ui.Model3DService;
import com.btxtech.shared.dto.TerrainObjectConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TerrainObjectService extends AbstractConfigCrudService<TerrainObjectConfig, TerrainObjectEntity> {
    private final Model3DService model3DService;

    public TerrainObjectService(Model3DService model3DService, TerrainObjectRepository terrainObjectRepository) {
        super(TerrainObjectEntity.class, terrainObjectRepository);
        this.model3DService = model3DService;
    }

    @Override
    protected TerrainObjectConfig toConfig(TerrainObjectEntity entity) {
        return entity.toTerrainObjectConfig();
    }

    @Override
    protected void fromConfig(TerrainObjectConfig config, TerrainObjectEntity entity) {
        entity.fromTerrainObjectConfig(config, model3DService);
    }

    @Transactional
    public void updateRadius(int terrainObjectId, double radius) {
        TerrainObjectEntity terrainObjectEntity = getEntity(terrainObjectId);
        terrainObjectEntity.setRadius(radius);
        getJpaRepository().save(terrainObjectEntity);
    }
}