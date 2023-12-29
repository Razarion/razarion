package com.btxtech.server.persistence.inventory;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Singleton
public class InventoryItemCrudPersistence extends AbstractCrudPersistence<InventoryItem, InventoryItemEntity> {
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;

    public InventoryItemCrudPersistence() {
        super(InventoryItemEntity.class, InventoryItemEntity_.id, InventoryItemEntity_.internalName);
    }

    @Override
    protected InventoryItem toConfig(InventoryItemEntity entity) {
        return entity.toInventoryItem();
    }

    @Override
    protected void fromConfig(InventoryItem config, InventoryItemEntity entity) {
        entity.fromInventoryItem(config);
        entity.setImage(imagePersistence.getImageLibraryEntity(config.getImageId()));
        entity.setBaseItemType(baseItemTypeCrudPersistence.getEntity(config.getBaseItemTypeId()));
    }
}
