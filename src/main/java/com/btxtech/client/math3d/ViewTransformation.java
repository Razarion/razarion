package com.btxtech.client.math3d;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 25.04.2015.
 */
@Singleton
public class ViewTransformation {
    private double translateX;
    private double translateY;
    private double translateZ;
    private double rotateX;
    private ViewTransformationObserver viewTransformationObserver;

    public interface ViewTransformationObserver{
        void onChanged();
    }

    public ViewTransformation() {
        //setGame();
        setTop();
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

    public Matrix4 createMatrix() {
        Matrix4 matrix4 = Matrix4.createXRotation(rotateX);
        return matrix4.multiply(Matrix4.createTranslation(translateX, translateY, translateZ));
    }

    public void setTop() {
        translateX = -40;
        translateY = -50;
        translateZ = -30;
        rotateX = Math.toRadians(0);
        fireChanged();
    }

    public void setFront() {
        translateX = -50;
        translateY = 20;
        translateZ = 10;
        rotateX = Math.toRadians(90);
        fireChanged();
    }

    public void setGame() {
        translateX = -50;
        translateY = 50;
        translateZ = -70;
        rotateX = Math.toRadians(40);
        fireChanged();
    }

    public void setViewTransformationObserver(ViewTransformationObserver viewTransformationObserver) {
        this.viewTransformationObserver = viewTransformationObserver;
    }

    private void fireChanged() {
        if(viewTransformationObserver != null) {
            viewTransformationObserver.onChanged();
        }
    }
}
