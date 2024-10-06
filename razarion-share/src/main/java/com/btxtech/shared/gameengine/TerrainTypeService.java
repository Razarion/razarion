package com.btxtech.shared.gameengine;

import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 19.07.2016.
 */
@JsType
@Singleton
public class TerrainTypeService {
    private final Map<Integer, SlopeConfig> slopeConfigs = new HashMap<>();
    private final Map<Integer, GroundConfig> groundConfigs = new HashMap<>();
    private final Map<Integer, WaterConfig> waterConfigs = new HashMap<>();
    private final Map<Integer, DrivewayConfig> drivewayConfigs = new HashMap<>();
    private final Map<Integer, TerrainObjectConfig> terrainObjectConfigs = new HashMap<>();

    @Inject
    public TerrainTypeService(InitializeService initializeService) {
        initializeService.receiveStaticGameConfig(this::init);
    }

    public void init(StaticGameConfig staticGameConfig) {
        setGroundConfigs(staticGameConfig.getGroundConfigs());
        setSlopeConfigs(staticGameConfig.getSlopeConfigs());
        setWaterConfigs(staticGameConfig.getWaterConfigs());
        setTerrainObjectConfigs(staticGameConfig.getTerrainObjectConfigs());
        setDrivewayConfigs(staticGameConfig.getDrivewayConfigs());
    }

    @SuppressWarnings("unused") // Called by Angular
    public double calculateGroundHeight(int slopeConfigId) {
        SlopeConfig slopeConfig = getSlopeConfig(slopeConfigId);
        if (slopeConfig.hasWaterConfigId()) {
            return getWaterConfig(slopeConfig.getWaterConfigId()).getGroundLevel();
        }
        return slopeConfig.getSlopeShapes().get(slopeConfig.getSlopeShapes().size() - 1).getPosition().getY();
    }

    private void setGroundConfigs(Collection<GroundConfig> groundConfigs) {
        this.groundConfigs.clear();
        if (groundConfigs != null) {
            for (GroundConfig groundConfig : groundConfigs) {
                this.groundConfigs.put(groundConfig.getId(), groundConfig);
            }
        }
    }

    private void setWaterConfigs(Collection<WaterConfig> waterConfigs) {
        this.waterConfigs.clear();
        if (waterConfigs != null) {
            for (WaterConfig waterConfig : waterConfigs) {
                this.waterConfigs.put(waterConfig.getId(), waterConfig);
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

    public SlopeConfig getSlopeConfig(int id) {
        SlopeConfig slopeConfig = slopeConfigs.get(id);
        if (slopeConfig == null) {
            throw new IllegalArgumentException("No entry in SlopeConfigs for id: " + id);
        }
        return slopeConfig;
    }

    public Collection<SlopeConfig> getSlopeConfigs() {
        return slopeConfigs.values();
    }

    private void setSlopeConfigs(Collection<SlopeConfig> slopeConfigs) {
        this.slopeConfigs.clear();
        if (slopeConfigs != null) {
            for (SlopeConfig slopeConfig : slopeConfigs) {
                this.slopeConfigs.put(slopeConfig.getId(), slopeConfig);
            }
        }
    }

    public Collection<TerrainObjectConfig> getTerrainObjectConfigs() {
        return terrainObjectConfigs.values();
    }

    public void setTerrainObjectConfigs(Collection<TerrainObjectConfig> terrainObjectConfigs) {
        this.terrainObjectConfigs.clear();
        if (terrainObjectConfigs != null) {
            for (TerrainObjectConfig terrainObjectConfig : terrainObjectConfigs) {
                this.terrainObjectConfigs.put(terrainObjectConfig.getId(), terrainObjectConfig);
            }
        }
    }

    public WaterConfig getWaterConfig(int id) {
        WaterConfig waterConfig = waterConfigs.get(id);
        if (waterConfig == null) {
            throw new IllegalArgumentException("No entry in WaterConfigs for id: " + id);
        }
        return waterConfig;
    }

    public DrivewayConfig getDrivewayConfig(Integer drivewayConfigId) {
        DrivewayConfig drivewayConfig = drivewayConfigs.get(drivewayConfigId);
        if (drivewayConfig == null) {
            throw new IllegalArgumentException("No DrivewayConfig for drivewayConfigId: " + drivewayConfigId);
        }
        return drivewayConfig;
    }

    @SuppressWarnings("unused") // Used by Angular
    public GroundConfig getGroundConfig(int groundConfigId) {
        GroundConfig groundConfig = groundConfigs.get(groundConfigId);
        if (groundConfig == null) {
            throw new IllegalArgumentException("No GroundConfig for groundConfigId: " + groundConfigId);
        }
        return groundConfig;
    }
}
