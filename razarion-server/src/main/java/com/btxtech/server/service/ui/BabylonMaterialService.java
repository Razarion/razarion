package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.repository.ui.BabylonMaterialRepository;
import com.btxtech.server.rest.ui.MaterialSizeInfo;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ContentDigest;
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

    /**
     * The entity tag for one material, or null if there is nothing to tag.
     * <p>
     * Reads the digest column and nothing else, so answering "still the same?" costs a small row
     * instead of up to five megabytes of blob. A row written before this column existed has none
     * yet; it is computed once, on the first request that needs it, and kept.
     */
    @Transactional
    public String getDataDigest(int id) {
        BabylonMaterialEntity entity = getEntity(id);
        if (entity.getDataDigest() == null) {
            byte[] data = entity.getData();
            if (data == null) {
                return null;
            }
            entity.setDataDigest(ContentDigest.of(data));
            getJpaRepository().save(entity);
        }
        return entity.getDataDigest();
    }

    @Transactional
    public void setData(int id, byte[] data) {
        BabylonMaterialEntity entity = getEntity(id);
        entity.setData(data);
        // In the same transaction as the bytes. A digest written separately could survive a failed
        // write of the material, and every browser holding the old copy would then be told it is
        // current - the one outcome this must never produce.
        entity.setDataDigest(data != null ? ContentDigest.of(data) : null);
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
