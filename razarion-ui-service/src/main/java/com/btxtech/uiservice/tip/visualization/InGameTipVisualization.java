package com.btxtech.uiservice.tip.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: beat
 * Date: 21.08.12
 * Time: 22:51
 */
public abstract class InGameTipVisualization {
    private List<Vertex> cornerVertices;
    private final double moveDistance;
    private final long duration;
    private Color cornerColor;
    private Integer shape3DId;

    public InGameTipVisualization(double cornerLength, double moveDistance, long duration, Color cornerColor, Integer shape3DId) {
        this.moveDistance = moveDistance;
        this.duration = duration;
        this.cornerColor = cornerColor;
        this.shape3DId = shape3DId;
        setupCornerVertices(cornerLength);
    }

    public abstract List<ModelMatrices> provideCornerModelMatrices(long timeStamp);

    abstract Vertex getShape3Position();

    List<ModelMatrices> createCornerModelMatrices(Vertex position, long timeStamp) {
        double distance = moveDistance - moveDistance * (double) (timeStamp % duration) / (double) duration;
        Matrix4 animationMatrix = Matrix4.createTranslation(distance, -distance, 0);
        Matrix4 matrix1 = Matrix4.createTranslation(position).multiply(animationMatrix);
        Matrix4 matrix2 = Matrix4.createTranslation(position).multiply(Matrix4.ROT_90_Z.multiply(animationMatrix));
        Matrix4 matrix3 = Matrix4.createTranslation(position).multiply(Matrix4.ROT_180_Z.multiply(animationMatrix));
        Matrix4 matrix4 = Matrix4.createTranslation(position).multiply(Matrix4.ROT_270_Z.multiply(animationMatrix));

        //Matrix4 matrix2 = Matrix4.createTranslation(position).multiply(Matrix4.createTranslation(distance, -distance, 0)).multiply(Matrix4.ROT_90_Z);
        // Matrix4 matrix3 = matrix1.multiply(Matrix4.ROT_180_Z);
        // Matrix4 matrix4 = matrix1.multiply(Matrix4.ROT_270_Z);
        //  Matrix4 matrixItem = Matrix4.createTranslation(position);

        List<ModelMatrices> result = new ArrayList<>();
        result.add(new ModelMatrices(matrix1));
        result.add(new ModelMatrices(matrix2));
        result.add(new ModelMatrices(matrix3));
        result.add(new ModelMatrices(matrix4));


//        result = new ArrayList<>();
//        result.add(new ModelMatrices(matrixItem));
//        result.add(new ModelMatrices(Matrix4.createTranslation(10, -10, 0)));
//        result.add(new ModelMatrices(Matrix4.createTranslation(-10, 10, 0)));
//        result.add(new ModelMatrices(Matrix4.createTranslation(-10, -10, 0)));

        return result;

    }

    public Color getCornerColor() {
        return cornerColor;
    }

    public List<ModelMatrices> provideShape3DModelMatrices() {
        return Collections.singletonList(new ModelMatrices(Matrix4.createTranslation(getShape3Position())));
    }

    public Integer getShape3DId() {
        return shape3DId;
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

}
