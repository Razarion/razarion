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

package com.btxtech.shared.gameengine.datatypes.config;


import com.btxtech.shared.gameengine.planet.condition.AbstractComparison;
import com.btxtech.shared.gameengine.planet.condition.AbstractConditionProgress;
import com.btxtech.shared.gameengine.planet.condition.SyncBaseItemConditionProgress;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 20:30:23
 */
public enum ConditionTrigger {
    SYNC_ITEM_KILLED(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new SyncBaseItemConditionProgress(this, abstractComparison);
        }
    },
//    MONEY_INCREASED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ValueConditionTrigger(this, abstractComparison);
//        }
//    },
    SYNC_ITEM_CREATED(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new SyncBaseItemConditionProgress(this, abstractComparison);
        }
    },
//    XP_INCREASED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ValueConditionTrigger(this, abstractComparison);
//        }
//    },
//    BASE_KILLED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ValueConditionTrigger(this, abstractComparison);
//        }
//    },
//    TUTORIAL(false) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new SimpleConditionTrigger(this);
//        }
//    },
    SYNC_ITEM_POSITION(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new SyncBaseItemConditionProgress(this, abstractComparison);
        }
    };
//    CRYSTALS_INCREASED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ValueConditionTrigger(this, abstractComparison);
//        }
//    },
//    ARTIFACT_ITEM_ADDED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ArtifactItemIdConditionTrigger(this, abstractComparison);
//        }
//    },
//    BOX_PICKED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new BoxPickedConditionTrigger(this, abstractComparison);
//        }
//    };

    private boolean comparisonNeeded;

    ConditionTrigger(boolean comparisonNeeded) {
        this.comparisonNeeded = comparisonNeeded;
    }

    public abstract AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison);

    public boolean isComparisonNeeded() {
        return comparisonNeeded;
    }
}
