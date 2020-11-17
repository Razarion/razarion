package com.btxtech.shared.datatypes.shape.config;

import com.btxtech.shared.dto.Config;

import java.util.List;

/**
 * Created by Beat
 * 21.08.2016.
 */
public class Shape3DConfig implements Config {
    private int id;
    private String internalName;
    private String colladaString;
    private List<Shape3DElementConfig> shape3DElementConfigs;

    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getColladaString() {
        return colladaString;
    }

    public void setColladaString(String colladaString) {
        this.colladaString = colladaString;
    }

    public List<Shape3DElementConfig> getShape3DElementConfigs() {
        return this.shape3DElementConfigs;
    }

    public void setShape3DElementConfigs(List<Shape3DElementConfig> shape3DElementConfigs) {
        this.shape3DElementConfigs = shape3DElementConfigs;
    }

    public Shape3DConfig id(int id) {
        this.id = id;
        return this;
    }

    public Shape3DConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public Shape3DConfig colladaString(String colladaString) {
        setColladaString(colladaString);
        return this;
    }

    public Shape3DConfig shape3DElementConfigs(List<Shape3DElementConfig> shape3DElementConfigs) {
        setShape3DElementConfigs(shape3DElementConfigs);
        return this;
    }
}
