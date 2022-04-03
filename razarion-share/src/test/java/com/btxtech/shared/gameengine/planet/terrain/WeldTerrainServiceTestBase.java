package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.WeldMasterBaseTest;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 25.09.2017.
 */
public class WeldTerrainServiceTestBase extends WeldMasterBaseTest {
    public static int DRIVEWAY_ID_1 = 1;

    protected void setupTerrainTypeService(List<SlopeConfig> slopeConfigs, List<DrivewayConfig> drivewayConfigs, List<WaterConfig> waterConfigs, List<TerrainObjectConfig> terrainObjectConfigs, PlanetConfig planetConfig, List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions, GroundConfig groundConfig, List<ThreeJsModelConfig> threeJsModelConfigs) {
        StaticGameConfig staticGameConfig = FallbackConfig.setupStaticGameConfig();
        if (groundConfig == null) {
            groundConfig = staticGameConfig.getGroundConfigs().get(0);
        }
        staticGameConfig.setGroundConfigs(Collections.singletonList(groundConfig));
        staticGameConfig.setSlopeConfigs(slopeConfigs);
        if (waterConfigs != null) {
            staticGameConfig.setWaterConfigs(waterConfigs);
        } else {
            staticGameConfig.setWaterConfigs(FallbackConfig.setupWaterConfigs());
        }
        staticGameConfig.setTerrainObjectConfigs(terrainObjectConfigs);
        if (drivewayConfigs == null) {
            drivewayConfigs = Collections.singletonList(new DrivewayConfig().id(DRIVEWAY_ID_1).angle(Math.toRadians(20)));
        }
        staticGameConfig.setDrivewayConfigs(drivewayConfigs);
        if (planetConfig == null) {
            planetConfig = FallbackConfig.setupPlanetConfig();
        }
        staticGameConfig.setThreeJsModelConfigs(threeJsModelConfigs);
        setupEnvironment(staticGameConfig, planetConfig);
        getTestNativeTerrainShapeAccess().setPlanetConfig(planetConfig);
        getTestNativeTerrainShapeAccess().setTerrainSlopeAndObjectPositions(terrainSlopePositions, terrainObjectPositions);
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
