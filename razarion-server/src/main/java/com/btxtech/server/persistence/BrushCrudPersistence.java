package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.BrushConfig;

import javax.inject.Singleton;

@Singleton
public class BrushCrudPersistence extends AbstractConfigCrudPersistence<BrushConfig, BrushConfigEntity> {
    public BrushCrudPersistence() {
        super(BrushConfigEntity.class, BrushConfigEntity_.id, BrushConfigEntity_.internalName);
    }

    @Override
    protected BrushConfig toConfig(BrushConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(BrushConfig config, BrushConfigEntity entity) {
        entity.fromConfig(config);
    }
}
