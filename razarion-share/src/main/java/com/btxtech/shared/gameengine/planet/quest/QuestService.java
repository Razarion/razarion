/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: beat
 * Date: 28.12.2010
 * Time: 13:09:19
 */
@Singleton
public class QuestService {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private Instance<AbstractComparison> instance;
    @Inject
    private LevelService levelService;
    private Collection<QuestListener> questListeners = new ArrayList<>();
    private final Map<HumanPlayerId, AbstractConditionProgress> progressMap = new HashMap<>();

    public void activateCondition(HumanPlayerId humanPlayerId, QuestConfig questConfig) {
        AbstractComparison abstractComparison = null;
        if (questConfig.getConditionConfig().getConditionTrigger().isComparisonNeeded()) {
            abstractComparison = createAbstractComparison(humanPlayerId, questConfig.getConditionConfig());
//            if (abstractComparison instanceof AbstractUpdatingComparison) {
//                ((AbstractUpdatingComparison) abstractComparison).setGlobalServices(getGlobalServices());
//            }
//   TODO         if (abstractComparison instanceof TimeAware && ((TimeAware) abstractComparison).isTimerNeeded()) {
//   TODO             timeAwareList.add((TimeAware) abstractComparison);
//   TODO             if (timeAwareList.size() == 1) {
//   TODO                 startTimer();
//   TODO             }
//   TODO         }
        }
        AbstractConditionProgress conditionProgress = questConfig.getConditionConfig().getConditionTrigger().createConditionProgress(abstractComparison);
        conditionProgress.setHumanPlayerId(humanPlayerId);
        conditionProgress.setQuestConfig(questConfig);
        synchronized (progressMap) {
            progressMap.put(humanPlayerId, conditionProgress);
        }
        if (conditionProgress.isFulfilled()) {
            conditionPassed(conditionProgress);
        }
    }

    public void deactivateActorCondition(HumanPlayerId humanPlayerId) {
        synchronized (progressMap) {
            progressMap.remove(humanPlayerId);
        }
        // TODO if (abstractConditionTrigger != null) {
        // TODO handleTimerRemoval(abstractConditionTrigger);
        // TODO }
    }

