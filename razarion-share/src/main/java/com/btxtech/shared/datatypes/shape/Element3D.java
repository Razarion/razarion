package com.btxtech.shared.datatypes.shape;

import java.util.List;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class Element3D {
    private String id;
    private List<ModelMatrixAnimation> modelMatrixAnimations;
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

    public void setVertexContainers(List<VertexContainer> vertexContainers) {
        this.vertexContainers = vertexContainers;
    }

    public void setModelMatrixAnimations(List<ModelMatrixAnimation> modelMatrixAnimations) {
        this.modelMatrixAnimations = modelMatrixAnimations;
    }

    public List<ModelMatrixAnimation> getModelMatrixAnimations() {
        return modelMatrixAnimations;
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
