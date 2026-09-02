package com.btxtech.server.model.ui;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.system.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
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
    /**
     * SHA-256 of {@link #data}, so a browser can be told "unchanged" without sending the bytes.
     * <p>
     * Two of these are read on every game start and weigh 1.5 MB between them. They were found by
     * the STARTUP_PAYLOAD detail naming the heaviest resource that matched no category - before
     * that, they were part of an anonymous 2.5 MB.
     * <p>
     * In the database rather than in a map on the server, for the reasons given at
     * {@link GltfEntity#getGlbDigest()}. Length is stated: a truncated digest would silently start
     * matching the wrong content.
     */
    @Column(length = 64)
    @JsonIgnore
    private String dataDigest;
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

    public String getDataDigest() {
        return dataDigest;
    }

    public void setDataDigest(String dataDigest) {
        this.dataDigest = dataDigest;
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
