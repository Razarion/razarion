package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.nativejs.NativeVertexDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 23.01.2017.
 */
public class SyncItemState {
    private int syncItemId;
    private NativeVertexDto interpolatableVelocity;
    private DecimalPosition position2d;
    private Vertex position3d;
    private double radius;
    private Consumer<SyncItemState> releaseMonitorCallback;
    private Collection<SyncItemMonitor> monitors = new ArrayList<>();

    public SyncItemState(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, NativeVertexDto interpolatableVelocity, double radius, Consumer<SyncItemState> releaseMonitorCallback) {
        syncItemId = nativeSyncBaseItemTickInfo.id;
        this.interpolatableVelocity = interpolatableVelocity;
        position2d = NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo);
        position3d = NativeUtil.toSyncBaseItemPosition3d(nativeSyncBaseItemTickInfo);
        this.radius = radius;
        this.releaseMonitorCallback = releaseMonitorCallback;
    }

    public SyncItemState(int syncItemId, DecimalPosition position2d, Vertex position3d, double radius, Consumer<SyncItemState> releaseMonitorCallback) {
        this.syncItemId = syncItemId;
        this.position2d = position2d;
        this.position3d = position3d;
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

    public NativeVertexDto getInterpolatableVelocity() {
        return interpolatableVelocity;
    }

    public DecimalPosition getPosition2d() {
        return position2d;
    }

    public Vertex getPosition3d() {
        return position3d;
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

    public void update(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo, NativeVertexDto interpolatableVelocity) {
        DecimalPosition newPosition = NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo);
        if (position2d != null || newPosition != null) {
            if (position2d != null && newPosition == null) {
                position2d = null;
                position3d = null;
                this.interpolatableVelocity = null;
                for (SyncItemMonitor monitor : monitors) {
                    monitor.onPositionChanged();
                }
            } else if (position2d == null || !position2d.equals(newPosition) || !Objects.equals(this.interpolatableVelocity, interpolatableVelocity)) {
                position2d = newPosition;
                position3d = NativeUtil.toSyncBaseItemPosition3d(nativeSyncBaseItemTickInfo);
                this.interpolatableVelocity = interpolatableVelocity;
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
