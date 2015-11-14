package com.btxtech.shared;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.shared.primitives.Color;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 11.04.2015.
 */
@Portable
public class VertexList {
    List<Vertex> vertices = new ArrayList<>();
    List<Vertex> normVertices = new ArrayList<>();
    List<Vertex> tangentVertices = new ArrayList<>();
    List<Vertex> barycentric = new ArrayList<>();
    List<TextureCoordinate> textureCoordinates = new ArrayList<>();
    List<Color> colors = new ArrayList<>();
    List<Double> edges = new ArrayList<>();

    public void add(Triangle triangle) {
        triangle.appendVertexTo(vertices);
        triangle.appendNormVertexTo(normVertices);
        triangle.appendTangentVertexTo(tangentVertices);
        triangle.appendBarycentricTo(barycentric);
        triangle.appendColorsTo(colors);
        triangle.appendTextureCoordinateTo(textureCoordinates);
        triangle.appendEdgesTo(edges);
    }

    public void add(Vertex vertexA, Vertex normA, Vertex vertexB, Vertex normB, Vertex vertexC, Vertex normC) {
        vertices.add(vertexA);
        vertices.add(vertexB);
        vertices.add(vertexC);
        normVertices.add(normA);
        normVertices.add(normB);
        normVertices.add(normC);
        barycentric.add(new Vertex(1, 0, 0));
        barycentric.add(new Vertex(0, 1, 0));
        barycentric.add(new Vertex(0, 0, 1));
    }

    public void add(Vertex vertexA, Vertex normA, TextureCoordinate textureA, Vertex vertexB, Vertex normB, TextureCoordinate textureB, Vertex vertexC, Vertex normC, TextureCoordinate textureC) {
        vertices.add(vertexA);
        vertices.add(vertexB);
        vertices.add(vertexC);
        normVertices.add(normA);
        normVertices.add(normB);
        normVertices.add(normC);
        textureCoordinates.add(textureA);
        textureCoordinates.add(textureB);
        textureCoordinates.add(textureC);
        barycentric.add(new Vertex(1, 0, 0));
        barycentric.add(new Vertex(0, 1, 0));
        barycentric.add(new Vertex(0, 0, 1));
    }

    public int getVerticesCount() {
        return vertices.size();
    }

    public List<Double> createPositionDoubles() {
        List<Double> doubleList = new ArrayList<>();
        for (Vertex vertex : vertices) {
            vertex.appendTo(doubleList);
        }
        return doubleList;
    }

    public List<Double> createNormPositionDoubles() {
        List<Double> doubleList = new ArrayList<>();
        for (Vertex vertex : normVertices) {
            vertex.appendTo(doubleList);
        }
        return doubleList;
    }

    public List<Double> createTangentPositionDoubles() {
        List<Double> doubleList = new ArrayList<>();
        for (Vertex vertex : tangentVertices) {
            vertex.appendTo(doubleList);
        }
        return doubleList;
    }

    public List<Double> createEdgeDoubles() {
        return new ArrayList<>(edges);
    }

    public List<Double> createBarycentricDoubles() {
        List<Double> doubleList = new ArrayList<>();
        for (Vertex vertex : barycentric) {
            vertex.appendTo(doubleList);
        }
        return doubleList;
    }

    public List<Double> createTextureDoubles() {
        List<Double> doubleList = new ArrayList<>();
        for (TextureCoordinate textureCoordinate : textureCoordinates) {
            textureCoordinate.appendTo(doubleList);
        }
        return doubleList;
    }

    public List<Double> createColorDoubles() {
        List<Double> doubleList = new ArrayList<>();
        for (Color color : colors) {
            color.appendToColorRGBA(doubleList);
        }
        return doubleList;
    }

    public void normalize(ImageDescriptor imageDescriptor) {
        List<TextureCoordinate> normalized = new ArrayList<>();
        for (TextureCoordinate textureCoordinate : textureCoordinates) {
            normalized.add(textureCoordinate.divide(imageDescriptor.getQuadraticEdge()));
        }
        textureCoordinates = normalized;
    }


    public void multiply(Matrix4 matrix) {
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            vertices.set(i, matrix.multiply(vertex, 1.0));
        }
    }

    public void append(VertexList vertexList) {
        vertices.addAll(vertexList.vertices);
        normVertices.addAll(vertexList.normVertices);
        tangentVertices.addAll(vertexList.tangentVertices);
        barycentric.addAll(vertexList.barycentric);
        textureCoordinates.addAll(vertexList.textureCoordinates);
        colors.addAll(vertexList.colors);
        edges.addAll(vertexList.edges);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Vertex> getNormVertices() {
        return normVertices;
    }

    public List<Vertex> getTangentVertices() {
        return tangentVertices;
    }

    @Override
    public String toString() {
        return "VertexList{" +
                "vertices=" + vertices +
                ", normVertices=" + normVertices +
                ", barycentric=" + barycentric +
                ", textureCoordinates=" + textureCoordinates +
                ", colors=" + colors +
                ", edges=" + edges +
                '}';
    }
}
