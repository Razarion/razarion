package com.btxtech.uiservice.datatypes;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.uiservice.nativejs.NativeMatrix;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

/**
 * Created by Beat
 * 15.05.2016.
 */
// May only be used in the GUI part (renderer)
// Move to razaion-ui-services
public class ModelMatrices {
    private NativeMatrix matrix;
    private NativeMatrix norm;
    private double progress;
    private DecimalPosition interpolatableVelocity;
    private int particleXColorRampOffsetIndex;
    private double radius;
    private Color color;
    private Color bgColor;

    public ModelMatrices(NativeMatrix matrix) {
        this(matrix, null, 0);
    }

    public ModelMatrices(NativeMatrix matrix, double progress) {
        this(matrix, null, progress);
    }

    public ModelMatrices(NativeMatrix matrix, DecimalPosition interpolatableVelocity) {
        this(matrix, interpolatableVelocity, 0);
    }

    public ModelMatrices(NativeMatrix matrix, DecimalPosition interpolatableVelocity, double progress) {
        this.matrix = matrix;
        this.interpolatableVelocity = interpolatableVelocity;
        this.progress = progress;
    }

    public ModelMatrices(Matrix4 model, NativeMatrixFactory nativeMatrixFactory) {
        this(model, 0, nativeMatrixFactory);
    }

    public ModelMatrices(Matrix4 model, double progress, NativeMatrixFactory nativeMatrixFactory) {
        this(model, null, progress, nativeMatrixFactory);
    }

    public ModelMatrices(Matrix4 model, DecimalPosition interpolatableVelocity, NativeMatrixFactory nativeMatrixFactory) {
        this(model, interpolatableVelocity, 0, nativeMatrixFactory);
    }

    public ModelMatrices(Matrix4 model, DecimalPosition interpolatableVelocity, double progress, NativeMatrixFactory nativeMatrixFactory) {
        matrix = nativeMatrixFactory.createFromColumnMajorArray(model.toWebGlArray());
        this.interpolatableVelocity = interpolatableVelocity;
        this.progress = progress;
    }

    public NativeMatrix getModel() {
        return matrix;
    }

    public void updateProgress(double progress) {
        this.progress = progress;
    }

    public void updatePositionScale(Vertex position, double scale, DecimalPosition interpolatableVelocity) {
        matrix = matrix.getNativeMatrixFactory().createTranslation(position.getX(), position.getY(), position.getZ());
        matrix = matrix.multiply(matrix.getNativeMatrixFactory().createScale(scale, scale, scale));
        norm = null;
        this.interpolatableVelocity = interpolatableVelocity;
    }


    public void updatePositionScale(Vertex position, double scale, double progress) {
        updatePositionScale(position, scale, null);
        this.progress = progress;
    }

    public void updatePositionScaleX(Vertex position, double scale, DecimalPosition interpolatableVelocity) {
        matrix = matrix.getNativeMatrixFactory().createTranslation(position.getX(), position.getY(), position.getZ());
        matrix = matrix.multiply(matrix.getNativeMatrixFactory().createScale(scale, 0, 0));
        norm = null;
        this.interpolatableVelocity = interpolatableVelocity;
    }

    public double getProgress() {
        return progress;
    }

    public double getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public int getParticleXColorRampOffsetIndex() {
        return particleXColorRampOffsetIndex;
    }

    public ModelMatrices interpolateVelocity(double factor) {
        if (interpolatableVelocity != null && factor != 0.0) {
            DecimalPosition interpolation = interpolatableVelocity.multiply(factor);
            ModelMatrices modelMatrices = new ModelMatrices(matrix.getNativeMatrixFactory().createTranslation(interpolation.getX(), interpolation.getY(), 0).multiply(matrix));
            modelMatrices.progress = progress;
            modelMatrices.interpolatableVelocity = interpolatableVelocity;
            modelMatrices.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
            modelMatrices.radius = radius;
            modelMatrices.color = color;
            modelMatrices.bgColor = bgColor;
            return modelMatrices;
        } else {
            return this;
        }
    }

    public ModelMatrices multiplyStaticShapeTransform(NativeMatrix staticShapeTransform) {
        ModelMatrices modelMatrices = new ModelMatrices(matrix.multiply(staticShapeTransform));

        modelMatrices.progress = progress;
        modelMatrices.interpolatableVelocity = interpolatableVelocity;
        modelMatrices.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
        modelMatrices.radius = radius;
        modelMatrices.color = color;
        modelMatrices.bgColor = bgColor;
        return modelMatrices;
    }

