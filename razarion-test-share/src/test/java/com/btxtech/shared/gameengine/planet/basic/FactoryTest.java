package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 17.11.2017.
 */
public class FactoryTest extends BaseBasicTest {

    @Test
    public void land() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(185, 140), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        SyncFactory syncFactory = factory.getSyncFactory();
        Assert.assertEquals(0, syncFactory.getBuildup(), 0.0001);
        tickPlanetServiceSeconds(1);
        Assert.assertEquals(0.25, syncFactory.getBuildup(), 0.0001);
        tickPlanetServiceSeconds(1);
        Assert.assertEquals(0.5, syncFactory.getBuildup(), 0.0001);
        tickPlanetServiceSeconds(1);
        Assert.assertEquals(0.75, syncFactory.getBuildup(), 0.0001);
        tickPlanetServiceSeconds(1);
        Assert.assertEquals(0, syncFactory.getBuildup(), 0.0001);
        assertSyncItemCount(3, 0, 0);
        SyncBaseItem attacker = findSyncBaseItem(playerBaseFull, FallbackConfig.ATTACKER_ITEM_TYPE_ID);
        Assert.assertEquals(0, attacker.getBuildup(), 1);
        Assert.assertEquals(20, attacker.getHealth(), 1);

        // showDisplay();
    }

    @Test
    public void coastWater() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(189, 193), getBaseItemType(FallbackConfig.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(playerBaseFull, FallbackConfig.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(FallbackConfig.SHIP_ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        assertSyncItemCount(3, 0, 0);
        SyncBaseItem ship = findSyncBaseItem(playerBaseFull, FallbackConfig.SHIP_ATTACKER_ITEM_TYPE_ID);
        Assert.assertEquals(0, ship.getBuildup(), 1);
        Assert.assertEquals(30, ship.getHealth(), 1);

        // showDisplay();
    }

    @Test
    public void landBuildQueue() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(185, 140), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        SyncFactory syncFactory = factory.getSyncFactory();

        // Fabricate three attackers back-to-back (no drain in between) so two get queued behind the
        // active one. All three queued commands execute in the next single tick.
        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetService();
        Assert.assertTrue(syncFactory.isActive());
        Assert.assertEquals(2, syncFactory.getBuildQueue().size());

        // Draining the whole queue produces all three units and leaves the factory idle.
        tickPlanetServiceBaseServiceActive();
        Assert.assertFalse(syncFactory.isActive());
        Assert.assertEquals(0, syncFactory.getBuildQueue().size());
        assertSyncItemCount(5, 0, 0); // builder + factory + 3 attackers

        // showDisplay();
    }

    @Test
    public void landBuildQueueCancel() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(185, 140), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        SyncFactory syncFactory = factory.getSyncFactory();

        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetService();
        Assert.assertEquals(2, syncFactory.getBuildQueue().size());

        // Cancel one waiting entry -> queue shrinks, active build unaffected.
        getCommandService().cancelFactoryQueue(factory.getId(), 0);
        tickPlanetService();
        Assert.assertEquals(1, syncFactory.getBuildQueue().size());
        Assert.assertTrue(syncFactory.isActive());

        // Two units total remain (active + one queued).
        tickPlanetServiceBaseServiceActive();
        Assert.assertFalse(syncFactory.isActive());
        assertSyncItemCount(4, 0, 0); // builder + factory + 2 attackers

        // showDisplay();
    }

    @Test
    public void landMultiple() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(185, 140), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);

        for (int i = 0; i < 10; i++) {
            getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
            tickPlanetServiceBaseServiceActive();
        }
        assertSyncItemCount(12, 0, 0);

        // showDisplay();
    }
}
