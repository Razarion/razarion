package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * REGION_CLEARED (2026-10-14): level 9 starts by clearing the noob island. Passes once nothing of the
 * player's own is left in the place, however it went, and at once when nothing was there. Engine-free
 * like TransportQuestUnitTest; the place is a set of item ids instead of a polygon.
 */
public class RegionClearedQuestUnitTest {
    private static final String USER = "u";

    private BaseItemType type;
    private PlayerBaseFull base;
    private QuestService questService;
    private final Set<Integer> onTheIsland = new HashSet<>();
    private final List<QuestConfig> passed = new ArrayList<>();
    private final List<Integer> progress = new ArrayList<>();

    @Before
    public void setUp() {
        type = new BaseItemType();
        type.setId(4);
        base = new PlayerBaseFull(1, "n", null, 0, 0, null, null, USER, null, System.currentTimeMillis());
        BaseItemService baseItemService = new BaseItemService(null, null, null, null, null, null, null, null, null, null,
                new InitializeService(), null) {
            @Override
            public PlayerBaseFull getPlayerBaseFull4UserId(String userId) {
                return base;
            }
        };
        GameLogicService gameLogicService = new GameLogicService(null, null, null) {
            @Override
            public void onQuestProgressUpdate(String userId, QuestProgressInfo questProgressInfo) {
                progress.add(questProgressInfo.getCount());
            }
        };
        questService = new QuestService(null, null, null, null, null, null,
                () -> new RegionClearedComparison(gameLogicService, baseItemService),
                new ItemTypeService(new InitializeService()));
        questService.addQuestListener((userId, questConfig) -> passed.add(questConfig));
    }

    @Test
    public void passesWhenTheLastOneIsGone() {
        SyncBaseItem factory = item(1, true);
        SyncBaseItem radar = item(2, true);
        item(3, false); // the builder, already across the water
        activate();
        questService.tick();
        Assert.assertTrue("two left on the island", passed.isEmpty());

        remove(factory);
        questService.tick();
        Assert.assertTrue("one left on the island", passed.isEmpty());

        remove(radar);
        questService.tick();
        Assert.assertEquals(1, passed.size());
        Assert.assertEquals("progress counts down what is left", List.of(1, 0), progress.subList(progress.size() - 2, progress.size()));
    }

    @Test
    public void passesAtOnceWithNothingThere() {
        item(3, false);
        activate();
        Assert.assertEquals("nothing on the island: passed on activation", 1, passed.size());
    }

    private void activate() {
        PlaceConfig island = new PlaceConfig() {
            @Override
            public boolean checkInside(SyncItem syncItem) {
                return onTheIsland.contains(syncItem.getId());
            }
        };
        questService.activateCondition(USER, new QuestConfig().id(9002).conditionConfig(new ConditionConfig()
                .conditionTrigger(ConditionTrigger.REGION_CLEARED).comparisonConfig(new ComparisonConfig().placeConfig(island))));
    }

    @SuppressWarnings("unchecked")
    private void remove(SyncBaseItem syncBaseItem) {
        ((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("items", base)).remove(syncBaseItem);
    }

    @SuppressWarnings("unchecked")
    private SyncBaseItem item(int id, boolean island) {
        SyncBaseItem syncBaseItem = new SyncBaseItem(null, null, null, null, null, null, null, null, null, null, null, null, null);
        syncBaseItem.init(id, type);
        syncBaseItem.setBuildup(1.0);
        SimpleTestEnvironment.injectService("health", syncBaseItem, SyncBaseItem.class, 100.0);
        if (island) {
            onTheIsland.add(id);
        }
        ((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("items", base)).add(syncBaseItem);
        return syncBaseItem;
    }
}
