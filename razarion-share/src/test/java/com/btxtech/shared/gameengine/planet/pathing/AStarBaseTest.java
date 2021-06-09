package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 15.02.2018.
 */
public abstract class AStarBaseTest extends WeldTerrainServiceTestBase {
    @Before
    public void before() {
        // Land slope config
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.horizontalSpace(5);
        slopeConfigLand.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 5)).slopeFactor(0),
                new SlopeShape().position(new DecimalPosition(4, 10)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(7, 15)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(9, 20)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(11, 25)).slopeFactor(0)
        ));
        slopeConfigLand.outerLineGameEngine(2).innerLineGameEngine(9);
        // Water slope config
        SlopeConfig slopeConfigWater = new SlopeConfig();
        slopeConfigWater.id(2).waterConfigId(1);
        slopeConfigWater.horizontalSpace(5);
        slopeConfigWater.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, -1)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(8, -1.5)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(12, -2)).slopeFactor(0.7)));
        slopeConfigWater.outerLineGameEngine(3).coastDelimiterLineGameEngine(6).innerLineGameEngine(10);

        List<WaterConfig> waterConfigs = Collections.singletonList(new WaterConfig().id(1).waterLevel(-0.2).groundLevel(-2));

        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        slopeConfigs.add(slopeConfigLand);
        slopeConfigs.add(slopeConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        // Land slope 1
        TerrainSlopePosition terrainSlopePositionLand1 = new TerrainSlopePosition();
        terrainSlopePositionLand1.id(1);
        terrainSlopePositionLand1.slopeConfigId(1);
        terrainSlopePositionLand1.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, 1), GameTestHelper.createTerrainSlopeCorner(100, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand1);
        // Land slope 2
        TerrainSlopePosition terrainSlopePositionLand2 = new TerrainSlopePosition();
        terrainSlopePositionLand2.id(2);
        terrainSlopePositionLand2.slopeConfigId(1);
        terrainSlopePositionLand2.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(165.39227716727615, 409.86546092796124, null),
                GameTestHelper.createTerrainSlopeCorner(178.39227716727615, 431.36546092796124, 1),
                GameTestHelper.createTerrainSlopeCorner(200.39227716727615, 467.36546092796124, 1),
                GameTestHelper.createTerrainSlopeCorner(218.89227716727615, 498.86546092796124, null),
                GameTestHelper.createTerrainSlopeCorner(111.89227716727615, 563.3654609279613, null),
                GameTestHelper.createTerrainSlopeCorner(54.16666666666667, 442.99999999999983, null)));
        terrainSlopePositions.add(terrainSlopePositionLand2);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.id(3);
        terrainSlopePositionWater.slopeConfigId(2);
        terrainSlopePositionWater.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(64, 200, null), GameTestHelper.createTerrainSlopeCorner(231, 200, null),
                GameTestHelper.createTerrainSlopeCorner(231, 256, null), GameTestHelper.createTerrainSlopeCorner(151, 257, null), // driveway
                GameTestHelper.createTerrainSlopeCorner(239, 359, null), GameTestHelper.createTerrainSlopeCorner(49, 360, null)));
        terrainSlopePositions.add(terrainSlopePositionWater);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10));

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(5120, 512));
        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(76, 30))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(114, 28))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(95, 11))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(223, 95))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(191, 116))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(48, 124))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(132, 131))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(50, 280))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(127, 290))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(212, 325))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(223, 290))));

        setupTerrainTypeService(slopeConfigs, null, waterConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions, terrainObjectPositions, null);
    }

    protected SimplePath setupPath(double actorRadius, TerrainType actorTerrainType, DecimalPosition actorPosition, double range, double targetRadius, TerrainType targetTerrainType, DecimalPosition targetPosition) {
        SyncBaseItem actor = GameTestHelper.createMockSyncBaseItem(actorRadius, actorTerrainType, actorPosition);
        SyncBaseItem target = GameTestHelper.createMockSyncBaseItem(targetRadius, targetTerrainType, targetPosition);
        return getPathingService().setupPathToDestination(actor, range, target);
    }

    protected SimplePath setupPath(double radius, TerrainType land, DecimalPosition start, DecimalPosition destination) {
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(radius, land, start);
        return getPathingService().setupPathToDestination(syncBaseItem, destination);
    }

}
