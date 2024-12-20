package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Test;

/**
 * Created by Beat
 * on 20.07.2018.
 */
public class PushAwayUnitTest extends ScenarioBaseTest {
    @Test
    public void singlePushAway1() {
        testScenario(new Scenario("PushAwayUnitTest_singlePushAway1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 140), new DecimalPosition(100, 140));
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(60, 140), null);
            }
        });
    }

    @Test
    public void singlePushAway2() {
        testScenario(new Scenario("PushAwayUnitTest_singlePushAway2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 140), new DecimalPosition(100, 140));
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(60, 141), null);
            }
        });
    }

    @Test
    public void doublePushAway1() {
        testScenario(new Scenario("PushAwayUnitTest_doublePushAway1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 140), new DecimalPosition(100, 140));
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(60, 140), null);
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(64, 140), null);
            }
        });
    }

    @Test
    public void doublePushAway2() {
        testScenario(new Scenario("PushAwayUnitTest_doublePushAway2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 140), new DecimalPosition(100, 140));
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(60, 141), null);
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(64, 141), null);
            }
        });
    }

    @Test
    public void multiplePushAway1() {
        testScenario(new Scenario("PushAwayUnitTest_multiplePushAway1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(100, 142), new DecimalPosition(160, 142));
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 2, new DecimalPosition(140, 144), null);
            }
        });
    }

    @Test
    public void multiplePushAway1R() {
        testScenario(new Scenario("PushAwayUnitTest_multiplePushAway1R.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 2, new DecimalPosition(140, 144), null);
                createSyncBaseItemSimplePath(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(100, 142), new DecimalPosition(160, 142));
            }
        });
    }

    @Test
    public void multiplePushAway2() {
        testScenario(new Scenario("PushAwayUnitTest_multiplePushAway2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(100, 144), new DecimalPosition(160, 144));
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(140, 144), null);
            }
        });
    }

    @Test
    public void multiplePushAway2R() {
        testScenario(new Scenario("PushAwayUnitTest_multiplePushAway2R.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(140, 144), null);
                createSyncBaseItemSimplePath(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(100, 144), new DecimalPosition(160, 144));
            }
        });
    }

    @Test
    public void multiplePushAway3() {
        testScenario(new Scenario("PushAwayUnitTest_multiplePushAway3.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 2, new DecimalPosition(100, 146), new DecimalPosition(170, 144));
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(140, 144), null);
            }
        });
    }

    @Test
    public void multiplePushAway3R() {
        testScenario(new Scenario("PushAwayUnitTest_multiplePushAway3R.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(140, 144), null);
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 2, new DecimalPosition(100, 146), new DecimalPosition(170, 144));
            }
        });
    }
}
