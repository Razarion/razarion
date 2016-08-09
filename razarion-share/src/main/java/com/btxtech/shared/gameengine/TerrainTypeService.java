package com.btxtech.shared.gameengine;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 19.07.2016.
 */
@Singleton
public class TerrainTypeService {
    private Map<Integer, SlopeSkeletonConfig> slopeSkeletonConfigs = new HashMap<>();
    private GroundSkeletonConfig groundSkeletonConfig;
    private Map<Integer, TerrainObjectConfig> terrainObjectConfigs = new HashMap<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        groundSkeletonConfig = engineInitEvent.getGameEngineConfig().getGroundSkeletonConfig();

        slopeSkeletonConfigs.clear();
        if (engineInitEvent.getGameEngineConfig().getSlopeSkeletonConfigs() != null) {
            for (SlopeSkeletonConfig slopeSkeletonConfig : engineInitEvent.getGameEngineConfig().getSlopeSkeletonConfigs()) {
                slopeSkeletonConfigs.put(slopeSkeletonConfig.getId(), slopeSkeletonConfig);
            }
        }

        terrainObjectConfigs.clear();
        if (engineInitEvent.getGameEngineConfig().getTerrainObjectConfigs() != null) {
            for (TerrainObjectConfig terrainObjectConfig : engineInitEvent.getGameEngineConfig().getTerrainObjectConfigs()) {
                terrainObjectConfigs.put(terrainObjectConfig.getId(), terrainObjectConfig);
            }
        }
    }

    public GroundSkeletonConfig getGroundSkeletonConfig() {
        return groundSkeletonConfig;
    }

    public SlopeSkeletonConfig getSlopeSkeleton(int id) {
        SlopeSkeletonConfig slopeSkeletonConfig = slopeSkeletonConfigs.get(id);
        if (slopeSkeletonConfig == null) {
            throw new IllegalArgumentException("No entry in integerSlopeSkeletonMap for id: " + id);
        }
        return slopeSkeletonConfig;
    }

    public Collection<TerrainObjectConfig> getTerrainObjectConfigs() {
        return terrainObjectConfigs.values();
    }

    public TerrainObjectConfig getTerrainObjectConfig(int id) {
        TerrainObjectConfig terrainObjectConfig = terrainObjectConfigs.get(id);
        if (terrainObjectConfig == null) {
            throw new IllegalArgumentException("No TerrainObjectConfig for id: " + id);
        }
        return terrainObjectConfig;
    }

}
