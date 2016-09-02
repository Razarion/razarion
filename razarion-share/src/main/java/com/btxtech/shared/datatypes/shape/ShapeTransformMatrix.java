package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 06.08.2016.
 */
public class ShapeTransformMatrix implements ShapeTransform {
    private Matrix4 matrix;

    public ShapeTransformMatrix setMatrix(Matrix4 matrix) {
        this.matrix = matrix;
        return this;
    }

    @Override
    public Matrix4 setupMatrix() {
        return matrix;
    }

    @Override
    public Matrix4 setupNormMatrix() {
        return matrix.normTransformation();
    }
}
