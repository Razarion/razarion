package com.btxtech.server.persistence;

import com.btxtech.server.persistence.inventory.InventoryItemCrudPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import javax.inject.Singleton;
import javax.inject.Inject;

/**
 * Created by Beat
 * 18.04.2017.
 */
@Singleton
public class StaticGameConfigPersistence {
    @Inject
    private TerrainObjectCrudPersistence terrainObjectCrudPersistence;
    @Inject
    private GroundCrudPersistence groundCrudPersistence;
    @Inject
    private SlopeCrudPersistence slopeCrudPersistence;
    @Inject
    private DrivewayCrudPersistence drivewayCrudPersistence;
    @Inject
    private WaterCrudPersistence waterCrudPersistence;
    @Inject
    private BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    @Inject
    private ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence;
    @Inject
    private BoxItemTypeCrudPersistence boxItemTypeCrudPersistence;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private InventoryItemCrudPersistence inventoryItemCrudPersistence;
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;
    @Inject
    private ThreeJsModelPackCrudPersistence threeJsModelPackCrudPersistence;
    @Inject
    private ParticleSystemCrudPersistence particleSystemCrudPersistence;

    public StaticGameConfig loadStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setSlopeConfigs(slopeCrudPersistence.read());
        staticGameConfig.setDrivewayConfigs(drivewayCrudPersistence.read());
        staticGameConfig.setGroundConfigs(groundCrudPersistence.read());
        staticGameConfig.setTerrainObjectConfigs(terrainObjectCrudPersistence.read());
        staticGameConfig.setWaterConfigs(waterCrudPersistence.read());
        staticGameConfig.setBaseItemTypes(baseItemTypeCrudPersistence.read());
        staticGameConfig.setResourceItemTypes(resourceItemTypeCrudPersistence.read());
        staticGameConfig.setBoxItemTypes(boxItemTypeCrudPersistence.read());
        staticGameConfig.setLevelConfigs(levelCrudPersistence.readLevelConfigs());
        staticGameConfig.setInventoryItems(inventoryItemCrudPersistence.read());
        staticGameConfig.setThreeJsModelConfigs(threeJsModelCrudPersistence.read());
        staticGameConfig.setThreeJsModelPackConfigs(threeJsModelPackCrudPersistence.read());
        staticGameConfig.setParticleSystemConfigs(particleSystemCrudPersistence.read());
        return staticGameConfig;
    }
}
