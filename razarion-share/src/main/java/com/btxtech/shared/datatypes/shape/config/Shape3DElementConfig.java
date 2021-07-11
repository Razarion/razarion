package com.btxtech.shared.datatypes.shape.config;

import java.util.List;

public class Shape3DElementConfig {
    private String id;
    private List<VertexContainerMaterialConfig> vertexContainerMaterialConfigs;
    private List<Shape3DAnimationTriggerConfig> shape3DAnimationTriggerConfigs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<VertexContainerMaterialConfig> getVertexContainerMaterialConfigs() {
        return vertexContainerMaterialConfigs;
    }

    public void setVertexContainerMaterialConfigs(List<VertexContainerMaterialConfig> vertexContainerMaterialConfigs) {
        this.vertexContainerMaterialConfigs = vertexContainerMaterialConfigs;
    }

    public List<Shape3DAnimationTriggerConfig> getShape3DAnimationTriggerConfigs() {
        return shape3DAnimationTriggerConfigs;
    }

    public void setShape3DAnimationTriggerConfigs(List<Shape3DAnimationTriggerConfig> shape3DAnimationTriggerConfigs) {
        this.shape3DAnimationTriggerConfigs = shape3DAnimationTriggerConfigs;
    }

    public Shape3DElementConfig id(String id) {
        setId(id);
        return this;
    }

    public Shape3DElementConfig shape3DMaterialConfigs(List<VertexContainerMaterialConfig> shape3DVertexContainerConfig) {
        this.vertexContainerMaterialConfigs = shape3DVertexContainerConfig;
        return this;
    }

    public Shape3DElementConfig shape3DAnimationTriggerConfigs(List<Shape3DAnimationTriggerConfig> shape3DAnimationTriggerConfigs) {
        setShape3DAnimationTriggerConfigs(shape3DAnimationTriggerConfigs);
        return this;
    }
}
