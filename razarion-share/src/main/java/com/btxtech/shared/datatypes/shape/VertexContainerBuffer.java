package com.btxtech.shared.datatypes.shape;

import java.util.List;

/**
 * Created by Beat
 * 07.03.2017.
 */
// This class is not handled ba Errai JAX-RS due to performance issues
// Only editor is using this class via Errai JAX-RS
public class VertexContainerBuffer {
    private String key;
    private List<Float> vertexData;
    private List<Float> normData;
    private List<Float> textureCoordinate;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Float> getVertexData() {
        return vertexData;
    }

    public void setVertexData(List<Float> vertexData) {
        this.vertexData = vertexData;
    }

    public List<Float> getNormData() {
        return normData;
    }

    public void setNormData(List<Float> normData) {
        this.normData = normData;
    }

    public List<Float> getTextureCoordinate() {
        return textureCoordinate;
    }

    public void setTextureCoordinate(List<Float> textureCoordinate) {
        this.textureCoordinate = textureCoordinate;
    }

    public int calculateVertexCount() {
        return vertexData.size() / 3;
    }
}
