package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.InventoryArtifactEntity;
import com.btxtech.server.repository.engine.InventoryArtifactRepository;
import com.btxtech.shared.gameengine.datatypes.InventoryArtifact;
import org.springframework.stereotype.Service;

@Service
public class InventoryArtifactService extends AbstractConfigCrudService<InventoryArtifact, InventoryArtifactEntity> {

    public InventoryArtifactService(InventoryArtifactRepository inventoryArtifactRepository) {
        super(InventoryArtifactEntity.class, inventoryArtifactRepository);
    }

    @Override
    protected InventoryArtifact toConfig(InventoryArtifactEntity entity) {
        return entity.toInventoryArtifact();
    }

    @Override
    protected void fromConfig(InventoryArtifact config, InventoryArtifactEntity entity) {
        entity.fromInventoryArtifact(config);
        entity.setImage(getServiceProviderService().getImagePersistence().getImageLibraryEntity(config.getImageId()));
    }
}
