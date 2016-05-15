package com.btxtech.shared.dto;

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

    /**
     * Used by errai
     */
    public VertexContainer() {
    }

    public VertexContainer(List<Vertex> vertices, List<Vertex> norms, List<TextureCoordinate> textureCoordinates) {
        this.vertices = vertices;
        this.norms = norms;
        this.textureCoordinates = textureCoordinates;
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

    @Override
    public String toString() {
        return "VertexContainer{" +
                "vertices=" + vertices +
                ", norms=" + norms +
                ", textureCoordinates=" + textureCoordinates +
                '}';
    }
}
