package com.btxtech.shared.datatypes.asset;

import java.util.List;

public class MeshContainer {
    private Mesh mesh;
    private List<MeshContainer> children;

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public List<MeshContainer> getChildren() {
        return children;
    }

    public void setChildren(List<MeshContainer> children) {
        this.children = children;
    }

    public MeshContainer mesh(Mesh mesh) {
        setMesh(mesh);
        return this;
    }

    public MeshContainer children(List<MeshContainer> children) {
        setChildren(children);
        return this;
    }
}
