package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.GltfBabylonMaterialEntity;
import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.repository.ui.GltfRepository;
import com.btxtech.server.rest.ui.GltfController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ContentDigest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GltfService extends AbstractBaseEntityCrudService<GltfEntity> {
    private final BabylonMaterialService babylonMaterialService;

    public GltfService(GltfRepository gltfRepository, BabylonMaterialService babylonMaterialService) {
        super(GltfEntity.class, gltfRepository);
        this.babylonMaterialService = babylonMaterialService;
    }

    @Transactional
    public byte[] getGlb(int id) {
        return getEntity(id).getGlb();
    }

    /**
     * The entity tag for one model, or null if there is no model to tag.
     * <p>
     * Reads the digest column and nothing else, so answering "still the same?" costs a small row
     * instead of eleven megabytes of blob. A row written before this column existed has none yet;
     * it is computed once, on the first request that needs it, and kept.
     */
    @Transactional
    public String getGlbDigest(int id) {
        GltfEntity entity = getEntity(id);
        if (entity.getGlbDigest() == null) {
            byte[] glb = entity.getGlb();
            if (glb == null) {
                return null;
            }
            entity.setGlbDigest(ContentDigest.of(glb));
            getJpaRepository().save(entity);
        }
        return entity.getGlbDigest();
    }

    @Transactional
    public void setGlb(int id, byte[] glb) {
        GltfEntity entity = getEntity(id);
        entity.setGlb(glb);
        // In the same transaction as the bytes. A digest written separately could survive a failed
        // write of the model, and every browser holding the old file would then be told it is
        // current - the one outcome this must never produce.
        entity.setGlbDigest(glb != null ? ContentDigest.of(glb) : null);
        getJpaRepository().save(entity);
    }


    @Transactional
    public List<GltfEntity> readAllBaseEntitiesJson() {
        return getEntities()
                .stream()
                .map(GltfController::jpa2JsonStatic)
                .collect(Collectors.toList());
    }

    @Override
    protected GltfEntity jsonToJpa(GltfEntity gltfEntity) {
        GltfEntity dbGltfEntity = getEntity(gltfEntity.getId());
        dbGltfEntity.getGltfBabylonMaterials().clear();
        dbGltfEntity.setInternalName(gltfEntity.getInternalName());
        if (gltfEntity.getMaterialGltfNames() != null) {
            gltfEntity.getMaterialGltfNames().forEach((gltfMaterialName, babylonMaterialId) ->
                    dbGltfEntity.getGltfBabylonMaterials().add(new GltfBabylonMaterialEntity()
                            .babylonMaterialEntity(babylonMaterialService.getEntity(babylonMaterialId))
                            .gltfMaterialName(gltfMaterialName)
                    ));
        }
        return dbGltfEntity;
    }
}
