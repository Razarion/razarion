package com.btxtech.shared.datatypes;

import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * Created by Beat
 * 15.05.2016.
 */
public class ModelMatrices {
    private Matrix4 model;
    private Matrix4 norm;
    private SyncBaseItem syncBaseItem;

    public ModelMatrices setModel(Matrix4 model) {
        this.model = model;
        return this;
    }

    public ModelMatrices setNorm(Matrix4 norm) {
        this.norm = norm;
        return this;
    }

    public ModelMatrices setSyncBaseItem(SyncBaseItem syncBaseItem) {
        this.syncBaseItem = syncBaseItem;
        return this;
    }

    public Matrix4 getModel() {
        return model;
    }

    public Matrix4 getNorm() {
        return norm;
    }

    public SyncBaseItem getSyncBaseItem() {
        return syncBaseItem;
    }

    public ModelMatrices multiply(Matrix4 model, Matrix4 norm) {
        ModelMatrices modelMatrices = new ModelMatrices();
        modelMatrices.setSyncBaseItem(syncBaseItem);
        if (model != null) {
            modelMatrices.setModel(this.model.multiply(model));
        } else {
            modelMatrices.setModel(this.model);
        }
        if (norm != null) {
            modelMatrices.setNorm(this.norm.multiply(norm));
        } else {
            modelMatrices.setNorm(this.norm);
        }
        return modelMatrices;
    }

    public static ModelMatrices createFromPositionAndDirection(Vertex position, Vertex direction) {
        direction = direction.normalize(1.0);
        double yRotation = -Math.asin(direction.getZ());
        double zRotation = direction.toXY().angle();
        Matrix4 model = Matrix4.createTranslation(position).multiply(Matrix4.createZRotation(zRotation).multiply(Matrix4.createYRotation(yRotation)));
        return new ModelMatrices().setModel(model).setNorm(model.normTransformation());
    }
}
