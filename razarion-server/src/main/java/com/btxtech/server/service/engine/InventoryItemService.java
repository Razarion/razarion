package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.InventoryItemEntity;
import com.btxtech.server.repository.engine.InventoryItemRepository;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import org.springframework.stereotype.Service;

@Service
public class InventoryItemService extends AbstractConfigCrudService<InventoryItem, InventoryItemEntity> {

    public InventoryItemService(InventoryItemRepository inventoryItemRepository) {
        super(InventoryItemEntity.class, inventoryItemRepository);
    }

    @Override
    protected InventoryItem toConfig(InventoryItemEntity entity) {
        return entity.toInventoryItem();
    }

    @Override
    protected void fromConfig(InventoryItem config, InventoryItemEntity entity) {
        entity.fromInventoryItem(config);
        entity.setImage(getServiceProviderService().getImagePersistence().getImageLibraryEntity(config.getImageId()));
        entity.setBaseItemType(getServiceProviderService().getBaseItemTypeCrudPersistence().getEntity(config.getBaseItemTypeId()));
    }
}
