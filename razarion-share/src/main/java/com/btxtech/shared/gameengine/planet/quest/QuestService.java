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

import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger.SYNC_ITEM_CREATED;

/**
 * User: beat
 * Date: 28.12.2010
 * Time: 13:09:19
 */
@Singleton
public class QuestService {
    private static final int TICKS_TO_SEND_DEFERRED = PlanetService.TICKS_PER_SECONDS * 2;
    private final Logger logger = Logger.getLogger(QuestService.class.getName());
    private final ItemTypeService itemTypeService;
    private final Provider<CountComparison> countComparisonProvider;
    private final Provider<InventoryItemCountComparison> inventoryItemCountComparisonnProvider;
    private final Provider<BaseItemTypeComparison> baseItemTypeComparisonProvider;
    private final Provider<BaseItemCountComparison> baseItemCountComparisonProvider;
    private final Provider<BaseItemPositionComparison> baseItemPositionComparisonProvider;
    private final Provider<CountPositionComparison> countPositionComparisonProvider;
    private final Collection<QuestListener> questListeners = new ArrayList<>();
    private final Map<String, AbstractConditionProgress> progressMap = new HashMap<>();
    private int tickCount;

    @Inject
    public QuestService(Provider<BaseItemPositionComparison> baseItemPositionComparisonProvider,
                        Provider<CountPositionComparison> countPositionComparisonProvider,
                        Provider<BaseItemCountComparison> baseItemCountComparisonProvider,
                        Provider<BaseItemTypeComparison> baseItemTypeComparisonProvider,
                        Provider<InventoryItemCountComparison> inventoryItemCountComparisonnProvider,
                        Provider<CountComparison> countComparisonProvider,
                        ItemTypeService itemTypeService) {
        this.baseItemPositionComparisonProvider = baseItemPositionComparisonProvider;
        this.countPositionComparisonProvider = countPositionComparisonProvider;
        this.baseItemCountComparisonProvider = baseItemCountComparisonProvider;
        this.baseItemTypeComparisonProvider = baseItemTypeComparisonProvider;
        this.inventoryItemCountComparisonnProvider = inventoryItemCountComparisonnProvider;
        this.countComparisonProvider = countComparisonProvider;
        this.itemTypeService = itemTypeService;
    }

