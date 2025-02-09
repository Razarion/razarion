package com.btxtech.server.persistence;

import com.btxtech.server.persistence.inventory.InventoryItemCrudPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    public StaticGameConfig loadStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setGroundConfigs(groundCrudPersistence.read());
        staticGameConfig.setTerrainObjectConfigs(terrainObjectCrudPersistence.read());
        staticGameConfig.setBaseItemTypes(baseItemTypeCrudPersistence.read());
        staticGameConfig.setResourceItemTypes(resourceItemTypeCrudPersistence.read());
        staticGameConfig.setBoxItemTypes(boxItemTypeCrudPersistence.read());
        staticGameConfig.setLevelConfigs(levelCrudPersistence.readLevelConfigs());
        staticGameConfig.setInventoryItems(inventoryItemCrudPersistence.read());
        staticGameConfig.setThreeJsModelConfigs(threeJsModelCrudPersistence.read());
        return staticGameConfig;
    }
}
