package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public interface GameTestContent {
    int QUEST_CONFIG_1_ID = 1001;
    int QUEST_CONFIG_2_ID = 1002;
    int QUEST_CONFIG_3_ID = 1002;
    int QUEST_CONFIG_4_ID = 1003;
    int QUEST_CONFIG_5_ID = 1004;


    static BaseItemType findBaseItemType(int baseItemTypeId, Collection<BaseItemType> baseItemTypes) {
        return baseItemTypes.stream().filter(baseItemType -> baseItemType.getId() == baseItemTypeId).findFirst().orElseThrow(IllegalArgumentException::new);
    }

    static QuestConfig createItemCountCreatedQuest() {
        return new QuestConfig().setId(QUEST_CONFIG_1_ID).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setCount(10)));
    }

    static QuestConfig createItemTypeCountCreatedQuest() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 2);
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 5);
        typeCount.put(FallbackConfig.GENERATOR_ITEM_TYPE_ID, 6);
        typeCount.put(FallbackConfig.CONSUMER_ITEM_TYPE_ID, 6);
        typeCount.put(FallbackConfig.HARVESTER_ITEM_TYPE_ID, 3);
        return new QuestConfig().setId(QUEST_CONFIG_2_ID).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount)));
    }

    static QuestConfig createItemTypeCountCreatedQuest2() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        typeCount.put(FallbackConfig.HARVESTER_ITEM_TYPE_ID, 1);
        return new QuestConfig().setId(QUEST_CONFIG_3_ID).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount)));
    }

    static QuestConfig createNoPositionQuest() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 7);
        return new QuestConfig().setId(QUEST_CONFIG_4_ID).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount)));
    }

    static QuestConfig createPositionQuest() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 3);
        return new QuestConfig().setId(QUEST_CONFIG_5_ID).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount).setPlaceConfig(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(130, 130, 100, 40)))));
    }

    static QuestConfig createPositionTimeQuest() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        return new QuestConfig().setId(QUEST_CONFIG_5_ID).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount).setPlaceConfig(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(130, 130, 100, 40))).setTimeSeconds(60)));
    }
}
