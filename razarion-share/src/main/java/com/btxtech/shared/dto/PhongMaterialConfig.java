package com.btxtech.shared.dto;

public class PhongMaterialConfig {
    private ImageScaleConfig textureScaleConfig;
    private Integer bumpMapId;
    private double bumpMapDepth;
    private double shininess;
    private double specularStrength;

    public ImageScaleConfig getTextureScaleConfig() {
        return textureScaleConfig;
    }

    public PhongMaterialConfig setTextureScaleConfig(ImageScaleConfig textureScaleConfig) {
        this.textureScaleConfig = textureScaleConfig;
        return this;
    }

    public Integer getBumpMapId() {
        return bumpMapId;
    }

    public PhongMaterialConfig setBumpMapId(Integer bumpMapId) {
        this.bumpMapId = bumpMapId;
        return this;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public PhongMaterialConfig setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
        return this;
    }

    public double getShininess() {
        return shininess;
    }

    public PhongMaterialConfig setShininess(double shininess) {
        this.shininess = shininess;
        return this;
    }

    public double getSpecularStrength() {
        return specularStrength;
    }

    public PhongMaterialConfig setSpecularStrength(double specularStrength) {
        this.specularStrength = specularStrength;
        return this;
    }
}
