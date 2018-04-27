package com.btxtech.shared.gameengine.planet.testframework;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.gui.userobject.ScenarioPlayback;
import com.btxtech.shared.gameengine.planet.pathing.AStarBaseTest;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 12.04.2018.
 */
public class ScenarioBaseTest extends WeldTerrainServiceTestBase {
    private static final int MAX_TICK_COUNT = 100000;
    public static final String SAVE_DIRECTORY = TestHelper.SAVE_DIRECTORY + "pathing//move";

    @Before
    public void before() {
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
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(76, 30))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(114, 28))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(95, 11))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(223, 95))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(191, 116))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(48, 124))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(132, 131))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(50, 280))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(127, 290))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(212, 325))));
//        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(223, 290))));

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions, terrainObjectPositions);
    }

    protected void testScenario(Scenario scenario) {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase1 = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getHumanPlayerId(), userContext.getName());
        playerBase1.setResources(Double.MAX_VALUE);

        scenario.setup(playerBase1, getItemTypeService(), getBaseItemService(), getPathingService());
        scenario.createSyncItems();

        List<List<SyncBaseItemInfo>> actualTicks = runScenario();
        scenario.setSaveCallback(() -> scenario.save(SAVE_DIRECTORY, actualTicks));
        try {
            List<List<SyncBaseItemInfo>> expectedTicks = scenario.readExpectedTicks();
            compareScenario(expectedTicks, actualTicks, scenario);
        } catch (Throwable t) {
            t.printStackTrace();
            showDisplay(new ScenarioPlayback().setSyncBaseItemInfo(actualTicks).setScenario(scenario));
            throw new RuntimeException(t);
        }
    }

    private List<List<SyncBaseItemInfo>> runScenario() {
        List<List<SyncBaseItemInfo>> actualTicks = new ArrayList<>();
        for (int tickCount = 0; tickCount < MAX_TICK_COUNT && (isBaseServiceActive() || isPathingServiceMoving()); tickCount++) {
            actualTicks.add(getBaseItemService().getSyncBaseItemInfos());
            tickPlanetService();
        }
        actualTicks.add(getBaseItemService().getSyncBaseItemInfos());
        return actualTicks;
    }

    private void compareScenario(List<List<SyncBaseItemInfo>> expectedTicks, List<List<SyncBaseItemInfo>> actualTicks, Scenario scenario) {
        Assert.assertEquals(expectedTicks.size(), actualTicks.size());
        for (int i = 0, expectedTicksSize = expectedTicks.size(); i < expectedTicksSize; i++) {
            try {
                ScenarioAssert.compareSyncBaseItemInfo(expectedTicks.get(i), actualTicks.get(i));
            } catch (Throwable t) {
                System.out.println("Failed on tick: " + i);
                throw t;
            }
        }
    }
}
