package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;

import javax.inject.Singleton;

@Singleton
public class ThreeJsModelCrudPersistence extends AbstractCrudPersistence<ThreeJsModelConfig, ThreeJsModelConfigEntity> {
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
}
