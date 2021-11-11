package com.btxtech.shared.datatypes.asset;

import com.btxtech.shared.dto.Config;

import java.util.List;

public class Asset implements Config {
    private int id;
    private String unityAssetGuid;
    private String internalName;
    private List<MeshContainer> meshContainers;

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

    public Asset unityAssetGuid(String unityAssetGuid) {
        setUnityAssetGuid(unityAssetGuid);
        return this;
    }

    public Asset internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public Asset meshes(List<MeshContainer> meshes) {
        setMeshContainers(meshes);
        return this;
    }
}
