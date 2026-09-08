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
     * <p>
     * Sent to the client rather than hidden, which is what lets the model be cached at all. The
     * entity tag it backs only ever produced a 304 - measured over a day, 19 of 154 requests for
     * this file, because a paid visitor arrives once and holds nothing to revalidate against. With
     * the digest in hand the client can ask for /rest/gltf/glb/{id}/{digest}, and that url is
     * immutable by construction: an edited model gets a new digest and therefore a new url, so the
     * response may be cached publicly and served from an edge instead of from us-central1. The
     * guarantee the no-store header protects is kept - it moves from revalidation into the path.
     */
    @Column(length = 64)
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