    public void checkPositionCondition() {
        Collection<PlayerBase> playerBases = new ArrayList<>();
        synchronized (progressMap) {
            for (AbstractConditionProgress abstractConditionProgress : progressMap.values()) {
                if (abstractConditionProgress.getConditionTrigger().equals(ConditionTrigger.SYNC_ITEM_POSITION)) {
                    playerBases.add(baseItemService.getPlayerBase4HumanPlayerId(abstractConditionProgress.getHumanPlayerId()));
                    break;
                }
            }
        }
        if (playerBases.isEmpty()) {
            return;
        }
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!playerBases.contains(syncBaseItem.getBase())) {
                return null;
            }
            triggerSyncItem(syncBaseItem.getBase().getHumanPlayerId(), ConditionTrigger.SYNC_ITEM_POSITION, syncBaseItem);
            return null;
        });
    }

    public void onSyncItemBuilt(SyncBaseItem syncBaseItem) {
        HumanPlayerId humanPlayerId = syncBaseItem.getBase().getHumanPlayerId();
        if (humanPlayerId == null) {
            return;
        }
        triggerSyncItem(humanPlayerId, ConditionTrigger.SYNC_ITEM_CREATED, syncBaseItem);
        triggerSyncItem(humanPlayerId, ConditionTrigger.SYNC_ITEM_POSITION, syncBaseItem);
    }

    public void onSyncItemKilled(SyncBaseItem target, SyncBaseItem actor) {
        HumanPlayerId humanPlayerId = actor.getBase().getHumanPlayerId();
        if (humanPlayerId == null) {
            return;
        }
        triggerSyncItem(humanPlayerId, ConditionTrigger.SYNC_ITEM_KILLED, target);
    }

    public void onSyncBoxItemPicked(SyncBaseItem picker) {
        HumanPlayerId humanPlayerId = picker.getBase().getHumanPlayerId();
        if (humanPlayerId == null) {
            return;
        }
        triggerValue(humanPlayerId, ConditionTrigger.BOX_PICKED, 1.0);
    }

    public void onInventoryItemPlaced(HumanPlayerId humanPlayerId, InventoryItem inventoryItem) {
        InventoryItemConditionProgress inventoryItemConditionProgress = (InventoryItemConditionProgress) findProgress(humanPlayerId, ConditionTrigger.INVENTORY_ITEM_PLACED);
        if (inventoryItemConditionProgress == null) {
            return;
        }
        inventoryItemConditionProgress.onInventoryItem(inventoryItem);
        if (inventoryItemConditionProgress.isFulfilled()) {
            conditionPassed(inventoryItemConditionProgress);
        }
    }

    public void onHarvested(SyncBaseItem harvester, double amount) {
        HumanPlayerId humanPlayerId = harvester.getBase().getHumanPlayerId();
        if (humanPlayerId == null) {
            return;
        }
        triggerValue(humanPlayerId, ConditionTrigger.HARVEST, amount);
    }

    public void onBaseKilled(SyncBaseItem actor) {
        HumanPlayerId humanPlayerId = actor.getBase().getHumanPlayerId();
        if (humanPlayerId == null) {
            return;
        }
        triggerValue(humanPlayerId, ConditionTrigger.BASE_KILLED, 1.0);
    }

    public void addQuestListener(QuestListener questListener) {
        questListeners.add(questListener);
    }

    public void removeQuestListener(QuestListener questListener) {
        questListeners.remove(questListener);
    }

    private AbstractComparison createAbstractComparison(HumanPlayerId humanPlayerId, ConditionConfig conditionConfig) {
        ComparisonConfig comparisonConfig = conditionConfig.getComparisonConfig();
        switch (conditionConfig.getConditionTrigger().getType()) {
            case BASE_ITEM: {
                if (comparisonConfig.getCount() != null) {
                    BaseItemCountComparison baseItemCountComparison = instance.select(BaseItemCountComparison.class).get();
                    baseItemCountComparison.init(comparisonConfig.getCount());
                    return baseItemCountComparison;
                } else if (comparisonConfig.getPlaceConfig() != null) {
                    BaseItemPositionComparison baseItemPositionComparison = instance.select(BaseItemPositionComparison.class).get();
                    baseItemPositionComparison.init(convertItemCount(comparisonConfig.getTypeCount()), comparisonConfig.getPlaceConfig(), comparisonConfig.getTime(), comparisonConfig.getAddExisting(), humanPlayerId);
                    return baseItemPositionComparison;
                } else if (comparisonConfig.getTypeCount() != null) {
                    BaseItemTypeComparison syncItemTypeComparison = instance.select(BaseItemTypeComparison.class).get();
                    syncItemTypeComparison.init(convertItemCount(comparisonConfig.getTypeCount()));
                    return syncItemTypeComparison;
                } else {
                    throw new UnsupportedOperationException();
                }
            }
            case COUNT:
                if (comparisonConfig.getCount() != null) {
                    CountComparison countComparison = instance.select(CountComparison.class).get();
                    countComparison.init(comparisonConfig.getCount());
                    return countComparison;
                } else {
                    throw new UnsupportedOperationException();
                }
            case INVENTORY_ITEM:
                if (comparisonConfig.getCount() != null) {
                    InventoryItemCountComparison inventoryItemCountComparison = instance.select(InventoryItemCountComparison.class).get();
                    inventoryItemCountComparison.init(comparisonConfig.getCount());
                    return inventoryItemCountComparison;
                } else {
                    throw new UnsupportedOperationException();
                }
            default:
                throw new UnsupportedOperationException("Don't known: " + conditionConfig.getConditionTrigger().getType());
        }
    }

    private AbstractConditionProgress findProgress(HumanPlayerId humanPlayerId, ConditionTrigger conditionTrigger) {
        AbstractConditionProgress abstractConditionProgress;
        synchronized (progressMap) {
            abstractConditionProgress = progressMap.get(humanPlayerId);
        }
        if (abstractConditionProgress == null) {
            return null;
        }
        if (!abstractConditionProgress.getConditionTrigger().equals(conditionTrigger)) {
            return null;
        }
        return abstractConditionProgress;
    }

    private void triggerValue(HumanPlayerId humanPlayerId, ConditionTrigger conditionTrigger, double value) {
        ValueConditionProgress valueConditionProgress = (ValueConditionProgress) findProgress(humanPlayerId, conditionTrigger);
        if (valueConditionProgress == null) {
            return;
        }
        valueConditionProgress.onTriggerValue(value);
        if (valueConditionProgress.isFulfilled()) {
            conditionPassed(valueConditionProgress);
        }
    }

    private void triggerSyncItem(HumanPlayerId humanPlayerId, ConditionTrigger conditionTrigger, SyncBaseItem syncBaseItem) {
        BaseItemConditionProgress baseItemConditionProgress = (BaseItemConditionProgress) findProgress(humanPlayerId, conditionTrigger);
        if (baseItemConditionProgress == null) {
            return;
        }
        baseItemConditionProgress.onItem(syncBaseItem);
        if (baseItemConditionProgress.isFulfilled()) {
            conditionPassed(baseItemConditionProgress);
        }
    }

    private void conditionPassed(AbstractConditionProgress abstractConditionProgress) {
        deactivateActorCondition(abstractConditionProgress.getHumanPlayerId());
        questListeners.forEach(questListener -> questListener.onQuestPassed(abstractConditionProgress.getHumanPlayerId(), abstractConditionProgress.getQuestConfig()));
        // TODO levelService.increaseXp(abstractConditionProgress.getHumanPlayerId(), abstractConditionProgress.getQuestConfig().getXp());
    }

    private Map<BaseItemType, Integer> convertItemCount(Map<Integer, Integer> itemIdCount) {
        Map<BaseItemType, Integer> baseItemType = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : itemIdCount.entrySet()) {
            baseItemType.put(itemTypeService.getBaseItemType(entry.getKey()), entry.getValue());
        }
        return baseItemType;
    }
}
