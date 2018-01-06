package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 10.07.2017.
 */
public class AStarTest extends WeldTerrainServiceTestBase {
    protected void setup() {
        // Land slope config
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(5).setSegments(1).setWidth(11).setVerticalSpace(5).setHeight(25);
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
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(12).setVerticalSpace(5).setHeight(-2);
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
        // Land slope
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, 1), GameTestHelper.createTerrainSlopeCorner(100, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.setId(2);
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
        planetConfig.setTerrainObjectPositions(terrainObjectPositions);

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions, null);
    }

    protected PathingService getPathingService() {
        return getWeldBean(PathingService.class);
    }

    @Test
    public void land() throws Exception {
        setup();
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(3, TerrainType.LAND, new DecimalPosition(62, 11));
        SimplePath simplePath = getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(64, 48));
        // showDisplay(simplePath);
        printSimplePath(simplePath);
        assertSimplePath(simplePath, 0.0, new DecimalPosition(60.0, 20.0), new DecimalPosition(62.0, 26.0), new DecimalPosition(66.0, 26.0), new DecimalPosition(70.0, 26.0),
                new DecimalPosition(74.0, 26.0), new DecimalPosition(78.0, 26.0), new DecimalPosition(82.0, 26.0), new DecimalPosition(86.0, 26.0), new DecimalPosition(90.0, 26.0),
                new DecimalPosition(94.0, 26.0), new DecimalPosition(98.0, 26.0), new DecimalPosition(102.0, 26.0), new DecimalPosition(108.0, 28.0), new DecimalPosition(116.0, 28.0),
                new DecimalPosition(116.0, 36.0), new DecimalPosition(116.0, 44.0), new DecimalPosition(124.0, 44.0), new DecimalPosition(132.0, 44.0), new DecimalPosition(140.0, 44.0),
                new DecimalPosition(148.0, 44.0), new DecimalPosition(156.0, 44.0), new DecimalPosition(164.0, 44.0), new DecimalPosition(172.0, 44.0), new DecimalPosition(180.0, 44.0),
                new DecimalPosition(188.0, 44.0), new DecimalPosition(188.0, 52.0), new DecimalPosition(188.0, 60.0), new DecimalPosition(188.0, 68.0), new DecimalPosition(182.0, 66.0),
                new DecimalPosition(178.0, 66.0), new DecimalPosition(172.0, 68.0), new DecimalPosition(164.0, 68.0), new DecimalPosition(156.0, 68.0), new DecimalPosition(148.0, 68.0),
                new DecimalPosition(140.0, 68.0), new DecimalPosition(132.0, 68.0), new DecimalPosition(124.0, 68.0), new DecimalPosition(116.0, 68.0), new DecimalPosition(111.5, 64.5),
                new DecimalPosition(110.5, 64.5), new DecimalPosition(110.5, 63.5), new DecimalPosition(109.0, 63.0), new DecimalPosition(106.0, 62.0), new DecimalPosition(100.0, 60.0),
                new DecimalPosition(98.0, 54.0), new DecimalPosition(92.0, 52.0), new DecimalPosition(84.0, 52.0), new DecimalPosition(76.0, 52.0), new DecimalPosition(64.0, 48.0));
    }

    @Test
    public void landDestinationNearBlocked() throws Exception {
        setup();
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(3, TerrainType.LAND, new DecimalPosition(115, 124));
        SimplePath simplePath = getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(77, 119.9));
        // printSimplePath(simplePath);
        // showDisplay(simplePath);
        assertSimplePath(simplePath, 0.0, new DecimalPosition(108.0, 124.0), new DecimalPosition(100.0, 124.0), new DecimalPosition(92.0, 124.0), new DecimalPosition(84.0, 124.0), new DecimalPosition(77.0, 119.9));

    }

    @Test
    public void water() throws Exception {
        setup();
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(4, TerrainType.WATER, new DecimalPosition(207, 240));
        SimplePath simplePath = getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(192, 344));
        // showDisplay(simplePath);
        // printSimplePath(simplePath);
        assertSimplePath(simplePath, 0.0, new DecimalPosition(204.0, 252.0), new DecimalPosition(196.0, 252.0), new DecimalPosition(188.0, 252.0),
                new DecimalPosition(180.0, 252.0), new DecimalPosition(172.0, 252.0), new DecimalPosition(170.0, 258.0), new DecimalPosition(164.0, 260.0),
                new DecimalPosition(162.0, 266.0), new DecimalPosition(162.0, 270.0), new DecimalPosition(164.0, 276.0), new DecimalPosition(164.0, 284.0),
                new DecimalPosition(170.0, 286.0), new DecimalPosition(172.0, 292.0), new DecimalPosition(178.0, 294.0), new DecimalPosition(180.0, 300.0),
                new DecimalPosition(186.0, 302.0), new DecimalPosition(188.0, 308.0), new DecimalPosition(188.0, 316.0), new DecimalPosition(188.0, 324.0),
                new DecimalPosition(188.0, 332.0), new DecimalPosition(188.0, 340.0), new DecimalPosition(188.0, 348.0), new DecimalPosition(192.0, 344.0));
    }

    @Test
    public void slopeError() throws Exception {
        setup();
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(4, TerrainType.LAND, new DecimalPosition(170, 151));
        try {
            getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(70, 117));
            Assert.fail("Fail expected. Destination is not free");
        } catch (PathFindingNotFreeException e) {
            // Expected
            Assert.assertTrue(e.getMessage().startsWith("Destination tile is not free:"));
        }
    }

    @Test
    public void landWaterError() throws Exception {
        setup();
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(4, TerrainType.LAND, new DecimalPosition(76, 92));
        try {
            getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(100.375, 226.0));
            Assert.fail("Fail expected. Destination is not free");
        } catch (PathFindingNotFreeException e) {
            // Expected
            Assert.assertTrue(e.getMessage().startsWith("Destination tile is not free:"));
        }
    }

    @Test
    public void waterLandError() throws Exception {
        setup();
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(4, TerrainType.WATER, new DecimalPosition(153, 296));
        try {
            getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(274, 233));
            Assert.fail("Fail expected. Destination is not free");
        } catch (PathFindingNotFreeException e) {
            // Expected
            Assert.assertTrue(e.getMessage().startsWith("Destination tile is not free:"));
        }
    }

    @Test
    public void harbor1() throws Exception {
        setup();
        SyncBaseItem builder = GameTestHelper.createMockSyncBaseItem(4, TerrainType.LAND, new DecimalPosition(140, 164));
        SimplePath simplePath = getPathingService().setupPathToDestination(builder, 10, TerrainType.WATER_COAST, new DecimalPosition(96, 197), 5);
        // printSimplePath(simplePath);
        // showDisplay(simplePath);
    }

    @Test
    public void stuck() {
        setup();
        SyncBaseItem builder = GameTestHelper.createMockSyncBaseItem(3, TerrainType.WATER, new DecimalPosition(178.5, 259.5));
        SimplePath simplePath = getPathingService().setupPathToDestination(builder, new DecimalPosition(180, 250));
        printSimplePath(simplePath);
        assertSimplePath(simplePath, 0.0, new DecimalPosition(178.5, 258.5), new DecimalPosition(179.0, 257.0), new DecimalPosition(180.0, 250.0));
        // showDisplay(simplePath);
    }

    private void assertSimplePath(SimplePath actual, double expectedTotalRanges, DecimalPosition... expectedPosition) {
        Assert.assertEquals("totalRange", expectedTotalRanges, actual.getTotalRange(), 0.001);
        TestHelper.assertDecimalPositions(Arrays.asList(expectedPosition), actual.getWayPositions());
    }

    private void printSimplePath(SimplePath simplePath) {
        System.out.println("assertSimplePath(simplePath, " + simplePath.getTotalRange() + ", " + TestHelper.decimalPositionsToString(simplePath.getWayPositions()) + ");");
    }
}