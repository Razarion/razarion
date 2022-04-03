package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Singleton
public class ThreeJsModelCrudPersistence extends AbstractCrudPersistence<ThreeJsModelConfig, ThreeJsModelConfigEntity> {
    @PersistenceContext
    private EntityManager entityManager;

    public ThreeJsModelCrudPersistence() {
        super(ThreeJsModelConfigEntity.class, ThreeJsModelConfigEntity_.id, ThreeJsModelConfigEntity_.internalName);
    }

    @Override
    protected ThreeJsModelConfig toConfig(ThreeJsModelConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(ThreeJsModelConfig config, ThreeJsModelConfigEntity entity) {
        entity.from(config);
    }

    @Transactional
    public void saveData(int id, byte[] bytes) {
        ThreeJsModelConfigEntity threeJsModelConfig = getEntity(id);
        threeJsModelConfig.setData(bytes);
        entityManager.merge(threeJsModelConfig);
    }

    @Transactional
    public byte[] getThreeJsModel(int id) {
        return getEntity(id).getData();
    }
}
