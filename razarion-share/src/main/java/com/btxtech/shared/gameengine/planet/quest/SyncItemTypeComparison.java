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

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.context.Dependent;
import java.util.HashMap;
import java.util.Map;

/**
 * User: beat Date: 18.07.2010 Time: 21:06:41
 */
@Dependent
public class SyncItemTypeComparison extends AbstractSyncItemComparison {
    private Map<BaseItemType, Integer> remaining;
    private Map<BaseItemType, Integer> total;

    public void init(Map<BaseItemType, Integer> baseItemType) {
        remaining = new HashMap<>(baseItemType);
        total = new HashMap<>(baseItemType);
    }

    @Override
    protected void privateOnSyncBaseItem(SyncBaseItem syncBaseItem) {
        Integer remainingCount = remaining.get(syncBaseItem.getBaseItemType());
        if (remainingCount == null) {
            return;
        }
        remainingCount--;
        if (remainingCount == 0) {
            remaining.remove(syncBaseItem.getBaseItemType());
        } else {
            remaining.put(syncBaseItem.getBaseItemType(), remainingCount);
        }
        onProgressChanged();
    }

    @Override
    public boolean isFulfilled() {
        return remaining.isEmpty();
    }

    public Map<BaseItemType, Integer> getRemaining() {
        return remaining;
    }

    public void setRemaining(Map<BaseItemType, Integer> remaining) {
        this.remaining = remaining;
    }

//    @Override
//    public void fillGenericComparisonValues(GenericComparisonValueContainer genericComparisonValueContainer) {
//        GenericComparisonValueContainer itemCounts = genericComparisonValueContainer.createChildContainer(GenericComparisonValueContainer.Key.REMAINING_ITEM_TYPES);
//        for (Map.Entry<BaseItemType, Integer> entry : remaining.entrySet()) {
//            itemCounts.addChild(entry.getKey(), entry.getValue());
//        }
//    }
//
//    @Override
//    public void restoreFromGenericComparisonValue(GenericComparisonValueContainer genericComparisonValueContainer) {
//        remaining.clear();
//        GenericComparisonValueContainer itemCounts = genericComparisonValueContainer.getChildContainer(GenericComparisonValueContainer.Key.REMAINING_ITEM_TYPES);
//        for (Map.Entry entry : itemCounts.getEntries()) {
//            remaining.put((BaseItemType) entry.getKey(), ((Number) entry.getValue()).intValue());
//        }
//    }
//
//    @Override
//    public void fillQuestProgressInfo(QuestProgressInfo questProgressInfo, QuestService conditionService) {
//        Map<Integer, QuestProgressInfo.Amount> itemIdAmounts = new HashMap<Integer, QuestProgressInfo.Amount>();
//        for (Map.Entry<BaseItemType, Integer> entry : total.entrySet()) {
//            Integer remaining = this.remaining.get(entry.getKey());
//            if (remaining == null) {
//                remaining = 0;
//            }
//            int amount = entry.getValue() - remaining;
//            itemIdAmounts.put(entry.getKey().getId(), new QuestProgressInfo.Amount(amount, entry.getValue()));
//        }
//        questProgressInfo.setItemIdAmounts(itemIdAmounts);
//    }
}