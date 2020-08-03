package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.DrivewayConfigEntity;
import com.btxtech.server.persistence.surface.DrivewayConfigEntity_;
import com.btxtech.shared.dto.DrivewayConfig;

public class DrivewayCrudPersistence extends AbstractCrudPersistence<DrivewayConfig, DrivewayConfigEntity> {
    public DrivewayCrudPersistence() {
        super(DrivewayConfigEntity.class, DrivewayConfigEntity_.id, DrivewayConfigEntity_.internalName);
    }

    @Override
    protected DrivewayConfig toConfig(DrivewayConfigEntity entity) {
        return entity.toDrivewayConfig();
    }

    @Override
    protected void fromConfig(DrivewayConfig config, DrivewayConfigEntity entity) {
        entity.fromDrivewayConfig(config);
    }
}
