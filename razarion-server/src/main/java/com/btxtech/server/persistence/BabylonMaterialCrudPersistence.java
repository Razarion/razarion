package com.btxtech.server.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

public class BabylonMaterialCrudPersistence extends AbstractEntityCrudPersistence<BabylonMaterialEntity> {
    @PersistenceContext
    private EntityManager entityManager;

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
        entityManager.merge(entity);
    }

}
