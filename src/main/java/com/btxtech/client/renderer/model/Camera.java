package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Matrix4;

import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.04.2015.
 */
@Singleton
public class Camera {
    private double translateX;
    private double translateY;
    private double translateZ;
    private double rotateX;
    private double rotateZ;
    private double saveTranslateX;
    private double saveTranslateY;
    private double saveTranslateZ;
    private double saveRotateX;
    private double saveRotateZ;

    private Logger logger = Logger.getLogger(Camera.class.getName());

    public Camera() {
        setGame();
        // setTop();
        // setCustom();
    }

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
        fireChanged();
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
        fireChanged();
    }

    public double getTranslateZ() {
        return translateZ;
    }

    public void setTranslateZ(double translateZ) {
        this.translateZ = translateZ;
        fireChanged();
    }

    public double getRotateX() {
        return rotateX;
    }

    public void setRotateX(double rotateX) {
        this.rotateX = rotateX;
        fireChanged();
    }

    public void setRotateZ(double rotateZ) {
        this.rotateZ = rotateZ;
        fireChanged();
    }

    public double getRotateZ() {
        return rotateZ;
    }

    public Matrix4 createMatrix() {
        Matrix4 matrix4 = Matrix4.createXRotation(rotateX);
        matrix4 = matrix4.multiply(Matrix4.createZRotation(rotateZ));
        return matrix4.multiply(Matrix4.createTranslation(-translateX, -translateY, -translateZ));
    }

    public void setTop() {
        translateX = 0;
        translateY = 0;
        translateZ = 1000;
        rotateX = Math.toRadians(0);
        rotateZ = Math.toRadians(0);
        fireChanged();
    }

    public void setFront() {
        translateX = 50;
        translateY = -20;
        translateZ = -10;
        rotateX = Math.toRadians(90);
        rotateZ = Math.toRadians(0);
        fireChanged();
    }

    public void setGame() {
        translateX = 300;
        translateY = -60;
        translateZ = 500;
        rotateX = Math.toRadians(40);
        rotateZ = Math.toRadians(0);
        fireChanged();
    }

    public void setCustom() {
        translateX = 6.5;
        translateY = -51;
        translateZ = 6;
        rotateX = Math.toRadians(87.00000000000001);
        rotateZ = Math.toRadians(0);
    }

    public void restore() {
        translateX = saveTranslateX;
        translateY = saveTranslateY;
        translateZ = saveTranslateZ;
        rotateX = saveRotateX;
        rotateZ = saveRotateZ;
    }

    public void save() {
        saveTranslateX = translateX;
        saveTranslateY = translateY;
        saveTranslateZ = translateZ;
        saveRotateX = rotateX;
        saveRotateZ = rotateZ;
    }

    private void fireChanged() {
        // testPrint();
    }

    public void testPrint() {
        logger.severe("translateX = " + translateX + "; translateY = " + translateY + "; translateZ = " + translateZ + "; rotateX = Math.toRadians(" + Math.toDegrees(rotateX) + "); rotateZ = Math.toRadians(" + Math.toDegrees(rotateZ) + ");");
    }
}
