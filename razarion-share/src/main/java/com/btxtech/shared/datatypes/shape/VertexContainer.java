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
    private Double alphaCutout;
    private boolean characterRepresenting;

    public PhongMaterialConfig getPhongMaterialConfig() {
        return phongMaterialConfig;
    }

    public void setPhongMaterialConfig(PhongMaterialConfig phongMaterialConfig) {
        this.phongMaterialConfig = phongMaterialConfig;
    }

    public VertexContainer phongMaterialConfig(PhongMaterialConfig phongMaterialConfig) {
        setPhongMaterialConfig(phongMaterialConfig);
        return this;
    }

    public String getKey() {
        return key;
    }

    public VertexContainer setKey(String key) {
        this.key = key;
        return this;
    }

    public VertexContainer setMaterialId(String materialId) {
        this.materialId = materialId;
        return this;
    }

    public VertexContainer setMaterialName(String materialName) {
        this.materialName = materialName;
        return this;
    }

    public VertexContainer setVerticesCount(int verticesCount) {
        this.verticesCount = verticesCount;
        return this;
    }

    public ShapeTransform getShapeTransform() {
        return shapeTransform;
    }

    public VertexContainer setShapeTransform(ShapeTransform shapeTransform) {
        this.shapeTransform = shapeTransform;
        return this;
    }

    public VertexContainer setCharacterRepresenting(boolean characterRepresenting) {
        this.characterRepresenting = characterRepresenting;
        return this;
    }

    @Deprecated
    public void setTextureId(Integer textureId) {
    }

    @Deprecated
    public boolean hasTextureId() {
        return false;
    }

    @Deprecated
    public Integer getTextureId() {
        return 0;
    }

    public Double getAlphaCutout() {
        return alphaCutout;
    }

    public void setAlphaCutout(Double alphaCutout) {
        this.alphaCutout = alphaCutout;
    }

    public String getMaterialId() {
        return materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public int getVerticesCount() {
        return verticesCount;
    }

    public boolean isCharacterRepresenting() {
        return characterRepresenting;
    }
}
