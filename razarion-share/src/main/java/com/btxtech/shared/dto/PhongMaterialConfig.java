package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.CollectionReference;
import com.btxtech.shared.datatypes.CollectionReferenceType;

public class PhongMaterialConfig {
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer textureId;
    private double scale;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer normalMapId;
    private Double normalMapDepth;
    @CollectionReference(CollectionReferenceType.IMAGE)
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

    public Integer getNormalMapId() {
        return normalMapId;
    }

    public void setNormalMapId(Integer normalMapId) {
        this.normalMapId = normalMapId;
    }

    public Double getNormalMapDepth() {
        return normalMapDepth;
    }

    public void setNormalMapDepth(Double normalMapDepth) {
        this.normalMapDepth = normalMapDepth;
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

    public PhongMaterialConfig normalMapId(Integer normalMapId) {
        setNormalMapId(normalMapId);
        return this;
    }

    public PhongMaterialConfig normalMapDepth(Double normalMapDepth) {
        setNormalMapDepth(normalMapDepth);
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
