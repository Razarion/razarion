package com.btxtech.shared.datatypes.shape;

import java.util.List;
import java.util.Objects;

/**
 * Created by Beat
 * 28.07.2016.
 */
@Deprecated // Use ThreeJsModel
public class Element3D {
    private String id;
    private List<ModelMatrixAnimation> modelMatrixAnimations;
    private List<VertexContainer> vertexContainers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ModelMatrixAnimation> getModelMatrixAnimations() {
        return modelMatrixAnimations;
    }

    public void setModelMatrixAnimations(List<ModelMatrixAnimation> modelMatrixAnimations) {
        this.modelMatrixAnimations = modelMatrixAnimations;
    }

    public List<VertexContainer> getVertexContainers() {
        return vertexContainers;
    }

    public void setVertexContainers(List<VertexContainer> vertexContainers) {
        this.vertexContainers = vertexContainers;
    }

    public Element3D id(String id) {
        setId(id);
        return this;
    }

    public Element3D modelMatrixAnimations(List<ModelMatrixAnimation> modelMatrixAnimations) {
        setModelMatrixAnimations(modelMatrixAnimations);
        return this;
    }

    public Element3D vertexContainers(List<VertexContainer> vertexContainers) {
        setVertexContainers(vertexContainers);
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

        return Objects.equals(id, element3D.id);

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
