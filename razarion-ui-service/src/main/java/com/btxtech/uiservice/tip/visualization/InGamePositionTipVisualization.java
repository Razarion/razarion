package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.nativejs.NativeMatrixFactory;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class InGamePositionTipVisualization extends InGameTipVisualization {
    private Vertex position;

    public InGamePositionTipVisualization(double moveDistance, long duration, double cornerLength, Color cornerColor, Integer shape3DId, Integer outOfViewShape3DId, NativeMatrixFactory nativeMatrixFactory) {
        super(cornerLength, moveDistance, duration, cornerColor, shape3DId, outOfViewShape3DId, nativeMatrixFactory);
    }

    public void setPosition(Vertex position) {
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

    @Override
    boolean checkReady() {
        return position != null;
    }
}
