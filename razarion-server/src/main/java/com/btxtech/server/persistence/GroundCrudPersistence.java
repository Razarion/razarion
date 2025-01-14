package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity_;
import com.btxtech.shared.dto.GroundConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GroundCrudPersistence extends AbstractConfigCrudPersistence<GroundConfig, GroundConfigEntity> {
    @Inject
    private BabylonMaterialCrudPersistence babylonMaterialCrudPersistence;

    public GroundCrudPersistence() {
        super(GroundConfigEntity.class, GroundConfigEntity_.id, GroundConfigEntity_.internalName);
    }

    @Override
    protected GroundConfig toConfig(GroundConfigEntity entity) {
        return entity.toConfig();
    }

    @Override
    protected void fromConfig(GroundConfig config, GroundConfigEntity entity) {
        entity.fromGroundConfig(config, babylonMaterialCrudPersistence);
    }
}