    public void activateCondition(String userId, QuestConfig questConfig) {
        AbstractComparison abstractComparison = null;
        if (questConfig.getConditionConfig().getConditionTrigger().isComparisonNeeded()) {
            abstractComparison = createAbstractComparison(userId, questConfig.getConditionConfig(), questConfig.getConditionConfig().getConditionTrigger());
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
        conditionProgress.setUserId(userId);
        conditionProgress.setQuestConfig(questConfig);
        synchronized (progressMap) {
            progressMap.put(userId, conditionProgress);
        }
        if (conditionProgress.isFulfilled()) {
            conditionPassed(conditionProgress);
        }
    }

    public void clean() {
        // TODO timer timeAwareList TimeAware
        progressMap.clear();
    }

    public boolean hasActiveQuest(String userId) {
        return progressMap.containsKey(userId);
    }

    public void deactivateActorCondition(String userId) {
        synchronized (progressMap) {
            progressMap.remove(userId);
        }
        // TODO if (abstractConditionTrigger != null) {
        // TODO handleTimerRemoval(abstractConditionTrigger);
        // TODO }
    }

    public void tick() {
        try {
            Collection<AbstractConditionProgress> fulfilled = new ArrayList<>();
            synchronized (progressMap) {
                for (AbstractConditionProgress abstractConditionProgress : progressMap.values()) {
                    if (abstractConditionProgress instanceof TickConditionProgress) {
                        ((TickConditionProgress) abstractConditionProgress).tick();
                        if (abstractConditionProgress.isFulfilled()) {
                            fulfilled.add(abstractConditionProgress);
                        }
                    }
                }
            }
            fulfilled.forEach(this::conditionPassed);
            tickCount++;
            if (tickCount > TICKS_TO_SEND_DEFERRED) {
                synchronized (progressMap) {
                    for (AbstractConditionProgress abstractConditionProgress : progressMap.values()) {
                        abstractConditionProgress.getAbstractComparison().handleDeferredUpdate();
                    }
                }
                tickCount = 0;
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
        }
    }

    public void onSyncItemBuilt(SyncBaseItem syncBaseItem) {
        String userId = syncBaseItem.getBase().getUserId();
        if (userId == null) {
            return;
        }
        triggerSyncItem(userId, SYNC_ITEM_CREATED, syncBaseItem);
    }

    public void onSyncItemKilled(SyncBaseItem target, SyncBaseItem actor) {
        String userId = actor.getBase().getUserId();
        if (userId == null) {
            return;
        }
        triggerSyncItem(userId, ConditionTrigger.SYNC_ITEM_KILLED, target);
    }

    public void onSyncBoxItemPicked(String userId) {
        triggerValue(userId, ConditionTrigger.BOX_PICKED, 1.0);
    }

    public void onInventoryItemPlaced(String userId, InventoryItem inventoryItem) {
        InventoryItemConditionProgress inventoryItemConditionProgress = (InventoryItemConditionProgress) findProgress(userId, ConditionTrigger.INVENTORY_ITEM_PLACED);
        if (inventoryItemConditionProgress == null) {
            return;
        }
        inventoryItemConditionProgress.onInventoryItem(inventoryItem);
        if (inventoryItemConditionProgress.isFulfilled()) {
            conditionPassed(inventoryItemConditionProgress);
        }
    }

    public void onHarvested(SyncBaseItem harvester, double amount) {
        String userId = harvester.getBase().getUserId();
        if (userId == null) {
            return;
        }
        triggerValue(userId, ConditionTrigger.HARVEST, amount);
    }

    public void onBaseKilled(SyncBaseItem actor) {
        String userId = actor.getBase().getUserId();
        if (userId == null) {
            return;
        }
        triggerValue(userId, ConditionTrigger.BASE_KILLED, 1.0);
    }

    public void onUnlock(String userId) {
        triggerValue(userId, ConditionTrigger.UNLOCKED, 1.0);
    }

    public void addQuestListener(QuestListener questListener) {
        questListeners.add(questListener);
    }

    public void removeQuestListener(QuestListener questListener) {
        questListeners.remove(questListener);
    }

    private AbstractComparison createAbstractComparison(String userId, ConditionConfig conditionConfig, ConditionTrigger conditionTrigger) {
        ComparisonConfig comparisonConfig = conditionConfig.getComparisonConfig();
        switch (conditionTrigger) {
            case HARVEST:
            case BASE_KILLED:
            case BOX_PICKED:
            case UNLOCKED:
                if (comparisonConfig.getCount() != null) {
                    CountComparison countComparison = countComparisonProvider.get();
                    countComparison.init(comparisonConfig.getCount());
                    countComparison.setMinSendDelayEnabled(conditionTrigger == ConditionTrigger.HARVEST);
                    return countComparison;
                } else {
                    throw new UnsupportedOperationException();
                }
            case INVENTORY_ITEM_PLACED:
                if (comparisonConfig.getCount() != null) {
                    InventoryItemCountComparison inventoryItemCountComparison = inventoryItemCountComparisonnProvider.get();
                    inventoryItemCountComparison.init(comparisonConfig.getCount());
                    return inventoryItemCountComparison;
                } else {
                    throw new UnsupportedOperationException();
                }
            case SYNC_ITEM_KILLED:
            case SYNC_ITEM_CREATED:
                String includeExistingUserId = null;
                if (conditionTrigger == SYNC_ITEM_CREATED && comparisonConfig.isIncludeExisting()) {
                    includeExistingUserId = userId;
                }
                if (comparisonConfig.getTypeCount() != null) {
                    BaseItemTypeComparison syncItemTypeComparison = baseItemTypeComparisonProvider.get();
                    syncItemTypeComparison.init(convertItemCount(comparisonConfig.getTypeCount()), includeExistingUserId, comparisonConfig.toBotIdSet());
                    return syncItemTypeComparison;
                } else if (comparisonConfig.getCount() != null) {
                    BaseItemCountComparison baseItemCountComparison = baseItemCountComparisonProvider.get();
                    baseItemCountComparison.init(comparisonConfig.getCount(), includeExistingUserId, comparisonConfig.toBotIdSet());
                    return baseItemCountComparison;
                } else {
                    throw new UnsupportedOperationException();
                }
            case SYNC_ITEM_POSITION:
                if (comparisonConfig.getTypeCount() != null) {
                    BaseItemPositionComparison baseItemPositionComparison = baseItemPositionComparisonProvider.get();
                    baseItemPositionComparison.init(convertItemCount(comparisonConfig.getTypeCount()), comparisonConfig.getPlaceConfig(), comparisonConfig.getTimeSeconds(), userId);
                    return baseItemPositionComparison;
                } else if (comparisonConfig.getCount() != null) {
                    CountPositionComparison countPositionComparison = countPositionComparisonProvider.get();
                    countPositionComparison.init(comparisonConfig.getCount(), comparisonConfig.getPlaceConfig(), comparisonConfig.getTimeSeconds(), userId);
                    return countPositionComparison;
                } else {
                    throw new UnsupportedOperationException();
                }
            default:
                throw new IllegalArgumentException("QuestService.createAbstractComparison() Unknown conditionTrigger: " + conditionTrigger);
        }
    }

    private AbstractConditionProgress findProgress(String userId, ConditionTrigger conditionTrigger) {
        AbstractConditionProgress abstractConditionProgress;
        synchronized (progressMap) {
            abstractConditionProgress = progressMap.get(userId);
        }
        if (abstractConditionProgress == null) {
            return null;
        }
        if (!abstractConditionProgress.getConditionTrigger().equals(conditionTrigger)) {
            return null;
        }
        return abstractConditionProgress;
    }

    private void triggerValue(String userId, ConditionTrigger conditionTrigger, double value) {
        ValueConditionProgress valueConditionProgress = (ValueConditionProgress) findProgress(userId, conditionTrigger);
        if (valueConditionProgress == null) {
            return;
        }
        valueConditionProgress.onTriggerValue(value);
        if (valueConditionProgress.isFulfilled()) {
            conditionPassed(valueConditionProgress);
        }
    }

    private void triggerSyncItem(String userId, ConditionTrigger conditionTrigger, SyncBaseItem syncBaseItem) {
        BaseItemConditionProgress baseItemConditionProgress = (BaseItemConditionProgress) findProgress(userId, conditionTrigger);
        if (baseItemConditionProgress == null) {
            return;
        }
        baseItemConditionProgress.onItem(syncBaseItem);
        if (baseItemConditionProgress.isFulfilled()) {
            conditionPassed(baseItemConditionProgress);
        }
    }

    private void conditionPassed(AbstractConditionProgress abstractConditionProgress) {
        deactivateActorCondition(abstractConditionProgress.getUserId());
        questListeners.forEach(questListener -> questListener.onQuestPassed(abstractConditionProgress.getUserId(), abstractConditionProgress.getQuestConfig()));
        // TODO levelService.increaseXp(abstractConditionProgress.getHumanPlayerId(), abstractConditionProgress.getQuestConfig().getXp());
    }

    private Map<BaseItemType, Integer> convertItemCount(Map<Integer, Integer> itemIdCount) {
        Map<BaseItemType, Integer> baseItemType = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : itemIdCount.entrySet()) {
            baseItemType.put(itemTypeService.getBaseItemType(entry.getKey()), entry.getValue());
        }
        return baseItemType;
    }

    public QuestProgressInfo getQuestProgressInfo(String userId) {
        AbstractConditionProgress abstractConditionProgress = progressMap.get(userId);
        if (abstractConditionProgress == null) {
            return null;
        }
        return abstractConditionProgress.getAbstractComparison().generateQuestProgressInfo();
    }

    public void fillBackup(BackupPlanetInfo backupPlanetInfo) {
        List<BackupComparisionInfo> backupComparisionInfos = new ArrayList<>();
        synchronized (progressMap) {
            progressMap.forEach((userId, abstractConditionProgress) -> {
                if (userId != null) {
                    try {
                        backupComparisionInfos.add(abstractConditionProgress.generateBackupComparisionInfo());
                    } catch (Throwable t) {
                        logger.log(Level.SEVERE, "Could not backup quest " + abstractConditionProgress, t);
                    }
                }
            });
        }
        backupPlanetInfo.setBackupComparisionInfos(backupComparisionInfos);
    }

    public void restore(BackupPlanetInfo backupPlanetInfo) {
        if (backupPlanetInfo == null || backupPlanetInfo.getBackupComparisionInfos() == null) {
            return;
        }
        synchronized (progressMap) {
            backupPlanetInfo.getBackupComparisionInfos().forEach(backupComparisionInfo -> {
                try {
                    AbstractConditionProgress abstractConditionProgress = progressMap.get(backupComparisionInfo.getUserId());
                    if (abstractConditionProgress == null) {
                        logger.warning("QuestService.restore() No AbstractConditionProgress for HumanPlayerId: " + backupComparisionInfo.getUserId());
                        return;
                    }
                    if (abstractConditionProgress.getQuestConfig().getId() != backupComparisionInfo.getQuestId()) {
                        logger.warning("QuestService.restore() different quest. HumanPlayerId: " + backupComparisionInfo.getUserId() + ". Quest in backup: " + backupComparisionInfo.getQuestId() + ". Active quest: " + abstractConditionProgress.getQuestConfig());
                        return;
                    }
                    abstractConditionProgress.restore(backupComparisionInfo);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Could not restore quest " + backupComparisionInfo, t);
                }
            });
        }
    }

    public void updateUserId(String userId) {
        synchronized (progressMap) {
            AbstractConditionProgress abstractConditionProgress = progressMap.remove(userId);
            if (abstractConditionProgress != null) {
                abstractConditionProgress.setUserId(userId);
                progressMap.put(userId, abstractConditionProgress);
            }
        }
    }
}
