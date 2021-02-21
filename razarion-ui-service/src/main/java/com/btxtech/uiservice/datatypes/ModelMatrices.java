package com.btxtech.uiservice.datatypes;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.shared.utils.MathHelper;

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
    private NativeVertexDto interpolatableVelocity;
    private int particleXColorRampOffsetIndex;
    private double radius;
    private Color color;
    private Color bgColor;
    private Double turretAngle;

    public ModelMatrices(NativeMatrix matrix) {
        this(matrix, null, 0, null);
    }

    public ModelMatrices(NativeMatrix matrix, Color color) {
        this(matrix, null, 0, color);
    }

    public ModelMatrices(NativeMatrix matrix, double progress, Color color) {
        this(matrix, null, progress, color);
    }

    public ModelMatrices(NativeMatrix matrix, NativeVertexDto interpolatableVelocity, Color color) {
        this(matrix, interpolatableVelocity, 0, color);
    }

    public ModelMatrices(NativeMatrix matrix, NativeVertexDto interpolatableVelocity, double progress, Color color) {
        this.matrix = matrix;
        this.interpolatableVelocity = interpolatableVelocity;
        this.progress = progress;
        this.color = color;
    }

    public ModelMatrices(ModelMatrices modelMatrices, double turretAngle) {
        this.matrix = modelMatrices.matrix;
        this.norm = modelMatrices.norm;
        this.progress = modelMatrices.progress;
        this.interpolatableVelocity = modelMatrices.interpolatableVelocity;
        this.particleXColorRampOffsetIndex = modelMatrices.particleXColorRampOffsetIndex;
        this.radius = modelMatrices.radius;
        this.color = modelMatrices.color;
        this.bgColor = modelMatrices.bgColor;
        this.turretAngle = turretAngle;
    }

    public ModelMatrices(Matrix4 model, NativeMatrixFactory nativeMatrixFactory) {
        this(model, 0, nativeMatrixFactory);
    }

    public ModelMatrices(Matrix4 model, double progress, NativeMatrixFactory nativeMatrixFactory) {
        this(model, null, progress, nativeMatrixFactory);
    }

    public ModelMatrices(Matrix4 model, NativeVertexDto interpolatableVelocity, NativeMatrixFactory nativeMatrixFactory) {
        this(model, interpolatableVelocity, 0, nativeMatrixFactory);
    }

    public ModelMatrices(Matrix4 model, NativeVertexDto interpolatableVelocity, double progress, NativeMatrixFactory nativeMatrixFactory) {
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

    public void updatePositionScale(Vertex position, double scale, NativeVertexDto interpolatableVelocity) {
        matrix = matrix.getNativeMatrixFactory().createTranslation(position.getX(), position.getY(), position.getZ());
        matrix = matrix.multiply(matrix.getNativeMatrixFactory().createScale(scale, scale, scale));
        norm = null;
        this.interpolatableVelocity = interpolatableVelocity;
    }


    public void updatePositionScale(Vertex position, double scale, double progress) {
        updatePositionScale(position, scale, null);
        this.progress = progress;
    }

    public void updatePositionScaleX(Vertex position, double scale, NativeVertexDto interpolatableVelocity) {
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

    public Double getTurretAngle() {
        return turretAngle;
    }

    public ModelMatrices interpolateVelocity(double factor) {
        if (interpolatableVelocity != null && factor != 0.0) {
            factor = MathHelper.clamp(factor, 0.0, PlanetService.TICK_FACTOR);
            ModelMatrices modelMatrices = new ModelMatrices(matrix.getNativeMatrixFactory().createTranslation(interpolatableVelocity.x * factor, interpolatableVelocity.y * factor, interpolatableVelocity.z * factor).multiply(matrix));
            modelMatrices.progress = progress;
            modelMatrices.interpolatableVelocity = interpolatableVelocity;
            modelMatrices.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
            modelMatrices.radius = radius;
            modelMatrices.color = color;
            modelMatrices.bgColor = bgColor;
            modelMatrices.turretAngle = turretAngle;
            return modelMatrices;
        } else {
            return this;
        }
    }

    public ModelMatrices multiplyStaticShapeTransform(NativeMatrix nativeMatrix) {
        ModelMatrices modelMatrices = new ModelMatrices(matrix.multiply(nativeMatrix));

        modelMatrices.progress = progress;
        modelMatrices.interpolatableVelocity = interpolatableVelocity;
        modelMatrices.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
        modelMatrices.radius = radius;
        modelMatrices.color = color;
        modelMatrices.bgColor = bgColor;
        modelMatrices.turretAngle = turretAngle;
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
        modelMatrices.turretAngle = turretAngle;
        return modelMatrices;
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

    public ModelMatrices calculateFromTurretAngle() {
        if (turretAngle == null) {
            return this;
        }
        NativeMatrix newMatrix = matrix.multiply(matrix.getNativeMatrixFactory().createZRotation(turretAngle));
        ModelMatrices modelMatrices = new ModelMatrices(newMatrix);
        modelMatrices.progress = progress;
        modelMatrices.interpolatableVelocity = interpolatableVelocity;
        modelMatrices.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
        modelMatrices.radius = radius;
        modelMatrices.color = color;
        modelMatrices.bgColor = bgColor;
        return modelMatrices;
    }

    public static ModelMatrices create4Marker(Vertex position, double scale, NativeVertexDto interpolatableVelocity, Color color, double radius, NativeMatrixFactory nativeMatrixFactory) {
        ModelMatrices modelMatrices = new ModelMatrices(matrixFromPositionAndScale(position, scale, nativeMatrixFactory));
        modelMatrices.interpolatableVelocity = interpolatableVelocity;
        modelMatrices.color = color;
        modelMatrices.radius = radius;
        return modelMatrices;
    }

    public static ModelMatrices create4Status(Vertex position, double scaleX, NativeVertexDto interpolatableVelocity, Color color, Color bgColor, double progress, NativeMatrixFactory nativeMatrixFactory) {
        ModelMatrices modelMatrices = new ModelMatrices(createPositionScaleX(position, scaleX, nativeMatrixFactory));
        modelMatrices.interpolatableVelocity = interpolatableVelocity;
        modelMatrices.color = color;
        modelMatrices.bgColor = bgColor;
        modelMatrices.progress = progress;
        return modelMatrices;
    }

    public static ModelMatrices create4Particle(Vertex position, double scale, double progress, int particleXColorRampOffsetIndex, NativeMatrixFactory nativeMatrixFactory) {
        ModelMatrices modelMatrices = new ModelMatrices(matrixFromPositionAndScale(position, scale, nativeMatrixFactory), progress, null);
        modelMatrices.progress = progress;
        modelMatrices.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
        return modelMatrices;
    }

    public static ModelMatrices create4Wreckage(Vertex position, double zRotation, NativeMatrixFactory nativeMatrixFactory) {
        return createFromPositionAndZRotation(position.getX(), position.getY(), position.getZ(), zRotation, nativeMatrixFactory);
    }

    public static ModelMatrices createFromPosition(Vertex position, NativeMatrixFactory nativeMatrixFactory) {
        return createFromPosition(position.getX(), position.getY(), position.getZ(), null, nativeMatrixFactory);
    }

    public static ModelMatrices createFromPosition(double x, double y, double z, Color color, NativeMatrixFactory nativeMatrixFactory) {
        return new ModelMatrices(nativeMatrixFactory.createTranslation(x, y, z), color);
    }

    public static ModelMatrices createFromPositionAndZRotation(double x, double y, double z, double zRotation, NativeMatrixFactory nativeMatrixFactory) {
        NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(x, y, z);
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createZRotation(zRotation));
        return new ModelMatrices(newMatrix);
    }

    public static ModelMatrices createFromPositionAndZRotation(NativeVertexDto position, NativeVertexDto direction, NativeMatrixFactory nativeMatrixFactory) {
        direction = NativeUtil.normalize(direction);
        double yRotation = -Math.asin(direction.z);
        double zRotation = NativeUtil.angleXY(direction);
        NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(position.x, position.y, position.z);
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createZRotation(zRotation));
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createYRotation(yRotation));
        return new ModelMatrices(newMatrix);
    }

    public static ModelMatrices create4Editor(double x, double y, double z, Vertex scale, NativeMatrixFactory nativeMatrixFactory) {
        NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(x, y, z);
        newMatrix = newMatrix.multiply(nativeMatrixFactory.createScale(scale.getX(), scale.getY(), scale.getZ()));
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
}
