package com.btxtech.server.rest;

import com.btxtech.server.persistence.ui.BabylonMaterialEntity;

import java.util.List;

public class UiConfigCollection {
    private List<BabylonMaterialEntity> babylonMaterials;
    private Integer selectionItemMaterialId;

    public List<BabylonMaterialEntity> getBabylonMaterials() {
        return babylonMaterials;
    }

    public void setBabylonMaterials(List<BabylonMaterialEntity> babylonMaterials) {
        this.babylonMaterials = babylonMaterials;
    }

    public Integer getSelectionItemMaterialId() {
        return selectionItemMaterialId;
    }

    public void setSelectionItemMaterialId(Integer selectionItemMaterialId) {
        this.selectionItemMaterialId = selectionItemMaterialId;
    }

    public UiConfigCollection babylonMaterials(List<BabylonMaterialEntity> babylonMaterials) {
        setBabylonMaterials(babylonMaterials);
        return this;
    }

    public UiConfigCollection selectionItemMaterialId(Integer selectionItemMaterialId) {
        setSelectionItemMaterialId(selectionItemMaterialId);
        return this;
    }
}
