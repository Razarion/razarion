package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.PhongMaterialConfig;

/**
 * Created by Beat
 * 14.05.2016.
 */
public class VertexContainer {
    private String key;
    private String materialId;
    private String materialName;
    private PhongMaterialConfig phongMaterialConfig;
    private int verticesCount;
    private ShapeTransform shapeTransform;
    private boolean alphaToCoverage;
    private boolean characterRepresenting;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public PhongMaterialConfig getPhongMaterialConfig() {
        return phongMaterialConfig;
    }

    public void setPhongMaterialConfig(PhongMaterialConfig phongMaterialConfig) {
        this.phongMaterialConfig = phongMaterialConfig;
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

    public boolean isAlphaToCoverage() {
        return alphaToCoverage;
    }

    public void setAlphaToCoverage(boolean alphaToCoverage) {
        this.alphaToCoverage = alphaToCoverage;
    }

    public boolean isCharacterRepresenting() {
        return characterRepresenting;
    }

    public void setCharacterRepresenting(boolean characterRepresenting) {
        this.characterRepresenting = characterRepresenting;
    }

    public VertexContainer key(String key) {
        setKey(key);
        return this;
    }

    public VertexContainer materialId(String materialId) {
        setMaterialId(materialId);
        return this;
    }

    public VertexContainer materialName(String materialName) {
        setMaterialName(materialName);
        return this;
    }

    public VertexContainer phongMaterialConfig(PhongMaterialConfig phongMaterialConfig) {
        setPhongMaterialConfig(phongMaterialConfig);
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

    public VertexContainer alphaToCoverage(boolean alphaToCoverage) {
        setAlphaToCoverage(alphaToCoverage);
        return this;
    }

    public VertexContainer characterRepresenting(boolean characterRepresenting) {
        setCharacterRepresenting(characterRepresenting);
        return this;
    }
}
