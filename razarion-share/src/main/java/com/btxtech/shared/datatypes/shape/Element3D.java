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

    public void updateVertexContainerHelperName(String shape3DInternalName) {
        for (VertexContainer vertexContainer : vertexContainers) {
            vertexContainer.setShapeElementInternalName(shape3DInternalName + "|" + id);
        }
    }

    public List<VertexContainer> getVertexContainers() {
        return vertexContainers;
    }

    public Element3D setVertexContainers(List<VertexContainer> vertexContainers) {
        this.vertexContainers = vertexContainers;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Element3D element3D = (Element3D) o;

        return id != null ? id.equals(element3D.id) : element3D.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Element3D{" +
                "id='" + id + '\'' +
                ", vertexContainers=" + vertexContainers +
                '}';
    }
}
