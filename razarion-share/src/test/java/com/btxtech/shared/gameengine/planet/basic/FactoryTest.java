package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
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
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(185, 140), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);
        getCommandService().fabricate(factory, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
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
        SyncBaseItem attacker = findSyncBaseItem(playerBaseFull, GameTestContent.ATTACKER_ITEM_TYPE_ID);
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
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(189, 193), getBaseItemType(GameTestContent.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem harbour = findSyncBaseItem(playerBaseFull, GameTestContent.HARBOUR_ITEM_TYPE_ID);
        getCommandService().fabricate(harbour, getBaseItemType(GameTestContent.SHIP_ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        assertSyncItemCount(3, 0, 0);
        SyncBaseItem ship = findSyncBaseItem(playerBaseFull, GameTestContent.SHIP_ATTACKER_ITEM_TYPE_ID);
        Assert.assertEquals(0, ship.getBuildup(), 1);
        Assert.assertEquals(30, ship.getHealth(), 1);

        showDisplay();
    }

    @Test
    public void landMultiple() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(185, 140), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);

        for (int i = 0; i < 10; i++) {
            getCommandService().fabricate(factory, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
            tickPlanetServiceBaseServiceActive();
        }
        assertSyncItemCount(12, 0, 0);

        showDisplay();
    }
}
