package com.btxtech.client.terrain;

import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 14.03.2016.
 */
public class VertexData {
    private Vertex vertex;
    private Vertex norm;
    private Vertex tangent;
    private double splatting;

    public VertexData(Vertex vertex) {
        this.vertex = vertex;
    }

    public VertexData(VertexData vertexData) {
        this.vertex = vertexData.vertex;
        this.norm = vertexData.norm;
        this.tangent = vertexData.tangent;
        this.splatting = vertexData.splatting;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void addZ(double z) {
        this.vertex = this.vertex.add(0, 0, z);
    }

    public double getSplatting() {
        return splatting;
    }

    public void setSplatting(double splatting) {
        this.splatting = splatting;
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
        return "VertexData{" +
                "vertex=" + vertex +
                ", norm=" + norm +
                ", tangent=" + tangent +
                ", splatting=" + splatting +
                '}';
    }
}
