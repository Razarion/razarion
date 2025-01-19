package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.DaggerMasterBaseTest;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 25.09.2017.
 */
public class DaggerTerrainServiceTestBase extends DaggerMasterBaseTest {
    protected void setupTerrainTypeService(StaticGameConfig staticGameConfig,
                                           List<TerrainObjectConfig> terrainObjectConfigs,
                                           PlanetConfig planetConfig,
                                           List<TerrainObjectPosition> terrainObjectPositions,
                                           List<GroundConfig> groundConfigs,
                                           List<ThreeJsModelConfig> threeJsModelConfigs,
                                           List<ThreeJsModelPackConfig> threeJsModelPackConfigs,
                                           List<ParticleSystemConfig> particleSystemConfigs) {
        if (staticGameConfig == null) {
            staticGameConfig = FallbackConfig.setupStaticGameConfig();
        }
        if (groundConfigs != null) {
            staticGameConfig.setGroundConfigs(groundConfigs);
        }
        if (terrainObjectConfigs != null) {
            staticGameConfig.setTerrainObjectConfigs(terrainObjectConfigs);
        }
        if (planetConfig == null) {
            planetConfig = FallbackConfig.setupPlanetConfig();
        }
        staticGameConfig.setThreeJsModelConfigs(threeJsModelConfigs);
        staticGameConfig.setThreeJsModelPackConfigs(threeJsModelPackConfigs);
        staticGameConfig.setParticleSystemConfigs(particleSystemConfigs);
        setupEnvironment(staticGameConfig, planetConfig);
        getTestNativeTerrainShapeAccess().setPlanetConfig(planetConfig);
        getPlanetService().initialise(getPlanetConfig(), GameEngineMode.MASTER, setupMasterPlanetConfig(), () -> getPlanetService().start(), null);
        if (getAlarmService().hasAlarms()) {
            throw new RuntimeException("Setup Terrain failed. Check AlarmService and Log");
        }
    }

    protected TerrainShapeManager getTerrainShape() {
        return (TerrainShapeManager) SimpleTestEnvironment.readField("terrainShape", getTerrainService());
    }

    protected MasterPlanetConfig setupMasterPlanetConfig() {
        MasterPlanetConfig masterPlanetConfig = new MasterPlanetConfig();
        masterPlanetConfig.setResourceRegionConfigs(new ArrayList<>());
        return masterPlanetConfig;
    }

    protected List<TerrainTile> generateTerrainTiles(Index... indices) {
        return Arrays.stream(indices).map(index -> getTerrainService().generateTerrainTile(index)).collect(Collectors.toList());
    }
}
