package com.btxtech.uiservice.item;

import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 14.02.2017.
 */
public class SyncBaseItemState extends SyncItemState {
    private double health;
    private double constructing;
    private boolean contained;
    private Integer constructingBaseItemTypeId;
    private NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo;

    public SyncBaseItemState(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, double radius, Consumer<SyncItemState> releaseMonitorCallback) {
        super(nativeSyncBaseItemTickInfo, radius, releaseMonitorCallback);
        health = nativeSyncBaseItemTickInfo.health;
        constructing = nativeSyncBaseItemTickInfo.constructing;
        if (nativeSyncBaseItemTickInfo.constructingBaseItemTypeId > -1) {
            constructingBaseItemTypeId = nativeSyncBaseItemTickInfo.constructingBaseItemTypeId;
        }
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
        return constructingBaseItemTypeId != null;
    }

    public Integer getConstructingBaseItemTypeId() {
        return constructingBaseItemTypeId;
    }

    public SyncBaseItemSimpleDto getSyncBaseItem() {
        return SyncBaseItemSimpleDto.from(nativeSyncBaseItemTickInfo);
    }

    @Override
    public void update(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        super.update(nativeSyncBaseItemTickInfo);
        this.nativeSyncBaseItemTickInfo = nativeSyncBaseItemTickInfo;

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

        Integer tmpConstructingBaseItemTypeId = null;
        if (nativeSyncBaseItemTickInfo.constructingBaseItemTypeId > -1) {
            tmpConstructingBaseItemTypeId = nativeSyncBaseItemTickInfo.constructingBaseItemTypeId;
        }
        if (!Objects.equals(constructingBaseItemTypeId, tmpConstructingBaseItemTypeId)) {
            constructingBaseItemTypeId = tmpConstructingBaseItemTypeId;
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
