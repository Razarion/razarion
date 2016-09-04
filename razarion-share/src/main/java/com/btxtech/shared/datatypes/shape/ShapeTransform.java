package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 05.08.2016.
 */
public class ShapeTransform {
    private Matrix4 matrix;
    private double xTranslate;
    private double yTranslate;
    private double zTranslate;
    private double xRotate;
    private double yRotate;
    private double zRotate;
    private double xScale;
    private double yScale;
    private double zScale;

    // -------------------------

    public Matrix4 setupMatrix() {
        if (matrix != null) {
            return matrix;
        } else {
            Matrix4 matrix = Matrix4.createIdentity();
            matrix = matrix.multiply(Matrix4.createTranslation(xTranslate, yTranslate, zTranslate));
            matrix = matrix.multiply(Matrix4.createZRotation(zRotate));
            matrix = matrix.multiply(Matrix4.createYRotation(yRotate));
            matrix = matrix.multiply(Matrix4.createXRotation(xRotate));
            matrix = matrix.multiply(Matrix4.createScale(xScale, yScale, zScale));
            return matrix;
        }
    }

    public ShapeTransform copyTRS() {
        ShapeTransform shapeTransform = new ShapeTransform();
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


    // -----------------------------------
    public ShapeTransform setMatrix(Matrix4 matrix) {
        this.matrix = matrix;
        return this;
    }

    public Matrix4 getMatrix() {
        return matrix;
    }

    public ShapeTransform setXTranslate(double xTranslate) {
        this.xTranslate = xTranslate;
        return this;
    }

    public ShapeTransform setYTranslate(double yTranslate) {
        this.yTranslate = yTranslate;
        return this;
    }

    public ShapeTransform setZTranslate(double zTranslate) {
        this.zTranslate = zTranslate;
        return this;
    }

    public ShapeTransform setXRotate(double xRotate) {
        this.xRotate = xRotate;
        return this;
    }

    public ShapeTransform setYRotate(double yRotate) {
        this.yRotate = yRotate;
        return this;
    }

    public ShapeTransform setZRotate(double zRotate) {
        this.zRotate = zRotate;
        return this;
    }

    public ShapeTransform setXScale(double xScale) {
        this.xScale = xScale;
        return this;
    }

    public ShapeTransform setYScale(double yScale) {
        this.yScale = yScale;
        return this;
    }

    public ShapeTransform setZScale(double zScale) {
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