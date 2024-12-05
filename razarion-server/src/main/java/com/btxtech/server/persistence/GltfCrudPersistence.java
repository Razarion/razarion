package com.btxtech.server.persistence;

import com.btxtech.server.persistence.ui.GltfEntity;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Singleton
public class GltfCrudPersistence extends AbstractEntityCrudPersistence<GltfEntity> {
    @PersistenceContext
    private EntityManager entityManager;

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

}
