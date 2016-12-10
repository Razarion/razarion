package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainScrollListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: beat
 * Date: 21.08.12
 * Time: 22:51
 */
public abstract class InGameTipVisualization implements TerrainScrollListener {
    private List<Vertex> cornerVertices;
    private final double moveDistance;
    private final long duration;
    private Color cornerColor;
    private Integer shape3DId;
    private Integer outOfViewShape3DId;
    private boolean inViewFiled;
    private ViewField viewField;

    public InGameTipVisualization(double cornerLength, double moveDistance, long duration, Color cornerColor, Integer shape3DId, Integer outOfViewShape3DId) {
        this.moveDistance = moveDistance;
        this.duration = duration;
        this.cornerColor = cornerColor;
        this.shape3DId = shape3DId;
        this.outOfViewShape3DId = outOfViewShape3DId;
        setupCornerVertices(cornerLength);
    }

    abstract Vertex getPosition3D();

    abstract boolean hasPositionChanged();

    abstract DecimalPosition getPosition2D();

    public void preRender() {
        if (hasPositionChanged()) {
            checkInView();
        }
    }

    public List<ModelMatrices> provideCornerModelMatrices(long timeStamp) {
        if (inViewFiled) {
            return createCornerModelMatrices(getPosition3D(), timeStamp);
        } else {
            return null;
        }
    }

    public List<ModelMatrices> provideShape3DModelMatrices() {
        if (inViewFiled) {
            return Collections.singletonList(new ModelMatrices(Matrix4.createTranslation(getPosition3D())));
        } else {
            return null;
        }
    }

    public List<ModelMatrices> provideOutOfViewShape3DModelMatrices() {
        if (inViewFiled) {
            return null;
        } else {
            DecimalPosition center = viewField.calculateCenter();
            double angle = center.getAngle(getPosition2D());
            Matrix4 model = Matrix4.createTranslation(center.getX(), center.getY(), 0).multiply(Matrix4.createZRotation(angle));
            return Collections.singletonList(new ModelMatrices(model));
        }
    }

    private List<ModelMatrices> createCornerModelMatrices(Vertex position, long timeStamp) {
        double distance = moveDistance - moveDistance * (double) (timeStamp % duration) / (double) duration;
        Matrix4 positionMatrix = Matrix4.createTranslation(position);
        Matrix4 animationMatrix = Matrix4.createTranslation(distance, -distance, 0);
        Matrix4 matrix1 = positionMatrix.multiply(animationMatrix);
        Matrix4 matrix2 = positionMatrix.multiply(Matrix4.ROT_90_Z.multiply(animationMatrix));
        Matrix4 matrix3 = positionMatrix.multiply(Matrix4.ROT_180_Z.multiply(animationMatrix));
        Matrix4 matrix4 = positionMatrix.multiply(Matrix4.ROT_270_Z.multiply(animationMatrix));

        List<ModelMatrices> result = new ArrayList<>();
        result.add(new ModelMatrices(matrix1));
        result.add(new ModelMatrices(matrix2));
        result.add(new ModelMatrices(matrix3));
        result.add(new ModelMatrices(matrix4));

        return result;
    }

    public Color getCornerColor() {
        return cornerColor;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    public Integer getOutOfViewShape3DId() {
        return outOfViewShape3DId;
    }

    public List<Vertex> getCornerVertices() {
        return cornerVertices;
    }

    private void setupCornerVertices(double cornerLength) {
        cornerVertices = new ArrayList<>();
        // Leg 1
        cornerVertices.add(new Vertex(-cornerLength, 0, 0));
        cornerVertices.add(new Vertex(0, 0, 0));
        // Leg 2
        cornerVertices.add(new Vertex(0, 0, 0));
        cornerVertices.add(new Vertex(0, cornerLength, 0));
    }

    @Override
    public void onScroll(ViewField viewField) {
        this.viewField = viewField;
        checkInView();
    }

    private void checkInView() {
        inViewFiled = viewField.isInside(getPosition2D());
    }
}
