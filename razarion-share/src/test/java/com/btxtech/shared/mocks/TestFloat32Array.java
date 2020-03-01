package com.btxtech.shared.mocks;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;

import java.util.List;

public class TestFloat32Array implements Float32ArrayEmu {
    private List<Vertex> vertices;

    public TestFloat32Array(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }
}
