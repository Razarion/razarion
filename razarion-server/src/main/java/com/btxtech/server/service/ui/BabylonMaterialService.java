package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.repository.ui.BabylonMaterialRepository;
import com.btxtech.server.rest.ui.MaterialSizeInfo;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BabylonMaterialService extends AbstractBaseEntityCrudService<BabylonMaterialEntity> {
    private final BabylonMaterialRepository babylonMaterialRepository;

    public BabylonMaterialService(BabylonMaterialRepository babylonMaterialRepository) {
        super(BabylonMaterialEntity.class, babylonMaterialRepository);
        this.babylonMaterialRepository = babylonMaterialRepository;
    }

    @Transactional
    public byte[] getData(int id) {
        return getEntity(id).getData();
    }

    @Transactional
    public void setData(int id, byte[] data) {
        BabylonMaterialEntity entity = getEntity(id);
        entity.setData(data);
        getJpaRepository().save(entity);
    }

    @Transactional
    public List<MaterialSizeInfo> getMaterialSizes() {
        return babylonMaterialRepository.findAllMaterialSizes().stream()
                .map(row -> new MaterialSizeInfo(
                        (Integer) row[0],
                        (String) row[1],
                        ((Number) row[2]).intValue()
                ))
                .toList();
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
