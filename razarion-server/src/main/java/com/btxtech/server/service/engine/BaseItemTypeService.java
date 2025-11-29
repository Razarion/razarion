package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.server.repository.engine.BaseItemTypeRepository;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import org.springframework.stereotype.Service;

@Service
public class BaseItemTypeService extends AbstractConfigCrudService<BaseItemType, BaseItemTypeEntity> {

    public BaseItemTypeService(BaseItemTypeRepository baseItemTypeRepository) {
        super(BaseItemTypeEntity.class, baseItemTypeRepository);
    }

    @Override
    protected BaseItemType toConfig(BaseItemTypeEntity entity) {
        return entity.toBaseItemType();
    }

    @Override
    protected void fromConfig(BaseItemType baseItemType, BaseItemTypeEntity baseItemTypeEntity) {
        baseItemTypeEntity.fromBaseItemType(baseItemType,
                this,
                getServiceProviderService().getBoxItemTypeCrudPersistence(),
                getServiceProviderService().getAudioPersistence(),
                getServiceProviderService().getParticleSystemCrudPersistence());
        baseItemTypeEntity.setModel3DEntity(getServiceProviderService().getModel3DCrudPersistence().getEntity(baseItemType.getModel3DId()));
        baseItemTypeEntity.setBuildupTexture(getServiceProviderService().getImagePersistence().getImageLibraryEntity(baseItemType.getBuildupTextureId()));
        baseItemTypeEntity.setDemolitionImage(getServiceProviderService().getImagePersistence().getImageLibraryEntity(baseItemType.getDemolitionImageId()));
        // TODO baseItemTypeEntity.setWreckageShape3D(shape3DPersistence.getEntity(baseItemType.getWreckageShape3DId()));
        baseItemTypeEntity.setSpawnAudio(getServiceProviderService().getAudioPersistence().getAudioLibraryEntity(baseItemType.getSpawnAudioId()));
        baseItemTypeEntity.setThumbnail(getServiceProviderService().getImagePersistence().getImageLibraryEntity(baseItemType.getThumbnail()));
    }
}