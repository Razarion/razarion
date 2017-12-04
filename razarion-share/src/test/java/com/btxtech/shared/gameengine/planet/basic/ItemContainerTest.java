package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.GameTestContent;
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
        getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(189, 193), getBaseItemType(GameTestContent.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), GameTestContent.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(GameTestContent.SHIP_TRANSPORTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem transporter = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), GameTestContent.SHIP_TRANSPORTER_ITEM_TYPE_ID);
        getCommandService().move(transporter, new DecimalPosition(146, 200));
        tickPlanetServiceBaseServiceActive();
        // Load 1.
        getCommandService().loadContainer(humanBaseContext.getBuilder(), transporter);
        tickPlanetServiceBaseServiceActive();
        // Verify loaded
        assertSyncItems(transporter.getSyncItemContainer().getContainedItems(), humanBaseContext.getBuilder());
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        Assert.assertNull(humanBaseContext.getBuilder().getSyncPhysicalArea().getPosition2d());
        Assert.assertFalse(humanBaseContext.getBuilder().getSyncPhysicalArea().hasPosition());
        assertNoCommand4Contained(humanBaseContext);
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
}
