package com.btxtech.shared.datatypes.asset;

import com.btxtech.shared.dto.Config;
import jsinterop.annotations.JsType;

import java.util.List;

@JsType
public class MeshContainer implements Config {
    private int id;
    private String internalName;
    private String guid;
    private Mesh mesh;
    private List<MeshContainer> children;

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

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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

    @SuppressWarnings("unused")
    public MeshContainer[] getChildrenArray() {
        if (children == null) {
            return null;
        } else {
            return children.toArray(new MeshContainer[0]);
        }
    }

    public void setChildren(List<MeshContainer> children) {
        this.children = children;
    }

    public MeshContainer id(int id) {
        setId(id);
        return this;
    }

    public MeshContainer internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public MeshContainer guid(String guid) {
        setGuid(guid);
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
