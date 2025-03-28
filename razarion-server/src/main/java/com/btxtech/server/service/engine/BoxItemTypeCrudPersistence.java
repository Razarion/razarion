package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BoxItemTypeEntity;
import com.btxtech.server.repository.engine.BoxItemTypeRepository;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import org.springframework.stereotype.Service;

@Service
public class BoxItemTypeCrudPersistence extends AbstractConfigCrudPersistence<BoxItemType, BoxItemTypeEntity> {

    public BoxItemTypeCrudPersistence(BoxItemTypeRepository boxItemTypeRepository) {
        super(BoxItemTypeEntity.class, boxItemTypeRepository);
    }

    @Override
    protected BoxItemType toConfig(BoxItemTypeEntity entity) {
        return entity.toBoxItemType();
    }

    @Override
    protected void fromConfig(BoxItemType boxItemType, BoxItemTypeEntity boxItemTypeEntity) {
        boxItemTypeEntity.fromBoxItemType(boxItemType, getServiceProviderService().getInventoryItemCrudPersistence());
        boxItemTypeEntity.setModel3DEntity(getServiceProviderService().getModel3DCrudPersistence().getEntity(boxItemType.getModel3DId()));
        boxItemTypeEntity.setThumbnail(getServiceProviderService().getImagePersistence().getImageLibraryEntity(boxItemType.getThumbnail()));
    }
}