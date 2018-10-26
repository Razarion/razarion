package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Test;

/**
 * Created by Beat
 * on 18.03.2018.
 */
public class MoveMultipleUnitTest extends ScenarioBaseTest {

    @Test
    public void move4East1() {
        testScenario(new Scenario("MoveMultipleUnitTest_move4East1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 158), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 162), new DecimalPosition(100, 160));

                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(44, 158), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(44, 162), new DecimalPosition(100, 160));
            }
        });
    }

    @Test
    public void move6East1() {
        testScenario(new Scenario("MoveMultipleUnitTest_move6East1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 158), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 162), new DecimalPosition(100, 160));

                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(44, 158), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(44, 162), new DecimalPosition(100, 160));

                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(48, 158), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(48, 162), new DecimalPosition(100, 160));
            }
        });
    }

    @Test
    public void move25East1() {
        testScenario(new Scenario("MoveMultipleUnitTest_move25East1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemGroup(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(40, 160), new DecimalPosition(160, 160));
            }
        });
    }
}
