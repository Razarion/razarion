package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 06.08.2016.
 */
public class ShapeTransformTRS implements ShapeTransform {
    private double xTranslate;
    private double yTranslate;
    private double zTranslate;
    private double xRotate;
    private double yRotate;
    private double zRotate;
    private double xScale;
    private double yScale;
    private double zScale;


    public ShapeTransformTRS setXTranslate(double xTranslate) {
        this.xTranslate = xTranslate;
        return this;
    }

    public ShapeTransformTRS setYTranslate(double yTranslate) {
        this.yTranslate = yTranslate;
        return this;
    }

    public ShapeTransformTRS setZTranslate(double zTranslate) {
        this.zTranslate = zTranslate;
        return this;
    }

    public ShapeTransformTRS setXRotate(double xRotate) {
        this.xRotate = xRotate;
        return this;
    }

    public ShapeTransformTRS setYRotate(double yRotate) {
        this.yRotate = yRotate;
        return this;
    }

    public ShapeTransformTRS setZRotate(double zRotate) {
        this.zRotate = zRotate;
        return this;
    }

    public ShapeTransformTRS setXScale(double xScale) {
        this.xScale = xScale;
        return this;
    }

    public ShapeTransformTRS setYScale(double yScale) {
        this.yScale = yScale;
        return this;
    }

    public ShapeTransformTRS setZScale(double zScale) {
        this.zScale = zScale;
        return this;
    }

    public double getxTranslate() {
        return xTranslate;
    }

    public double getyTranslate() {
        return yTranslate;
    }

    public double getzTranslate() {
        return zTranslate;
    }

    public double getxRotate() {
        return xRotate;
    }

    public double getyRotate() {
        return yRotate;
    }

    public double getzRotate() {
        return zRotate;
    }

    public double getxScale() {
        return xScale;
    }

    public double getyScale() {
        return yScale;
    }

    public double getzScale() {
        return zScale;
    }

    public ShapeTransformTRS copy() {
        ShapeTransformTRS shapeTransform = new ShapeTransformTRS();
        shapeTransform.xTranslate = xTranslate;
        shapeTransform.yTranslate = yTranslate;
        shapeTransform.zTranslate = zTranslate;
        shapeTransform.xRotate = xRotate;
        shapeTransform.yRotate = yRotate;
        shapeTransform.zRotate = zRotate;
        shapeTransform.xScale = xScale;
        shapeTransform.yScale = yScale;
        shapeTransform.zScale = zScale;

        return shapeTransform;
    }

    @Override
    public Matrix4 setupMatrix() {
        Matrix4 matrix = Matrix4.createIdentity();
        matrix = matrix.multiply(Matrix4.createTranslation(xTranslate, yTranslate, zTranslate));
        matrix = matrix.multiply(Matrix4.createZRotation(zRotate));
        matrix = matrix.multiply(Matrix4.createYRotation(yRotate));
        matrix = matrix.multiply(Matrix4.createXRotation(xRotate));
        matrix = matrix.multiply(Matrix4.createScale(xScale, yScale, zScale));
        return matrix;
    }

    @Override
    public Matrix4 setupNormMatrix() {
        Matrix4 matrix = Matrix4.createIdentity();
        matrix = matrix.multiply(Matrix4.createXRotation(xRotate));
        matrix = matrix.multiply(Matrix4.createXRotation(yRotate));
        matrix = matrix.multiply(Matrix4.createXRotation(zRotate));
        return matrix;
    }

    @Override
    public String toString() {
        return "ShapeTransformTRS{" +
                "xTranslate=" + xTranslate +
                ", yTranslate=" + yTranslate +
                ", zTranslate=" + zTranslate +
                ", xRotate=" + xRotate +
                ", yRotate=" + yRotate +
                ", zRotate=" + zRotate +
                ", xScale=" + xScale +
                ", yScale=" + yScale +
                ", zScale=" + zScale +
                '}';
    }
}
