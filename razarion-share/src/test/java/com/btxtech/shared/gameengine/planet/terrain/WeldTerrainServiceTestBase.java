package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.DrivewayConfig;
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
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.WeldMasterBaseTest;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 25.09.2017.
 */
public class WeldTerrainServiceTestBase extends WeldMasterBaseTest {
    public static int DRIVEWAY_ID_1 = 1;

    protected void setupTerrainTypeService(List<SlopeConfig> slopeConfigs, List<TerrainObjectConfig> terrainObjectConfigs, PlanetConfig planetConfig, List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions, GroundConfig groundConfig) {
        StaticGameConfig staticGameConfig = GameTestContent.setupStaticGameConfig();
        staticGameConfig.setWaterConfig(new WaterConfig().setWaterLevel(-0.7));
        if(groundConfig == null) {
            groundConfig = new GroundConfig();
        }
        staticGameConfig.setGroundConfigs(Collections.singletonList(groundConfig));
        staticGameConfig.setSlopeConfigs(slopeConfigs);
        staticGameConfig.setTerrainObjectConfigs(terrainObjectConfigs);
        List<DrivewayConfig> drivewayConfigs = new ArrayList<>();
        drivewayConfigs.add(new DrivewayConfig().setId(DRIVEWAY_ID_1).setAngle(Math.toRadians(20)));
        staticGameConfig.setDrivewayConfigs(drivewayConfigs);
        if (planetConfig == null) {
            planetConfig = GameTestContent.setupPlanetConfig();
        }
        setupEnvironment(staticGameConfig, planetConfig);
        getTestNativeTerrainShapeAccess().setPlanetConfig(planetConfig);
        getTestNativeTerrainShapeAccess().setTerrainSlopeAndObjectPositions(terrainSlopePositions, terrainObjectPositions);
        getPlanetService().initialise(getPlanetConfig(), GameEngineMode.MASTER, setupMasterPlanetConfig(), () -> getPlanetService().start(), null);
    }

    protected TerrainShape getTerrainShape() {
        return (TerrainShape) SimpleTestEnvironment.readField("terrainShape", getTerrainService());
    }

    protected MasterPlanetConfig setupMasterPlanetConfig() {
        MasterPlanetConfig masterPlanetConfig = new MasterPlanetConfig();
        masterPlanetConfig.setResourceRegionConfigs(new ArrayList<>());
        return masterPlanetConfig;
    }

    protected Collection<TerrainTile> generateTerrainTiles(Index... indices) {
        return Arrays.stream(indices).map(index -> getTerrainService().generateTerrainTile(index)).collect(Collectors.toList());
    }
}
