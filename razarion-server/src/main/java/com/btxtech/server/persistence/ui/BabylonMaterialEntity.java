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

    public BabylonMaterialEntity data(byte[] data) {
        setData(data);
        return this;
    }
}
