package com.btxtech.shared.datatypes.shape;

import java.util.List;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class Element3D {
    private String id;
    private List<VertexContainer> vertexContainers;

    public String getId() {
        return id;
    }

    public Element3D setId(String id) {
        this.id = id;
        return this;
    }

    public List<VertexContainer> getVertexContainers() {
        return vertexContainers;
    }

    public Element3D setVertexContainers(List<VertexContainer> vertexContainers) {
        this.vertexContainers = vertexContainers;
        return this;
    }

    @Override
    public String toString() {
        return "Element3D{" +
                "id='" + id + '\'' +
                ", vertexContainers=" + vertexContainers +
                '}';
    }
}
