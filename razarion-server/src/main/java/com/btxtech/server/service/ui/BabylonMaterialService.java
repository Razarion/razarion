package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.repository.ui.BabylonMaterialRepository;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;


@Service
public class BabylonMaterialService extends AbstractBaseEntityCrudService<BabylonMaterialEntity> {
    @Autowired
    private BabylonMaterialRepository babylonMaterialRepository;

    public BabylonMaterialService() {
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
        return babylonMaterialRepository;
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
