package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.DaggerSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

import static com.btxtech.shared.TestHelper.assertDecimalPosition;

/**
 * Created by Beat
 * on 20.10.2017.
 */
public class BuildTest extends BaseBasicTest {

    @Test
    public void landLand() {
        setup();

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(userContext, this);

        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();

        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        assertDecimalPosition("Position", builder.getAbstractSyncPhysical().getPosition(), slaveBuilder.getAbstractSyncPhysical().getPosition());

        getCommandService().build(builder, new DecimalPosition(104, 144), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();

        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        assertFactory(factory, 30, FallbackConfig.FACTORY_ITEM_TYPE_ID, new DecimalPosition(104, 144));

        SyncBaseItem slaveFactory = permSlave.getSyncItemContainerService().getSyncBaseItemSave(factory.getId());
        assertFactory(slaveFactory, 30, FallbackConfig.FACTORY_ITEM_TYPE_ID, new DecimalPosition(104, 144));

        // showDisplay();
    }

    @Test
    public void landWater() {
        setup();

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(userContext, this);

        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(150, 150), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();

        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        assertDecimalPosition("Position", builder.getAbstractSyncPhysical().getPosition(), slaveBuilder.getAbstractSyncPhysical().getPosition());

        //showDisplay();

        // Build harbour
        getCommandService().build(builder, new DecimalPosition(183, 173), getBaseItemType(FallbackConfig.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();

        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.HARBOUR_ITEM_TYPE_ID);
        //assertFactory(factory, 40, FallbackConfig.HARBOUR_ITEM_TYPE_ID, new DecimalPosition(174.5, 194.5));

        SyncBaseItem slaveFactory = permSlave.getSyncItemContainerService().getSyncBaseItemSave(factory.getId());
        //assertFactory(slaveFactory, 40, FallbackConfig.HARBOUR_ITEM_TYPE_ID, new DecimalPosition(174.5, 194.5));
    }

    private void assertFactory(SyncBaseItem factory, double health, int factoryItemTypeId, DecimalPosition position) {
        Assert.assertTrue(factory.isBuildup());
        Assert.assertEquals(health, factory.getHealth(), 0.001);
        Assert.assertEquals(factoryItemTypeId, factory.getBaseItemType().getId());
        assertDecimalPosition(null, position, factory.getAbstractSyncPhysical().getPosition());
    }

}
