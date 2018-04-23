package com.btxtech.shared.gameengine.planet.testframework;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.gui.userobject.ScenarioPlayback;
import com.btxtech.shared.gameengine.planet.pathing.AStarBaseTest;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 12.04.2018.
 */
public class ScenarioBaseTest extends AStarBaseTest {
    public static final String SAVE_DIRECTORY = TestHelper.SAVE_DIRECTORY + "pathing//move";

    protected void testScenario(Scenario scenario) {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase1 = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getHumanPlayerId(), userContext.getName());
        playerBase1.setResources(Double.MAX_VALUE);

        scenario.setup(playerBase1, getItemTypeService(), getBaseItemService());
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
        int tickCount = 0;
        while (isBaseServiceActive() || isPathingServiceMoving()) {
            actualTicks.add(getBaseItemService().getSyncBaseItemInfos());
            tickPlanetService();
            tickCount++;
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
