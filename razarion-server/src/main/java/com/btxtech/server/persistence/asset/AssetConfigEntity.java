package com.btxtech.server.persistence.asset;

import com.btxtech.shared.datatypes.asset.AssetConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

import static com.btxtech.server.persistence.PersistenceUtil.toConfigList;

@Entity
@Table(name = "ASSET")
public class AssetConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private String unityAssetGuid;
    private String assetMetaFileHint;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "asset")
    private List<MeshContainerEntity> meshContainers;

    public AssetConfig toConfig() {
        return new AssetConfig()
                .id(id)
                .internalName(internalName)
                .unityAssetGuid(unityAssetGuid)
                .assetMetaFileHint(assetMetaFileHint)
                .meshContainers(toConfigList(meshContainers, MeshContainerEntity::toConfig));
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setUnityAssetGuid(String unityAssetGuid) {
        this.unityAssetGuid = unityAssetGuid;
    }

    public void setAssetMetaFileHint(String assetMetaFileHint) {
        this.assetMetaFileHint = assetMetaFileHint;
    }

    public List<MeshContainerEntity> getMeshContainers() {
        return meshContainers;
    }

    public void setMeshContainers(List<MeshContainerEntity> meshContainers) {
        this.meshContainers = meshContainers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AssetConfigEntity that = (AssetConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
