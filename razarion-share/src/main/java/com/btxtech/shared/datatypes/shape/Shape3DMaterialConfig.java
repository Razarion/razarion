package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.PhongMaterialConfig;

public class Shape3DMaterialConfig {
    private String materialId;
    private String materialName;
    private PhongMaterialConfig phongMaterialConfig;
    private boolean characterRepresenting;
    private Double alphaCutout;

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public PhongMaterialConfig getPhongMaterialConfig() {
        return phongMaterialConfig;
    }

    public void setPhongMaterialConfig(PhongMaterialConfig phongMaterialConfig) {
        this.phongMaterialConfig = phongMaterialConfig;
    }

    public boolean isCharacterRepresenting() {
        return characterRepresenting;
    }

    public void setCharacterRepresenting(boolean characterRepresenting) {
        this.characterRepresenting = characterRepresenting;
    }

    public Double getAlphaCutout() {
        return alphaCutout;
    }

    public void setAlphaCutout(Double alphaCutout) {
        this.alphaCutout = alphaCutout;
    }

    public Shape3DMaterialConfig materialId(String materialId) {
        setMaterialId(materialId);
        return this;
    }

    public Shape3DMaterialConfig materialName(String materialName) {
        setMaterialName(materialName);
        return this;
    }

    public Shape3DMaterialConfig phongMaterialConfig(PhongMaterialConfig phongMaterialConfig) {
        setPhongMaterialConfig(phongMaterialConfig);
        return this;
    }

    public Shape3DMaterialConfig characterRepresenting(boolean characterRepresenting) {
        setCharacterRepresenting(characterRepresenting);
        return this;
    }

    public Shape3DMaterialConfig alphaCutout(Double alphaCutout) {
        setAlphaCutout(alphaCutout);
        return this;
    }
}