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
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.context.Dependent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 16:31:29
 */
@Dependent
public class BaseItemCountComparison extends AbstractBaseItemComparison {
    private int count;
    private double countTotal;
    private Set<Integer> botIds;

    public void init(int count, Collection<Integer> botIds) {
        this.count = count;
        if (botIds != null) {
            this.botIds = new HashSet<>(botIds);
        } else {
            this.botIds = null;
        }
        countTotal = count;
    }

    @Override
    protected void privateOnSyncBaseItem(SyncBaseItem syncBaseItem) {
        if (!isBotIdAllowed(botIds, syncBaseItem)) {
            return;
        }
        count--;
        onProgressChanged();
    }

    @Override
    public boolean isFulfilled() {
        return count <= 0;
    }

    public double getCount() {
        return count;
    }


    @Override
    public void fillGenericComparisonValues(BackupComparisionInfo backupComparisionInfo) {
        backupComparisionInfo.setRemainingCount(count);
    }

    @Override
    public void restoreFromGenericComparisonValue(BackupComparisionInfo backupComparisionInfo) {
        backupComparisionInfo.checkRemainingCount();
        count = backupComparisionInfo.getRemainingCount();
    }

    @Override
    public QuestProgressInfo generateQuestProgressInfo() {
        return new QuestProgressInfo().setCount((int) (countTotal - count));
    }
}
