package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity_;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;

import javax.inject.Singleton;

@Singleton
public class GroundCrudPersistence extends CrudPersistence<GroundSkeletonConfig, GroundConfigEntity> {
    public GroundCrudPersistence() {
        super(GroundConfigEntity.class, GroundConfigEntity_.id, GroundConfigEntity_.internalName, GroundSkeletonConfig::getId);
    }

    @Override
    protected GroundSkeletonConfig toConfig(GroundConfigEntity entity) {
        return entity.generateGroundSkeleton();
    }

    @Override
    protected void fromConfig(GroundSkeletonConfig config, GroundConfigEntity entity) {
        entity.fromGroundConfig(new GroundConfig(), null);
    }
}
