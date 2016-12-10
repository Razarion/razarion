package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class InGameItemTipVisualization extends InGameTipVisualization {
    private SyncItem syncItem;
    private DecimalPosition lastPosition2D;

    public InGameItemTipVisualization(SyncItem syncItem, double moveDistance, long duration, double cornerLength, Color cornerColor, Integer shape3DId, Integer outOfViewShape3DId) {
        super(cornerLength, moveDistance, duration, cornerColor, shape3DId, outOfViewShape3DId);
        this.syncItem = syncItem;
    }

    @Override
    Vertex getPosition3D() {
        return syncItem.getSyncPhysicalArea().getPosition3d();
    }

    @Override
    boolean hasPositionChanged() {
        if (lastPosition2D == null) {
            lastPosition2D = syncItem.getSyncPhysicalArea().getPosition2d();
            return true;
        } else if (lastPosition2D.equals(syncItem.getSyncPhysicalArea().getPosition2d())) {
            return false;
        } else {
            lastPosition2D = syncItem.getSyncPhysicalArea().getPosition2d();
            return true;
        }
    }

    @Override
    DecimalPosition getPosition2D() {
        return syncItem.getSyncPhysicalArea().getPosition2d();
    }
}
