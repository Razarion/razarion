package com.btxtech.server.model.ui;

import com.btxtech.server.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "MODEL_3D")
public class Model3DEntity extends BaseEntity {
    private String gltfName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnore
    private GltfEntity gltfEntity;
    @Transient
    private Integer gltfEntityId;

    public String getGltfName() {
        return gltfName;
    }

    public void setGltfName(String name) {
        this.gltfName = name;
    }

    public GltfEntity getGltfEntity() {
        return gltfEntity;
    }

    public void setGltfEntity(GltfEntity gltfEntity) {
        this.gltfEntity = gltfEntity;
    }

    public Integer getGltfEntityId() {
        return gltfEntityId;
    }

    public void setGltfEntityId(Integer gltfEntityId) {
        this.gltfEntityId = gltfEntityId;
    }

    public Model3DEntity gltfName(String gltfName) {
        setGltfName(gltfName);
        return this;
    }

    public Model3DEntity gltfEntity(GltfEntity gltfEntity) {
        setGltfEntity(gltfEntity);
        return this;
    }

    public Model3DEntity gltfEntityId(Integer gltfEntityId) {
        setGltfEntityId(gltfEntityId);
        return this;
    }

}
