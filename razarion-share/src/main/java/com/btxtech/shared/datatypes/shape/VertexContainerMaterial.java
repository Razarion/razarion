package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.PhongMaterialConfig;

public class VertexContainerMaterial {
    private String materialId;
    private String materialName;
    private PhongMaterialConfig phongMaterialConfig;
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

    public VertexContainerMaterial materialId(String materialId) {
        setMaterialId(materialId);
        return this;
    }

    public VertexContainerMaterial materialName(String materialName) {
        setMaterialName(materialName);
        return this;
    }

    public VertexContainerMaterial phongMaterialConfig(PhongMaterialConfig phongMaterialConfig) {
        setPhongMaterialConfig(phongMaterialConfig);
        return this;
    }

    public VertexContainerMaterial characterRepresenting(boolean characterRepresenting) {
        setCharacterRepresenting(characterRepresenting);
        return this;
    }

    public VertexContainerMaterial alphaToCoverage(Double alphaToCoverage) {
        setAlphaToCoverage(alphaToCoverage);
        return this;
    }

    public void override(VertexContainerMaterial origin) {
        if (origin == null) {
            return;
        }
        if (origin.phongMaterialConfig != null) {
            if (phongMaterialConfig == null) {
                phongMaterialConfig = new PhongMaterialConfig();
            }
            phongMaterialConfig.scale(origin.phongMaterialConfig.getScale())
                    .textureId(origin.phongMaterialConfig.getTextureId())
                    .bumpMapId(origin.phongMaterialConfig.getBumpMapId())
                    .bumpMapDepth(origin.phongMaterialConfig.getBumpMapDepth())
                    .normalMapId(origin.phongMaterialConfig.getNormalMapId())
                    .normalMapDepth(origin.phongMaterialConfig.getNormalMapDepth())
                    .shininess(origin.phongMaterialConfig.getShininess())
                    .specularStrength(origin.phongMaterialConfig.getSpecularStrength());
        }
        characterRepresenting = origin.characterRepresenting;
        alphaToCoverage = origin.alphaToCoverage;
    }
}
