/*
 * Copyright (c) 2011.
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
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.utils.TimeDateUtil;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: beat Date: 18.07.2010 Time: 21:06:41
 */
@Dependent
public class BaseItemPositionComparison extends AbstractTickComparison /*implements TimeAware*/ {
    @Inject
    private BaseItemService baseItemService;
    private AbstractConditionProgress abstractConditionProgress;
    private Map<BaseItemType, Integer> itemTypes;
    private PlaceConfig placeConfig;
    private Integer time;
    private HumanPlayerId humanPlayerId;
    private boolean isFulfilled = false;
    private final Collection<SyncBaseItem> fulfilledItems = new HashSet<>();
    private Long fulfilledTimeStamp;

    public void init(Map<BaseItemType, Integer> itemTypes, PlaceConfig placeConfig, Integer time, HumanPlayerId humanPlayerId) {
        this.itemTypes = itemTypes;
        this.placeConfig = placeConfig;
        this.time = time;
        this.humanPlayerId = humanPlayerId;
    }

    @Override
    public void tick() {
        Collection<SyncBaseItem> fulfilledItems = setupFulfilled();
        if (compareWithFulfilled(fulfilledItems)) {
            // TODO handle time if fulfilled
            return;
        }
        synchronized (this.fulfilledItems) {
            this.fulfilledItems.clear();
            this.fulfilledItems.addAll(fulfilledItems);
        }
        checkFulfilled();
    }

    private boolean compareWithFulfilled(Collection<SyncBaseItem> fulfilledItems) {
        return fulfilledItems.size() == this.fulfilledItems.size() && this.fulfilledItems.containsAll(fulfilledItems);
    }

    private void checkFulfilled() {
        if (isTimerNeeded()) {
            checkIfTimeFulfilled();
        } else {
            isFulfilled = checkItemsComplete();
            onProgressChanged();
        }
    }

    private Collection<SyncBaseItem> setupFulfilled() {
        PlayerBaseFull playerBase = baseItemService.getPlayerBaseFull4HumanPlayerId(humanPlayerId);
        if (playerBase == null) {
            return Collections.emptyList();
        }

        Collection<SyncBaseItem> items;
        if (placeConfig != null) {
            items = playerBase.findItemsInPlace(placeConfig);
        } else {
            items = playerBase.getItems();
        }
        return items.stream().filter(syncBaseItem -> syncBaseItem.isAlive() && syncBaseItem.isBuildup()).collect(Collectors.toList());
    }

    @Override
    public boolean isFulfilled() {
        return isFulfilled;
    }

    @Override
    public AbstractConditionProgress getAbstractConditionProgress() {
        return abstractConditionProgress;
    }

    @Override
    public void setAbstractConditionProgress(AbstractConditionProgress abstractConditionProgress) {
        this.abstractConditionProgress = abstractConditionProgress;
    }

//    @Override
//    public void onTimer() {
//        if (!isFulfilled) {
//            synchronized (fulfilledItems) {
//                checkIfTimeFulfilled();
//            }
//            if (isFulfilled) {
//                getAbstractConditionTrigger().setFulfilled();
//            }
//        }
//    }

    // @Override
    public boolean isTimerNeeded() {
        // return time != null && time > 0;
        return false;
    }

    private void checkIfTimeFulfilled() {
        if (checkItemsComplete()) {
            if (fulfilledTimeStamp == null) {
                fulfilledTimeStamp = System.currentTimeMillis();
            } else {
                isFulfilled = fulfilledTimeStamp + time < System.currentTimeMillis();
            }
        } else {
            fulfilledTimeStamp = null;
        }
        onProgressChanged();
    }

    private boolean checkItemsComplete() {
        if (itemTypes == null) {
            return true;
        }
        Map<BaseItemType, Integer> tmpItemTypes = new HashMap<>(itemTypes);
        synchronized (fulfilledItems) {
            for (SyncBaseItem fulfilledItem : fulfilledItems) {
                BaseItemType fulfilledItemType = fulfilledItem.getBaseItemType();
                Integer count = tmpItemTypes.get(fulfilledItemType);
                if (count == null) {
                    continue;
                }
                count--;
                if (count == 0) {
                    tmpItemTypes.remove(fulfilledItemType);
                } else {
                    tmpItemTypes.put(fulfilledItemType, count);
                }
            }
        }
        return tmpItemTypes.isEmpty();
    }

    @Override
    public void fillGenericComparisonValues(BackupComparisionInfo backupComparisionInfo) {
        if (fulfilledTimeStamp != null) {
            long remainingTime = time - (System.currentTimeMillis() - fulfilledTimeStamp);
            backupComparisionInfo.setRemainingMilliSeconds((int) remainingTime);
        }
    }

    @Override
    public void restoreFromGenericComparisonValue(BackupComparisionInfo backupComparisionInfo) {
        synchronized (fulfilledItems) {
            fulfilledItems.clear();
        }

        if (backupComparisionInfo.hasRemainingMilliSeconds()) {
            long remainingTime = (long) backupComparisionInfo.getRemainingMilliSeconds();
            fulfilledTimeStamp = remainingTime + System.currentTimeMillis() - time;
        }
    }

    @Override
    public QuestProgressInfo generateQuestProgressInfo() {
        QuestProgressInfo questProgressInfo = new QuestProgressInfo();

        // Add time
        if (time != null) {
            int timeCount = 0;
            if (fulfilledTimeStamp != null) {
                long longAmount = System.currentTimeMillis() - fulfilledTimeStamp;
                if (longAmount > TimeDateUtil.MILLIS_IN_MINUTE) {
                    timeCount = (int) (longAmount / TimeDateUtil.MILLIS_IN_MINUTE);
                } else {
                    timeCount = 1;
                }
            }
            questProgressInfo.setTime(timeCount);
        }
        // Items
        Map<Integer, Integer> typeCount = new HashMap<>();
        for (BaseItemType baseItemType : itemTypes.keySet()) {
            typeCount.put(baseItemType.getId(), getCount(baseItemType));
        }
        questProgressInfo.setTypeCount(typeCount);
        return questProgressInfo;
    }

    private int getCount(BaseItemType baseItemType) {
        int count = 0;
        synchronized (fulfilledItems) {
            for (SyncBaseItem fulfilledItem : fulfilledItems) {
                if (fulfilledItem.getItemType().equals(baseItemType)) {
                    count++;
                }
            }
        }
        return count;
    }
}