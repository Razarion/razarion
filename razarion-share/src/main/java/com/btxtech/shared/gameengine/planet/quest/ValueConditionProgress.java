package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;

/**
 * Created by Beat
 * 28.10.2016.
 */
public class ValueConditionProgress extends AbstractConditionProgress {
    public ValueConditionProgress(ConditionTrigger conditionTrigger, AbstractComparison abstractComparison) {
        super(conditionTrigger, abstractComparison);
    }

    public void onTriggerValue(double value) {
        ((BaseItemCountComparison) getAbstractComparison()).onValue(value);
        if (getAbstractComparison().isFulfilled()) {
            setFulfilled();
        }
    }
}
