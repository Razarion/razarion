package com.btxtech.server.service.engine;

import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import org.springframework.stereotype.Service;

@Service
public class StaticGameConfigService {
    private final TerrainObjectService terrainObjectService;
    private final GroundCrudService groundCrudPersistence;
    private final BaseItemTypeService baseItemTypeCrudPersistence;
    private final ResourceItemTypeService resourceItemTypeCrudPersistence;
    private final BoxItemTypeCrudService boxItemTypeCrudPersistence;
    private final LevelCrudService levelCrudPersistence;
    private final InventoryItemService inventoryItemCrudPersistence;
    private final InventoryArtifactService inventoryArtifactCrudPersistence;

    public StaticGameConfigService(TerrainObjectService terrainObjectService,
                                   GroundCrudService groundCrudPersistence,
                                   BaseItemTypeService baseItemTypeCrudPersistence,
                                   ResourceItemTypeService resourceItemTypeCrudPersistence,
                                   BoxItemTypeCrudService boxItemTypeCrudPersistence,
                                   LevelCrudService levelCrudPersistence,
                                   InventoryItemService inventoryItemCrudPersistence,
                                   InventoryArtifactService inventoryArtifactCrudPersistence) {
        this.terrainObjectService = terrainObjectService;
        this.groundCrudPersistence = groundCrudPersistence;
        this.baseItemTypeCrudPersistence = baseItemTypeCrudPersistence;
        this.resourceItemTypeCrudPersistence = resourceItemTypeCrudPersistence;
        this.boxItemTypeCrudPersistence = boxItemTypeCrudPersistence;
        this.levelCrudPersistence = levelCrudPersistence;
        this.inventoryItemCrudPersistence = inventoryItemCrudPersistence;
        this.inventoryArtifactCrudPersistence = inventoryArtifactCrudPersistence;
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
        staticGameConfig.setInventoryArtifacts(inventoryArtifactCrudPersistence.read());
        return staticGameConfig;
    }
}
