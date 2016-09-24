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

package com.btxtech.shared.gameengine.planet.condition;

import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 16:31:29
 */
public class CountComparison extends AbstractSyncItemComparison {
    private double count;
    private double countTotal;

    public CountComparison(int count) {
        this.count = count;
        countTotal = count;
    }

    @Override
    protected void privateOnSyncBaseItem(SyncBaseItem syncBaseItem) {
        count -= 1.0;
        onProgressChanged();
    }

    public void onValue(double value) {
        count -= value;
        onProgressChanged();
    }

    @Override
    public boolean isFulfilled() {
        return count <= 0.0;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }
//
//    @Override
//    public void fillGenericComparisonValues(GenericComparisonValueContainer genericComparisonValueContainer) {
//        genericComparisonValueContainer.addChild(GenericComparisonValueContainer.Key.REMAINING_COUNT, count);
//    }
//
//    @Override
//    public void restoreFromGenericComparisonValue(GenericComparisonValueContainer genericComparisonValueContainer) {
//        count = (Double) genericComparisonValueContainer.getValue(GenericComparisonValueContainer.Key.REMAINING_COUNT);
//    }
//
//    @Override
//    public void fillQuestProgressInfo(QuestProgressInfo questProgressInfo, ConditionService conditionService) {
//        questProgressInfo.setAmount(new QuestProgressInfo.Amount((int) (countTotal - count), (int) countTotal));
//    }
}
