package com.btxtech.server.persistence;

import com.btxtech.server.persistence.inventory.InventoryItemCrudPersistence;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity_;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class BoxItemTypeCrudPersistence extends AbstractConfigCrudPersistence<BoxItemType, BoxItemTypeEntity> {
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private Instance<InventoryItemCrudPersistence> inventoryPersistence;
    @Inject
    private ThreeJsModelPackCrudPersistence threeJsModelPackCrudPersistence;

    public BoxItemTypeCrudPersistence() {
        super(BoxItemTypeEntity.class, BoxItemTypeEntity_.id, BoxItemTypeEntity_.internalName);
    }

    @Override
    protected BoxItemType toConfig(BoxItemTypeEntity entity) {
        return entity.toBoxItemType();
    }

    @Override
    protected void fromConfig(BoxItemType boxItemType, BoxItemTypeEntity boxItemTypeEntity) {
        boxItemTypeEntity.fromBoxItemType(boxItemType, inventoryPersistence.get());
        boxItemTypeEntity.setThreeJsModelPackConfigEntity(threeJsModelPackCrudPersistence.getEntity(boxItemType.getThreeJsModelPackConfigId()));
        boxItemTypeEntity.setThumbnail(imagePersistence.getImageLibraryEntity(boxItemType.getThumbnail()));
    }
}