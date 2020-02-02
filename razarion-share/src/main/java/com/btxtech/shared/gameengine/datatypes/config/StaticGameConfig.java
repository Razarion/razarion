package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.WaterConfig;
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
    private GroundConfig groundConfig;
    @Deprecated
    private WaterConfig waterConfig;
    private List<SlopeConfig> slopeConfigs;
    private List<TerrainObjectConfig> terrainObjectConfigs;
    private List<BaseItemType> baseItemTypes;
    private List<ResourceItemType> resourceItemTypes;
    private List<BoxItemType> boxItemTypes;
    private List<LevelConfig> levelConfigs;
    private List<InventoryItem> inventoryItems;
    private List<DrivewayConfig> drivewayConfigs;

    public GroundConfig getGroundConfig() {
        return groundConfig;
    }

    public StaticGameConfig setGroundConfig(GroundConfig groundConfig) {
        this.groundConfig = groundConfig;
        return this;
    }

    @Deprecated
    public WaterConfig getWaterConfig() {
        return waterConfig;
    }

    @Deprecated
    public void setWaterConfig(WaterConfig waterConfig) {
        this.waterConfig = waterConfig;
    }

    public List<SlopeConfig> getSlopeConfigs() {
        return slopeConfigs;
    }

    public StaticGameConfig setSlopeConfigs(List<SlopeConfig> slopeConfigs) {
        this.slopeConfigs = slopeConfigs;
        return this;
    }

    public List<TerrainObjectConfig> getTerrainObjectConfigs() {
        return terrainObjectConfigs;
    }

    public StaticGameConfig setTerrainObjectConfigs(List<TerrainObjectConfig> terrainObjectConfigs) {
        this.terrainObjectConfigs = terrainObjectConfigs;
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

    public List<DrivewayConfig> getDrivewayConfigs() {
        return drivewayConfigs;
    }

    public StaticGameConfig setDrivewayConfigs(List<DrivewayConfig> drivewayConfigs) {
        this.drivewayConfigs = drivewayConfigs;
        return this;
    }
}
