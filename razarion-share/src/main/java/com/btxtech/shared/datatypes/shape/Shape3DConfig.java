package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.Config;

import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 21.08.2016.
 */
public class Shape3DConfig implements Config {
    private int id;
    private String internalName;
    private String colladaString;
    private List<Shape3DMaterialConfig> shape3DMaterialConfigs;
    private Map<String, AnimationTrigger> animations;

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

    public List<Shape3DMaterialConfig> getShape3DMaterialConfigs() {
        return shape3DMaterialConfigs;
    }

    public void setShape3DMaterialConfigs(List<Shape3DMaterialConfig> shape3DMaterialConfigs) {
        this.shape3DMaterialConfigs = shape3DMaterialConfigs;
    }

    public Map<String, AnimationTrigger> getAnimations() {
        return animations;
    }

    public void setAnimations(Map<String, AnimationTrigger> animations) {
        this.animations = animations;
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

    public Shape3DConfig shape3DMaterialConfigs(List<Shape3DMaterialConfig> shape3DMaterialConfigs) {
        setShape3DMaterialConfigs(shape3DMaterialConfigs);
        return this;
    }

    public Shape3DConfig animations(Map<String, AnimationTrigger> animations) {
        setAnimations(animations);
        return this;
    }
}
