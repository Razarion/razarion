package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 15.05.2016.
 */
public class ModelMatrices {
    private Matrix4 model;
    private Matrix4 norm;
    private double progress;

    public ModelMatrices(Matrix4 model) {
        this(model, 0);
    }

    public ModelMatrices(Matrix4 model, double progress) {
        this(model, model.normTransformation(), progress);
    }

    public ModelMatrices(Matrix4 model, Matrix4 norm, double progress) {
        this.model = model;
        this.norm = norm;
        this.progress = progress;
    }

    public Matrix4 getModel() {
        return model;
    }

    public Matrix4 getNorm() {
        return norm;
    }

    public double getProgress() {
        return progress;
    }

    public ModelMatrices multiply(ModelMatrices modelMatrices) {
        return new ModelMatrices(model.multiply(modelMatrices.model));
    }

    public ModelMatrices multiply(Matrix4 matrix4) {
        return new ModelMatrices(model.multiply(matrix4), progress);
    }

    public ModelMatrices copy(double progress) {
        return new ModelMatrices(model, norm, progress);
    }

    public static ModelMatrices createFromPosition(Vertex position, double progress) {
        return new ModelMatrices(Matrix4.createTranslation(position), progress);
    }

    public static ModelMatrices createFromPositionAndZRotation(Vertex position, Vertex direction, double progress) {
        direction = direction.normalize(1.0);
        double yRotation = -Math.asin(direction.getZ());
        double zRotation = direction.toXY().angle();
        Matrix4 model = Matrix4.createTranslation(position).multiply(Matrix4.createZRotation(zRotation).multiply(Matrix4.createYRotation(yRotation)));
        return new ModelMatrices(model, progress);
    }

    public static ModelMatrices createFromPositionAndZRotation(Vertex position, Vertex direction) {
        return createFromPositionAndZRotation(position, direction, 0);
    }

    public static ModelMatrices createFromPositionAndZRotation(Vertex position, double zRotation) {
        Matrix4 model = Matrix4.createTranslation(position).multiply(Matrix4.createZRotation(zRotation));
        return new ModelMatrices(model);
    }
}
