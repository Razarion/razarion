package com.btxtech.shared.datatypes.shape.config;

import java.util.List;

public class Shape3DElementConfig {
    private List<Shape3DMaterialConfig> shape3DMaterialConfigs;
    private List<Shape3DAnimationTriggerConfig> shape3DAnimationTriggerConfigs;

    public List<Shape3DMaterialConfig> getShape3DMaterialConfigs() {
        return shape3DMaterialConfigs;
    }

    public void setShape3DMaterialConfigs(List<Shape3DMaterialConfig> shape3DMaterialConfigs) {
        this.shape3DMaterialConfigs = shape3DMaterialConfigs;
    }

    public List<Shape3DAnimationTriggerConfig> getShape3DAnimationTriggerConfigs() {
        return shape3DAnimationTriggerConfigs;
    }

    public void setShape3DAnimationTriggerConfigs(List<Shape3DAnimationTriggerConfig> shape3DAnimationTriggerConfigs) {
        this.shape3DAnimationTriggerConfigs = shape3DAnimationTriggerConfigs;
    }

    public Shape3DElementConfig shape3DMaterialConfigs(List<Shape3DMaterialConfig> shape3DVertexContainerConfig) {
        this.shape3DMaterialConfigs = shape3DVertexContainerConfig;
        return this;
    }

    public Shape3DElementConfig shape3DAnimationTriggerConfigs(List<Shape3DAnimationTriggerConfig> shape3DAnimationTriggerConfigs) {
        setShape3DAnimationTriggerConfigs(shape3DAnimationTriggerConfigs);
        return this;
    }
}
