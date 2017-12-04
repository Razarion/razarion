package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.WeldSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 01.12.2017.
 */
public class ItemContainerTest extends BaseBasicTest {

    @Test
    public void land() {
        setup();

        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        WeldSlaveEmulator permSlave = new WeldSlaveEmulator();
        permSlave.connectToMater(createLevel1UserContext(), this);
        getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(189, 193), getBaseItemType(GameTestContent.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), GameTestContent.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(GameTestContent.SHIP_TRANSPORTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem transporter = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), GameTestContent.SHIP_TRANSPORTER_ITEM_TYPE_ID);
        getCommandService().move(transporter, new DecimalPosition(146, 200));
        tickPlanetServiceBaseServiceActive();
        assertAllSlaves(permSlave, transporter, humanBaseContext.getBuilder(), humanBaseContext.getAttacker());
        // Load 1.
        getCommandService().loadContainer(humanBaseContext.getBuilder(), transporter);
        tickPlanetServiceBaseServiceActive();
        // Verify loaded
        assertSyncItems(transporter.getSyncItemContainer().getContainedItems(), humanBaseContext.getBuilder());
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        Assert.assertNull(humanBaseContext.getBuilder().getSyncPhysicalArea().getPosition2d());
        Assert.assertFalse(humanBaseContext.getBuilder().getSyncPhysicalArea().hasPosition());
        assertNoCommand4Contained(humanBaseContext);
        assertAllSlaves(permSlave, transporter, humanBaseContext.getBuilder(), humanBaseContext.getAttacker());
        // Load 2.
        getCommandService().loadContainer(humanBaseContext.getAttacker(), transporter);
        tickPlanetServiceBaseServiceActive();
        // Verify loaded
        assertSyncItems(transporter.getSyncItemContainer().getContainedItems(), humanBaseContext.getBuilder(), humanBaseContext.getAttacker());
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        Assert.assertNull(humanBaseContext.getBuilder().getSyncPhysicalArea().getPosition2d());
        Assert.assertFalse(humanBaseContext.getBuilder().getSyncPhysicalArea().hasPosition());
        assertNoCommand4Contained(humanBaseContext);
        Assert.assertEquals(transporter, humanBaseContext.getAttacker().getContainedIn());
        Assert.assertNull(humanBaseContext.getAttacker().getSyncPhysicalArea().getPosition2d());
        Assert.assertFalse(humanBaseContext.getAttacker().getSyncPhysicalArea().hasPosition());
        assertAllSlaves(permSlave, transporter, humanBaseContext.getBuilder(), humanBaseContext.getAttacker());
        // Move to unload position
        getCommandService().move(transporter, new DecimalPosition(63, 222));
        tickPlanetServiceBaseServiceActive();
        // Unload
        getCommandService().unloadContainer(transporter, new DecimalPosition(47, 222));
        tickPlanetServiceBaseServiceActive();
        // Verify unloaded
        assertSyncItems(transporter.getSyncItemContainer().getContainedItems());
        Assert.assertNull(humanBaseContext.getBuilder().getContainedIn());
        Assert.assertEquals(new DecimalPosition(47, 222), humanBaseContext.getBuilder().getSyncPhysicalArea().getPosition2d());
        Assert.assertTrue(humanBaseContext.getBuilder().getSyncPhysicalArea().hasPosition());
        Assert.assertNull(humanBaseContext.getAttacker().getContainedIn());
        Assert.assertEquals(new DecimalPosition(47, 222), humanBaseContext.getAttacker().getSyncPhysicalArea().getPosition2d());
        Assert.assertTrue(humanBaseContext.getAttacker().getSyncPhysicalArea().hasPosition());
        assertAllSlaves(permSlave, transporter, humanBaseContext.getBuilder(), humanBaseContext.getAttacker());

        // showDisplay();
    }

    private void assertNoCommand4Contained(HumanBaseContext humanBaseContext) {
        try {
            getCommandService().move(humanBaseContext.getBuilder(), new DecimalPosition(146, 150));
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(146, 150), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().finalizeBuild(humanBaseContext.getBuilder(), humanBaseContext.getFactory());
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().fabricate(humanBaseContext.getBuilder(), getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().harvest(humanBaseContext.getBuilder(), null);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().attack(humanBaseContext.getBuilder(), humanBaseContext.getFactory(), true);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().attack(humanBaseContext.getFactory(), humanBaseContext.getBuilder(), true);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().pickupBox(humanBaseContext.getBuilder(), null);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().loadContainer(humanBaseContext.getBuilder(), humanBaseContext.getFactory());
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().loadContainer(humanBaseContext.getFactory(), humanBaseContext.getBuilder());
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().unloadContainer(humanBaseContext.getBuilder(), new DecimalPosition(150, 100));
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().defend(humanBaseContext.getBuilder(), humanBaseContext.getFactory());
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().defend(humanBaseContext.getFactory(), humanBaseContext.getBuilder());
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
    }

    private void assertAllSlaves(WeldSlaveEmulator permSlave, SyncBaseItem masterTransporter, SyncBaseItem... masterContainedIn) {
        assertNewSlave(masterTransporter, masterContainedIn);
        assertSlave(permSlave, masterTransporter, masterContainedIn);
    }

    private void assertNewSlave(SyncBaseItem masterTransporter, SyncBaseItem... masterContainedIns) {
        UserContext tmpUserContext = createLevel1UserContext();
        WeldSlaveEmulator tmpSalve = new WeldSlaveEmulator();
        tmpSalve.connectToMater(tmpUserContext, this);
        assertSlave(tmpSalve, masterTransporter, masterContainedIns);
    }

    private void assertSlave(WeldSlaveEmulator weldSlaveEmulator, SyncBaseItem masterTransporter, SyncBaseItem... masterContainedIns) {
        SyncBaseItem slaveTransporter = weldSlaveEmulator.getSyncItemContainerService().getSyncBaseItemSave(masterTransporter.getId());
        TestHelper.assertIds(masterTransporter.getSyncItemContainer().getContainedItems(), slaveTransporter.getSyncItemContainer().getContainedItems().toArray(new Integer[]{}));

        for (SyncBaseItem masterContainedIn : masterContainedIns) {
            SyncBaseItem slaveContainedIn = weldSlaveEmulator.getSyncItemContainerService().getSyncBaseItemSave(masterContainedIn.getId());
            Assert.assertEquals("masterContainedIn: " + masterContainedIn + " slaveContainedIn: " + slaveContainedIn, masterContainedIn.getContainedIn(), slaveContainedIn.getContainedIn());
            TestHelper.assertDecimalPosition("masterContainedIn: " + masterContainedIn + " slaveContainedIn: " + slaveContainedIn, masterContainedIn.getSyncPhysicalArea().getPosition2d(), slaveContainedIn.getSyncPhysicalArea().getPosition2d(), 1.0);
            Assert.assertEquals("masterContainedIn: " + masterContainedIn + " slaveContainedIn: " + slaveContainedIn, masterContainedIn.getSyncPhysicalArea().hasPosition(), slaveContainedIn.getSyncPhysicalArea().hasPosition());
        }
    }

}
