package com.btxtech.shared.gameengine;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

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
    private WaterConfig waterConfig;

    public void onGameEngineInit(@Observes StaticGameInitEvent engineInitEvent) {
        init(engineInitEvent.getStaticGameConfig());
    }

    public void init(StaticGameConfig staticGameConfig) {
        waterConfig = staticGameConfig.getWaterConfig();
        groundSkeletonConfig = staticGameConfig.getGroundSkeletonConfig();
        setSlopeSkeletonConfigs(staticGameConfig.getSlopeSkeletonConfigs());
        setTerrainObjectConfigs(staticGameConfig.getTerrainObjectConfigs());
    }

    public void setSlopeSkeletonConfigs(Collection<SlopeSkeletonConfig> slopeSkeletonConfigs) {
        this.slopeSkeletonConfigs.clear();
        if (slopeSkeletonConfigs != null) {
            for (SlopeSkeletonConfig slopeSkeletonConfig : slopeSkeletonConfigs) {
                this.slopeSkeletonConfigs.put(slopeSkeletonConfig.getId(), slopeSkeletonConfig);
            }
        }
    }

    public void setTerrainObjectConfigs(Collection<TerrainObjectConfig> terrainObjectConfigs) {
        this.terrainObjectConfigs.clear();
        if (terrainObjectConfigs != null) {
            for (TerrainObjectConfig terrainObjectConfig : terrainObjectConfigs) {
                this.terrainObjectConfigs.put(terrainObjectConfig.getId(), terrainObjectConfig);
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

    public Collection<SlopeSkeletonConfig> getSlopeSkeletonConfigs() {
        return slopeSkeletonConfigs.values();
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

    public WaterConfig getWaterConfig() {
        return waterConfig;
    }

    // Methods used by the editors -----------------------------------------------------------------
    public void overrideTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        terrainObjectConfigs.put(terrainObjectConfig.getId(), terrainObjectConfig);
    }

    public void deleteTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        terrainObjectConfigs.remove(terrainObjectConfig.getId());
    }

    public void overrideSlopeSkeletonConfig(SlopeSkeletonConfig slopeSkeletonConfig) {
        slopeSkeletonConfigs.put(slopeSkeletonConfig.getId(), slopeSkeletonConfig);
    }

    public void deleteSlopeSkeletonConfig(SlopeSkeletonConfig slopeSkeletonConfig) {
        slopeSkeletonConfigs.remove(slopeSkeletonConfig.getId());
    }
}
