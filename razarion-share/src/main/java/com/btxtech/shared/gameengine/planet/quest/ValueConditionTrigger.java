package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;

/**
 * Created by Beat
 * 28.10.2016.
 */
public class ValueConditionTrigger extends AbstractConditionProgress {
    public ValueConditionTrigger(ConditionTrigger conditionTrigger, AbstractComparison abstractComparison) {
        super(conditionTrigger, abstractComparison);
    }

    public void onTriggerValue(double value) {
        ((CountComparison) getAbstractComparison()).onValue(value);
        if (getAbstractComparison().isFulfilled()) {
            setFulfilled();
        }
    }
}
