package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.AudioPersistence;
import com.btxtech.server.persistence.BoxItemTypeCrudPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.Model3DCrudPersistence;
import com.btxtech.server.persistence.ParticleSystemCrudPersistence;
import com.btxtech.server.persistence.ThreeJsModelPackCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class BaseItemTypeCrudPersistence extends AbstractConfigCrudPersistence<BaseItemType, BaseItemTypeEntity> {
    @Inject
    private Model3DCrudPersistence model3DCrudPersistence;
    @Inject
    private ThreeJsModelPackCrudPersistence threeJsModelPackCrudPersistence;
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private AudioPersistence audioPersistence;
    @Inject
    private ParticleSystemCrudPersistence particleSystemCrudPersistence;
    @Inject
    private BoxItemTypeCrudPersistence boxItemTypeCrudPersistence;

    public BaseItemTypeCrudPersistence() {
        super(BaseItemTypeEntity.class, BaseItemTypeEntity_.id, BaseItemTypeEntity_.internalName);
    }

    @Override
    protected BaseItemType toConfig(BaseItemTypeEntity entity) {
        return entity.toBaseItemType();
    }

    @Override
    protected void fromConfig(BaseItemType baseItemType, BaseItemTypeEntity baseItemTypeEntity) {
        baseItemTypeEntity.fromBaseItemType(baseItemType, this, boxItemTypeCrudPersistence, audioPersistence, particleSystemCrudPersistence);
        baseItemTypeEntity.setModel3DEntity(model3DCrudPersistence.getEntity(baseItemType.getModel3DId()));
        baseItemTypeEntity.setThreeJsModelPackConfigEntity(threeJsModelPackCrudPersistence.getEntity(baseItemType.getThreeJsModelPackConfigId()));
        // TODO baseItemTypeEntity.setSpawnShape3DId(shape3DPersistence.getEntity(baseItemType.getSpawnShape3DId()));
        baseItemTypeEntity.setBuildupTexture(imagePersistence.getImageLibraryEntity(baseItemType.getBuildupTextureId()));
        baseItemTypeEntity.setDemolitionImage(imagePersistence.getImageLibraryEntity(baseItemType.getDemolitionImageId()));
        // TODO baseItemTypeEntity.setWreckageShape3D(shape3DPersistence.getEntity(baseItemType.getWreckageShape3DId()));
        baseItemTypeEntity.setSpawnAudio(audioPersistence.getAudioLibraryEntity(baseItemType.getSpawnAudioId()));
        baseItemTypeEntity.setThumbnail(imagePersistence.getImageLibraryEntity(baseItemType.getThumbnail()));
    }
}