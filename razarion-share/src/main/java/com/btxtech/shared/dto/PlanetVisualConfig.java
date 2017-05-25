package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 14.05.2017.
 */
public class PlanetVisualConfig {
    private double shadowRotationX;
    private double shadowRotationY;
    private double shadowAlpha;
    private double shape3DLightRotateX;
    private double shape3DLightRotateY;

    public double getShadowRotationX() {
        return shadowRotationX;
    }

    public PlanetVisualConfig setShadowRotationX(double shadowRotationX) {
        this.shadowRotationX = shadowRotationX;
        return this;
    }

    public double getShadowRotationY() {
        return shadowRotationY;
    }

    public PlanetVisualConfig setShadowRotationY(double shadowRotationY) {
        this.shadowRotationY = shadowRotationY;
        return this;
    }

    public double getShadowAlpha() {
        return shadowAlpha;
    }

    public PlanetVisualConfig setShadowAlpha(double shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
        return this;
    }

    public double getShape3DLightRotateX() {
        return shape3DLightRotateX;
    }

    public PlanetVisualConfig setShape3DLightRotateX(double shape3DLightRotateX) {
        this.shape3DLightRotateX = shape3DLightRotateX;
        return this;
    }

    public double getShape3DLightRotateY() {
        return shape3DLightRotateY;
    }

    public PlanetVisualConfig setShape3DLightRotateY(double shape3DLightRotateY) {
        this.shape3DLightRotateY = shape3DLightRotateY;
        return this;
    }


}
