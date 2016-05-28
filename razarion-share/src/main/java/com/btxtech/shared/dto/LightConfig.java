package com.btxtech.shared.dto;

import com.btxtech.shared.primitives.Color;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
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

    public void setXRotation(double xRotation) {
        this.xRotation = xRotation;
    }

    public double getYRotation() {
        return yRotation;
    }

    public void setYRotation(double yRotation) {
        this.yRotation = yRotation;
    }

    public Color getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(Color diffuse) {
        this.diffuse = diffuse;
    }

    public Color getAmbient() {
        return ambient;
    }

    public void setAmbient(Color ambient) {
        this.ambient = ambient;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public void setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
    }

    public Vertex getDirection() {
        return Matrix4.createXRotation(xRotation).multiply(Matrix4.createYRotation(yRotation)).multiply(new Vertex(0, 0, -1), 1.0);
    }
}
