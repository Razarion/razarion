package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.GroundConfigEntity;
import com.btxtech.server.repository.engine.GroundConfigRepository;
import com.btxtech.server.service.ui.BabylonMaterialService;
import com.btxtech.shared.dto.GroundConfig;
import org.springframework.stereotype.Service;

@Service
public class GroundCrudService extends AbstractConfigCrudService<GroundConfig, GroundConfigEntity> {
    private final BabylonMaterialService babylonMaterialService;

    public GroundCrudService(BabylonMaterialService babylonMaterialService, GroundConfigRepository groundConfigRepository) {
        super(GroundConfigEntity.class, groundConfigRepository);
        this.babylonMaterialService = babylonMaterialService;
    }

    @Override
    protected GroundConfig toConfig(GroundConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(GroundConfig config, GroundConfigEntity entity) {
        entity.fromGroundConfig(config, babylonMaterialService);
    }
}
