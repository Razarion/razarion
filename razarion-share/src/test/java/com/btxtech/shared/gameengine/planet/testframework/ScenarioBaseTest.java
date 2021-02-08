package com.btxtech.shared.gameengine.planet.testframework;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.WeldSlaveEmulator;
import com.btxtech.shared.gameengine.planet.gui.userobject.ScenarioPlayback;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
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
    private static final int MAX_TICK_COUNT = 1000;
    public static final String SAVE_DIRECTORY = TestHelper.SAVE_DIRECTORY + "pathing//move";
    private WeldSlaveEmulator slave;

    @Before
    public void before() {
        // Land slope config
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1).internalName("Mountain");
        slopeConfigLand.setHorizontalSpace(5);
        slopeConfigLand.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 5)).slopeFactor(0),
                new SlopeShape().position(new DecimalPosition(4, 10)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(7, 15)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(9, 20)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(11, 25)).slopeFactor(0)
        ));
        slopeConfigLand.setOuterLineGameEngine(2).setInnerLineGameEngine(9);
        // Water slope config
        SlopeConfig slopeConfigWater = new SlopeConfig();
        slopeConfigWater.id(2).waterConfigId(FallbackConfig.WATER_CONFIG_ID).setInternalName("Water");
        slopeConfigWater.setHorizontalSpace(5);
        slopeConfigWater.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, -1)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(8, -1.5)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(12, -2)).slopeFactor(0.7)
        ));
        slopeConfigWater.setOuterLineGameEngine(3).setCoastDelimiterLineGameEngine(6).setInnerLineGameEngine(10);

        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        slopeConfigs.add(slopeConfigLand);
        slopeConfigs.add(slopeConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        // Land slope
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.id(1);
        terrainSlopePositionLand.slopeConfigId(1);
        terrainSlopePositionLand.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, 1), GameTestHelper.createTerrainSlopeCorner(100, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.id(2);
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
        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().terrainObjectId(1).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(340, 140)),
                new TerrainObjectPosition().terrainObjectId(1).scale(new Vertex(2, 2, 2)).position(new DecimalPosition(344, 95)),
                new TerrainObjectPosition().terrainObjectId(2).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(362, 65)),
                new TerrainObjectPosition().terrainObjectId(2).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(368, 21)),
                new TerrainObjectPosition().terrainObjectId(3).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(400, 55)),
                new TerrainObjectPosition().terrainObjectId(3).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(420, 115)),
                new TerrainObjectPosition().terrainObjectId(3).scale(new Vertex(0.5, 0.5, 0.5)).position(new DecimalPosition(450, 75))
        );

        setupTerrainTypeService(slopeConfigs, null, null, terrainObjectConfigs, planetConfig, terrainSlopePositions, terrainObjectPositions, null);
    }

    protected void testScenario(Scenario scenario) {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase1 = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getUserId(), userContext.getName());
        playerBase1.setResources(Double.MAX_VALUE);

        scenario.setup(playerBase1, getItemTypeService(), getBaseItemService(), getPathingService());
        scenario.createSyncItems();

        slave = new WeldSlaveEmulator();
        slave.connectToMaster(createLevel1UserContext(), this);

        ScenarioTicks actualTicks = runScenario();
        scenario.setSaveCallback(() -> scenario.save(SAVE_DIRECTORY, actualTicks));
        ScenarioTicks expectedTicks = null;
        try {
            expectedTicks = scenario.readExpectedTicks();
            if (actualTicks.size() >= MAX_TICK_COUNT + 1) {
                throw new Exception("Max ticks (+ start state) reached: " + expectedTicks.size());
            }
            compareScenario(expectedTicks, actualTicks, scenario);
        } catch (Throwable t) {
            t.printStackTrace();
            try {
                showDisplay(new ScenarioPlayback().setActualSyncBaseItemInfo(actualTicks).setExpectedSyncBaseItemInfo(expectedTicks).setScenario(scenario));
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new RuntimeException(t);
        }
    }

    private ScenarioTicks runScenario() {
        ScenarioTicks actualTicks = new ScenarioTicks();
        actualTicks.addMasterTick(getBaseItemService().getSyncBaseItemInfos());
        actualTicks.addSlaveTick(slave.getBaseItemService().getSyncBaseItemInfos());
        actualTicks.compareMasterSlave();
        for (int tickCount = 0; tickCount < MAX_TICK_COUNT && (isBaseServiceActive() || isPathingServiceMoving()); tickCount++) {
            DebugHelperStatic.setCurrentTick(actualTicks.size());
            tickPlanetService();
            // System.out.println("----------------- Master ticks done: " + getPlanetService().getTickCount());
            slave.tickPlanetService();
            // System.out.println("----------------- Slave ticks done: " + slave.getPlanetService().getTickCount());
            DebugHelperStatic.printAfterTick(null);
            actualTicks.addMasterTick(getBaseItemService().getSyncBaseItemInfos());
            actualTicks.addSlaveTick(slave.getBaseItemService().getSyncBaseItemInfos());
            actualTicks.compareMasterSlave();
        }
        return actualTicks;
    }

    private void compareScenario(ScenarioTicks expectedTicks, ScenarioTicks actualTicks, Scenario scenario) {
        Assert.assertEquals(expectedTicks.size(), actualTicks.size());
        for (int i = 0, expectedTicksSize = expectedTicks.size(); i < expectedTicksSize; i++) {
            try {
                ScenarioAssert.compareSyncBaseItemInfo(expectedTicks.getMasterTick(i), actualTicks.getMasterTick(i));
                ScenarioAssert.compareSyncBaseItemInfo(expectedTicks.getSlaveTick(i), actualTicks.getSlaveTick(i));
            } catch (Throwable t) {
                System.out.println("Failed on tick: " + i);
                throw t;
            }
        }
    }

}
