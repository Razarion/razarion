package com.btxtech.shared.gameengine;

import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
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
    private Map<Integer, GroundConfig> groundConfigs = new HashMap<>();;
    private Map<Integer, TerrainObjectConfig> terrainObjectConfigs = new HashMap<>();
    private Map<Integer, DrivewayConfig> drivewayConfigs = new HashMap<>();
    private WaterConfig waterConfig;

    public void onGameEngineInit(@Observes StaticGameInitEvent engineInitEvent) {
        init(engineInitEvent.getStaticGameConfig());
    }

    public void init(StaticGameConfig staticGameConfig) {
        setGroundConfigs(staticGameConfig.getGroundConfigs());
        waterConfig = staticGameConfig.getWaterConfig();
        setSlopeSkeletonConfigs(staticGameConfig.getSlopeConfigs());
        setTerrainObjectConfigs(staticGameConfig.getTerrainObjectConfigs());
        setDrivewayConfigs(staticGameConfig.getDrivewayConfigs());
    }

    public void setGroundConfigs(Collection<GroundConfig> groundConfigs) {
        this.groundConfigs.clear();
        if (groundConfigs != null) {
            for (GroundConfig groundConfig : groundConfigs) {
                this.groundConfigs.put(groundConfig.getId(), groundConfig);
            }
        }
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


    public TerrainObjectConfig getTerrainObjectConfig(int id) {
        TerrainObjectConfig terrainObjectConfig = terrainObjectConfigs.get(id);
        if (terrainObjectConfig == null) {
            throw new IllegalArgumentException("No TerrainObjectConfig for id: " + id);
        }
        return terrainObjectConfig;
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

    public GroundConfig getGroundConfig(Integer groundConfigId) {
        if(groundConfigId == null) {
            return null;
        }
        GroundConfig groundConfig = groundConfigs.get(groundConfigId);
        if (groundConfig == null) {
            throw new IllegalArgumentException("No GroundConfig for groundConfigId: " + groundConfigId);
        }
        return groundConfig;
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
