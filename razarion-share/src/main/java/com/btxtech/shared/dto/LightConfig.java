package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 28.05.2016.
 */
public class LightConfig {
    private double rotationX;
    private double rotationY;
    private Color diffuse;
    private Color ambient;
    private double specularIntensity;
    private double specularHardness;

    public double getRotationX() {
        return rotationX;
    }

    public LightConfig setRotationX(double rotationX) {
        this.rotationX = rotationX;
        return this;
    }

    public double getRotationY() {
        return rotationY;
    }

    public LightConfig setRotationY(double rotationY) {
        this.rotationY = rotationY;
        return this;
    }

    public Color getDiffuse() {
        return diffuse;
    }

    public LightConfig setDiffuse(Color diffuse) {
        this.diffuse = diffuse;
        return this;
    }

    public Color getAmbient() {
        return ambient;
    }

    public LightConfig setAmbient(Color ambient) {
        this.ambient = ambient;
        return this;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public LightConfig setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
        return this;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public LightConfig setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
        return this;
    }

    public Vertex setupDirection() {
        return Matrix4.createXRotation(rotationX).multiply(Matrix4.createYRotation(rotationY)).multiply(new Vertex(0, 0, -1), 1.0);
    }
}
