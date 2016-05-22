package com.btxtech.server.collada;

import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 11.05.2016.
 */
public abstract class ColladaConverterControl {
    private List<Vertex> vertices;
    private List<Vertex> norms;
    private List<TextureCoordinate> textureCoordinates;
    private String name;

    protected abstract void onNewVertexContainer(String name, VertexContainer vertexContainer);

    void createVertexContainer(String name) {
        this.name = name;
        vertices = new ArrayList<>();
        norms = new ArrayList<>();
        textureCoordinates = new ArrayList<>();
    }

    public void vertexContainerCreated() {
        onNewVertexContainer(name, new VertexContainer(vertices, norms, textureCoordinates));
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
}
