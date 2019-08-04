package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 15.02.2018.
 */
public abstract class AStarBaseTest extends WeldTerrainServiceTestBase {
    @Before
    public void before() {
        // Land slope config
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(5).setSegments(1).setWidth(11).setHorizontalSpace(5).setHeight(25);
        SlopeNode[][] slopeNodeLand = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 5, 0),},
                {GameTestHelper.createSlopeNode(4, 10, 0.7),},
                {GameTestHelper.createSlopeNode(7, 15, 1),},
                {GameTestHelper.createSlopeNode(9, 20, 0.7),},
                {GameTestHelper.createSlopeNode(11, 25, 0),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodeLand));
        slopeSkeletonConfigLand.setOuterLineGameEngine(2).setInnerLineGameEngine(9);
        // Water slope config
        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(12).setHorizontalSpace(5).setHeight(-2);
        SlopeNode[][] slopeNodeWater = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 1),},
                {GameTestHelper.createSlopeNode(4, -1, 0.7),},
                {GameTestHelper.createSlopeNode(8, -1.5, 0.7),},
                {GameTestHelper.createSlopeNode(12, -2, 0.7),},
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodeWater));
        slopeSkeletonConfigWater.setOuterLineGameEngine(3).setCoastDelimiterLineGameEngine(6).setInnerLineGameEngine(10);

        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        // Land slope 1
        TerrainSlopePosition terrainSlopePositionLand1 = new TerrainSlopePosition();
        terrainSlopePositionLand1.setId(1);
        terrainSlopePositionLand1.setSlopeConfigId(1);
        terrainSlopePositionLand1.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, 1), GameTestHelper.createTerrainSlopeCorner(100, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand1);
        // Land slope 2
        TerrainSlopePosition terrainSlopePositionLand2 = new TerrainSlopePosition();
        terrainSlopePositionLand2.setId(2);
        terrainSlopePositionLand2.setSlopeConfigId(1);
        terrainSlopePositionLand2.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(165.39227716727615, 409.86546092796124, null),
                GameTestHelper.createTerrainSlopeCorner(178.39227716727615, 431.36546092796124, 1),
                GameTestHelper.createTerrainSlopeCorner(200.39227716727615, 467.36546092796124, 1),
                GameTestHelper.createTerrainSlopeCorner(218.89227716727615, 498.86546092796124, null),
                GameTestHelper.createTerrainSlopeCorner(111.89227716727615, 563.3654609279613, null),
                GameTestHelper.createTerrainSlopeCorner(54.16666666666667, 442.99999999999983, null)));
        terrainSlopePositions.add(terrainSlopePositionLand2);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.setId(3);
        terrainSlopePositionWater.setSlopeConfigId(2);
        terrainSlopePositionWater.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(64, 200, null), GameTestHelper.createTerrainSlopeCorner(231, 200, null),
                GameTestHelper.createTerrainSlopeCorner(231, 256, null), GameTestHelper.createTerrainSlopeCorner(151, 257, null), // driveway
                GameTestHelper.createTerrainSlopeCorner(239, 359, null), GameTestHelper.createTerrainSlopeCorner(49, 360, null)));
        terrainSlopePositions.add(terrainSlopePositionWater);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 10, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3}
        };

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        PlanetConfig planetConfig = GameTestContent.setupPlanetConfig();
        planetConfig.setPlayGround(new Rectangle2D(50, 50, 5000, 5000));
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 32, 32));
        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(76, 30))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(114, 28))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(95, 11))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(223, 95))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(191, 116))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(48, 124))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(132, 131))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(50, 280))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(127, 290))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(212, 325))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(223, 290))));

        setupTerrainTypeService(splattings, slopeSkeletonConfigs, terrainObjectConfigs, heights, planetConfig, terrainSlopePositions, terrainObjectPositions, null);
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
