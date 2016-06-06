package com.btxtech.shared.dto;

import com.btxtech.shared.primitives.Color;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.05.2016.
 */
@Portable
public class VertexContainer {
    private List<Vertex> vertices;
    private List<Vertex> norms;
    private List<TextureCoordinate> textureCoordinates;
    private Color ambient;
    private Color diffuse;
    private Color specular;
    private Color emission;

    /**
     * Used by errai
     */
    public VertexContainer() {
    }

    public VertexContainer(List<Vertex> vertices, List<Vertex> norms, List<TextureCoordinate> textureCoordinates, Color ambient, Color diffuse, Color specular, Color emission) {
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

    @Override
    public String toString() {
        return "VertexContainer{" +
                "vertices=" + vertices +
                ", norms=" + norms +
                ", textureCoordinates=" + textureCoordinates +
                ", ambient=" + ambient +
                ", diffuse=" + diffuse +
                ", specular=" + specular +
                ", emission=" + emission +
                '}';
    }
}
