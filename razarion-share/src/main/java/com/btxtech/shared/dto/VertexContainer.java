package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.05.2016.
 */
@Portable
public class VertexContainer {
    private String materialId;
    private String materialName;
    private List<Vertex> vertices;
    private List<Vertex> norms;
    private List<TextureCoordinate> textureCoordinates;
    private Color ambient;
    private Color diffuse;
    private Color specular;
    private Color emission;
    private Integer textureId;

    /**
     * Used by errai
     */
    public VertexContainer() {
    }

    public VertexContainer(String materialId, String materialName, List<Vertex> vertices, List<Vertex> norms, List<TextureCoordinate> textureCoordinates, Color ambient, Color diffuse, Color specular, Color emission) {
        this.materialId = materialId;
        this.materialName = materialName;
        this.vertices = vertices;
        this.norms = norms;
        this.textureCoordinates = textureCoordinates;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.emission = emission;
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

    public int getVerticesCount() {
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

    public boolean isEmpty() {
        return vertices.isEmpty();
    }

    public boolean checkWrongTextureSize() {
        return textureCoordinates == null || textureCoordinates.size() != getVerticesCount();
    }

    public boolean checkWrongNormSize() {
        return norms == null || norms.size() != getVerticesCount();
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
