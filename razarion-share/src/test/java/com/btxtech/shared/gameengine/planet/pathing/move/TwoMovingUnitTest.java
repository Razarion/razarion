package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Test;

/**
 * Created by Beat
 * on 22.05.2018.
 */
public class TwoMovingUnitTest extends ScenarioBaseTest {
    @Test
    public void frontal1() {
        testScenario(new Scenario("TwoMovingUnitTest_frontal1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(100, 160), new DecimalPosition(40, 160));
            }
        });
    }

    @Test
    public void frontal2() {
        testScenario(new Scenario("TwoMovingUnitTest_frontal2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(100, 161), new DecimalPosition(40, 161));
            }
        });
    }

    @Test
    public void parallel1() {
        testScenario(new Scenario("TwoMovingUnitTest_parallel1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 158), new DecimalPosition(100, 160));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 162), new DecimalPosition(100, 160));
            }
        });
    }

    @Test
    public void perpendicular1() {
        testScenario(new Scenario("TwoMovingUnitTest_perpendicular1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(120, 150), new DecimalPosition(180, 150));
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(150, 120), new DecimalPosition(150, 180));
            }
        });
    }

}