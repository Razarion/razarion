package com.btxtech.server.persistence.ui;

import com.btxtech.server.persistence.BaseEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "PARTICLE_SYSTEM")
public class ParticleSystemEntity extends BaseEntity {
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] data;
    private String emitterNodeId;
    private Vertex positionOffset;
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

    public @Nullable String getEmitterNodeId() {
        return emitterNodeId;
    }

    public void setEmitterNodeId(@Nullable String emitterNodeId) {
        this.emitterNodeId = emitterNodeId;
    }

    public @Nullable Vertex getPositionOffset() {
        return positionOffset;
    }

    public void setPositionOffset(@Nullable Vertex positionOffset) {
        this.positionOffset = positionOffset;
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
