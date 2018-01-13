package com.btxtech.uiservice.item;

import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 14.02.2017.
 */
public class SyncBaseItemState extends SyncItemState {
    private double health;
    private double constructing;
    private boolean contained;
    private NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo;

    public SyncBaseItemState(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, double radius, Consumer<SyncItemState> releaseMonitorCallback) {
        super(nativeSyncBaseItemTickInfo, nativeSyncBaseItemTickInfo.interpolatableVelocity, radius, releaseMonitorCallback);
        health = nativeSyncBaseItemTickInfo.health;
        constructing = nativeSyncBaseItemTickInfo.constructing;
        contained = nativeSyncBaseItemTickInfo.contained;
        this.nativeSyncBaseItemTickInfo = nativeSyncBaseItemTickInfo;
    }

    @Override
    protected SyncItemMonitor createMonitor() {
        return new SyncBaseItemMonitor(this);
    }

    public double getHealth() {
        return health;
    }

    public double getConstructing() {
        return constructing;
    }

    public boolean checkConstructing() {
        return constructing > 0.0;
    }

    public SyncBaseItemSimpleDto getSyncBaseItem() {
        return SyncBaseItemSimpleDto.from(nativeSyncBaseItemTickInfo);
    }

    @Override
    public void update(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, NativeVertexDto interpolatableVelocity) {
        super.update(nativeSyncBaseItemTickInfo, interpolatableVelocity);

        if (health != nativeSyncBaseItemTickInfo.health) {
            health = nativeSyncBaseItemTickInfo.health;
            for (SyncItemMonitor monitor : getMonitors()) {
                ((SyncBaseItemMonitor) monitor).onHealthChanged();
            }
        }

        if (constructing != nativeSyncBaseItemTickInfo.constructing) {
            constructing = nativeSyncBaseItemTickInfo.constructing;
            for (SyncItemMonitor monitor : getMonitors()) {
                ((SyncBaseItemMonitor) monitor).onConstructingChanged();
            }
        }

        if (contained != nativeSyncBaseItemTickInfo.contained) {
            contained = nativeSyncBaseItemTickInfo.contained;
            for (SyncItemMonitor monitor : getMonitors()) {
                ((SyncBaseItemMonitor) monitor).onContainedChanged();
            }
        }

    }
}
