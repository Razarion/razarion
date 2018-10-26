package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Test;

/**
 * Created by Beat
 * on 27.04.2018.
 */
public class MoveMultipleUnitObstacleTest extends ScenarioBaseTest {

    @Test
    public void move25ThroughDriveWay1() {
        testScenario(new Scenario("MoveMultipleUnitObstacleTest_move25ThroughDriveWay1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemGroup(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(136, 136), new DecimalPosition(72, 72));
            }
        });
    }

    @Test
    public void move25AroundTerrainObject1() {
        testScenario(new Scenario("MoveMultipleUnitObstacleTest_move25AroundTerrainObject1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemGroup(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, 5, new DecimalPosition(333, 20), new DecimalPosition(450, 140));
            }
        });
    }

}
