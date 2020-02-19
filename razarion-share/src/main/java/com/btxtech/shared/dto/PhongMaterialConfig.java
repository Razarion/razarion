package com.btxtech.shared.dto;

public class PhongMaterialConfig {
    private Integer textureId;
    private double scale;
    private Integer bumpMapId;
    private Double bumpMapDepth;
    private Double shininess;
    private Double specularStrength;

    public Integer getTextureId() {
        return textureId;
    }

    public void setTextureId(Integer textureId) {
        this.textureId = textureId;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Integer getBumpMapId() {
        return bumpMapId;
    }

    public void setBumpMapId(Integer bumpMapId) {
        this.bumpMapId = bumpMapId;
    }

    public Double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(Double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    public Double getShininess() {
        return shininess;
    }

    public void setShininess(Double shininess) {
        this.shininess = shininess;
    }

    public Double getSpecularStrength() {
        return specularStrength;
    }

    public void setSpecularStrength(Double specularStrength) {
        this.specularStrength = specularStrength;
    }

    public PhongMaterialConfig textureId(Integer textureId) {
        setTextureId(textureId);
        return this;
    }

    public PhongMaterialConfig scale(double scale) {
        setScale(scale);
        return this;
    }

    public PhongMaterialConfig bumpMapId(Integer bumpMapId) {
        setBumpMapId(bumpMapId);
        return this;
    }

    public PhongMaterialConfig bumpMapDepth(Double bumpMapDepth) {
        setBumpMapDepth(bumpMapDepth);
        return this;
    }

    public PhongMaterialConfig shininess(Double shininess) {
        setShininess(shininess);
        return this;
    }

    public PhongMaterialConfig specularStrength(Double specularStrength) {
        setSpecularStrength(specularStrength);
        return this;
    }
}
