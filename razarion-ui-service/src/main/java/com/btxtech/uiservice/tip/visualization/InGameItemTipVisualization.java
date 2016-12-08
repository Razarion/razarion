package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

import java.util.List;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class InGameItemTipVisualization extends InGameTipVisualization {
    private SyncItem syncItem;

    public InGameItemTipVisualization(SyncItem syncItem, double moveDistance, long duration, double cornerLength, Color cornerColor, Integer shape3DId) {
        super(cornerLength, moveDistance, duration, cornerColor, shape3DId);
        this.syncItem = syncItem;
    }

    @Override
    public List<ModelMatrices> provideCornerModelMatrices(long timeStamp) {
        return createCornerModelMatrices(syncItem.getSyncPhysicalArea().getPosition3d(), timeStamp);
    }

    @Override
    public Vertex getShape3Position() {
        return syncItem.getSyncPhysicalArea().getPosition3d();
    }
}
