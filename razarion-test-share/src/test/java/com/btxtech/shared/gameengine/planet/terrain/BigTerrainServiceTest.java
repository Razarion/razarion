package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 11.11.2017.
 */
public class BigTerrainServiceTest extends DaggerTerrainServiceTestBase {
    private void setup(int slopeConfigId) {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();

        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.horizontalSpace(5);
        slopeConfigLand.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, 8)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(7, 12)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(10, 20)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(11, 20)).slopeFactor(0.7)));
        slopeConfigLand.outerLineGameEngine(3).innerLineGameEngine(7);
        slopeConfigs.add(slopeConfigLand);

        SlopeConfig slopeConfigWater = new SlopeConfig();
        slopeConfigWater.id(2).setWaterConfigId(1);
        slopeConfigWater.horizontalSpace(5);
        slopeConfigWater.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, 0)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(8, -1)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(10, -1.5)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(12, -2)).slopeFactor(0.7)));
        slopeConfigWater.outerLineGameEngine(4).coastDelimiterLineGameEngine(7).innerLineGameEngine(11);
        slopeConfigs.add(slopeConfigWater);

        List<WaterConfig> waterConfigs = Collections.singletonList(new WaterConfig().id(1).waterLevel(-0.2).groundLevel(-2));

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.id(1);
        terrainSlopePositionLand.slopeConfigId(slopeConfigId);
        terrainSlopePositionLand.polygon(Arrays.asList(
                GameTestHelper.createTerrainSlopeCorner(100, 4000, null),
                GameTestHelper.createTerrainSlopeCorner(4000, 100, null),
                GameTestHelper.createTerrainSlopeCorner(4800, 800, null),
                GameTestHelper.createTerrainSlopeCorner(800, 4800, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(5000, 5000));

        setupTerrainTypeService(null, slopeConfigs, null, waterConfigs, null, planetConfig, terrainSlopePositions, null, null, null, null, null);
    }
}
