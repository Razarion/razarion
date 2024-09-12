package com.btxtech.server.rest;

import com.btxtech.server.persistence.ui.BabylonMaterialEntity;
import com.btxtech.shared.system.Nullable;

import java.util.List;

public class UiConfigCollection {
    private List<BabylonMaterialEntity> babylonMaterials;
    private Integer selectionItemMaterialId;
    private Integer progressBarNodeMaterialId;
    private Integer healthBarNodeMaterialId;

    public List<BabylonMaterialEntity> getBabylonMaterials() {
        return babylonMaterials;
    }

    public void setBabylonMaterials(List<BabylonMaterialEntity> babylonMaterials) {
        this.babylonMaterials = babylonMaterials;
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
