package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 05.08.2016.
 */
public class ShapeTransform {
    private Matrix4 matrix;
    private double translateX;
    private double translateY;
    private double translateZ;
    private double rotateX;
    private double rotateY;
    private double rotateZ;
    private double scaleX;
    private double scaleY;
    private double scaleZ;

    // -------------------------

    public Matrix4 setupMatrix() {
        if (matrix != null) {
            return matrix;
        } else {
            Matrix4 matrix = Matrix4.createIdentity();
            matrix = matrix.multiply(Matrix4.createTranslation(translateX, translateY, translateZ));
            matrix = matrix.multiply(Matrix4.createZRotation(rotateZ));
            matrix = matrix.multiply(Matrix4.createYRotation(rotateY));
            matrix = matrix.multiply(Matrix4.createXRotation(rotateX));
            matrix = matrix.multiply(Matrix4.createScale(scaleX, scaleY, scaleZ));
            return matrix;
        }
    }

    public ShapeTransform copyTRS() {
        ShapeTransform shapeTransform = new ShapeTransform();
        shapeTransform.translateX = translateX;
        shapeTransform.translateY = translateY;
        shapeTransform.translateZ = translateZ;
        shapeTransform.rotateX = rotateX;
        shapeTransform.rotateY = rotateY;
        shapeTransform.rotateZ = rotateZ;
        shapeTransform.scaleX = scaleX;
        shapeTransform.scaleY = scaleY;
        shapeTransform.scaleZ = scaleZ;

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

    public ShapeTransform setTranslateX(double translateX) {
        this.translateX = translateX;
        return this;
    }

    public ShapeTransform setTranslateY(double translateY) {
        this.translateY = translateY;
        return this;
    }

    public ShapeTransform setTranslateZ(double translateZ) {
        this.translateZ = translateZ;
        return this;
    }

    public ShapeTransform setRotateX(double rotateX) {
        this.rotateX = rotateX;
        return this;
    }

    public ShapeTransform setRotateY(double rotateY) {
        this.rotateY = rotateY;
        return this;
    }

    public ShapeTransform setRotateZ(double rotateZ) {
        this.rotateZ = rotateZ;
        return this;
    }

    public ShapeTransform setScaleX(double scaleX) {
        this.scaleX = scaleX;
        return this;
    }

    public ShapeTransform setScaleY(double scaleY) {
        this.scaleY = scaleY;
        return this;
    }

    public ShapeTransform setScaleZ(double scaleZ) {
        this.scaleZ = scaleZ;
        return this;
    }

    public double getTranslateX() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public double getTranslateZ() {
        return translateZ;
    }

    public double getRotateX() {
        return rotateX;
    }

    public double getRotateY() {
        return rotateY;
    }

    public double getRotateZ() {
        return rotateZ;
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public double getScaleZ() {
        return scaleZ;
    }

    @Override
    public String toString() {
        return "ShapeTransformTRS{" +
                "translateX=" + translateX +
                ", translateY=" + translateY +
                ", translateZ=" + translateZ +
                ", rotateX=" + rotateX +
                ", rotateY=" + rotateY +
                ", rotateZ=" + rotateZ +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                ", scaleZ=" + scaleZ +
                '}';
    }
}