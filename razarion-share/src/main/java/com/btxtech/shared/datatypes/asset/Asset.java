package com.btxtech.shared.datatypes.asset;

import com.btxtech.shared.dto.Config;

import java.util.List;

public class Asset implements Config {
    private int id;
    private String internalName;
    private List<Mesh> meshes;

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

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public void setMeshes(List<Mesh> meshes) {
        this.meshes = meshes;
    }
}
