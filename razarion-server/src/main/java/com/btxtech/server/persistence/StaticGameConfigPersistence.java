package com.btxtech.server.persistence;

import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 18.04.2017.
 */
@ApplicationScoped
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
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @Inject
    private InventoryPersistence inventoryPersistence;
    @Inject
    private ThreeJsModelCrudPersistence threeJsModelCrudPersistence;

    public StaticGameConfig loadStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setSlopeConfigs(slopeCrudPersistence.read());
        staticGameConfig.setDrivewayConfigs(drivewayCrudPersistence.read());
        staticGameConfig.setGroundConfigs(groundCrudPersistence.read());
        staticGameConfig.setTerrainObjectConfigs(terrainObjectCrudPersistence.read());
        staticGameConfig.setWaterConfigs(waterCrudPersistence.read());
        staticGameConfig.setBaseItemTypes(baseItemTypeCrudPersistence.read());
        staticGameConfig.setResourceItemTypes(resourceItemTypeCrudPersistence.read());
        staticGameConfig.setBoxItemTypes(itemTypePersistence.readBoxItemTypes());
        staticGameConfig.setLevelConfigs(levelCrudPersistence.read());
        staticGameConfig.setInventoryItems(inventoryPersistence.readInventoryItems());
        staticGameConfig.setThreeJsModelConfigs(threeJsModelCrudPersistence.read());
        return staticGameConfig;
    }
}
