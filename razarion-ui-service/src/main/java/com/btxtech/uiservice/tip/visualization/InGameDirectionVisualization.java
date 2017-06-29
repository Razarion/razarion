package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;

import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 16.12.2016.
 */
public class InGameDirectionVisualization implements ViewService.ViewFieldListener {
    private NativeMatrixFactory nativeMatrixFactory;
    private Integer shape3DId;
    private DecimalPosition terrainPositionHint;
    private ViewField viewField;
    private boolean visible;

    public InGameDirectionVisualization(Integer shape3DId, DecimalPosition terrainPositionHint, boolean visible, NativeMatrixFactory nativeMatrixFactory) {
        this.shape3DId = shape3DId;
        this.terrainPositionHint = terrainPositionHint;
        this.visible = visible;
        this.nativeMatrixFactory = nativeMatrixFactory;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    public List<ModelMatrices> provideDModelMatrices() {
        if (visible) {
            DecimalPosition center = viewField.calculateCenter();
            double angle = center.getAngle(terrainPositionHint);
            return Collections.singletonList(ModelMatrices.createFromPositionAndZRotation(center.getX(), center.getY(), 0, angle, nativeMatrixFactory));
        } else {
            return null;
        }
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        this.viewField = viewField;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
