package com.btxtech.server.persistence;

import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 18.04.2017.
 */
@ApplicationScoped
public class StaticGameConfigPersistence {
    @Deprecated
    // GameUiControlEntity has minimal level. Should be handled with that
    public static final int MULTI_PLAYER_PLANET_LEVEL_ID = 5;
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
        staticGameConfig.setWaterLevel(-0.7); // TODO move to DB
        staticGameConfig.setBaseItemTypes(itemTypePersistence.readBaseItemTypes());
        staticGameConfig.setResourceItemTypes(itemTypePersistence.readResourceItemTypes());
        staticGameConfig.setBoxItemTypes(itemTypePersistence.readBoxItemTypes());
        staticGameConfig.setLevelConfigs(levelPersistence.read());
        staticGameConfig.setInventoryItems(inventoryPersistence.readInventoryItems()); // TODO move to DB
        return staticGameConfig;
    }
}
