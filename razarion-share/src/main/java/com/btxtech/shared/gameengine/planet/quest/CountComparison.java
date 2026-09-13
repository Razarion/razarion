package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.GameLogicService;

import jakarta.inject.Inject;

/**
 * User: Razarion contributors
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
