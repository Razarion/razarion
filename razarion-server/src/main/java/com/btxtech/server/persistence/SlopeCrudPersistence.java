package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity_;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SlopeCrudPersistence extends AbstractCrudPersistence<SlopeConfig, SlopeConfigEntity> {
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private GroundCrudPersistence groundCrudPersistence;
    @Inject
    private WaterCrudPersistence waterCrudPersistence;
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;

    public SlopeCrudPersistence() {
        super(SlopeConfigEntity.class, SlopeConfigEntity_.id, SlopeConfigEntity_.internalName);
    }

    @Override
    protected SlopeConfig toConfig(SlopeConfigEntity entity) {
        return entity.toSlopeConfig();
    }

    @Override
    protected void fromConfig(SlopeConfig config, SlopeConfigEntity entity) {
        entity.fromSlopeConfig(config,
                imagePersistence,
                groundCrudPersistence.getEntity(config.getGroundConfigId()),
                waterCrudPersistence.getEntity(config.getWaterConfigId()),
                threeJsModelCrudPersistence);
    }
}
