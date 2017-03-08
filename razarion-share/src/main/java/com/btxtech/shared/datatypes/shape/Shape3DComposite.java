package com.btxtech.shared.datatypes.shape;

import java.util.List;

/**
 * Created by Beat
 * 08.03.2017.
 */
public class Shape3DComposite {
    private Shape3D shape3D;
    private List<VertexContainerBuffer> vertexContainerBuffers;

    public Shape3D getShape3D() {
        return shape3D;
    }

    public Shape3DComposite setShape3D(Shape3D shape3D) {
        this.shape3D = shape3D;
        return this;
    }

    public List<VertexContainerBuffer> getVertexContainerBuffers() {
        return vertexContainerBuffers;
    }

    public Shape3DComposite setVertexContainerBuffers(List<VertexContainerBuffer> vertexContainerBuffers) {
        this.vertexContainerBuffers = vertexContainerBuffers;
        return this;
    }
}
