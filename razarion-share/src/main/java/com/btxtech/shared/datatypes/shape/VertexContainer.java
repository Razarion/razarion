package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.05.2016.
 */
public class VertexContainer {
    private String materialId;
    private String materialName;
    private List<Vertex> vertices;
    private List<Vertex> norms;
    private List<TextureCoordinate> textureCoordinates;
    private ShapeTransform shapeTransform;
    private Color ambient;
    private Color diffuse;
    private Color specular;
    private Color emission;
    private Integer textureId;
    private String shapeElementInternalName;

    public VertexContainer setMaterialId(String materialId) {
        this.materialId = materialId;
        return this;
    }

    public VertexContainer setMaterialName(String materialName) {
        this.materialName = materialName;
        return this;
    }

    public VertexContainer setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
        return this;
    }

    public VertexContainer setNorms(List<Vertex> norms) {
        this.norms = norms;
        return this;
    }

    public VertexContainer setTextureCoordinates(List<TextureCoordinate> textureCoordinates) {
        this.textureCoordinates = textureCoordinates;
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

    public VertexContainer setShapeElementInternalName(String shapeElementInternalName) {
        this.shapeElementInternalName = shapeElementInternalName;
        return this;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Vertex> getNorms() {
        return norms;
    }

    public List<TextureCoordinate> getTextureCoordinates() {
        return textureCoordinates;
    }

    public int verticesCount() {
        return vertices.size();
    }

    public List<Vertex> generateBarycentric() {
        List<Vertex> barycentric = new ArrayList<>();
        for (int i = 0; i < vertices.size() / 3; i++) {
            barycentric.add(new Vertex(1, 0, 0));
            barycentric.add(new Vertex(0, 1, 0));
            barycentric.add(new Vertex(0, 0, 1));
        }
        return barycentric;
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

    public boolean empty() {
        return vertices.isEmpty();
    }

    public boolean checkWrongTextureSize() {
        return textureCoordinates == null || textureCoordinates.size() != verticesCount();
    }

    public boolean checkWrongNormSize() {
        return norms == null || norms.size() != verticesCount();
    }

    public String createShapeElementVertexContainerTag() {
        return shapeElementInternalName + "|" + materialId + ":" + materialName;
    }

    @Override
    public String toString() {
        return "VertexContainer{" +
                "materialId=" + materialId +
                ", materialName=" + materialName +
                ", vertices=" + vertices +
                ", norms=" + norms +
                ", textureCoordinates=" + textureCoordinates +
                ", ambient=" + ambient +
                ", diffuse=" + diffuse +
                ", specular=" + specular +
                ", emission=" + emission +
                ", textureId=" + textureId +
                '}';
    }
}
