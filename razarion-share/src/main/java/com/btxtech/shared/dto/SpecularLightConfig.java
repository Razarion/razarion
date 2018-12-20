package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 28.05.2016.
 */
public class SpecularLightConfig {
    private double specularIntensity;
    private double specularHardness;

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public SpecularLightConfig setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
        return this;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public SpecularLightConfig setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
        return this;
    }
}
