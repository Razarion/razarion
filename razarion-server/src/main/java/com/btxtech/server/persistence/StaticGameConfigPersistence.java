package com.btxtech.server.persistence;

import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelPersistence;
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
    private TerrainElementPersistence terrainElementPersistence;
    @Inject
    private GroundCrudPersistence groundCrudPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private InventoryPersistence inventoryPersistence;

    public StaticGameConfig loadStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setSlopeConfigs(terrainElementPersistence.loadSlopeSkeletons());
        staticGameConfig.setDrivewayConfigs(terrainElementPersistence.loadDrivewayConfigs());
        staticGameConfig.setGroundSkeletonConfig(groundCrudPersistence.getDefaultGround());
        staticGameConfig.setTerrainObjectConfigs(terrainElementPersistence.readTerrainObjects());
        staticGameConfig.setWaterConfig(terrainElementPersistence.readWaterConfig());
        staticGameConfig.setBaseItemTypes(itemTypePersistence.readBaseItemTypes());
        staticGameConfig.setResourceItemTypes(itemTypePersistence.readResourceItemTypes());
        staticGameConfig.setBoxItemTypes(itemTypePersistence.readBoxItemTypes());
        staticGameConfig.setLevelConfigs(levelPersistence.read());
        staticGameConfig.setInventoryItems(inventoryPersistence.readInventoryItems());
        return staticGameConfig;
    }
}
