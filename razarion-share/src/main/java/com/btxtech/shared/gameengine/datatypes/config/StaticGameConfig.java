package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import org.dominokit.jackson.annotation.JSONMapper;
import org.teavm.flavour.json.JsonPersistable;

import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
@JSONMapper
@JsonPersistable
public class StaticGameConfig {
    private List<GroundConfig> groundConfigs;
    private List<TerrainObjectConfig> terrainObjectConfigs;
    private List<BaseItemType> baseItemTypes;
    private List<ResourceItemType> resourceItemTypes;
    private List<BoxItemType> boxItemTypes;
    private List<LevelConfig> levelConfigs;
    private List<InventoryItem> inventoryItems;

    public List<GroundConfig> getGroundConfigs() {
        return groundConfigs;
    }

    public void setGroundConfigs(List<GroundConfig> groundConfigs) {
        this.groundConfigs = groundConfigs;
    }

    public List<TerrainObjectConfig> getTerrainObjectConfigs() {
        return terrainObjectConfigs;
    }

    public void setTerrainObjectConfigs(List<TerrainObjectConfig> terrainObjectConfigs) {
        this.terrainObjectConfigs = terrainObjectConfigs;
    }

    public List<BaseItemType> getBaseItemTypes() {
        return baseItemTypes;
    }

    public void setBaseItemTypes(List<BaseItemType> baseItemTypes) {
        this.baseItemTypes = baseItemTypes;
    }

    public List<ResourceItemType> getResourceItemTypes() {
        return resourceItemTypes;
    }

    public void setResourceItemTypes(List<ResourceItemType> resourceItemTypes) {
        this.resourceItemTypes = resourceItemTypes;
    }

    public List<LevelConfig> getLevelConfigs() {
        return levelConfigs;
    }

    public void setLevelConfigs(List<LevelConfig> levelConfigs) {
        this.levelConfigs = levelConfigs;
    }

    public List<BoxItemType> getBoxItemTypes() {
        return boxItemTypes;
    }

    public void setBoxItemTypes(List<BoxItemType> boxItemTypes) {
        this.boxItemTypes = boxItemTypes;
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    public StaticGameConfig groundConfigs(List<GroundConfig> groundConfigs) {
        setGroundConfigs(groundConfigs);
        return this;
    }

    public StaticGameConfig terrainObjectConfigs(List<TerrainObjectConfig> terrainObjectConfigs) {
        setTerrainObjectConfigs(terrainObjectConfigs);
        return this;
    }

    public StaticGameConfig baseItemTypes(List<BaseItemType> baseItemTypes) {
        setBaseItemTypes(baseItemTypes);
        return this;
    }

    public StaticGameConfig resourceItemTypes(List<ResourceItemType> resourceItemTypes) {
        setResourceItemTypes(resourceItemTypes);
        return this;
    }

    public StaticGameConfig boxItemTypes(List<BoxItemType> boxItemTypes) {
        setBoxItemTypes(boxItemTypes);
        return this;
    }

    public StaticGameConfig levelConfigs(List<LevelConfig> levelConfigs) {
        setLevelConfigs(levelConfigs);
        return this;
    }

    public StaticGameConfig inventoryItems(List<InventoryItem> inventoryItems) {
        setInventoryItems(inventoryItems);
        return this;
    }
}
