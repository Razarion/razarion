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
public class MoveSingleUnitObstacleTest extends ScenarioBaseTest {

    @Test
    public void moveAroundDriveWay1() {
        testScenario(new Scenario("moveAroundDriveWay1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(168, 104), new DecimalPosition(152, 46));
            }
        });
    }

    @Test
    public void moveThroughDriveWay1() {
        testScenario(new Scenario("moveThroughDriveWay1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(168, 104), new DecimalPosition(72, 96));
            }
        });
    }

}