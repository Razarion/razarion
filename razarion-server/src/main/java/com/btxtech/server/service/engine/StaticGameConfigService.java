package com.btxtech.server.service.engine;

import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import org.springframework.stereotype.Service;

@Service
public class StaticGameConfigService {
    private final TerrainObjectService terrainObjectService;
    private final GroundCrudPersistence groundCrudPersistence;
    private final BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    private final ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence;
    private final BoxItemTypeCrudPersistence boxItemTypeCrudPersistence;
    private final LevelCrudPersistence levelCrudPersistence;
    private final InventoryItemCrudPersistence inventoryItemCrudPersistence;

    public StaticGameConfigService(TerrainObjectService terrainObjectService,
                                   GroundCrudPersistence groundCrudPersistence,
                                   BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                                   ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence,
                                   BoxItemTypeCrudPersistence boxItemTypeCrudPersistence,
                                   LevelCrudPersistence levelCrudPersistence,
                                   InventoryItemCrudPersistence inventoryItemCrudPersistence) {
        this.terrainObjectService = terrainObjectService;
        this.groundCrudPersistence = groundCrudPersistence;
        this.baseItemTypeCrudPersistence = baseItemTypeCrudPersistence;
        this.resourceItemTypeCrudPersistence = resourceItemTypeCrudPersistence;
        this.boxItemTypeCrudPersistence = boxItemTypeCrudPersistence;
        this.levelCrudPersistence = levelCrudPersistence;
        this.inventoryItemCrudPersistence = inventoryItemCrudPersistence;
    }

    public StaticGameConfig loadStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setGroundConfigs(groundCrudPersistence.read());
        staticGameConfig.setTerrainObjectConfigs(terrainObjectService.read());
        staticGameConfig.setBaseItemTypes(baseItemTypeCrudPersistence.read());
        staticGameConfig.setResourceItemTypes(resourceItemTypeCrudPersistence.read());
        staticGameConfig.setBoxItemTypes(boxItemTypeCrudPersistence.read());
        staticGameConfig.setLevelConfigs(levelCrudPersistence.read());
        staticGameConfig.setInventoryItems(inventoryItemCrudPersistence.read());
        return staticGameConfig;
    }
}
