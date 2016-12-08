package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.Dependent;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 12:53
 */
@Dependent
public class IdleItemTipTask extends AbstractTipTask {
    private int actorItemTypeId;

    public void init(int actorItemTypeId) {
        this.actorItemTypeId = actorItemTypeId;
    }

    @Override
    public void internalStart() {
    }

    @Override
    public boolean isFulfilled() {
        return areAllItemsTypeIdle();
    }

    @Override
    public void internalCleanup() {
    }

    @Override
    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        if (syncBaseItem.getBaseItemType().getId() == actorItemTypeId) {
            if (areAllItemsTypeIdle()) {
                onSucceed();
            }
        }
    }

    @Override
    public InGameTipVisualization createInGameTip() {
        return null;
    }

    private boolean areAllItemsTypeIdle() {
        for (SyncBaseItem syncBaseItem : findItemsOfType(actorItemTypeId)) {
            if (!syncBaseItem.isIdle()) {
                return false;
            }
        }
        return true;
    }
}
