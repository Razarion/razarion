package com.btxtech.shared.gameengine;

import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
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
    private Map<Integer, SlopeConfig> slopeSkeletonConfigs = new HashMap<>();
    private GroundSkeletonConfig groundSkeletonConfig;
    private Map<Integer, TerrainObjectConfig> terrainObjectConfigs = new HashMap<>();
    private Map<Integer, DrivewayConfig> drivewayConfigs = new HashMap<>();
    private WaterConfig waterConfig;

    public void onGameEngineInit(@Observes StaticGameInitEvent engineInitEvent) {
        init(engineInitEvent.getStaticGameConfig());
    }

    public void init(StaticGameConfig staticGameConfig) {
        waterConfig = staticGameConfig.getWaterConfig();
        groundSkeletonConfig = staticGameConfig.getGroundSkeletonConfig();
        setSlopeSkeletonConfigs(staticGameConfig.getSlopeConfigs());
        setTerrainObjectConfigs(staticGameConfig.getTerrainObjectConfigs());
        setDrivewayConfigs(staticGameConfig.getDrivewayConfigs());
    }

    public void setSlopeSkeletonConfigs(Collection<SlopeConfig> slopeConfigs) {
        this.slopeSkeletonConfigs.clear();
        if (slopeConfigs != null) {
            for (SlopeConfig slopeConfig : slopeConfigs) {
                this.slopeSkeletonConfigs.put(slopeConfig.getId(), slopeConfig);
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

    public void setDrivewayConfigs(Collection<DrivewayConfig> drivewayConfigs) {
        this.drivewayConfigs.clear();
        if (drivewayConfigs != null) {
            for (DrivewayConfig drivewayConfig : drivewayConfigs) {
                this.drivewayConfigs.put(drivewayConfig.getId(), drivewayConfig);
            }
        }
    }

    public GroundSkeletonConfig getGroundSkeletonConfig() {
        return groundSkeletonConfig;
    }

    public void setGroundSkeletonConfig(GroundSkeletonConfig groundSkeletonConfig) {
        this.groundSkeletonConfig = groundSkeletonConfig;
    }

    public SlopeConfig getSlopeSkeleton(int id) {
        SlopeConfig slopeConfig = slopeSkeletonConfigs.get(id);
        if (slopeConfig == null) {
            throw new IllegalArgumentException("No entry in integerSlopeSkeletonMap for id: " + id);
        }
        return slopeConfig;
    }

    public Collection<SlopeConfig> getSlopeSkeletonConfigs() {
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

    @Deprecated
    public WaterConfig getWaterConfig() {
        return waterConfig;
    }

    public DrivewayConfig getDrivewayConfig(Integer drivewayConfigId) {
        DrivewayConfig drivewayConfig = drivewayConfigs.get(drivewayConfigId);
        if (drivewayConfig == null) {
            throw new IllegalArgumentException("No DrivewayConfig for drivewayConfigId: " + drivewayConfigId);
        }
        return drivewayConfig;
    }

    // Methods used by the editors -----------------------------------------------------------------
    public void overrideTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        terrainObjectConfigs.put(terrainObjectConfig.getId(), terrainObjectConfig);
    }

    public void deleteTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        terrainObjectConfigs.remove(terrainObjectConfig.getId());
    }

    public void overrideSlopeSkeletonConfig(SlopeConfig slopeConfig) {
        slopeSkeletonConfigs.put(slopeConfig.getId(), slopeConfig);
    }

    public void deleteSlopeSkeletonConfig(SlopeConfig slopeConfig) {
        slopeSkeletonConfigs.remove(slopeConfig.getId());
    }
}
