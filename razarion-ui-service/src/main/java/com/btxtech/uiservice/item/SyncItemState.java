package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;

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
    private DecimalPosition interpolatableVelocity;
    private DecimalPosition position2d;
    private Vertex position3d;
    private double radius;
    private Consumer<SyncItemState> releaseMonitorCallback;
    private Collection<SyncItemMonitor> monitors = new ArrayList<>();

    public SyncItemState(SyncItemSimpleDto syncItem, DecimalPosition interpolatableVelocity, double radius, Consumer<SyncItemState> releaseMonitorCallback) {
        syncItemId = syncItem.getId();
        this.interpolatableVelocity = interpolatableVelocity;
        position2d = syncItem.getPosition2d();
        position3d = syncItem.getPosition3d();
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

    public DecimalPosition getInterpolatableVelocity() {
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

    public void update(SyncItemSimpleDto syncItemSimpleDto, DecimalPosition interpolatableVelocity) {
        if (position2d == null && syncItemSimpleDto.getPosition2d() == null) {
            return;
        } else if (position2d != null && syncItemSimpleDto.getPosition2d() == null) {
            position2d = null;
            position3d = null;
            this.interpolatableVelocity = null;
            for (SyncItemMonitor monitor : monitors) {
                monitor.onPositionChanged();
            }
        } else if (position2d == null && syncItemSimpleDto.getPosition2d() != null || !position2d.equals(syncItemSimpleDto.getPosition2d()) || !Objects.equals(this.interpolatableVelocity, interpolatableVelocity)) {
            position2d = syncItemSimpleDto.getPosition2d();
            position3d = syncItemSimpleDto.getPosition3d();
            this.interpolatableVelocity = interpolatableVelocity;
            for (SyncItemMonitor monitor : monitors) {
                monitor.onPositionChanged();
            }
        }
    }

    public void releaseMonitor(SyncItemMonitor syncItemMonitor) {
        if (monitors.remove(syncItemMonitor) && monitors.isEmpty() && releaseMonitorCallback != null) {
            releaseMonitorCallback.accept(this);
        }
    }
}
