package com.btxtech.shared.gameengine;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
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
    private final Map<Integer, GroundConfig> groundConfigs = new HashMap<>();
    private final Map<Integer, TerrainObjectConfig> terrainObjectConfigs = new HashMap<>();

    @Inject
    public TerrainTypeService(InitializeService initializeService) {
        initializeService.receiveStaticGameConfig(this::init);
    }

    public void init(StaticGameConfig staticGameConfig) {
        setGroundConfigs(staticGameConfig.getGroundConfigs());
        setTerrainObjectConfigs(staticGameConfig.getTerrainObjectConfigs());
    }

    private void setGroundConfigs(Collection<GroundConfig> groundConfigs) {
        this.groundConfigs.clear();
        if (groundConfigs != null) {
            for (GroundConfig groundConfig : groundConfigs) {
                this.groundConfigs.put(groundConfig.getId(), groundConfig);
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

    @SuppressWarnings("unused") // Used by Angular
    public GroundConfig getGroundConfig(int groundConfigId) {
        GroundConfig groundConfig = groundConfigs.get(groundConfigId);
        if (groundConfig == null) {
            throw new IllegalArgumentException("No GroundConfig for groundConfigId: " + groundConfigId);
        }
        return groundConfig;
    }
}
