package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 23.01.2017.
 */
public class SyncItemState {
    private final int syncItemId;
    private final double radius;
    private final Consumer<SyncItemState> releaseMonitorCallback;
    private final Collection<SyncItemMonitor> monitors = new ArrayList<>();
    private DecimalPosition position2d;

    public SyncItemState(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, double radius, Consumer<SyncItemState> releaseMonitorCallback) {
        syncItemId = nativeSyncBaseItemTickInfo.id;
        position2d = NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo);
        this.radius = radius;
        this.releaseMonitorCallback = releaseMonitorCallback;
    }

    public SyncItemState(int syncItemId, DecimalPosition position2d, double radius, Consumer<SyncItemState> releaseMonitorCallback) {
        this.syncItemId = syncItemId;
        this.position2d = position2d;
        this.radius = radius;
        this.releaseMonitorCallback = releaseMonitorCallback;
    }

    // Override in subclasses
    protected SyncItemMonitor createMonitor() {
        return new SyncItemMonitor(this);
    }

    public int getSyncItemId() {
        return syncItemId;
    }

    public DecimalPosition getPosition2d() {
        return position2d;
    }

    public double getRadius() {
        return radius;
    }

    public SyncItemMonitor createSyncItemMonitor() {
        SyncItemMonitor syncItemMonitor = createMonitor();
        monitors.add(syncItemMonitor);
        return syncItemMonitor;
    }

    protected Collection<SyncItemMonitor> getMonitors() {
        return monitors;
    }

    public void update(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        DecimalPosition newPosition = NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo);
        if (position2d != null || newPosition != null) {
            if (position2d != null && newPosition == null) {
                position2d = null;
                for (SyncItemMonitor monitor : monitors) {
                    monitor.onPositionChanged();
                }
            } else if (position2d == null || !position2d.equals(newPosition)) {
                position2d = newPosition;
                for (SyncItemMonitor monitor : monitors) {
                    monitor.onPositionChanged();
                }
            }
        }
    }

    public void releaseMonitor(SyncItemMonitor syncItemMonitor) {
        if (monitors.remove(syncItemMonitor) && monitors.isEmpty() && releaseMonitorCallback != null) {
            releaseMonitorCallback.accept(this);
        }
    }
}
