package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.GltfBabylonMaterialEntity;
import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.repository.ui.GltfRepository;
import com.btxtech.server.rest.ui.GltfController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
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

    @Transactional
    public void setGlb(int id, byte[] glb) {
        GltfEntity entity = getEntity(id);
        entity.setGlb(glb);
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
