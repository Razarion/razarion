package com.btxtech.shared.gameengine.planet.quest;


import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

/**
 * User: Razarion contributors
 * Date: 27.12.2010
 * Time: 17:25:49
 */
public interface AbstractComparison {
    boolean isFulfilled();

    AbstractConditionProgress getAbstractConditionProgress();

    void setAbstractConditionProgress(AbstractConditionProgress abstractConditionProgress);

    void fillGenericComparisonValues(BackupComparisionInfo backupComparisionInfo);

    void restoreFromGenericComparisonValue(BackupComparisionInfo backupComparisionInfo);

    QuestProgressInfo generateQuestProgressInfo();

    void handleDeferredUpdate();
}
