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
public class MoveSingleUnitTest extends ScenarioBaseTest {

    @Test
    public void moveEast1() {
        testScenario(new Scenario("MoveSingleUnitTest_moveEast1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(100, 160));
            }
        });
    }

    @Test
    public void moveEast2() {
        testScenario(new Scenario("MoveSingleUnitTest_moveEast2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(60, 160));
            }
        });
    }

    @Test
    public void moveNorth1() {
        testScenario(new Scenario("MoveSingleUnitTest_moveNorth1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(16, 160), new DecimalPosition(16, 176));
            }
        });
    }

    @Test
    public void moveWest1() {
        testScenario(new Scenario("MoveSingleUnitTest_moveWest1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(64, 160), new DecimalPosition(16, 160));
            }
        });
    }

    @Test
    public void moveSouth1() {
        testScenario(new Scenario("MoveSingleUnitTest_moveSouth1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(24, 224), new DecimalPosition(24, 184));
            }
        });
    }
}
