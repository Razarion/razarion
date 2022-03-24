package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.Config;

public class ThreeJsModelConfig implements Config {
    private int id;
    private String internalName;

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

    public ThreeJsModelConfig id(int id) {
        setId(id);
        return this;
    }

    public ThreeJsModelConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }
}
