package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.planet.DaggerSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 01.12.2017.
 */
public class ItemContainerTest extends BaseBasicTest {

    @Test
    public void land() {
        setup();

        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(createLevel1UserContext(), this);
        getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(189, 193), getBaseItemType(FallbackConfig.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem transporter = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID);
        getCommandService().move(transporter, new DecimalPosition(146, 200));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        assertAllSlaves(permSlave, transporter, 0, humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(0, transporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);
        // Load 1.
        getCommandService().loadContainer(humanBaseContext.getBuilder(), transporter);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        // Verify loaded
        assertContainingSyncItems(transporter.getSyncItemContainer().getContainedItems(), humanBaseContext.getBuilder());
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        Assert.assertNull(humanBaseContext.getBuilder().getAbstractSyncPhysical().getPosition());
        Assert.assertFalse(humanBaseContext.getBuilder().getAbstractSyncPhysical().hasPosition());
        assertNoCommand4Contained(humanBaseContext);
        assertAllSlaves(permSlave, transporter, 3, humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(3, transporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);
        // Load 2.
        getCommandService().loadContainer(humanBaseContext.getAttacker1(), transporter);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        // Verify loaded
        assertContainingSyncItems(transporter.getSyncItemContainer().getContainedItems(), humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        Assert.assertNull(humanBaseContext.getBuilder().getAbstractSyncPhysical().getPosition());
        Assert.assertFalse(humanBaseContext.getBuilder().getAbstractSyncPhysical().hasPosition());
        assertNoCommand4Contained(humanBaseContext);
        Assert.assertEquals(transporter, humanBaseContext.getAttacker1().getContainedIn());
        Assert.assertNull(humanBaseContext.getAttacker1().getAbstractSyncPhysical().getPosition());
        Assert.assertFalse(humanBaseContext.getAttacker1().getAbstractSyncPhysical().hasPosition());
        assertAllSlaves(permSlave, transporter, 3, humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(3, transporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);
        // Move to unload position
        getCommandService().move(transporter, new DecimalPosition(63, 222));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        // Unload
        getCommandService().unloadContainer(transporter, new DecimalPosition(47, 222));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        // Verify unloaded
        assertContainingSyncItems(transporter.getSyncItemContainer().getContainedItems());
        Assert.assertNull(humanBaseContext.getBuilder().getContainedIn());
        Assert.assertEquals(new DecimalPosition(47, 222), humanBaseContext.getBuilder().getAbstractSyncPhysical().getPosition());
        Assert.assertTrue(humanBaseContext.getBuilder().getAbstractSyncPhysical().hasPosition());
        Assert.assertNull(humanBaseContext.getAttacker1().getContainedIn());
        Assert.assertEquals(new DecimalPosition(47, 222), humanBaseContext.getAttacker1().getAbstractSyncPhysical().getPosition());
        Assert.assertTrue(humanBaseContext.getAttacker1().getAbstractSyncPhysical().hasPosition());
        assertAllSlaves(permSlave, transporter, 0, humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(0, transporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);

        // showDisplay();
    }

    @Test
    public void sellContainer() {
        setup();

        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(createLevel1UserContext(), this);
        getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(189, 193), getBaseItemType(FallbackConfig.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem transporter = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID);
        getCommandService().move(transporter, new DecimalPosition(146, 200));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        assertAllSlaves(permSlave, transporter, 0, humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(0, transporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);
        // Load 1.
        getCommandService().loadContainer(humanBaseContext.getBuilder(), transporter);
        tickPlanetServiceBaseServiceActive();
        // Verify loaded
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        assertSyncItemCount(5, 0,0);
        // Sell
        getBaseItemService().sellItems(Collections.singletonList(transporter.getId()), humanBaseContext.getPlayerBaseFull());
        // Verify sold
        assertSyncItemCount(3, 0,0);
        Assert.assertNull(getSyncItemContainerService().getSyncBaseItem(transporter.getId()));
        Assert.assertNull(getSyncItemContainerService().getSyncBaseItem(humanBaseContext.getBuilder().getId()));
    }

    @Test
    public void sellContainerBaseLost() {
        setup();

        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(createLevel1UserContext(), this);
        getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(189, 193), getBaseItemType(FallbackConfig.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem transporter = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID);
        getCommandService().move(transporter, new DecimalPosition(146, 200));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        assertAllSlaves(permSlave, transporter, 0, humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(0, transporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);
        // Sell others
        getBaseItemService().sellItems(Arrays.asList(humanBaseContext.getAttacker1().getId(), humanBaseContext.getFactory().getId(), harbour.getId()), humanBaseContext.getPlayerBaseFull());
        // Load 1.
        getCommandService().loadContainer(humanBaseContext.getBuilder(), transporter);
        tickPlanetServiceBaseServiceActive();
        // Verify loaded
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        assertSyncItemCount(2, 0,0);
        // Sell
        getBaseItemService().sellItems(Collections.singletonList(transporter.getId()), humanBaseContext.getPlayerBaseFull());
        // Verify sold
        assertSyncItemCount(0, 0,0);
        Assert.assertNull(getSyncItemContainerService().getSyncBaseItem(transporter.getId()));
        Assert.assertNull(getSyncItemContainerService().getSyncBaseItem(humanBaseContext.getBuilder().getId()));
        // Verify base deleted
        try {
            getBaseItemService().getPlayerBase4BaseId(humanBaseContext.getPlayerBaseFull().getBaseId());
            Assert.fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("No base for BaseId:"));
        }
    }

    @Test
    public void killContainer() {
        setup();

        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(createLevel1UserContext(), this);
        getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(189, 193), getBaseItemType(FallbackConfig.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem transporter = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID);
        getCommandService().move(transporter, new DecimalPosition(146, 200));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        assertAllSlaves(permSlave, transporter, 0, humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(0, transporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);
        // Sell others
        getBaseItemService().sellItems(Arrays.asList(humanBaseContext.getAttacker1().getId(), humanBaseContext.getFactory().getId(), harbour.getId()), humanBaseContext.getPlayerBaseFull());
        // Load 1.
        getCommandService().loadContainer(humanBaseContext.getBuilder(), transporter);
        tickPlanetServiceBaseServiceActive();
        // Verify loaded
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        assertSyncItemCount(2, 0,0);
        // Setup bot
        setupSimpleAttackerBot();
        // Kill transporter
        getCommandService().move(transporter, new DecimalPosition(116, 332));
        tickPlanetServiceBaseServiceActive();
        // Verify killed
        Assert.assertNull(getSyncItemContainerService().getSyncBaseItem(transporter.getId())); // TODO Failed on 07.03.2018
        Assert.assertNull(getSyncItemContainerService().getSyncBaseItem(humanBaseContext.getBuilder().getId()));
        // Verify base deleted
        try {
            getBaseItemService().getPlayerBase4BaseId(humanBaseContext.getPlayerBaseFull().getBaseId());
            Assert.fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("No base for BaseId:"));
        }
    }

    @Test
    public void killContainerBaseLost() {
        setup();

        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(createLevel1UserContext(), this);
        getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(189, 193), getBaseItemType(FallbackConfig.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem transporter = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.SHIP_TRANSPORTER_ITEM_TYPE_ID);
        getCommandService().move(transporter, new DecimalPosition(146, 200));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        assertAllSlaves(permSlave, transporter, 0, humanBaseContext.getBuilder(), humanBaseContext.getAttacker1());
        Assert.assertEquals(0, transporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);
        // Load 1.
        getCommandService().loadContainer(humanBaseContext.getBuilder(), transporter);
        tickPlanetServiceBaseServiceActive();
        // Verify loaded
        Assert.assertEquals(transporter, humanBaseContext.getBuilder().getContainedIn());
        assertSyncItemCount(5, 0,0);
        // Setup bot
        setupSimpleAttackerBot();
        // Kill transporter
        getCommandService().move(transporter, new DecimalPosition(116, 352));
        tickPlanetServiceBaseServiceActive();
        // Verify killed
        Assert.assertNull(getSyncItemContainerService().getSyncBaseItem(transporter.getId()));
        Assert.assertNull(getSyncItemContainerService().getSyncBaseItem(humanBaseContext.getBuilder().getId()));
    }


    private void assertNoCommand4Contained(HumanBaseContext humanBaseContext) {
        try {
            getCommandService().move(humanBaseContext.getBuilder(), new DecimalPosition(146, 150));
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException ie) {
            Assert.assertTrue(ie.getMessage(), ie.getMessage().startsWith("CommandService.checkSyncBaseItem() Item is inside a item container:"));
        }
        try {
            getCommandService().build(humanBaseContext.getBuilder(), new DecimalPosition(146, 150), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
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
            getCommandService().fabricate(humanBaseContext.getBuilder(), getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
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

    private void assertAllSlaves(DaggerSlaveEmulator permSlave, SyncBaseItem masterTransporter, double maxContainingRadius, SyncBaseItem... masterContainedIn) {
        assertNewSlave(masterTransporter, maxContainingRadius, masterContainedIn);
        assertSlave(permSlave, masterTransporter, maxContainingRadius, masterContainedIn);
    }

    private void assertNewSlave(SyncBaseItem masterTransporter, double maxContainingRadius, SyncBaseItem... masterContainedIns) {
        UserContext tmpUserContext = createLevel1UserContext();
        DaggerSlaveEmulator tmpSalve = new DaggerSlaveEmulator();
        tmpSalve.connectToMaster(tmpUserContext, this);
        assertSlave(tmpSalve, masterTransporter, maxContainingRadius, masterContainedIns);
    }

    private void assertSlave(DaggerSlaveEmulator weldSlaveEmulator, SyncBaseItem masterTransporter, double maxContainingRadius, SyncBaseItem... masterContainedIns) {
        SyncBaseItem slaveTransporter = weldSlaveEmulator.getSyncItemContainerService().getSyncBaseItemSave(masterTransporter.getId());
        Assert.assertEquals(maxContainingRadius, slaveTransporter.getSyncItemContainer().getMaxContainingRadius(), 0.001);
        assertContainingSyncItems(masterTransporter.getSyncItemContainer().getContainedItems(), slaveTransporter.getSyncItemContainer().getContainedItems().toArray(new SyncBaseItem[]{}));
        for (SyncBaseItem masterContainedIn : masterContainedIns) {
            SyncBaseItem slaveContainedIn = weldSlaveEmulator.getSyncItemContainerService().getSyncBaseItemSave(masterContainedIn.getId());
            Assert.assertEquals("masterContainedIn: " + masterContainedIn + " slaveContainedIn: " + slaveContainedIn, masterContainedIn.getContainedIn(), slaveContainedIn.getContainedIn());
            // TODO No tick engine on the client. Wait until PathingService is done. TestHelper.assertDecimalPosition("masterContainedIn: " + masterContainedIn + " slaveContainedIn: " + slaveContainedIn, masterContainedIn.getSyncPhysicalArea().getPosition2d(), slaveContainedIn.getSyncPhysicalArea().getPosition2d(), 1.0);
            Assert.assertEquals("masterContainedIn: " + masterContainedIn + " slaveContainedIn: " + slaveContainedIn, masterContainedIn.getAbstractSyncPhysical().hasPosition(), slaveContainedIn.getAbstractSyncPhysical().hasPosition());
        }
    }

    public void setupSimpleAttackerBot() {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.SHIP_ATTACKER_ITEM_TYPE_ID).count(5).createDirectly(true));
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name("Test bot").npc(false).realm(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(96,312, 40, 40))));
        getBotService().startBots(botConfigs);
        tickPlanetServiceBaseServiceActive();
    }
}
