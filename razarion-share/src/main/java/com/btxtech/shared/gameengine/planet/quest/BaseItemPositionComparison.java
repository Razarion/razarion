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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * User: beat Date: 18.07.2010 Time: 21:06:41
 */
@Dependent
public class BaseItemPositionComparison extends AbstractBaseItemComparison /*implements TimeAware*/ {
    @Inject
    private BaseItemService baseItemService;
    private Map<BaseItemType, Integer> itemTypes;
    private PlaceConfig placeConfig;
    private Integer time;
    private HumanPlayerId humanPlayerId;
    private boolean isFulfilled = false;
    private final Collection<SyncBaseItem> fulfilledItems = new HashSet<>();
    private Long fulfilledTimeStamp;

    public void init(Map<BaseItemType, Integer> itemTypes, PlaceConfig placeConfig, Integer time, boolean addExistingItems, HumanPlayerId humanPlayerId) {
        this.itemTypes = itemTypes;
        this.placeConfig = placeConfig;
        this.time = time;
        this.humanPlayerId = humanPlayerId;
        if (addExistingItems) {
            addInitial();
            checkFulfilled();
        }
    }

    @Override
    protected void privateOnSyncBaseItem(SyncBaseItem syncBaseItem) {
        if (isFulfilled) {
            return;
        }
        if (itemTypes == null || !itemTypes.containsKey(syncBaseItem.getBaseItemType())) {
            return;
        }
        if (!checkRegion(syncBaseItem)) {
            onProgressChanged();
            return;
        }
        synchronized (fulfilledItems) {
            fulfilledItems.add(syncBaseItem);
            checkFulfilled();
        }
    }

    private void checkFulfilled() {
        if (isTimerNeeded()) {
            checkIfTimeFulfilled();
        } else {
            verifyFulfilledItems();
            isFulfilled = areItemsComplete();
            onProgressChanged();
        }
    }

    private void addInitial() {
        PlayerBaseFull playerBase = baseItemService.getPlayerBaseFull4HumanPlayerId(humanPlayerId);
        if (playerBase == null) {
            return;
        }

        Collection<SyncBaseItem> items;
        if (placeConfig != null) {
            items = playerBase.findItemsInPlace(placeConfig);
        } else {
            items = playerBase.getItems();
        }
        fulfilledItems.addAll(items);
    }

    @Override
    public boolean isFulfilled() {
        return isFulfilled;
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

    private void verifyFulfilledItems() {
        for (Iterator<SyncBaseItem> iterator = fulfilledItems.iterator(); iterator.hasNext(); ) {
            SyncBaseItem fulfilledItem = iterator.next();
            if (!fulfilledItem.isAlive()) {
                iterator.remove();
            }
            if (!checkRegion(fulfilledItem)) {
                iterator.remove();
            }
        }
    }

    private void checkIfTimeFulfilled() {
        verifyFulfilledItems();
        if (areItemsComplete()) {
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

    private boolean checkRegion(SyncBaseItem syncBaseItem) {
        return placeConfig == null || placeConfig.checkInside(syncBaseItem);
    }

    private boolean areItemsComplete() {
        if (itemTypes == null) {
            return true;
        }
        Map<BaseItemType, Integer> tmpItemTypes = new HashMap<>(itemTypes);
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
        return tmpItemTypes.isEmpty();
    }

//    @Override
//    public void fillGenericComparisonValues(GenericComparisonValueContainer genericComparisonValueContainer) {
//        if (fulfilledTimeStamp != null) {
//            long remainingTime = time - (System.currentTimeMillis() - fulfilledTimeStamp);
//            genericComparisonValueContainer.addChild(GenericComparisonValueContainer.Key.REMAINING_TIME, remainingTime);
//        }
//    }
//
//    @Override
//    public void restoreFromGenericComparisonValue(GenericComparisonValueContainer genericComparisonValueContainer) {
//        fulfilledItems.clear();
//        addInitial();
//
//        if (genericComparisonValueContainer.hasKey(GenericComparisonValueContainer.Key.REMAINING_TIME)) {
//            long remainingTime = (Long) genericComparisonValueContainer.getValue(GenericComparisonValueContainer.Key.REMAINING_TIME);
//            fulfilledTimeStamp = remainingTime + System.currentTimeMillis() - time;
//        }
//    }

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
        synchronized (fulfilledItems) {
            verifyFulfilledItems();
        }
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