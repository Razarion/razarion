package com.btxtech.client.math3d;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 24.04.2015.
 */
@Singleton
public class ModelTransformation {
    private double scaleX;
    private double scaleY;
    private double scaleZ;
    private double translateX;
    private double translateY;
    private double translateZ;

    public ModelTransformation() {
        setGame();
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

    public Matrix4 createMatrix() {
        Matrix4 matrix4 = Matrix4.createScale(scaleX, scaleY, scaleZ);
        return matrix4.multiply(Matrix4.createTranslation(translateX, translateY, translateZ));
    }

    public void setGame() {
        scaleX = 0.1;
        scaleY = 0.1;
        scaleZ = 0.1;
        translateX = 0;
        translateY = 70;
        translateZ = -100;
    }
}
