package com.btxtech.shared.dto;

import java.util.List;

/**
 * Created by Beat
 * 07.03.2017.
 */
public class FastVertexContainer {
    private String id;
    private List<Float> vertexData;
    private List<Float> normData;
    private List<Float> textureCoordinate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
