package com.btxtech.client.terrain;

import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 29.03.2016.
 */
public class VertexDataObject extends Vertex {
    private Vertex norm;
    private Vertex tangent;

    public VertexDataObject(Vertex vertex, Vertex norm, Vertex tangent) {
        super(vertex.getX(), vertex.getY(), vertex.getZ());
        this.norm = norm;
        this.tangent = tangent;
    }

    public Vertex getNorm() {
        return norm;
    }

    public Vertex getTangent() {
        return tangent;
    }
}
