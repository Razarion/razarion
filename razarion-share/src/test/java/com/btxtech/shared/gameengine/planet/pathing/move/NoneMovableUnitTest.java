package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Beat
 * on 27.04.2018.
 */
public class NoneMovableUnitTest extends ScenarioBaseTest {

    @Test
    public void oneNoneMovable11Movable1() {
        testScenario(new Scenario("NoneMovableUnitTest_oneNoneMovable11Movable1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(FallbackConfig.FACTORY_ITEM_TYPE_ID, new DecimalPosition(350, 280), null);
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(300, 280), new DecimalPosition(400, 280));
            }
        });
    }

    @Test
    @Ignore // Can not be solved with ORCA veoloicty Opstale http://gamma.cs.unc.edu/ORCA/
    public void twoNoneMovable11Movable1() {
        testScenario(new Scenario("NoneMovableUnitTest_twoNoneMovable11Movable1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(FallbackConfig.FACTORY_ITEM_TYPE_ID, new DecimalPosition(350, 290), null);
                createSyncBaseItem(FallbackConfig.FACTORY_ITEM_TYPE_ID, new DecimalPosition(350, 280), null);
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(300, 285), new DecimalPosition(400, 285));
            }
        });
    }

    @Test
    @Ignore // Can not be solved with ORCA veoloicty Opstale http://gamma.cs.unc.edu/ORCA/
    public void twoNoneMovable11Movable2() {
        testScenario(new Scenario("NoneMovableUnitTest_twoNoneMovable11Movable2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(FallbackConfig.FACTORY_ITEM_TYPE_ID, new DecimalPosition(357, 287), null);
                createSyncBaseItem(FallbackConfig.FACTORY_ITEM_TYPE_ID, new DecimalPosition(350, 280), null);
                createSyncBaseItem(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(300, 282), new DecimalPosition(400, 282));
            }
        });
    }

    @Test
    public void oneNoneMovable125Movable1() {
        testScenario(new Scenario("NoneMovableUnitTest_singleMovableTwoNoneMovable2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(FallbackConfig.FACTORY_ITEM_TYPE_ID, new DecimalPosition(350, 280), null);
                createSyncBaseItemGroup(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(300, 280), new DecimalPosition(400, 280));
            }
        });
    }
}
