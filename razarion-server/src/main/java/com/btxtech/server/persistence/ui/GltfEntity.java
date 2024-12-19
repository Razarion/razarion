package com.btxtech.server.persistence.ui;

import com.btxtech.server.persistence.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
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
    private List<GltfBabylonMaterial> gltfBabylonMaterials;

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

    public List<GltfBabylonMaterial> getGltfBabylonMaterials() {
        return gltfBabylonMaterials;
    }

    public void setGltfBabylonMaterials(List<GltfBabylonMaterial> gltfBabylonMaterials) {
        this.gltfBabylonMaterials = gltfBabylonMaterials;
    }
}
