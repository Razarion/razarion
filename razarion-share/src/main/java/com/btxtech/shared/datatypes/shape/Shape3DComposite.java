package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;

import java.util.List;

/**
 * Created by Beat
 * 08.03.2017.
 */
public class Shape3DComposite {
    private Shape3DConfig shape3DConfig;
    private Shape3D shape3D;
    private List<VertexContainerBuffer> vertexContainerBuffers;

    public Shape3DConfig getShape3DConfig() {
        return shape3DConfig;
    }

    public void setShape3DConfig(Shape3DConfig shape3DConfig) {
        this.shape3DConfig = shape3DConfig;
    }

    public Shape3D getShape3D() {
        return shape3D;
    }

    public void setShape3D(Shape3D shape3D) {
        this.shape3D = shape3D;
    }

    public List<VertexContainerBuffer> getVertexContainerBuffers() {
        return vertexContainerBuffers;
    }

    public void setVertexContainerBuffers(List<VertexContainerBuffer> vertexContainerBuffers) {
        this.vertexContainerBuffers = vertexContainerBuffers;
    }

    public Shape3DComposite shape3DConfig(Shape3DConfig shape3DConfig) {
        setShape3DConfig(shape3DConfig);
        return this;
    }

    public Shape3DComposite shape3D(Shape3D shape3D) {
        setShape3D(shape3D);
        return this;
    }

    public Shape3DComposite vertexContainerBuffers(List<VertexContainerBuffer> vertexContainerBuffers) {
        setVertexContainerBuffers(vertexContainerBuffers);
        return this;
    }
}
