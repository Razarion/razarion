package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.WaterConfigEntity;
import com.btxtech.server.persistence.surface.WaterConfigEntity_;
import com.btxtech.shared.dto.WaterConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WaterCrudPersistence extends AbstractCrudPersistence<WaterConfig, WaterConfigEntity> {
    @Inject
    private ImagePersistence imagePersistence;

    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;

    public WaterCrudPersistence() {
        super(WaterConfigEntity.class, WaterConfigEntity_.id, WaterConfigEntity_.internalName);
    }

    @Override
    protected WaterConfig toConfig(WaterConfigEntity entity) {
        return entity.toWaterConfig();
    }

    @Override
    protected void fromConfig(WaterConfig waterConfig, WaterConfigEntity waterConfigEntity) {
        waterConfigEntity.fromWaterConfig(waterConfig, imagePersistence, threeJsModelCrudPersistence);
    }
}
