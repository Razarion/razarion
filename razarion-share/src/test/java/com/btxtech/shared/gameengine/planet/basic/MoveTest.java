package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.WeldSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class MoveTest extends BaseBasicTest {
    @Test
    public void landSimpleTurn() {
        setup();

        UserContext userContext = createLevel1UserContext();
        WeldSlaveEmulator permSlave = new WeldSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(88, 144), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(40, 144));
        // showDisplay();
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        TestHelper.assertDecimalPosition(null, new DecimalPosition(40, 144), builder.getSyncPhysicalArea().getPosition2d(), 0.5);
        Assert.assertNull(builder.getSyncPhysicalMovable().getVelocity());

        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(40, 144), slaveBuilder.getSyncPhysicalArea().getPosition2d(), 0.5);
        Assert.assertNull(slaveBuilder.getSyncPhysicalMovable().getVelocity());
    }

    @Test
    public void land() {
        setup();

        UserContext userContext = createLevel1UserContext();
        WeldSlaveEmulator permSlave = new WeldSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(40, 144), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(152, 32));
        // showDisplay();
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        TestHelper.assertDecimalPosition(null, new DecimalPosition(152, 32), builder.getSyncPhysicalArea().getPosition2d(), 0.5);
        Assert.assertNull(builder.getSyncPhysicalMovable().getVelocity());

        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(152, 32), slaveBuilder.getSyncPhysicalArea().getPosition2d(), 0.5);
        Assert.assertNull(slaveBuilder.getSyncPhysicalMovable().getVelocity());
    }

    @Test
    public void landSecondCommand() {
        setup();

        UserContext userContext = createLevel1UserContext();
        WeldSlaveEmulator permSlave = new WeldSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(132, 144), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(168, 144));
        tickPlanetService(75);
        permSlave.tickPlanetService(75);
        getCommandService().move(builder, new DecimalPosition(184, 120));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();

        // showDisplay();
        TestHelper.assertDecimalPosition(null, new DecimalPosition(184, 120), builder.getSyncPhysicalArea().getPosition2d(), 0.5);
        Assert.assertNull(builder.getSyncPhysicalMovable().getVelocity());

        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(184, 120), slaveBuilder.getSyncPhysicalArea().getPosition2d(), 0.5);
        Assert.assertNull(slaveBuilder.getSyncPhysicalMovable().getVelocity());
    }

}
