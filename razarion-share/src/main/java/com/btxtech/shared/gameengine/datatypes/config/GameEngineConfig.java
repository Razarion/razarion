package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
public class GameEngineConfig {
    private GroundSkeletonConfig groundSkeletonConfig;
    private List<SlopeSkeletonConfig> slopeSkeletonConfigs;
    private List<TerrainObjectConfig> terrainObjectConfigs;
    private List<BaseItemType> baseItemTypes;
    private List<ResourceItemType> resourceItemTypes;
    private List<BoxItemType> boxItemTypes;
    private List<LevelConfig> levelConfigs;
    private PlanetConfig planetConfig;
    private List<InventoryItem> inventoryItems;

    public GroundSkeletonConfig getGroundSkeletonConfig() {
        return groundSkeletonConfig;
    }

    public GameEngineConfig setGroundSkeletonConfig(GroundSkeletonConfig groundSkeletonConfig) {
        this.groundSkeletonConfig = groundSkeletonConfig;
        return this;
    }

    public List<SlopeSkeletonConfig> getSlopeSkeletonConfigs() {
        return slopeSkeletonConfigs;
    }

    public GameEngineConfig setSlopeSkeletonConfigs(List<SlopeSkeletonConfig> slopeSkeletonConfigs) {
        this.slopeSkeletonConfigs = slopeSkeletonConfigs;
        return this;
    }

    public List<TerrainObjectConfig> getTerrainObjectConfigs() {
        return terrainObjectConfigs;
    }

    public GameEngineConfig setTerrainObjectConfigs(List<TerrainObjectConfig> terrainObjectConfigs) {
        this.terrainObjectConfigs = terrainObjectConfigs;
        return this;
    }

    public List<BaseItemType> getBaseItemTypes() {
        return baseItemTypes;
    }

    public GameEngineConfig setBaseItemTypes(List<BaseItemType> baseItemTypes) {
        this.baseItemTypes = baseItemTypes;
        return this;
    }

    public List<ResourceItemType> getResourceItemTypes() {
        return resourceItemTypes;
    }

    public GameEngineConfig setResourceItemTypes(List<ResourceItemType> resourceItemTypes) {
        this.resourceItemTypes = resourceItemTypes;
        return this;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public GameEngineConfig setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
        return this;
    }

    public List<LevelConfig> getLevelConfigs() {
        return levelConfigs;
    }

    public GameEngineConfig setLevelConfigs(List<LevelConfig> levelConfigs) {
        this.levelConfigs = levelConfigs;
        return this;
    }

    public List<BoxItemType> getBoxItemTypes() {
        return boxItemTypes;
    }

    public GameEngineConfig setBoxItemTypes(List<BoxItemType> boxItemTypes) {
        this.boxItemTypes = boxItemTypes;
        return this;
   }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public GameEngineConfig setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
        return this;
    }
}
