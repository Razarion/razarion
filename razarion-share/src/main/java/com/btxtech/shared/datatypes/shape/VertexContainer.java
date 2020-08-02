package com.btxtech.shared.datatypes.shape;

/**
 * Created by Beat
 * 14.05.2016.
 */
public class VertexContainer {
    private String key;
    private Shape3DMaterialConfig shape3DMaterialConfig;
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

    public Shape3DMaterialConfig getShape3DMaterialConfig() {
        return shape3DMaterialConfig;
    }

    public void setShape3DMaterialConfig(Shape3DMaterialConfig shape3DMaterialConfig) {
        this.shape3DMaterialConfig = shape3DMaterialConfig;
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

    public VertexContainer shape3DMaterialConfig(Shape3DMaterialConfig shape3DMaterialConfig) {
        setShape3DMaterialConfig(shape3DMaterialConfig);
        return this;
    }
}
