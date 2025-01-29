package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.Model3DCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ResourceItemTypeCrudPersistence extends AbstractConfigCrudPersistence<ResourceItemType, ResourceItemTypeEntity> {
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private Model3DCrudPersistence model3DCrudPersistence;

    public ResourceItemTypeCrudPersistence() {
        super(ResourceItemTypeEntity.class, ResourceItemTypeEntity_.id, ResourceItemTypeEntity_.internalName);
    }

    @Override
    protected ResourceItemType toConfig(ResourceItemTypeEntity entity) {
        return entity.toResourceItemType();
    }

    @Override
    protected void fromConfig(ResourceItemType resourceItemType, ResourceItemTypeEntity resourceItemTypeEntity) {
        resourceItemTypeEntity.setModel3DEntity(model3DCrudPersistence.getEntity(resourceItemType.getModel3DId()));
        resourceItemTypeEntity.fromResourceItemType(resourceItemType);
        resourceItemTypeEntity.setThumbnail(imagePersistence.getImageLibraryEntity(resourceItemType.getThumbnail()));
    }
}
