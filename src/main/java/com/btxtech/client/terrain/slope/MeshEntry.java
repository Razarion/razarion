package com.btxtech.client.terrain.slope;

import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class MeshEntry {
    private Vertex vertex;
    private Vertex norm;
    private Vertex tangent;
    private float slopeFactor;

    public MeshEntry(Vertex vertex, float slopeFactor) {
        this.vertex = vertex;
        this.slopeFactor = slopeFactor;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public float getSlopeFactor() {
        return slopeFactor;
    }

    public Vertex getNorm() {
        return norm;
    }

    public void setNorm(Vertex norm) {
        this.norm = norm;
    }

    public Vertex getTangent() {
        return tangent;
    }

    public void setTangent(Vertex tangent) {
        this.tangent = tangent;
    }
}
