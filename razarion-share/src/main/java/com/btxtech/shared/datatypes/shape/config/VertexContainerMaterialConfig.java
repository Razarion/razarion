package com.btxtech.shared.datatypes.shape.config;

import com.btxtech.shared.datatypes.shape.VertexContainerMaterial;
import com.btxtech.shared.dto.PhongMaterialConfig;

public class VertexContainerMaterialConfig {
    private String materialId;
    private String materialName;
    private PhongMaterialConfig phongMaterialConfig;
    private PhongMaterialConfig phongMaterial2Config;
    private boolean characterRepresenting;
    private Double alphaToCoverage;

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

    public PhongMaterialConfig getPhongMaterial2Config() {
        return phongMaterial2Config;
    }

    public void setPhongMaterial2Config(PhongMaterialConfig phongMaterial2Config) {
        this.phongMaterial2Config = phongMaterial2Config;
    }

    public boolean isCharacterRepresenting() {
        return characterRepresenting;
    }

    public void setCharacterRepresenting(boolean characterRepresenting) {
        this.characterRepresenting = characterRepresenting;
    }

    public Double getAlphaToCoverage() {
        return alphaToCoverage;
    }

    public void setAlphaToCoverage(Double alphaToCoverage) {
        this.alphaToCoverage = alphaToCoverage;
    }

    public VertexContainerMaterialConfig materialId(String materialId) {
        setMaterialId(materialId);
        return this;
    }

    public VertexContainerMaterialConfig materialName(String materialName) {
        setMaterialName(materialName);
        return this;
    }

    public VertexContainerMaterialConfig phongMaterialConfig(PhongMaterialConfig phongMaterialConfig) {
        setPhongMaterialConfig(phongMaterialConfig);
        return this;
    }

    public VertexContainerMaterialConfig phongMaterial2Config(PhongMaterialConfig phongMaterial2Config) {
        setPhongMaterial2Config(phongMaterial2Config);
        return this;
    }

    public VertexContainerMaterialConfig characterRepresenting(boolean characterRepresenting) {
        setCharacterRepresenting(characterRepresenting);
        return this;
    }

    public VertexContainerMaterialConfig alphaToCoverage(Double alphaToCoverage) {
        setAlphaToCoverage(alphaToCoverage);
        return this;
    }

    public VertexContainerMaterial toVertexContainerMaterial() {
        return new VertexContainerMaterial()
                .materialId(materialId)
                .materialName(materialName)
                .alphaToCoverage(alphaToCoverage)
                .characterRepresenting(characterRepresenting)
                .phongMaterialConfig(phongMaterialConfig)
                .phongMaterial2Config(phongMaterial2Config);
    }
}
