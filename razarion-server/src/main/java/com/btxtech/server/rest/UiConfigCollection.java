package com.btxtech.server.rest;

import com.btxtech.server.persistence.ui.BabylonMaterialEntity;

import java.util.List;

public class UiConfigCollection {
    private List<BabylonMaterialEntity> babylonMaterials;

    public List<BabylonMaterialEntity> getBabylonMaterials() {
        return babylonMaterials;
    }

    public void setBabylonMaterials(List<BabylonMaterialEntity> babylonMaterials) {
        this.babylonMaterials = babylonMaterials;
    }

    public UiConfigCollection babylonMaterials(List<BabylonMaterialEntity> babylonMaterials) {
        setBabylonMaterials(babylonMaterials);
        return this;
    }

}
