package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItemContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The two triggers quest 392 was split with (2026-09-22): SYNC_ITEM_LOADED - the builder is inside
 * a container - and LOADED_CONTAINER_POSITION - a container counts only while it carries something.
 * Engine-free, like PositionTimerUnitTest: the Dagger harness cannot build a harbour any more
 * (ItemContainerTest fails before it loads anything), so this drives QuestService directly.
 * <p>
 * Without a place both read the base's items as they are; with a place, contained items are never
 * inside it (PlayerBaseFull.findItemsInPlace), which is what keeps the unload quest from passing
 * with the builder still aboard.
 */
public class TransportQuestUnitTest {
    private static final int BUILDER = 1;
    private static final int TRANSPORTER = 18;
    private static final String USER = "u";

    private BaseItemType builderType;
    private BaseItemType transporterType;
    private PlayerBaseFull base;
    private QuestService questService;
    private final List<QuestConfig> passed = new ArrayList<>();

    @Before
    public void setUp() {
        builderType = new BaseItemType();
        builderType.setId(BUILDER);
        transporterType = new BaseItemType();
        transporterType.setId(TRANSPORTER);
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
            }
        };
        ItemTypeService itemTypeService = new ItemTypeService(new InitializeService()) {
            @Override
            public BaseItemType getBaseItemType(Integer baseItemTypeId) {
                return baseItemTypeId == BUILDER ? builderType : transporterType;
            }
        };
        questService = new QuestService(() -> new BaseItemPositionComparison(gameLogicService, baseItemService),
                null, null, null, null, null, itemTypeService);
        questService.addQuestListener((userId, questConfig) -> passed.add(questConfig));
    }

    @Test
    public void loaded() {
        SyncBaseItem builder = item(1, builderType);
        SyncBaseItem transporter = item(2, transporterType);
        activate(ConditionTrigger.SYNC_ITEM_LOADED, BUILDER);
        questService.tick();
        Assert.assertTrue("builder on the shore is not loaded", passed.isEmpty());

        load(builder, transporter);
        questService.tick();
        Assert.assertEquals(1, passed.size());
    }

    @Test
    public void loadedContainerOnlyWithCargo() {
        SyncBaseItem builder = item(1, builderType);
        SyncBaseItem transporter = item(2, transporterType);
        activate(ConditionTrigger.LOADED_CONTAINER_POSITION, TRANSPORTER);
        questService.tick();
        Assert.assertTrue("an empty transporter must not pass the sailing quest", passed.isEmpty());

        load(builder, transporter);
        questService.tick();
        Assert.assertEquals(1, passed.size());
    }

    private void activate(ConditionTrigger trigger, int itemTypeId) {
        ComparisonConfig comparison = new ComparisonConfig().typeCount(Collections.singletonMap(itemTypeId, 1));
        questService.activateCondition(USER, new QuestConfig().id(9001)
                .conditionConfig(new ConditionConfig().conditionTrigger(trigger).comparisonConfig(comparison)));
    }

    /** What SyncItemContainer.load() does to the two, without the MASTER check and the sync. */
    private void load(SyncBaseItem unit, SyncBaseItem container) {
        container.getSyncItemContainer().getContainedItems().add(unit);
        // setContained() also takes the unit off the ground, and these items have no physics
        SimpleTestEnvironment.injectService("containedIn", unit, SyncBaseItem.class, container);
    }

    @SuppressWarnings("unchecked")
    private SyncBaseItem item(int id, BaseItemType type) {
        SyncBaseItem syncBaseItem = new SyncBaseItem(null, null, null, null, null, null, null, null, null, null, null, null, null);
        syncBaseItem.init(id, type);
        syncBaseItem.setBuildup(1.0);
        SimpleTestEnvironment.injectService("health", syncBaseItem, SyncBaseItem.class, 100.0);
        if (type == transporterType) {
            SimpleTestEnvironment.injectService("syncItemContainer", syncBaseItem, SyncBaseItem.class,
                    new SyncItemContainer(null, null, null, null, null));
        }
        ((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("items", base)).add(syncBaseItem);
        return syncBaseItem;
    }
}
