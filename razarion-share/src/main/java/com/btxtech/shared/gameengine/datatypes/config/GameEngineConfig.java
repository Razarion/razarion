package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
public class GameEngineConfig {
    private GroundSkeletonConfig groundSkeletonConfig;
    private List<SlopeSkeletonConfig> slopeSkeletonConfigs;
    private List<TerrainObject> terrainObjects;
    private List<ItemType> itemTypes;
    private PlanetConfig planetConfig;

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

    public List<TerrainObject> getTerrainObjects() {
        return terrainObjects;
    }

    public GameEngineConfig setTerrainObjects(List<TerrainObject> terrainObjects) {
        this.terrainObjects = terrainObjects;
        return this;
    }

    public List<ItemType> getItemTypes() {
        return itemTypes;
    }

    public GameEngineConfig setItemTypes(List<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
        return this;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public GameEngineConfig setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
        return this;
    }
}
