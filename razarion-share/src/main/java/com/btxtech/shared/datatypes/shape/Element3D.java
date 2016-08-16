package com.btxtech.shared.datatypes.shape;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 28.07.2016.
 */
@Portable
public class Element3D {
    private String id;
    private String shape3DInternalName;
    private List<VertexContainer> vertexContainers;

    public String getId() {
        return id;
    }

    public Element3D setId(String id) {
        this.id = id;
        return this;
    }

    public void setShape3DInternalName(String shape3DInternalName) {
        this.shape3DInternalName = shape3DInternalName;
    }

    public List<VertexContainer> getVertexContainers() {
        return vertexContainers;
    }

    public Element3D setVertexContainers(List<VertexContainer> vertexContainers) {
        this.vertexContainers = vertexContainers;
        for (VertexContainer vertexContainer : vertexContainers) {
            vertexContainer.setShapeElementInternalName(shape3DInternalName + "|" + id);
        }
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
