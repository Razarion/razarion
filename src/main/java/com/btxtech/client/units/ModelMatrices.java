package com.btxtech.client.units;

import com.btxtech.shared.primitives.Matrix4;

/**
 * Created by Beat
 * 15.05.2016.
 */
public class ModelMatrices {
    private Matrix4 vertex;
    private Matrix4 norm;

    public ModelMatrices(Matrix4 vertex, Matrix4 norm) {
        this.vertex = vertex;
        this.norm = norm;
    }

    public Matrix4 getVertex() {
        return vertex;
    }

    public Matrix4 getNorm() {
        return norm;
    }
}
