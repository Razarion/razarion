package com.btxtech.shared.datatypes.shape;

/**
 * Created by Beat
 * 14.05.2016.
 */
public class VertexContainer {
    private String key;
    private VertexContainerMaterial vertexContainerMaterial;
    private int verticesCount;
    private ShapeTransform shapeTransform;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getVerticesCount() {
        return verticesCount;
    }

    public void setVerticesCount(int verticesCount) {
        this.verticesCount = verticesCount;
    }

    public ShapeTransform getShapeTransform() {
        return shapeTransform;
    }

    public void setShapeTransform(ShapeTransform shapeTransform) {
        this.shapeTransform = shapeTransform;
    }

    public VertexContainerMaterial getVertexContainerMaterial() {
        return vertexContainerMaterial;
    }

    public void setVertexContainerMaterial(VertexContainerMaterial vertexContainerMaterial) {
        this.vertexContainerMaterial = vertexContainerMaterial;
    }

    public VertexContainer key(String key) {
        setKey(key);
        return this;
    }

    public VertexContainer verticesCount(int verticesCount) {
        setVerticesCount(verticesCount);
        return this;
    }

    public VertexContainer shapeTransform(ShapeTransform shapeTransform) {
        setShapeTransform(shapeTransform);
        return this;
    }

    public VertexContainer shape3DMaterial(VertexContainerMaterial shape3DMaterial) {
        setVertexContainerMaterial(shape3DMaterial);
        return this;
    }
}
