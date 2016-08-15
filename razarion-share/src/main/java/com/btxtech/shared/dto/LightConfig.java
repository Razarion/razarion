package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 28.05.2016.
 */
@Portable
@Bindable
public class LightConfig {
    private double xRotation;
    private double yRotation;
    private Color diffuse;
    private Color ambient;
    private double specularIntensity;
    private double specularHardness;

    public double getXRotation() {
        return xRotation;
    }

    public LightConfig setXRotation(double xRotation) {
        this.xRotation = xRotation;
        return this;
    }

    public double getYRotation() {
        return yRotation;
    }

    public LightConfig setYRotation(double yRotation) {
        this.yRotation = yRotation;
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

    public Vertex getDirection() {
        return Matrix4.createXRotation(xRotation).multiply(Matrix4.createYRotation(yRotation)).multiply(new Vertex(0, 0, -1), 1.0);
    }
}
