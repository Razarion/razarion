package com.btxtech.shared.dto;

import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Portable
public class VertexContainer {
    public enum Type {
        OPAQUE,
        TRANSPARENT_NO_SHADOW_CAST,
        TRANSPARENT_SHADOW_CAST_ONLY
    }

    private Type type;
    private List<Vertex> vertices;
    private List<Vertex> norms;
    private List<TextureCoordinate> textureCoordinates;

    /**
     * Used by errai
     */
    public VertexContainer() {

    }

    public VertexContainer(Type type) {
        this.type = type;
        vertices = new ArrayList<>();
        norms = new ArrayList<>();
        textureCoordinates = new ArrayList<>();
    }

    public Type getType() {
        return type;
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

    public void addTriangle(Collection<Matrix4> matrices, Vertex vertexA, Vertex normA, TextureCoordinate textureCoordinateA, Vertex vertexB, Vertex normB, TextureCoordinate textureCoordinateB, Vertex vertexC, Vertex normC, TextureCoordinate textureCoordinateC) {
        addTriangle(matrices, vertexA, normA, vertexB, normB, vertexC, normC);
        textureCoordinates.add(textureCoordinateA);
        textureCoordinates.add(textureCoordinateB);
        textureCoordinates.add(textureCoordinateC);
    }

    public void addTriangle(Collection<Matrix4> matrices, Vertex vertexA, Vertex normA, Vertex vertexB, Vertex normB, Vertex vertexC, Vertex normC) {
        int index = vertices.size();
        vertices.add(vertexA);
        vertices.add(vertexB);
        vertices.add(vertexC);
        norms.add(normA);
        norms.add(normB);
        norms.add(normC);

        for (Matrix4 matrix : matrices) {
            vertices.set(index, matrix.multiply(vertices.get(index), 1.0));
            vertices.set(index + 1, matrix.multiply(vertices.get(index + 1), 1.0));
            vertices.set(index + 2, matrix.multiply(vertices.get(index + 2), 1.0));
            Matrix4 normTransformation = matrix.normTransformation();
            norms.set(index, normTransformation.multiply(norms.get(index), 0.0).normalize(1.0));
            norms.set(index + 1, normTransformation.multiply(norms.get(index + 1), 0.0).normalize(1.0));
            norms.set(index + 2, normTransformation.multiply(norms.get(index + 2), 0.0).normalize(1.0));
        }
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
                "type=" + type +
                ", vertices=" + vertices +
                ", norms=" + norms +
                ", textureCoordinates=" + textureCoordinates +
                '}';
    }
}
