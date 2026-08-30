package com.btxtech.server.model.ui;

import com.btxtech.server.model.BaseEntity;
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
    /**
     * SHA-256 of {@link #glb}, hex, as the HTTP entity tag for it.
     * <p>
     * It lives in the database rather than in a map on the server for two reasons. The blob is
     * eleven megabytes and lazily fetched, so a client asking "has it changed?" must be answerable
     * without loading it - this column is the only thing that has to be read. And a digest cached
     * per process would let a second pod answer "unchanged" from a value it computed before the
     * model was replaced, which is exactly the staleness the whole arrangement exists to prevent.
     * <p>
     * Length is stated: an @Enumerated or plain String without one has bitten this schema before,
     * and a truncated digest would silently start matching the wrong content.
     */
    @Column(length = 64)
    @JsonIgnore
    private String glbDigest;
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

    public String getGlbDigest() {
        return glbDigest;
    }

    public void setGlbDigest(String glbDigest) {
        this.glbDigest = glbDigest;
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
