package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.item.SyncItemMonitor;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

import java.util.function.Supplier;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class InGameItemTipVisualization extends InGameTipVisualization {
    private Supplier<SyncItemMonitor> syncItemProvider;
    private DecimalPosition lastPosition2D;
    private SyncItemMonitor syncItemMonitor;

    public InGameItemTipVisualization(Supplier<SyncItemMonitor> syncItemProvider, double moveDistance, long duration, double cornerLength, Color cornerColor, Integer shape3DId, Integer outOfViewShape3DId, NativeMatrixFactory nativeMatrixFactory) {
        super(cornerLength, moveDistance, duration, cornerColor, shape3DId, outOfViewShape3DId, nativeMatrixFactory);
        this.syncItemProvider = syncItemProvider;
    }

    @Override
    Vertex getPosition3D() {
        return syncItemMonitor.getPosition3d();
    }

    @Override
    boolean hasPositionChanged() {
        if (lastPosition2D == null) {
            lastPosition2D = syncItemMonitor.getPosition2d();
            return true;
        } else if (lastPosition2D.equals(syncItemMonitor.getPosition2d())) {
            return false;
        } else {
            lastPosition2D = syncItemMonitor.getPosition2d();
            return true;
        }
    }

    @Override
    boolean checkReady() {
        syncItemMonitor = syncItemProvider.get();
        return syncItemMonitor != null;
    }

    @Override
    DecimalPosition getPosition2D() {
        return syncItemMonitor.getPosition2d();
    }


    @Override
    public void cleanup() {
        if(syncItemMonitor != null) {
            syncItemMonitor.release();
        }
    }
}
