package com.btxtech.shared.gameengine.datatypes.config;


import com.btxtech.shared.gameengine.planet.quest.*;

/**
 * User: Razarion contributors
 * Date: 27.12.2010
 * Time: 20:30:23
 */
public enum ConditionTrigger {
    SYNC_ITEM_KILLED(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new BaseItemConditionProgress(this, abstractComparison);
        }
    },
    HARVEST(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new ValueConditionProgress(this, abstractComparison);
        }
    },
    SYNC_ITEM_CREATED(true) {
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
    BASE_KILLED(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new ValueConditionProgress(this, abstractComparison);
        }
    },
    //    TUTORIAL(false) {
//        @Override
//        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
//            return new SimpleConditionTrigger(this);
//        }
//    },
    SYNC_ITEM_POSITION(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new TickConditionProgress(this, abstractComparison);
        }
    },
    /**
     * Units of the type count sitting inside a container, anywhere. The first of the three steps
     * off the noob island (load, sail, unload): no quest before it ever asked for a load.
     */
    SYNC_ITEM_LOADED(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new TickConditionProgress(this, abstractComparison);
        }
    },
    /**
     * Like {@link #SYNC_ITEM_POSITION}, but a container only counts while it carries something. An
     * empty transporter in the region would pass the sailing step and leave the player at the
     * unloading step with nothing to unload.
     */
    LOADED_CONTAINER_POSITION(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new TickConditionProgress(this, abstractComparison);
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
    BOX_PICKED(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new ValueConditionProgress(this, abstractComparison);
        }
    },
    INVENTORY_ITEM_PLACED(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new InventoryItemConditionProgress(this, abstractComparison);
        }
    },
    UNLOCKED(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new ValueConditionProgress(this, abstractComparison);
        }
    },
    SELL(true) {
        @Override
        public AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison) {
            return new BaseItemConditionProgress(this, abstractComparison);
        }
    };

    private final boolean comparisonNeeded;

    ConditionTrigger(boolean comparisonNeeded) {
        this.comparisonNeeded = comparisonNeeded;
    }

    public abstract AbstractConditionProgress createConditionProgress(AbstractComparison abstractComparison);

    public boolean isComparisonNeeded() {
        return comparisonNeeded;
    }
}