    public ModelMatrices multiplyShapeTransform(ShapeTransform shapeTransform) {
        NativeMatrix newMatrix = matrix.multiply(matrix.getNativeMatrixFactory().createTranslation(shapeTransform.getTranslateX(), shapeTransform.getTranslateY(), shapeTransform.getTranslateZ()));
        newMatrix = newMatrix.multiply(matrix.getNativeMatrixFactory().createZRotation(shapeTransform.getRotateZ()));
        newMatrix = newMatrix.multiply(matrix.getNativeMatrixFactory().createYRotation(shapeTransform.getRotateY()));
        newMatrix = newMatrix.multiply(matrix.getNativeMatrixFactory().createXRotation(shapeTransform.getRotateX()));
        newMatrix = newMatrix.multiply(matrix.getNativeMatrixFactory().createScale(shapeTransform.getScaleX(), shapeTransform.getScaleY(), shapeTransform.getScaleZ()));

        ModelMatrices modelMatrices = new ModelMatrices(newMatrix);
        modelMatrices.progress = progress;
        modelMatrices.interpolatableVelocity = interpolatableVelocity;
        modelMatrices.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
        modelMatrices.radius = radius;
        modelMatrices.color = color;
        modelMatrices.bgColor = bgColor;
        return modelMatrices;
    }

    public static ModelMatrices create4Marker(Vertex position, double scale, DecimalPosition interpolatableVelocity, Color color, double radius, NativeMatrixFactory nativeMatrixFactory) {
        ModelMatrices modelMatrices = new ModelMatrices(matrixFromPositionAndScale(position, scale, nativeMatrixFactory));
        modelMatrices.interpolatableVelocity = interpolatableVelocity;
        modelMatrices.color = color;
        modelMatrices.radius = radius;
        return modelMatrices;
    }

    public static ModelMatrices create4Status(Vertex position, double scaleX, DecimalPosition interpolatableVelocity, Color color, Color bgColor, double progress, NativeMatrixFactory nativeMatrixFactory) {
        ModelMatrices modelMatrices = new ModelMatrices(createPositionScaleX(position, scaleX, nativeMatrixFactory));
        modelMatrices.interpolatableVelocity = interpolatableVelocity;
        modelMatrices.color = color;
        modelMatrices.bgColor = bgColor;
        modelMatrices.progress = progress;
        return modelMatrices;
    }

    public static ModelMatrices create4Particle(Vertex position, double scale, double progress, int particleXColorRampOffsetIndex, NativeMatrixFactory nativeMatrixFactory) {
        ModelMatrices modelMatrices = new ModelMatrices(matrixFromPositionAndScale(position, scale, nativeMatrixFactory), progress);
        modelMatrices.progress = progress;
        modelMatrices.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
        return modelMatrices;
    }

    public static ModelMatrices create4Wreckage(Vertex position, double zRotation, NativeMatrixFactory nativeMatrixFactory) {
        return createFromPositionAndZRotation(position.getX(), position.getY(), position.getZ(), zRotation, nativeMatrixFactory);
    }

    public static ModelMatrices create4TerrainObject(double x, double y, double z, double scale, double zRotation, NativeMatrixFactory nativeMatrixFactory) {
        NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(x, y, z);
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createScale(scale, scale, scale));
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createZRotation(zRotation));
        return new ModelMatrices(newMatrix);
    }

    public static ModelMatrices createFromPosition(Vertex position, NativeMatrixFactory nativeMatrixFactory) {
        return createFromPosition(position.getX(), position.getY(), position.getZ(), nativeMatrixFactory);
    }

    public static ModelMatrices createFromPosition(double x, double y, double z, NativeMatrixFactory nativeMatrixFactory) {
        return new ModelMatrices(nativeMatrixFactory.createTranslation(x, y, z));
    }

    public static ModelMatrices createFromPositionAndZRotation(double x, double y, double z, double zRotation, NativeMatrixFactory nativeMatrixFactory) {
        NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(x, y, z);
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createZRotation(zRotation));
        return new ModelMatrices(newMatrix);
    }

    public static ModelMatrices createFromPositionAndZRotation(Vertex position, Vertex direction, NativeMatrixFactory nativeMatrixFactory) {
        direction = direction.normalize(1.0);
        double yRotation = -Math.asin(direction.getZ());
        double zRotation = direction.toXY().angle();
        NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(position.getX(), position.getY(), position.getZ());
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createZRotation(zRotation));
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createYRotation(yRotation));
        return new ModelMatrices(newMatrix);
    }

    public static ModelMatrices create4Editor(double x, double y, double z, double scale, NativeMatrixFactory nativeMatrixFactory) {
        NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(x, y, z);
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createScale(scale, scale, scale));
        return new ModelMatrices(newMatrix);
    }

    public static NativeMatrix createPositionScaleX(Vertex position, double scale, NativeMatrixFactory nativeMatrixFactory) {
        NativeMatrix matrix = nativeMatrixFactory.createTranslation(position.getX(), position.getY(), position.getZ());
        matrix = matrix.multiply(nativeMatrixFactory.createScale(scale, 1, 1));
        return matrix;
    }

    private static NativeMatrix matrixFromPositionAndScale(Vertex position, double scale, NativeMatrixFactory nativeMatrixFactory) {
        NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(position.getX(), position.getY(), position.getZ());
        return newMatrix.multiply(nativeMatrixFactory.createScale(scale, scale, scale));
    }

    public NativeMatrix getNorm() {
        if (norm == null) {
            NativeMatrix inverse = matrix.invert();
            if (inverse != null) {
                norm = inverse.transpose();
            } else {
                norm = matrix;
            }
        }
        return norm;
    }
}