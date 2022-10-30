package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.Config;
import jsinterop.annotations.JsType;

@JsType
public class ThreeJsModelConfig implements Config {
    private int id;
    private String internalName;
    private Type type;

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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ThreeJsModelConfig id(int id) {
        setId(id);
        return this;
    }

    public ThreeJsModelConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public ThreeJsModelConfig type(Type type) {
        setType(type);
        return this;
    }

    public enum Type {
        GLTF,
        NODES_MATERIAL
    }
}
