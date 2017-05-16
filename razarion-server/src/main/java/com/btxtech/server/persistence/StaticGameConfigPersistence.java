package com.btxtech.server.persistence;

import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * 18.04.2017.
 */
@ApplicationScoped
public class StaticGameConfigPersistence {
    static final int BASE_ITEM_TYPE_BULLDOZER = 180807;
    static final int BASE_ITEM_TYPE_HARVESTER = 180830;
    static final int BASE_ITEM_TYPE_ATTACKER = 180832;
    static final int BASE_ITEM_TYPE_FACTORY = 272490;
    static final int BASE_ITEM_TYPE_TOWER = 272495;
    static final int RESOURCE_ITEM_TYPE = 180829;
    static final int BOX_ITEM_TYPE = 272481;
    static final int INVENTORY_ITEM = 1;
    @Inject
    private TerrainElementPersistence terrainElementPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private InventoryPersistence inventoryPersistence;

    @Transactional
    public StaticGameConfig loadStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setSlopeSkeletonConfigs(terrainElementPersistence.loadSlopeSkeletons());
        staticGameConfig.setGroundSkeletonConfig(terrainElementPersistence.loadGroundSkeleton());
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
