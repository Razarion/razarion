package com.btxtech.server.persistence;

import com.btxtech.server.persistence.ui.GltfBabylonMaterial;
import com.btxtech.server.persistence.ui.GltfEntity;
import com.btxtech.server.rest.crud.GltfController;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class GltfCrudPersistence extends AbstractEntityCrudPersistence<GltfEntity> {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private BabylonMaterialCrudPersistence babylonMaterialCrudPersistence;

    public GltfCrudPersistence() {
        super(GltfEntity.class);
    }

    @Transactional
    public byte[] getGlb(int id) {
        return getEntity(id).getGlb();
    }

    @Transactional
    public void setGlb(int id, byte[] glb) {
        GltfEntity entity = getEntity(id);
        entity.setGlb(glb);
        entityManager.merge(entity);
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
                    dbGltfEntity.getGltfBabylonMaterials().add(new GltfBabylonMaterial()
                            .babylonMaterialEntity(babylonMaterialCrudPersistence.getEntity(babylonMaterialId))
                            .gltfMaterialName(gltfMaterialName)
                    ));
        }
        return dbGltfEntity;
    }
}
