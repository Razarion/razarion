package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class ThreeJsModelPackCrudPersistence extends AbstractCrudPersistence<ThreeJsModelPackConfig, ThreeJsModelPackConfigEntity> {
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;

    public ThreeJsModelPackCrudPersistence() {
        super(ThreeJsModelPackConfigEntity.class, ThreeJsModelPackConfigEntity_.id, ThreeJsModelPackConfigEntity_.internalName);
    }

    @Override
    protected ThreeJsModelPackConfig toConfig(ThreeJsModelPackConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(ThreeJsModelPackConfig config, ThreeJsModelPackConfigEntity entity) {
        entity.from(config, threeJsModelCrudPersistence);
    }
}
