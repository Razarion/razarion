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


import com.btxtech.shared.gameengine.planet.quest.AbstractComparison;
import com.btxtech.shared.gameengine.planet.quest.AbstractConditionProgress;
import com.btxtech.shared.gameengine.planet.quest.BaseItemConditionProgress;
import com.btxtech.shared.gameengine.planet.quest.InventoryItemConditionProgress;
import com.btxtech.shared.gameengine.planet.quest.ValueConditionProgress;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 20:30:23
 */
public enum ConditionTrigger {
    SYNC_ITEM_KILLED(Type.BASE_ITEM, true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new BaseItemConditionProgress(this, abstractComparison);
        }
    },
    HARVEST(Type.HARVEST, true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new ValueConditionProgress(this, abstractComparison);
        }
    },
    SYNC_ITEM_CREATED(Type.BASE_ITEM, true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new BaseItemConditionProgress(this, abstractComparison);
        }
    },
    //    XP_INCREASED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ValueConditionProgress(this, abstractComparison);
//        }
//    },
//    BASE_KILLED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ValueConditionProgress(this, abstractComparison);
//        }
//    },
//    TUTORIAL(false) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new SimpleConditionTrigger(this);
//        }
//    },
    SYNC_ITEM_POSITION(Type.BASE_ITEM, true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new BaseItemConditionProgress(this, abstractComparison);
        }
    },
    //    CRYSTALS_INCREASED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ValueConditionProgress(this, abstractComparison);
//        }
//    },
//    ARTIFACT_ITEM_ADDED(true) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new ArtifactItemIdConditionTrigger(this, abstractComparison);
//        }
//    },
    BOX_PICKED(Type.BOX_PICKED, true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new ValueConditionProgress(this, abstractComparison);
        }
    },
    INVENTORY_ITEM_PLACED(Type.INVENTORY_ITEM, true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new InventoryItemConditionProgress(this, abstractComparison);
        }
    };

    public enum Type {
        BASE_ITEM,
        INVENTORY_ITEM,
        BOX_PICKED,
        HARVEST
    }

    private Type type;
    private boolean comparisonNeeded;

    ConditionTrigger(Type type, boolean comparisonNeeded) {
        this.type = type;
        this.comparisonNeeded = comparisonNeeded;
    }

    public abstract AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison);

    public Type getType() {
        return type;
    }

    public boolean isComparisonNeeded() {
        return comparisonNeeded;
    }
}
