package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * on 20.09.2017.
 */
public class ItemLimitationTest extends WeldMasterBaseTest {

    @Test
    public void testBuilder() {
        setupMasterEnvironment();

        UserContext userContext1 = createLevel1UserContext(1);
        PlayerBaseFull playerBaseFull1 = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext1);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder1 = findSyncBaseItem(playerBaseFull1, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder1, new DecimalPosition(40, 20), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        getCommandService().build(builder1, new DecimalPosition(80, 20), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertSyncItemCount(3, 0, 0);
        // Try to build third factory -> not allowed
        getCommandService().build(builder1, new DecimalPosition(120, 20), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertSyncItemCount(3, 0, 0);
        // Unlock
        Map<Integer, Integer> unlockedItemLimit = new HashMap<>();
        unlockedItemLimit.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 1);
        getBaseItemService().updateUnlockedItemLimit(userContext1.getHumanPlayerId(), unlockedItemLimit);
        getCommandService().build(builder1, new DecimalPosition(120, 20), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertSyncItemCount(4, 0, 0);
    }

    @Test
    public void testFactoryLimit() {
        setupMasterEnvironment();

        UserContext userContext1 = createLevel1UserContext(1);
        PlayerBaseFull playerBaseFull1 = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext1);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder1 = findSyncBaseItem(playerBaseFull1, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder1, new DecimalPosition(40, 20), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory1 = findSyncBaseItem(playerBaseFull1, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        // Verify
        assertSyncItemCount(2, 0, 0);
        // Try to build second builder -> not allowed
        getCommandService().fabricate(factory1, getBaseItemType(FallbackConfig.BUILDER_ITEM_TYPE_ID));
        tickPlanetService(100000L);
        assertSyncItemCount(2, 0, 0);
        // Unlock
        Map<Integer, Integer> unlockedItemLimit = new HashMap<>();
        unlockedItemLimit.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        getBaseItemService().updateUnlockedItemLimit(userContext1.getHumanPlayerId(), unlockedItemLimit);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertSyncItemCount(3, 0, 0);
    }
}
