package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.WeldSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
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
        testScenario(new Scenario("moveEast1.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(100, 160));
            }
        });
    }

    @Test
    public void moveEast2() {
        testScenario(new Scenario("moveEast2.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(101, 160));
            }
        });
    }

    @Test
    public void moveEast3() {
        testScenario(new Scenario("moveEast3.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(110, 160));
            }
        });
    }

    @Test
    public void moveEast4() {
        testScenario(new Scenario("moveEast4.json", getClass()) {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(GameTestContent.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(40, 160), new DecimalPosition(90, 160));
            }
        });
    }

    @Test
    public void __moveEast2() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(64, 160), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.MOVING_TEST_ITEM_TYPE_ID);

        WeldSlaveEmulator slave = new WeldSlaveEmulator();
        slave.connectToMater(userContext, this);
        slave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem slaveBuilder = slave.findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);

        getCommandService().move(builder, new DecimalPosition(200, 160));

        slave.tickPlanetServiceBaseServiceActive();
        tickPlanetServiceBaseServiceActive();

        TestHelper.assertDecimalPosition("Unexpected master position", new DecimalPosition(200, 160), builder.getSyncPhysicalMovable().getPosition2d(), 0.2);
        TestHelper.assertDecimalPosition("Unexpected slave position", new DecimalPosition(200, 160), slaveBuilder.getSyncPhysicalMovable().getPosition2d(), 0.2);
    }

    @Test
    public void __moveAroundDriveway() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(79, 48), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);

        WeldSlaveEmulator slave = new WeldSlaveEmulator();
        slave.connectToMater(userContext, this);
        slave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem slaveBuilder = slave.findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);

        getCommandService().move(builder, new DecimalPosition(128, 48));

        slave.tickPlanetServiceBaseServiceActive();
        tickPlanetServiceBaseServiceActive();
        // showDisplay();

        TestHelper.assertDecimalPosition("Unexpected master position", new DecimalPosition(128, 48), builder.getSyncPhysicalMovable().getPosition2d(), 1.5);
        TestHelper.assertDecimalPosition("Unexpected slave position", new DecimalPosition(128, 48), slaveBuilder.getSyncPhysicalMovable().getPosition2d(), 1.5);
    }

}
