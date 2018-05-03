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
    public void moveMultipleEast1() {
        testScenario(new Scenario("moveMultipleEast1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 162), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 158), new DecimalPosition(100, 160));
            }
        });
    }

    @Test
    public void moveMultipleEast2() {
        testScenario(new Scenario("moveMultipleEast2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(36, 160), new DecimalPosition(100, 160));
            }
        });
    }

    @Test
    public void move3East1() {
        testScenario(new Scenario("move3East1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 150), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(45, 160), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 170), new DecimalPosition(100, 160));
            }
        });
    }

    @Test
    public void movePushAway1() {
        testScenario(new Scenario("movePushAway1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(80, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(80, 160), null);
            }
        });
    }

    @Test
    public void movePushAway2() {
        testScenario(new Scenario("movePushAway2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(80, 160), null);
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(80, 160));
            }
        });
    }
}
