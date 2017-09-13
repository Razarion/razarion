package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;

import java.util.List;

/**
 * Created by Beat
 * on 13.09.2017.
 */
public abstract class AbstractSyncItemSetPositionMonitor {
    private Runnable releaseCallback;

    public abstract boolean hasInViewPositions();

    public abstract List<Vertex> getInViewPosition3d();

    public abstract DecimalPosition getNearestOutOfViewPosition2d();

    public AbstractSyncItemSetPositionMonitor(Runnable releaseCallback) {
        this.releaseCallback = releaseCallback;
    }

    public void release() {
        if (releaseCallback == null) {
            throw new IllegalStateException("AbstractSyncItemSetPositionMonitor.release() releaseCallback == null");
        }
        releaseCallback.run();
        releaseCallback = null;
    }

}
