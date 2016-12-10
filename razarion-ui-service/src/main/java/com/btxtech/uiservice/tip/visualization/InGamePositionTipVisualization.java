package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class InGamePositionTipVisualization extends InGameTipVisualization {
    private Vertex position;

    public InGamePositionTipVisualization(Vertex position, double moveDistance, long duration, double cornerLength, Color cornerColor, Integer shape3DId, Integer outOfViewShape3DId) {
        super(cornerLength, moveDistance, duration, cornerColor, shape3DId, outOfViewShape3DId);
        this.position = position;
    }

    @Override
    Vertex getPosition3D() {
        return position;
    }

    @Override
    boolean hasPositionChanged() {
        return false;
    }

    @Override
    DecimalPosition getPosition2D() {
        return position.toXY();
    }
}
