package com.btxtech.server.service.engine;


import com.btxtech.server.model.engine.ResourceItemTypeEntity;
import com.btxtech.server.repository.engine.ResourceItemTypeRepository;
import com.btxtech.server.service.ui.ImageService;
import com.btxtech.server.service.ui.Model3DService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import org.springframework.stereotype.Service;

@Service
public class ResourceItemTypeService extends AbstractConfigCrudService<ResourceItemType, ResourceItemTypeEntity> {
    private final ImageService imageService;
    private final Model3DService model3DCrudPersistence;

    public ResourceItemTypeService(ResourceItemTypeRepository resourceItemTypeRepository, ImageService imageService, Model3DService model3DCrudPersistence) {
        super(ResourceItemTypeEntity.class, resourceItemTypeRepository);
        this.imageService = imageService;
        this.model3DCrudPersistence = model3DCrudPersistence;
    }

    @Override
    protected ResourceItemType toConfig(ResourceItemTypeEntity entity) {
        return entity.toResourceItemType();
    }

    @Override
    protected void fromConfig(ResourceItemType resourceItemType, ResourceItemTypeEntity resourceItemTypeEntity) {
        resourceItemTypeEntity.setModel3DEntity(model3DCrudPersistence.getEntity(resourceItemType.getModel3DId()));
        resourceItemTypeEntity.fromResourceItemType(resourceItemType);
        resourceItemTypeEntity.setThumbnail(imageService.getImageLibraryEntity(resourceItemType.getThumbnail()));
    }
}
