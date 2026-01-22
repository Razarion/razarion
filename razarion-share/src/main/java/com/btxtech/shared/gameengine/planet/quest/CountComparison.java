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

import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.GameLogicService;

import jakarta.inject.Inject;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 16:31:29
 */

public class CountComparison extends AbstractUpdatingComparison {
    private AbstractConditionProgress abstractConditionTrigger;
    private double count;
    private double countTotal;

    @Inject
    public CountComparison(GameLogicService gameLogicService) {
        super(gameLogicService);
    }

    public void init(int count) {
        this.count = count;
        countTotal = count;
    }

    public void onValue(double value) {
        count -= value;
        onProgressChanged();
    }

    @Override
    public boolean isFulfilled() {
        return count <= 0.0;
    }

    @Override
    public AbstractConditionProgress getAbstractConditionProgress() {
        return abstractConditionTrigger;
    }

    @Override
    public void setAbstractConditionProgress(AbstractConditionProgress abstractConditionProgress) {
        this.abstractConditionTrigger = abstractConditionProgress;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    @Override
    public void fillGenericComparisonValues(BackupComparisionInfo backupComparisionInfo) {
        backupComparisionInfo.setRemainingCount((int) count);
    }

    @Override
    public void restoreFromGenericComparisonValue(BackupComparisionInfo backupComparisionInfo) {
        backupComparisionInfo.checkRemainingCount();
        count = (double) backupComparisionInfo.getRemainingCount();
    }

    @Override
    public QuestProgressInfo generateQuestProgressInfo() {
        return new QuestProgressInfo().setCount((int) (countTotal - count));
    }
}
