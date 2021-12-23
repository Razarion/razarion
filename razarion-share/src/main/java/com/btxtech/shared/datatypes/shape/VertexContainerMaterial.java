package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.utils.Shape3DUtils;

import java.util.Objects;

public class VertexContainerMaterial {
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

    public VertexContainerMaterial phongMaterial2Config(PhongMaterialConfig phongMaterial2Config) {
        setPhongMaterial2Config(phongMaterial2Config);
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

    public void override(VertexContainerMaterial source, Shape3DUtils.Context context) {
        if (source == null) {
            return;
        }
        if (source.phongMaterialConfig != null) {
            if (phongMaterialConfig == null) {
                phongMaterialConfig = new PhongMaterialConfig();
            }
            if (context != null) {
                context.image(!Objects.equals(phongMaterialConfig.getTextureId(), source.phongMaterialConfig.getTextureId()));
                context.image(!Objects.equals(phongMaterialConfig.getBumpMapId(), source.phongMaterialConfig.getBumpMapId()));
                context.image(!Objects.equals(phongMaterialConfig.getNormalMapId(), source.phongMaterialConfig.getNormalMapId()));
                context.specular(phongMaterialConfig.getSpecularStrength() == null ^ source.phongMaterialConfig.getSpecularStrength() == null);
                context.specular(phongMaterialConfig.getShininess() == null ^ source.phongMaterialConfig.getShininess() == null);
            }
            phongMaterialConfig.scale(source.phongMaterialConfig.getScale())
                    .textureId(source.phongMaterialConfig.getTextureId())
                    .bumpMapId(source.phongMaterialConfig.getBumpMapId())
                    .bumpMapDepth(source.phongMaterialConfig.getBumpMapDepth())
                    .normalMapId(source.phongMaterialConfig.getNormalMapId())
                    .normalMapDepth(source.phongMaterialConfig.getNormalMapDepth())
                    .shininess(source.phongMaterialConfig.getShininess())
                    .specularStrength(source.phongMaterialConfig.getSpecularStrength());
        }
        if (source.phongMaterial2Config != null) {
            if (phongMaterial2Config == null) {
                phongMaterial2Config = new PhongMaterialConfig();
            }
            if (context != null) {
                context.image(!Objects.equals(phongMaterial2Config.getTextureId(), source.phongMaterial2Config.getTextureId()));
                context.image(!Objects.equals(phongMaterial2Config.getBumpMapId(), source.phongMaterial2Config.getBumpMapId()));
                context.image(!Objects.equals(phongMaterial2Config.getNormalMapId(), source.phongMaterial2Config.getNormalMapId()));
                context.specular(phongMaterial2Config.getSpecularStrength() == null ^ source.phongMaterial2Config.getSpecularStrength() == null);
                context.specular(phongMaterial2Config.getShininess() == null ^ source.phongMaterial2Config.getShininess() == null);
            }
            phongMaterial2Config
                    .scale(source.phongMaterial2Config.getScale())
                    .textureId(source.phongMaterial2Config.getTextureId())
                    .bumpMapId(source.phongMaterial2Config.getBumpMapId())
                    .bumpMapDepth(source.phongMaterial2Config.getBumpMapDepth())
                    .normalMapId(source.phongMaterial2Config.getNormalMapId())
                    .normalMapDepth(source.phongMaterial2Config.getNormalMapDepth())
                    .shininess(source.phongMaterial2Config.getShininess())
                    .specularStrength(source.phongMaterial2Config.getSpecularStrength());
        } else {
            phongMaterial2Config = null;
        }
        if (context != null) {
            context.characterRepresenting(characterRepresenting != source.characterRepresenting);
            context.alphaToCoverage(alphaToCoverage == null ^ source.alphaToCoverage == null);
        }
        characterRepresenting = source.characterRepresenting;
        alphaToCoverage = source.alphaToCoverage;
    }
}
