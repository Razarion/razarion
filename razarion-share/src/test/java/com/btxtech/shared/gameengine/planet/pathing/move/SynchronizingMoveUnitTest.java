package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Test;

/**
 * Created by Beat
 * on 27.08.2018.
 */
public class SynchronizingMoveUnitTest extends ScenarioBaseTest {

    @Test
    public void single1() {
        testScenario(new Scenario("SynchronizingMoveUnitTest_single1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 140), new DecimalPosition(100, 140));
            }
        });
    }

    @Test
    public void multiple1() {
        testScenario(new Scenario("SynchronizingMoveUnitTest_multiple1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemGroup(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(40, 140), new DecimalPosition(100, 160));
            }
        });
    }

}
