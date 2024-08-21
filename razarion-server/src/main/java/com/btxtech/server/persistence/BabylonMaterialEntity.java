package com.btxtech.server.persistence;

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
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
