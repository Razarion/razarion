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
    int QUEST_CONFIG_3_ID = 1003;
    int QUEST_CONFIG_4_ID = 1004;
    int QUEST_CONFIG_5_ID = 1005;
    int QUEST_CONFIG_6_ID = 1006;
    int QUEST_CONFIG_7_ID = 1007;
    int QUEST_CONFIG_8_ID = 1008;


    static BaseItemType findBaseItemType(int baseItemTypeId, Collection<BaseItemType> baseItemTypes) {
        return baseItemTypes.stream().filter(baseItemType -> baseItemType.getId() == baseItemTypeId).findFirst().orElseThrow(IllegalArgumentException::new);
    }

    static QuestConfig createItemCountCreatedQuest3() {
        return new QuestConfig().id(QUEST_CONFIG_7_ID).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).comparisonConfig(new ComparisonConfig().count(3)));
    }

    static QuestConfig createItemCountCreatedQuest3IncludeExisting() {
        QuestConfig questConfig =  createItemCountCreatedQuest3().id(QUEST_CONFIG_8_ID);
        questConfig.getConditionConfig().getComparisonConfig().setIncludeExisting(true);
        return questConfig;
    }

    static QuestConfig createItemCountCreatedQuest10() {
        return new QuestConfig().id(QUEST_CONFIG_1_ID).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).comparisonConfig(new ComparisonConfig().count(10)));
    }

    static QuestConfig createItemTypeCountCreatedQuest() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 2);
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 5);
        typeCount.put(FallbackConfig.GENERATOR_ITEM_TYPE_ID, 6);
        typeCount.put(FallbackConfig.CONSUMER_ITEM_TYPE_ID, 6);
        typeCount.put(FallbackConfig.HARVESTER_ITEM_TYPE_ID, 3);
        return new QuestConfig().id(QUEST_CONFIG_2_ID).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).comparisonConfig(new ComparisonConfig().typeCount(typeCount)));
    }

    static QuestConfig createItemTypeCountCreatedQuest2() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        typeCount.put(FallbackConfig.HARVESTER_ITEM_TYPE_ID, 1);
        return new QuestConfig().id(QUEST_CONFIG_3_ID).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).comparisonConfig(new ComparisonConfig().typeCount(typeCount)));
    }

    static QuestConfig createItemTypeCountCreatedQuest2IncludeExisting() {
        QuestConfig questConfig = createItemTypeCountCreatedQuest2().id(QUEST_CONFIG_6_ID);
        questConfig.getConditionConfig().getComparisonConfig().setIncludeExisting(true);
        return questConfig;
    }

    static QuestConfig createNoPositionQuest() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 7);
        return new QuestConfig().id(QUEST_CONFIG_4_ID).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).comparisonConfig(new ComparisonConfig().typeCount(typeCount)));
    }

    static QuestConfig createPositionQuest() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 3);
        return new QuestConfig().id(QUEST_CONFIG_5_ID).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).comparisonConfig(new ComparisonConfig().typeCount(typeCount).placeConfig(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(130, 130, 100, 40)))));
    }

    static QuestConfig createPositionTimeQuest() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        return new QuestConfig().id(QUEST_CONFIG_5_ID).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).comparisonConfig(new ComparisonConfig().typeCount(typeCount).placeConfig(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(130, 130, 100, 40))).timeSeconds(60)));
    }
}
