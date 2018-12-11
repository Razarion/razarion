package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 14.05.2017.
 */
public class PlanetVisualConfig {
    private double shadowRotationX;
    private double shadowRotationY;
    private double shadowAlpha;
    private Vertex lightDirection;
    private Color ambient;
    private Color diffuse;

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

    public Vertex getLightDirection() {
        return lightDirection;
    }

    public PlanetVisualConfig setLightDirection(Vertex lightDirection) {
        if (!MathHelper.compareWithPrecision(lightDirection.magnitude(), 1.0, 0.000001)) {
            throw new IllegalArgumentException("Light direction mus be a unit vector. lightDirection: " + lightDirection + " magnitude: " + lightDirection.magnitude());
        }
        this.lightDirection = lightDirection;
        return this;
    }

    public Color getAmbient() {
        return ambient;
    }

    public PlanetVisualConfig setAmbient(Color ambient) {
        this.ambient = ambient;
        return this;
    }

    public Color getDiffuse() {
        return diffuse;
    }

    public PlanetVisualConfig setDiffuse(Color diffuse) {
        this.diffuse = diffuse;
        return this;
    }
}
