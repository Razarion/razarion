package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 15.05.2016.
 */
public class ModelMatrices {
    private Matrix4 model;
    private Matrix4 norm;
    private double progress;

    public Matrix4 getModel() {
        return model;
    }

    public ModelMatrices setModel(Matrix4 model) {
        this.model = model;
        return this;
    }

    public Matrix4 getNorm() {
        return norm;
    }

    public ModelMatrices setNorm(Matrix4 norm) {
        this.norm = norm;
        return this;
    }

    public double getProgress() {
        return progress;
    }

    public ModelMatrices setProgress(double progress) {
        this.progress = progress;
        return this;
    }

    public ModelMatrices multiply(Matrix4 model, Matrix4 norm) {
        ModelMatrices modelMatrices = new ModelMatrices();
        modelMatrices.setProgress(progress);
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

    public ModelMatrices copy(double progress) {
        return new ModelMatrices().setModel(model).setNorm(norm).setProgress(progress);
    }

    public static ModelMatrices createFromPosition(Vertex position) {
        return new ModelMatrices().setModel(Matrix4.createTranslation(position)).setNorm(Matrix4.createIdentity());
    }

    public static ModelMatrices createFromPositionAndDirection(Vertex position, Vertex direction) {
        direction = direction.normalize(1.0);
        double yRotation = -Math.asin(direction.getZ());
        double zRotation = direction.toXY().angle();
        Matrix4 model = Matrix4.createTranslation(position).multiply(Matrix4.createZRotation(zRotation).multiply(Matrix4.createYRotation(yRotation)));
        return new ModelMatrices().setModel(model).setNorm(model.normTransformation());
    }

    public static ModelMatrices createFromPositionAndDirection(Vertex position, double zRotation) {
        Matrix4 model = Matrix4.createTranslation(position).multiply(Matrix4.createZRotation(zRotation));
        return new ModelMatrices().setModel(model).setNorm(model.normTransformation());
    }
}
