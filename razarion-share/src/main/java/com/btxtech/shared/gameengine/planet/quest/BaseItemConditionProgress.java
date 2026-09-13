package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * User: Razarion contributors
 * Date: 27.12.2010
 * Time: 18:58:14
 */
public class BaseItemConditionProgress extends AbstractConditionProgress {

    public BaseItemConditionProgress(ConditionTrigger conditionTrigger, AbstractComparison abstractComparison) {
        super(conditionTrigger, abstractComparison);
    }

    public void onItem(SyncBaseItem syncBaseItem) {
        ((AbstractBaseItemComparison) getAbstractComparison()).onSyncBaseItem(syncBaseItem);
        if (getAbstractComparison().isFulfilled()) {
            setFulfilled();
        }
    }
}
