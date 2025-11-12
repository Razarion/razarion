package com.btxtech.server.model.ui;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.system.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "PARTICLE_SYSTEM")
public class ParticleSystemEntity extends BaseEntity {
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] data;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imageId_id")
    @JsonIgnore
    private ImageLibraryEntity imageLibraryEntity;
    @Transient
    private Integer imageId;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ImageLibraryEntity getImageLibraryEntity() {
        return imageLibraryEntity;
    }

    public void setImageLibraryEntity(ImageLibraryEntity imageLibraryEntity) {
        this.imageLibraryEntity = imageLibraryEntity;
    }

    public @Nullable Integer getImageId() {
        return imageId;
    }

    public void setImageId(@Nullable Integer imageId) {
        this.imageId = imageId;
    }
}
