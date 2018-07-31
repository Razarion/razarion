package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by Beat
 * on 27.04.2018.
 */
public class MoveSingleUnitObstacleTest extends ScenarioBaseTest {

//    @Test
//    public void testGUI() {
//        UserContext userContext = createLevel1UserContext();
//        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getHumanPlayerId(), userContext.getName());
//        SyncBaseItem syncBaseItem = getBaseItemService().spawnSyncBaseItem(getItemTypeService().getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID), new DecimalPosition(145, 144), 0, playerBase, true);
//
//        showDisplay(new MouseMoveCallback().setCallback(mousePosition -> {
//            return null;
//        }));
//    }

    @Test
    public void moveAroundCorner1() {
        testScenario(new Scenario("MoveSingleUnitObstacleTest_moveAroundCorner1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItemSimplePath(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(176, 102), new DecimalPosition(182, 97));
            }
        });
    }

    @Test
    public void moveAroundDriveWayPath1() {
        testScenario(new Scenario("MoveSingleUnitObstacleTest_moveAroundDriveWayPath1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(168, 104), new DecimalPosition(152, 46));
            }
        });
    }

    @Test
    public void moveThroughDriveWayPath1() {
        testScenario(new Scenario("MoveSingleUnitObstacleTest_moveThroughDriveWayPath1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(168, 104), new DecimalPosition(72, 96));
            }
        });
    }

    @Test
    public void moveAroundSeaPath1() {
        testScenario(new Scenario("MoveSingleUnitObstacleTest_moveAroundSeaPath1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(220, 315), new DecimalPosition(149, 368));
            }
        });
    }

}
