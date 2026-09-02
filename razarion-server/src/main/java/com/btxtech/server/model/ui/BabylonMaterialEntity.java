package com.btxtech.server.model.ui;

import com.btxtech.server.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "BABYLON_MATERIAL")
public class BabylonMaterialEntity extends BaseEntity {
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] data;
    /**
     * SHA-256 of {@link #data}, so a browser can be told "unchanged" without sending the bytes.
     * <p>
     * Six of these are read on every game start and they weigh eight megabytes between them - more
     * than half of everything that has to arrive before the game is playable, and until now
     * downloaded again in full every single time. The model beside them already works this way.
     * <p>
     * It lives in the database rather than in a map on the server for the same two reasons as
     * {@link GltfEntity#getGlbDigest()}: the blob is lazily fetched and megabytes long, so "has it
     * changed?" must be answerable without loading it, and a digest cached per process would let a
     * second pod answer "unchanged" from a value computed before the material was replaced.
     * <p>
     * Length is stated. A truncated digest would silently start matching the wrong content.
     */
    @Column(length = 64)
    @JsonIgnore
    private String dataDigest;
    private boolean nodeMaterial;
    private String diplomacyColorNode;
    private String overrideAlbedoTextureNode;
    private String overrideMetallicTextureNode;
    private String overrideBumpTextureNode;
    private String overrideAmbientOcclusionTextureNode;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getDataDigest() {
        return dataDigest;
    }

    public void setDataDigest(String dataDigest) {
        this.dataDigest = dataDigest;
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

    public String getOverrideAmbientOcclusionTextureNode() {
        return overrideAmbientOcclusionTextureNode;
    }

    public void setOverrideAmbientOcclusionTextureNode(String ambientOcclusionTextureNode) {
        this.overrideAmbientOcclusionTextureNode = ambientOcclusionTextureNode;
    }

    public BabylonMaterialEntity data(byte[] data) {
        setData(data);
        return this;
    }
}
