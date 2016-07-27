package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class MeshEntry {
    private Vertex vertex;
    private Vertex norm;
    private Vertex tangent;
    private float slopeFactor;
    private float splatting;

    public MeshEntry(Vertex vertex, float slopeFactor, float splatting) {
        this.vertex = vertex;
        this.slopeFactor = slopeFactor;
        this.splatting = splatting;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public float getSlopeFactor() {
        return slopeFactor;
    }

    public float getSplatting() {
        return splatting;
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

    @Override
    public String toString() {
        return "MeshEntry{" +
                "vertex=" + vertex +
                ", norm=" + norm +
                ", tangent=" + tangent +
                ", slopeFactor=" + slopeFactor +
                ", splatting=" + splatting +
                '}';
    }
}
