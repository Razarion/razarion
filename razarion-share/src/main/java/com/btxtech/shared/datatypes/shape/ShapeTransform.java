package com.btxtech.shared.datatypes.shape;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 05.08.2016.
 */
@JsType
public class ShapeTransform {
    private double translateX;
    private double translateY;
    private double translateZ;
    private double rotateX;
    private double rotateY;
    private double rotateZ;
    private double rotateW;
    private double scaleX;
    private double scaleY;
    private double scaleZ;

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
    }

    public double getTranslateZ() {
        return translateZ;
    }

    public void setTranslateZ(double translateZ) {
        this.translateZ = translateZ;
    }

    public double getRotateX() {
        return rotateX;
    }

    public void setRotateX(double rotateX) {
        this.rotateX = rotateX;
    }

    public double getRotateY() {
        return rotateY;
    }

    public void setRotateY(double rotateY) {
        this.rotateY = rotateY;
    }

    public double getRotateZ() {
        return rotateZ;
    }

    public void setRotateZ(double rotateZ) {
        this.rotateZ = rotateZ;
    }

    public double getRotateW() {
        return rotateW;
    }

    public void setRotateW(double rotateW) {
        this.rotateW = rotateW;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public double getScaleZ() {
        return scaleZ;
    }

    public void setScaleZ(double scaleZ) {
        this.scaleZ = scaleZ;
    }

    public ShapeTransform copyTRS() {
        ShapeTransform shapeTransform = new ShapeTransform();
        shapeTransform.translateX = translateX;
        shapeTransform.translateY = translateY;
        shapeTransform.translateZ = translateZ;
        shapeTransform.rotateX = rotateX;
        shapeTransform.rotateY = rotateY;
        shapeTransform.rotateZ = rotateZ;
        shapeTransform.rotateW = rotateW;
        shapeTransform.scaleX = scaleX;
        shapeTransform.scaleY = scaleY;
        shapeTransform.scaleZ = scaleZ;

        return shapeTransform;
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
                ", rotateW=" + rotateW +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                ", scaleZ=" + scaleZ +
                '}';
    }

    public ShapeTransform translateX(double translateX) {
        setTranslateX(translateX);
        return this;
    }

    public ShapeTransform translateY(double translateY) {
        setTranslateY(translateY);
        return this;
    }

    public ShapeTransform translateZ(double translateZ) {
        setTranslateZ(translateZ);
        return this;
    }

    public ShapeTransform rotateX(double rotateX) {
        setRotateX(rotateX);
        return this;
    }

    public ShapeTransform rotateY(double rotateY) {
        setRotateY(rotateY);
        return this;
    }

    public ShapeTransform rotateZ(double rotateZ) {
        setRotateZ(rotateZ);
        return this;
    }

    public ShapeTransform rotateW(double rotateW) {
        setRotateW(rotateW);
        return this;
    }

    public ShapeTransform scaleX(double scaleX) {
        setScaleX(scaleX);
        return this;
    }

    public ShapeTransform scaleY(double scaleY) {
        setScaleY(scaleY);
        return this;
    }

    public ShapeTransform scaleZ(double scaleZ) {
        setScaleZ(scaleZ);
        return this;
    }
}