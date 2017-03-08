package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.Dependent;
import java.util.List;

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
    private Color ambient;
    private Color diffuse;
    private Color specular;
    private Color emission;
    private Integer textureId;

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

    public VertexContainer setAmbient(Color ambient) {
        this.ambient = ambient;
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

    public VertexContainer setEmission(Color emission) {
        this.emission = emission;
        return this;
    }

    @Deprecated
    public List<Vertex> OLDgetVertices() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public List<Vertex> OLDgetNorms() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Color getAmbient() {
        return ambient;
    }

    public Color getDiffuse() {
        return diffuse;
    }

    public Color getSpecular() {
        return specular;
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

    @Override
    public String toString() {
        return "VertexContainer{" +
                "materialId=" + materialId +
                ", materialName=" + materialName +
                ", verticesCount=" + verticesCount +
                ", ambient=" + ambient +
                ", diffuse=" + diffuse +
                ", specular=" + specular +
                ", emission=" + emission +
                ", textureId=" + textureId +
                '}';
    }
}
