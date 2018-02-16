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
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 23:33:37
 */
public class AbstractConditionProgress {
    private ConditionTrigger conditionTrigger;
    private AbstractComparison abstractComparison;
    private boolean fulfilled = false;
    private HumanPlayerId humanPlayerId;
    private QuestConfig questConfig;

    public AbstractConditionProgress(ConditionTrigger conditionTrigger, AbstractComparison abstractComparison) {
        this.conditionTrigger = conditionTrigger;
        this.abstractComparison = abstractComparison;
        if (abstractComparison != null) {
            abstractComparison.setAbstractConditionProgress(this);
            fulfilled = abstractComparison.isFulfilled();
        }
    }

    public void setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
    }

    public ConditionTrigger getConditionTrigger() {
        return conditionTrigger;
    }

    public AbstractComparison getAbstractComparison() {
        return abstractComparison;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    protected void setFulfilled() {
        fulfilled = true;
    }

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public void setQuestConfig(QuestConfig questConfig) {
        this.questConfig = questConfig;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

    public BackupComparisionInfo generateBackupComparisionInfo() {
        BackupComparisionInfo backupComparisionInfo = new BackupComparisionInfo();
        backupComparisionInfo.setQuestId(questConfig.getId());
        backupComparisionInfo.setHumanPlayerId(humanPlayerId);
        abstractComparison.fillGenericComparisonValues(backupComparisionInfo);
        return backupComparisionInfo;
    }

    public void restore(BackupComparisionInfo backupComparisionInfo) {
        abstractComparison.restoreFromGenericComparisonValue(backupComparisionInfo);
    }

    @Override
    public String toString() {
        return "AbstractConditionProgress{" +
                "conditionTrigger=" + conditionTrigger +
                ", abstractComparison=" + abstractComparison +
                ", fulfilled=" + fulfilled +
                ", humanPlayerId=" + humanPlayerId +
                ", questConfig=" + questConfig +
                '}';
    }
}
