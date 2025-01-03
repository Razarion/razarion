package com.btxtech.server.persistence.ui;

import com.btxtech.server.persistence.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "BABYLON_MATERIAL")
public class BabylonMaterialEntity extends BaseEntity {
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] data;
    private boolean nodeMaterial;
    private String diplomacyColorNode;
    private String overrideAlbedoTextureNode;
    private String overrideMetallicTextureNode;
    private String overrideBumpTextureNode;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isNodeMaterial() {
        return nodeMaterial;
    }

    public void setNodeMaterial(boolean nodeMaterial) {
        this.nodeMaterial = nodeMaterial;
    }

    public String getDiplomacyColorNode() {
        return diplomacyColorNode;
    }

    public void setDiplomacyColorNode(String diplomacyColor) {
        this.diplomacyColorNode = diplomacyColor;
    }

    public String getOverrideAlbedoTextureNode() {
        return overrideAlbedoTextureNode;
    }

    public void setOverrideAlbedoTextureNode(String overrideAlbedoTextureNode) {
        this.overrideAlbedoTextureNode = overrideAlbedoTextureNode;
    }

    public String getOverrideMetallicTextureNode() {
        return overrideMetallicTextureNode;
    }

    public void setOverrideMetallicTextureNode(String overrideMetallicTextureNode) {
        this.overrideMetallicTextureNode = overrideMetallicTextureNode;
    }

    public String getOverrideBumpTextureNode() {
        return overrideBumpTextureNode;
    }

    public void setOverrideBumpTextureNode(String overrideBumpTextureNode) {
        this.overrideBumpTextureNode = overrideBumpTextureNode;
    }

    public BabylonMaterialEntity data(byte[] data) {
        setData(data);
        return this;
    }
}
