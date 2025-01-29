package com.btxtech.shared.gameengine.planet.testframework;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.DaggerSlaveEmulator;
import com.btxtech.shared.gameengine.planet.gui.userobject.ScenarioPlayback;
import com.btxtech.shared.gameengine.planet.terrain.DaggerTerrainServiceTestBase;
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
public class ScenarioBaseTest extends DaggerTerrainServiceTestBase {
    private static final int MAX_TICK_COUNT = 1000;
    public static final String SAVE_DIRECTORY = TestHelper.SAVE_DIRECTORY + "pathing//move";
    private DaggerSlaveEmulator slave;

    @Before
    public void before() {
        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10));

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(5120, 512));
        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().terrainObjectConfigId(1).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(340, 140)),
                new TerrainObjectPosition().terrainObjectConfigId(1).scale(new Vertex(2, 2, 2)).position(new DecimalPosition(344, 95)),
                new TerrainObjectPosition().terrainObjectConfigId(2).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(362, 65)),
                new TerrainObjectPosition().terrainObjectConfigId(2).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(368, 21)),
                new TerrainObjectPosition().terrainObjectConfigId(3).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(400, 55)),
                new TerrainObjectPosition().terrainObjectConfigId(3).scale(new Vertex(1, 1, 1)).position(new DecimalPosition(420, 115)),
                new TerrainObjectPosition().terrainObjectConfigId(3).scale(new Vertex(0.5, 0.5, 0.5)).position(new DecimalPosition(450, 75))
        );

        setupTerrainTypeService(null, terrainObjectConfigs, planetConfig, terrainObjectPositions, null, null, null);
    }

    protected void testScenario(Scenario scenario) {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase1 = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getUserId(), userContext.getName());
        playerBase1.setResources(Double.MAX_VALUE);

        scenario.setup(playerBase1, getItemTypeService(), getBaseItemService(), getPathingService());
        scenario.createSyncItems();

        slave = new DaggerSlaveEmulator();
        slave.connectToMaster(createLevel1UserContext(), this);

        ScenarioTicks actualTicks = runScenario();
        scenario.setSaveCallback(() -> scenario.save(SAVE_DIRECTORY, actualTicks));
        ScenarioTicks expectedTicks = null;
        try {
            expectedTicks = scenario.readExpectedTicks();
            if (actualTicks.size() >= MAX_TICK_COUNT + 1) {
                throw new Exception("Max ticks (+ start state) reached: " + MAX_TICK_COUNT + ". Expected ticks: + " + expectedTicks.size());
            }
            compareScenario(expectedTicks, actualTicks, scenario);
        } catch (Throwable t) {
            t.printStackTrace();
            // showDisplay(scenario, actualTicks, expectedTicks);
            throw new RuntimeException(t);
        }
        // showDisplay(scenario, actualTicks, expectedTicks);
    }

    private void showDisplay(Scenario scenario, ScenarioTicks actualTicks, ScenarioTicks expectedTicks) {
        try {
            showDisplay(new ScenarioPlayback()
                    .scenario(scenario)
                    .actualSyncBaseItemInfo(actualTicks)
                    .expectedSyncBaseItemInfo(expectedTicks));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ScenarioTicks runScenario() {
        ScenarioTicks actualTicks = new ScenarioTicks();
        actualTicks.addMasterTick(getBaseItemService().getSyncBaseItemInfos());
        actualTicks.addSlaveTick(slave.getBaseItemService().getSyncBaseItemInfos());
        actualTicks.compareMasterSlave();
        for (int tickCount = 0; tickCount < MAX_TICK_COUNT && (isBaseServiceActive() || isPathingServiceMoving()); tickCount++) {
            DebugHelperStatic.setCurrentTick(actualTicks.size() - 1);
            tickPlanetService();
            System.out.println("----------------- Master ticks done: " + getPlanetService().getTickCount());
            slave.tickPlanetService();
            System.out.println("----------------- Slave ticks done: " + slave.getPlanetService().getTickCount());
            DebugHelperStatic.printAfterTick();
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
