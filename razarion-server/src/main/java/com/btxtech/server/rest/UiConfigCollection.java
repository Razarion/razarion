package com.btxtech.server.rest;

import com.btxtech.server.persistence.ui.ParticleSystemEntity;
import com.btxtech.server.persistence.ui.BabylonMaterialEntity;
import com.btxtech.server.persistence.ui.GltfEntity;
import com.btxtech.server.persistence.ui.Model3DEntity;
import com.btxtech.shared.system.Nullable;

import java.util.List;

public class UiConfigCollection {
    private List<BabylonMaterialEntity> babylonMaterials;
    private List<GltfEntity> gltfs;
    private List<Model3DEntity> model3DEntities;
    private List<ParticleSystemEntity> particleSystemEntities;
    private Integer selectionItemMaterialId;
    private Integer progressBarNodeMaterialId;
    private Integer healthBarNodeMaterialId;

    public List<BabylonMaterialEntity> getBabylonMaterials() {
        return babylonMaterials;
    }

    public void setBabylonMaterials(List<BabylonMaterialEntity> babylonMaterials) {
        this.babylonMaterials = babylonMaterials;
    }

    public List<GltfEntity> getGltfs() {
        return gltfs;
    }

    public void setGltfs(List<GltfEntity> gltfs) {
        this.gltfs = gltfs;
    }

    public List<Model3DEntity> getModel3DEntities() {
        return model3DEntities;
    }

    public void setModel3DEntities(List<Model3DEntity> model3DEntities) {
        this.model3DEntities = model3DEntities;
    }

    public List<ParticleSystemEntity> getParticleSystemEntities() {
        return particleSystemEntities;
    }

    public void setParticleSystemEntities(List<ParticleSystemEntity> particleSystemEntities) {
        this.particleSystemEntities = particleSystemEntities;
    }

    public @Nullable Integer getSelectionItemMaterialId() {
        return selectionItemMaterialId;
    }

    public void setSelectionItemMaterialId(@Nullable Integer selectionItemMaterialId) {
        this.selectionItemMaterialId = selectionItemMaterialId;
    }

    public @Nullable Integer getProgressBarNodeMaterialId() {
        return progressBarNodeMaterialId;
    }

    public void setProgressBarNodeMaterialId(@Nullable Integer progressBarNodeMaterialId) {
        this.progressBarNodeMaterialId = progressBarNodeMaterialId;
    }

    public @Nullable Integer getHealthBarNodeMaterialId() {
        return healthBarNodeMaterialId;
    }

    public void setHealthBarNodeMaterialId(@Nullable Integer healthBarNodeMaterialId) {
        this.healthBarNodeMaterialId = healthBarNodeMaterialId;
    }

    public UiConfigCollection babylonMaterials(List<BabylonMaterialEntity> babylonMaterials) {
        setBabylonMaterials(babylonMaterials);
        return this;
    }

    public UiConfigCollection gltfs(List<GltfEntity> gltfs) {
        setGltfs(gltfs);
        return this;
    }

    public UiConfigCollection model3DEntities(List<Model3DEntity> model3DEntities) {
        setModel3DEntities(model3DEntities);
        return this;
    }

    public UiConfigCollection particleSystemEntities(List<ParticleSystemEntity> particleSystemEntities) {
        setParticleSystemEntities(particleSystemEntities);
        return this;
    }

    public UiConfigCollection selectionItemMaterialId(Integer selectionItemMaterialId) {
        setSelectionItemMaterialId(selectionItemMaterialId);
        return this;
    }

    public UiConfigCollection progressBarNodeMaterialId(Integer progressBarNodeMaterialId) {
        setProgressBarNodeMaterialId(progressBarNodeMaterialId);
        return this;
    }

    public UiConfigCollection healthBarNodeMaterialId(Integer healthBarNodeMaterialId) {
        setHealthBarNodeMaterialId(healthBarNodeMaterialId);
        return this;
    }
}
