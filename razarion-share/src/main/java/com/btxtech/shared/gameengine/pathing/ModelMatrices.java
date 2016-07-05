package com.btxtech.shared.gameengine.pathing;

import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 15.05.2016.
 */
public class ModelMatrices {
    private Matrix4 model;
    private Matrix4 norm;

    public ModelMatrices(Matrix4 model, Matrix4 norm) {
        this.model = model;
        this.norm = norm;
    }

    public Matrix4 getModel() {
        return model;
    }

    public Matrix4 getNorm() {
        return norm;
    }
}
