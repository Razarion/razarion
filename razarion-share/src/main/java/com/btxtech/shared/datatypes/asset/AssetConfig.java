package com.btxtech.shared.datatypes.asset;

import com.btxtech.shared.dto.Config;

import java.util.List;

public class AssetConfig implements Config {
    private int id; // Readonly
    private String unityAssetGuid; // Readonly
    private String internalName; // Readonly
    private String assetMetaFileHint;
    private List<MeshContainer> meshContainers; // Readonly

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public List<MeshContainer> getMeshContainers() {
        return meshContainers;
    }

    public void setMeshContainers(List<MeshContainer> meshContainers) {
        this.meshContainers = meshContainers;
    }

    public String getUnityAssetGuid() {
        return unityAssetGuid;
    }

    public void setUnityAssetGuid(String unityAssetGuid) {
        this.unityAssetGuid = unityAssetGuid;
    }

    public String getAssetMetaFileHint() {
        return assetMetaFileHint;
    }

    public void setAssetMetaFileHint(String assetMetaFileHint) {
        this.assetMetaFileHint = assetMetaFileHint;
    }

    public AssetConfig unityAssetGuid(String unityAssetGuid) {
        setUnityAssetGuid(unityAssetGuid);
        return this;
    }

    public AssetConfig id(int id) {
        setId(id);
        return this;
    }

    public AssetConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public AssetConfig assetMetaFileHint(String assetMetaFileHint) {
        setAssetMetaFileHint(assetMetaFileHint);
        return this;
    }

    public AssetConfig meshContainers(List<MeshContainer> meshContainers) {
        setMeshContainers(meshContainers);
        return this;
    }
}
