package com.btxtech.shared.datatypes.asset;

import java.util.List;

public class MeshContainer {
    private Integer id;
    private String name;
    private Mesh mesh;
    private List<MeshContainer> children;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public MeshContainer name(String name) {
        setName(name);
        return this;
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
