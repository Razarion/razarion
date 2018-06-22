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
    public void moveAroundDriveWay1() {
        testScenario(new Scenario("MoveMultipleUnitObstacleTest.moveAroundDriveWay1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                DecimalPosition start = new DecimalPosition(136,136);
                DecimalPosition destination = new DecimalPosition(72,72);

                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(4 * x + start.getX(), 4 * y + start.getY()), destination);
                    }
                }


            }
        });
    }
}
