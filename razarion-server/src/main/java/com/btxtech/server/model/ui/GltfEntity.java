package com.btxtech.server.model.ui;

import com.btxtech.server.service.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "GLTF")
public class GltfEntity extends BaseEntity {
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] glb;
    @Transient
    private Map<String, Integer> materialGltfNames;
    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "gltfEntity", nullable = false)
    private List<GltfBabylonMaterialEntity> gltfBabylonMaterials;

    public byte[] getGlb() {
        return glb;
    }

    public void setGlb(byte[] glb) {
        this.glb = glb;
    }

    public Map<String, Integer> getMaterialGltfNames() {
        return materialGltfNames;
    }

    public void setMaterialGltfNames(Map<String, Integer> materialGltfNames) {
        this.materialGltfNames = materialGltfNames;
    }

    public List<GltfBabylonMaterialEntity> getGltfBabylonMaterials() {
        return gltfBabylonMaterials;
    }

    public void setGltfBabylonMaterials(List<GltfBabylonMaterialEntity> gltfBabylonMaterials) {
        this.gltfBabylonMaterials = gltfBabylonMaterials;
    }
}
