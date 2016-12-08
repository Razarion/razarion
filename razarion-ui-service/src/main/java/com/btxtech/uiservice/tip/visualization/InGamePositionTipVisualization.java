package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;

import java.util.List;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class InGamePositionTipVisualization extends InGameTipVisualization {
    private Vertex position;

    public InGamePositionTipVisualization(Vertex position, double moveDistance, long duration, double cornerLength, Color cornerColor, Integer shape3DId) {
        super(cornerLength, moveDistance, duration, cornerColor, shape3DId);
        this.position = position;
    }

    @Override
    public List<ModelMatrices> provideCornerModelMatrices(long timeStamp) {
        return createCornerModelMatrices(position, timeStamp);
    }

    @Override
    public Vertex getShape3Position() {
        return position;
    }
}
