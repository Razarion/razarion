package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainScrollListener;

import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 16.12.2016.
 */
public class InGameDirectionVisualization implements TerrainScrollListener {
    private Integer shape3DId;
    private DecimalPosition terrainPositionHint;
    private ViewField viewField;
    private boolean visible;

    public InGameDirectionVisualization(Integer shape3DId, DecimalPosition terrainPositionHint, boolean visible) {
        this.shape3DId = shape3DId;
        this.terrainPositionHint = terrainPositionHint;
        this.visible = visible;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    public List<ModelMatrices> provideDModelMatrices() {
        if (visible) {
            DecimalPosition center = viewField.calculateCenter();
            double angle = center.getAngle(terrainPositionHint);
            return Collections.singletonList(ModelMatrices.createFromPositionAndZRotation(center.getX(), center.getY(), 0, angle));
        } else {
            return null;
        }
    }

    @Override
    public void onScroll(ViewField viewField) {
        this.viewField = viewField;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
