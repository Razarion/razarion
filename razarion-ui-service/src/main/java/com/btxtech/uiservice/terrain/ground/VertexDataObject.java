package com.btxtech.uiservice.terrain.ground;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 29.03.2016.
 */
public class VertexDataObject extends Vertex {
    private Vertex norm;
    private Vertex tangent;
    private double splatting;

    public VertexDataObject(Vertex vertex, Vertex norm, Vertex tangent, double splatting) {
        super(vertex.getX(), vertex.getY(), vertex.getZ());
        this.norm = norm;
        this.tangent = tangent;
        this.splatting = splatting;
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
