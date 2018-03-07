package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;

/**
 * Created by Beat
 * on 06.03.2018.
 */
public class TickConditionProgress extends AbstractConditionProgress {
    public TickConditionProgress(ConditionTrigger conditionTrigger, AbstractComparison abstractComparison) {
        super(conditionTrigger, abstractComparison);
    }

    public void tick() {
        ((AbstractTickComparison) getAbstractComparison()).tick();
        if (getAbstractComparison().isFulfilled()) {
            setFulfilled();
        }
    }
}
