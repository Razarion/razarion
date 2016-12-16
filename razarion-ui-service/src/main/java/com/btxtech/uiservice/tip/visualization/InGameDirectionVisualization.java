package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
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

    public InGameDirectionVisualization(Integer shape3DId, DecimalPosition terrainPositionHint ) {
        this.shape3DId = shape3DId;
        this.terrainPositionHint = terrainPositionHint;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    public List<ModelMatrices> provideDModelMatrices() {
        DecimalPosition center = viewField.calculateCenter();
        double angle = center.getAngle(terrainPositionHint);
        Matrix4 model = Matrix4.createTranslation(center.getX(), center.getY(), 0).multiply(Matrix4.createZRotation(angle));
        return Collections.singletonList(new ModelMatrices(model));
    }

    @Override
    public void onScroll(ViewField viewField) {
        this.viewField = viewField;
    }
}
