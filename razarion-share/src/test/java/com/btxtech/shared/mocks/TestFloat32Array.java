package com.btxtech.shared.mocks;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;

import java.util.List;

import static com.btxtech.shared.datatypes.Vertex.fromArray;

public class TestFloat32Array implements Float32ArrayEmu {
    private List<Vertex> vertices;

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public TestFloat32Array vertices(List<Vertex> vertices) {
        setVertices(vertices);
        return this;
    }

    public Float32ArrayEmu vertices(double[] vertices) {
        return vertices(fromArray(vertices));
    }
}
