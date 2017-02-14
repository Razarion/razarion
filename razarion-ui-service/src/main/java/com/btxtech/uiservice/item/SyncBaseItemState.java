package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 14.02.2017.
 */
public class SyncBaseItemState extends SyncItemState {
    private double health;

    public SyncBaseItemState(SyncBaseItemSimpleDto syncBaseItem, DecimalPosition interpolatableVelocity, double radius, Consumer<SyncItemState> releaseMonitorCallback) {
        super(syncBaseItem, interpolatableVelocity, radius, releaseMonitorCallback);
        health = syncBaseItem.getHealth();
    }

    @Override
    protected SyncItemMonitor createMonitor() {
        return new SyncBaseItemMonitor(this);
    }

    public double getHealth() {
        return health;
    }

    @Override
    public void update(SyncItemSimpleDto syncItemSimpleDto, DecimalPosition interpolatableVelocity) {
        super.update(syncItemSimpleDto, interpolatableVelocity);
        SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) syncItemSimpleDto;

        if (health != syncBaseItem.getHealth()) {
            health = syncBaseItem.getHealth();
            for (SyncItemMonitor monitor : getMonitors()) {
                ((SyncBaseItemMonitor) monitor).onHealthChanged();
            }
        }


    }

}
