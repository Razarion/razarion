package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Engine-free test of the position-quest timer logic (CountPositionComparison).
 * The full Dagger integration harness hangs on pathing, so this drives tick() directly.
 */
public class PositionTimerUnitTest {

    @SuppressWarnings("unchecked")
    private Collection<SyncBaseItem> backingItems(PlayerBaseFull playerBaseFull) {
        return (Collection<SyncBaseItem>) SimpleTestEnvironment.readField("items", playerBaseFull);
    }

    private SyncBaseItem createItem(int id) {
        return createItem(id, null);
    }

    private SyncBaseItem createItem(int id, BaseItemType baseItemType) {
        SyncBaseItem syncBaseItem = new SyncBaseItem(null, null, null, null, null, null, null, null, null, null, null, null, null);
        syncBaseItem.init(id, baseItemType);
        syncBaseItem.setBuildup(1.0);
        SimpleTestEnvironment.injectService("health", syncBaseItem, SyncBaseItem.class, 100.0);
        return syncBaseItem;
    }

    private BaseItemPositionComparison createTypeComparison(PlayerBaseFull playerBaseFull, Map<BaseItemType, Integer> itemTypes, Integer timeSeconds) {
        BaseItemService baseItemService = new BaseItemService(null, null, null, null, null, null, null, null, null, null,
                new InitializeService(), null) {
            @Override
            public PlayerBaseFull getPlayerBaseFull4UserId(String userId) {
                return playerBaseFull;
            }
        };
        BaseItemPositionComparison comparison = new BaseItemPositionComparison(null, baseItemService);
        // placeConfig == null -> uses getItems()
        comparison.init(itemTypes, null, timeSeconds, "u");
        return comparison;
    }

    @Test
    public void typeCountWithTimeCompletesAfterDuration() {
        BaseItemType attacker = new BaseItemType();
        attacker.setId(42);
        PlayerBaseFull base = createBase();
        backingItems(base).add(createItem(1, attacker));
        backingItems(base).add(createItem(2, attacker));
        Map<BaseItemType, Integer> itemTypes = new HashMap<>();
        itemTypes.put(attacker, 2);
        BaseItemPositionComparison comparison = createTypeComparison(base, itemTypes, 3);

        int ticks = 0;
        for (int i = 0; i < 60; i++) {
            comparison.tick();
            ticks++;
            if (comparison.isFulfilled()) {
                break;
            }
        }
        Assert.assertTrue("typeCount time quest must complete after the duration (ran " + ticks + ")", comparison.isFulfilled());
        Assert.assertTrue("Must not complete before the required duration (was " + ticks + ")", ticks >= 30);
    }

    private PlayerBaseFull createBase() {
        return new PlayerBaseFull(1, "n", null, 0, 0, null, null, "u", null);
    }

    private CountPositionComparison createComparison(PlayerBaseFull playerBaseFull, int count, Integer timeSeconds) {
        BaseItemService baseItemService = new BaseItemService(null, null, null, null, null, null, null, null, null, null,
                new InitializeService(), null) {
            @Override
            public PlayerBaseFull getPlayerBaseFull4UserId(String userId) {
                return playerBaseFull;
            }
        };
        CountPositionComparison comparison = new CountPositionComparison(null, baseItemService);
        // placeConfig == null -> uses getItems()
        comparison.init(count, null, timeSeconds, "u");
        return comparison;
    }

    @Test
    public void positionOnlyCompletesImmediately() {
        PlayerBaseFull base = createBase();
        backingItems(base).add(createItem(1));
        backingItems(base).add(createItem(2));
        CountPositionComparison comparison = createComparison(base, 2, null);

        comparison.tick();
        Assert.assertTrue("Position-only quest must be fulfilled once items present", comparison.isFulfilled());
    }

    @Test
    public void positionWithTimeCompletesAfterDuration() {
        PlayerBaseFull base = createBase();
        backingItems(base).add(createItem(1));
        backingItems(base).add(createItem(2));
        // 3 seconds -> 30 ticks (TICKS_PER_SECONDS = 10)
        CountPositionComparison comparison = createComparison(base, 2, 3);

        int ticks = 0;
        for (int i = 0; i < 60; i++) {
            comparison.tick();
            ticks++;
            if (comparison.isFulfilled()) {
                break;
            }
        }
        Assert.assertTrue("Time quest must eventually complete while items stay in place (ran " + ticks + " ticks)", comparison.isFulfilled());
        Assert.assertTrue("Must not complete before the required duration (was " + ticks + " ticks)", ticks >= 30);
    }

    @Test
    public void timeResetsWhenItemsLeave() {
        PlayerBaseFull base = createBase();
        Collection<SyncBaseItem> items = backingItems(base);
        SyncBaseItem item1 = createItem(1);
        SyncBaseItem item2 = createItem(2);
        items.add(item1);
        items.add(item2);
        CountPositionComparison comparison = createComparison(base, 2, 3);

        // Accrue 15 ticks
        for (int i = 0; i < 15; i++) {
            comparison.tick();
        }
        Assert.assertFalse(comparison.isFulfilled());
        // One item leaves
        items.remove(item2);
        comparison.tick();
        Assert.assertFalse(comparison.isFulfilled());
        // Item returns; must accrue full duration again
        items.add(item2);
        int ticks = 0;
        for (int i = 0; i < 60; i++) {
            comparison.tick();
            ticks++;
            if (comparison.isFulfilled()) {
                break;
            }
        }
        Assert.assertTrue(comparison.isFulfilled());
        Assert.assertTrue("Timer must restart after items leave (was " + ticks + ")", ticks >= 30);
    }
}
