package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 14.03.2016.
 */
public class VertexData {
    private Vertex vertex;
    private Vertex norm;
    private Vertex tangent;
    private double edge;

    public VertexData(Vertex vertex) {
        this.vertex = vertex;
    }

    public VertexData(VertexData vertexData) {
        this.vertex = vertexData.vertex;
        this.norm = vertexData.norm;
        this.tangent = vertexData.tangent;
        this.edge = vertexData.edge;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void addZ(double z) {
        this.vertex = this.vertex.add(0, 0, z);
    }

    public double getEdge() {
        return edge;
    }

    public void setEdge(double edge) {
        this.edge = edge;
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
                ", edge=" + edge +
                '}';
    }
}
