package com.btxtech.uiservice.tip.tiptask;

import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;

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
        return false;
    }

    @Override
    public void internalCleanup() {
    }

    @Override
    public void onSyncBaseItemIdle(SyncBaseItemSimpleDto syncBaseItem) {
        if (syncBaseItem.getItemTypeId() == actorItemTypeId) {
            onSucceed();
        }
    }
}
