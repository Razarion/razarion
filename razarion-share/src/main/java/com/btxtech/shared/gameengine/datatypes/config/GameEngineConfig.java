package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
@Portable
public class GameEngineConfig {
    private GroundSkeletonConfig groundSkeletonConfig;
    private List<SlopeSkeletonConfig> slopeSkeletonConfigs;
    private List<TerrainObjectConfig> terrainObjectConfigs;
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

    public List<TerrainObjectConfig> getTerrainObjectConfigs() {
        return terrainObjectConfigs;
    }

    public GameEngineConfig setTerrainObjectConfigs(List<TerrainObjectConfig> terrainObjectConfigs) {
        this.terrainObjectConfigs = terrainObjectConfigs;
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
