package com.btxtech.server.persistence;

import com.btxtech.server.persistence.ui.BabylonMaterialEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;


@Component
public class BabylonMaterialCrudPersistence extends AbstractEntityCrudPersistence<BabylonMaterialEntity, Integer> {
    @Autowired
    private BabylonMaterialCrudRepository babylonMaterialCrudRepository;

    public BabylonMaterialCrudPersistence() {
        super(BabylonMaterialEntity.class);
    }

    @Transactional
    public byte[] getData(int id) {
        return getEntity(id).getData();
    }

    @Transactional
    public void setData(int id, byte[] data) {
        BabylonMaterialEntity entity = getEntity(id);
        entity.setData(data);
        throw new UnsupportedOperationException("...TODO... entityManager.merge(entity)");
    }


    @Override
    protected JpaRepository<BabylonMaterialEntity, Integer> getJpaRepository() {
        return babylonMaterialCrudRepository;
    }

    @Override
    protected BabylonMaterialEntity jsonToJpa(BabylonMaterialEntity babylonMaterialEntity) {
        BabylonMaterialEntity dbBabylonMaterialEntity = getEntity(babylonMaterialEntity.getId());
        dbBabylonMaterialEntity.setInternalName(babylonMaterialEntity.getInternalName());
        dbBabylonMaterialEntity.setNodeMaterial(babylonMaterialEntity.isNodeMaterial());
        dbBabylonMaterialEntity.setDiplomacyColorNode(babylonMaterialEntity.getDiplomacyColorNode());
        dbBabylonMaterialEntity.setOverrideAlbedoTextureNode(babylonMaterialEntity.getOverrideAlbedoTextureNode());
        dbBabylonMaterialEntity.setOverrideMetallicTextureNode(babylonMaterialEntity.getOverrideMetallicTextureNode());
        dbBabylonMaterialEntity.setOverrideBumpTextureNode(babylonMaterialEntity.getOverrideBumpTextureNode());
        dbBabylonMaterialEntity.setOverrideAmbientOcclusionTextureNode(babylonMaterialEntity.getOverrideAmbientOcclusionTextureNode());
        return dbBabylonMaterialEntity;
    }

}
