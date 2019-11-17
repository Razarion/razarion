package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Color;

/**
 * Created by Beat
 * 14.05.2016.
 */
public class VertexContainer {
    private String key;
    private String materialId;
    private String materialName;
    private int verticesCount;
    private ShapeTransform shapeTransform;
    private Color diffuse;
    private Color specular;
    private Double shininess;
    private Color emission;
    private Integer textureId;
    private boolean characterRepresenting;

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

    public VertexContainer setDiffuse(Color diffuse) {
        this.diffuse = diffuse;
        return this;
    }

    public VertexContainer setSpecular(Color specular) {
        this.specular = specular;
        return this;
    }

    public VertexContainer setShininess(Double shininess) {
        this.shininess = shininess;
        return this;
    }

    public VertexContainer setEmission(Color emission) {
        this.emission = emission;
        return this;
    }

    public VertexContainer setCharacterRepresenting(boolean characterRepresenting) {
        this.characterRepresenting = characterRepresenting;
        return this;
    }

    public Color getDiffuse() {
        return diffuse;
    }

    public Color getSpecular() {
        return specular;
    }

    public Double getShininess() {
        return shininess;
    }

    public Color getEmission() {
        return emission;
    }

    public void setTextureId(Integer textureId) {
        this.textureId = textureId;
    }

    public boolean hasTextureId() {
        return textureId != null;
    }

    public Integer getTextureId() {
        return textureId;
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

    @Override
    public String toString() {
        return "VertexContainer{" +
                "key='" + key + '\'' +
                ", materialId='" + materialId + '\'' +
                ", materialName='" + materialName + '\'' +
                ", verticesCount=" + verticesCount +
                ", shapeTransform=" + shapeTransform +
                ", diffuse=" + diffuse +
                ", specular=" + specular +
                ", shininess=" + shininess +
                ", emission=" + emission +
                ", textureId=" + textureId +
                ", characterRepresenting=" + characterRepresenting +
                '}';
    }
}
