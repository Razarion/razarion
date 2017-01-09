package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;

import java.util.function.Supplier;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class InGameItemTipVisualization extends InGameTipVisualization {
    private Supplier<SyncItemSimpleDto> syncItemProvider;
    private DecimalPosition lastPosition2D;
    private SyncItemSimpleDto syncItem;

    public InGameItemTipVisualization(Supplier<SyncItemSimpleDto> syncItemProvider, double moveDistance, long duration, double cornerLength, Color cornerColor, Integer shape3DId, Integer outOfViewShape3DId) {
        super(cornerLength, moveDistance, duration, cornerColor, shape3DId, outOfViewShape3DId);
        this.syncItemProvider = syncItemProvider;
    }

    @Override
    Vertex getPosition3D() {
        return syncItem.getPosition3d();
    }

    @Override
    boolean hasPositionChanged() {
        if (lastPosition2D == null) {
            lastPosition2D = syncItem.getPosition2d();
            return true;
        } else if (lastPosition2D.equals(syncItem.getPosition2d())) {
            return false;
        } else {
            lastPosition2D = syncItem.getPosition2d();
            return true;
        }
    }

    @Override
    boolean checkReady() {
        syncItem = syncItemProvider.get();
        return syncItem != null;
    }

    @Override
    DecimalPosition getPosition2D() {
        return syncItem.getPosition2d();
    }
}
