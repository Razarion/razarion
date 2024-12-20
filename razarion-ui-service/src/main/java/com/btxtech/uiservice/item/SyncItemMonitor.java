package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 23.01.2017.
 */
public class SyncItemMonitor {
    private final SyncItemState syncItemState;
    private Consumer<SyncItemMonitor> positionChangeListener;

    public SyncItemMonitor(SyncItemState syncItemState) {
        this.syncItemState = syncItemState;
    }

    public SyncItemState getSyncItemState() {
        return syncItemState;
    }

    public int getSyncItemId() {
        return syncItemState.getSyncItemId();
    }

    public DecimalPosition getPosition2d() {
        return syncItemState.getPosition2d();
    }

    public double getRadius() {
        return syncItemState.getRadius();
    }

    public void setPositionChangeListener(Consumer<SyncItemMonitor> positionChangeListener) {
        this.positionChangeListener = positionChangeListener;
    }

    public void release() {
        syncItemState.releaseMonitor(this);
    }

    public void onPositionChanged() {
        if (positionChangeListener != null) {
            positionChangeListener.accept(this);
        }
    }
}
