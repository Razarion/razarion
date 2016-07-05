package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 02.07.2016.
 */
public class TerrainTriangleCorner {
    private Vertex vertex;
    private Vertex norm;
    private Vertex tangent;
    private double splatting;

    public TerrainTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, double splatting) {
        this.vertex = vertex;
        this.norm = norm;
        this.tangent = tangent;
        this.splatting = splatting;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public Vertex getNorm() {
        return norm;
    }

    public Vertex getTangent() {
        return tangent;
    }

    public double getSplatting() {
        return splatting;
    }
}
