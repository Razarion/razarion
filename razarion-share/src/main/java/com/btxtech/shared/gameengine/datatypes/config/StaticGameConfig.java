package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
public class StaticGameConfig {
    private GroundSkeletonConfig groundSkeletonConfig;
    private List<SlopeSkeletonConfig> slopeSkeletonConfigs;
    private List<TerrainObjectConfig> terrainObjectConfigs;
    private double waterLevel;
    private List<BaseItemType> baseItemTypes;
    private List<ResourceItemType> resourceItemTypes;
    private List<BoxItemType> boxItemTypes;
    private List<LevelConfig> levelConfigs;
    private List<InventoryItem> inventoryItems;

    public GroundSkeletonConfig getGroundSkeletonConfig() {
        return groundSkeletonConfig;
    }

    public StaticGameConfig setGroundSkeletonConfig(GroundSkeletonConfig groundSkeletonConfig) {
        this.groundSkeletonConfig = groundSkeletonConfig;
        return this;
    }

    public List<SlopeSkeletonConfig> getSlopeSkeletonConfigs() {
        return slopeSkeletonConfigs;
    }

    public StaticGameConfig setSlopeSkeletonConfigs(List<SlopeSkeletonConfig> slopeSkeletonConfigs) {
        this.slopeSkeletonConfigs = slopeSkeletonConfigs;
        return this;
    }

    public List<TerrainObjectConfig> getTerrainObjectConfigs() {
        return terrainObjectConfigs;
    }

    public StaticGameConfig setTerrainObjectConfigs(List<TerrainObjectConfig> terrainObjectConfigs) {
        this.terrainObjectConfigs = terrainObjectConfigs;
        return this;
    }

    public double getWaterLevel() {
        return waterLevel;
    }

    public StaticGameConfig setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
        return this;
    }

    public List<BaseItemType> getBaseItemTypes() {
        return baseItemTypes;
    }

    public StaticGameConfig setBaseItemTypes(List<BaseItemType> baseItemTypes) {
        this.baseItemTypes = baseItemTypes;
        return this;
    }

    public List<ResourceItemType> getResourceItemTypes() {
        return resourceItemTypes;
    }

    public StaticGameConfig setResourceItemTypes(List<ResourceItemType> resourceItemTypes) {
        this.resourceItemTypes = resourceItemTypes;
        return this;
    }

    public List<LevelConfig> getLevelConfigs() {
        return levelConfigs;
    }

    public StaticGameConfig setLevelConfigs(List<LevelConfig> levelConfigs) {
        this.levelConfigs = levelConfigs;
        return this;
    }

    public List<BoxItemType> getBoxItemTypes() {
        return boxItemTypes;
    }

    public StaticGameConfig setBoxItemTypes(List<BoxItemType> boxItemTypes) {
        this.boxItemTypes = boxItemTypes;
        return this;
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public StaticGameConfig setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
        return this;
    }
}