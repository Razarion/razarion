package com.btxtech.server.persistence.ui;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "GLTF_BABYLON_MATERIAL")
public class GltfBabylonMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "babylon_material_id", nullable = false)
    private BabylonMaterialEntity babylonMaterialEntity;
    // Does not work @Column(nullable = false, unique = true)
    private String gltfMaterialName;

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BabylonMaterialEntity getBabylonMaterialEntity() {
        return babylonMaterialEntity;
    }

    public void setBabylonMaterialEntity(BabylonMaterialEntity babylonMaterialEntity) {
        this.babylonMaterialEntity = babylonMaterialEntity;
    }

    public String getGltfMaterialName() {
        return gltfMaterialName;
    }

    public void setGltfMaterialName(String materialKey) {
        this.gltfMaterialName = materialKey;
    }

    public GltfBabylonMaterial babylonMaterialEntity(BabylonMaterialEntity babylonMaterialEntity) {
        setBabylonMaterialEntity(babylonMaterialEntity);
        return this;
    }

    public GltfBabylonMaterial gltfMaterialName(String gltfMaterialName) {
        setGltfMaterialName(gltfMaterialName);
        return this;
    }
}
