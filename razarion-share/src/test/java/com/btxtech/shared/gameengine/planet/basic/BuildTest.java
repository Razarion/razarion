package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 20.10.2017.
 */
public class BuildTest extends BaseBasicTest {

    @Test
    public void land() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(104, 144), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);

        Assert.assertTrue(factory.isBuildup());
        Assert.assertEquals(30, factory.getHealth(), 0.001);
        Assert.assertEquals(GameTestContent.FACTORY_ITEM_TYPE_ID, factory.getBaseItemType().getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(104, 144), factory.getSyncPhysicalArea().getPosition2d());
        // showDisplay();
    }

    @Test
    public void waterCoast() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(240, 130), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        // Build harbour
        getCommandService().build(builder, new DecimalPosition(174.5, 194.5), getBaseItemType(GameTestContent.HARBOUR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.HARBOUR_ITEM_TYPE_ID);
        // Verify
        Assert.assertTrue(factory.isBuildup());
        Assert.assertEquals(40, factory.getHealth(), 0.001);
        Assert.assertEquals(GameTestContent.HARBOUR_ITEM_TYPE_ID, factory.getBaseItemType().getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(174.5, 194.5), factory.getSyncPhysicalArea().getPosition2d());
    }

}
