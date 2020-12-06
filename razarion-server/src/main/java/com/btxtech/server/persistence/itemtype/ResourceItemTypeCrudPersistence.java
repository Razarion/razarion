package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ResourceItemTypeCrudPersistence extends AbstractCrudPersistence<ResourceItemType, ResourceItemTypeEntity> {
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;
    @Inject
    private ImagePersistence imagePersistence;

    public ResourceItemTypeCrudPersistence() {
        super(ResourceItemTypeEntity.class, ResourceItemTypeEntity_.id, ResourceItemTypeEntity_.internalName);
    }

    @Override
    protected ResourceItemType toConfig(ResourceItemTypeEntity entity) {
        return entity.toResourceItemType();
    }

    @Override
    protected void fromConfig(ResourceItemType resourceItemType, ResourceItemTypeEntity resourceItemTypeEntity) {
        resourceItemTypeEntity.fromResourceItemType(resourceItemType);
        resourceItemTypeEntity.setShape3DId(shape3DPersistence.getEntity(resourceItemType.getShape3DId()));
        resourceItemTypeEntity.setThumbnail(imagePersistence.getImageLibraryEntity(resourceItemType.getThumbnail()));
    }
}
