/*
 * Copyright (c) 2011.
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

import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 18:58:14
 */
public class BaseItemConditionProgress extends AbstractConditionProgress {

    public BaseItemConditionProgress(ConditionTrigger conditionTrigger, AbstractComparison abstractComparison) {
        super(conditionTrigger, abstractComparison);
    }

    public void onItem(SyncBaseItem syncBaseItem) {
        ((AbstractSyncItemComparison) getAbstractComparison()).onSyncBaseItem(syncBaseItem);
        if (getAbstractComparison().isFulfilled()) {
            setFulfilled();
        }
    }
}
