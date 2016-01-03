package com.btxtech.shared;

import com.btxtech.client.ImageDescriptor;
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
    private String name;
    private List<Vertex> vertices = new ArrayList<>();
    private List<Vertex> storedVertices;
    private List<Vertex> normVertices = new ArrayList<>();
    private List<Vertex> tangentVertices = new ArrayList<>();
    private List<Vertex> barycentric = new ArrayList<>();
    private List<TextureCoordinate> textureCoordinates = new ArrayList<>();
    private List<Double> edges = new ArrayList<>();
    private List<Double> slopeFactor = new ArrayList<>();
    private List<TerrainMeshVertex.Type> types = new ArrayList<>();
    // private static final Logger LOGGER = Logger.getLogger(VertexList.class.getName());

    /**
     * Used by Errai
     */
    public VertexList() {
    }

    public VertexList(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Triangle triangle) {
        triangle.appendVertexTo(vertices);
        triangle.appendNormVertexTo(normVertices);
        triangle.appendTangentVertexTo(tangentVertices);
        triangle.appendBarycentricTo(barycentric);
        triangle.appendTextureCoordinateTo(textureCoordinates);
        triangle.appendEdgesTo(edges);
        triangle.appendSlopeFactor(slopeFactor);
        triangle.appendType(types);
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

    public int getTriangleCount() {
        if (vertices.size() % 3 != 0) {
            throw new IllegalStateException("");
        }
        return vertices.size() / 3;
    }

    public List<Double> createPositionDoubles() {
        List<Double> doubleList = new ArrayList<>();
        for (Vertex vertex : vertices) {
            vertex.appendTo(doubleList);
        }
        return doubleList;
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

    public void normalize(ImageDescriptor imageDescriptor) {
        List<TextureCoordinate> normalized = new ArrayList<>();
        for (TextureCoordinate textureCoordinate : textureCoordinates) {
            normalized.add(textureCoordinate.divide(imageDescriptor.getQuadraticEdge()));
        }
        textureCoordinates = normalized;
    }

    public void storeVertices() {
        storedVertices = new ArrayList<>(vertices);
    }

    public void restoreVertices() {
        vertices = storedVertices;
        storedVertices = null;
    }

    public void multiply(Matrix4 matrix) {
        Matrix4 normTransformation = matrix.normTransformation();
        for (int i = 0; i < vertices.size(); i++) {
            vertices.set(i, matrix.multiply(vertices.get(i), 1.0));
            normVertices.set(i, normTransformation.multiply(normVertices.get(i), 0.0).normalize(1.0));
            if(!tangentVertices.isEmpty()) {
                tangentVertices.set(i, normTransformation.multiply(tangentVertices.get(i), 0.0).normalize(1.0));
            }
        }
    }

    public void append(VertexList vertexList) {
        vertices.addAll(vertexList.vertices);
        normVertices.addAll(vertexList.normVertices);
        tangentVertices.addAll(vertexList.tangentVertices);
        barycentric.addAll(vertexList.barycentric);
        textureCoordinates.addAll(vertexList.textureCoordinates);
        edges.addAll(vertexList.edges);
        slopeFactor.addAll(vertexList.slopeFactor);
        types.addAll(vertexList.types);
    }

    public void append(Matrix4 transformationMatrix, VertexList vertexList) {
        vertexList.storeVertices();
        vertexList.multiply(transformationMatrix);
        append(vertexList);
        vertexList.restoreVertices();
    }

    public void appendTo(int index, VertexList vertexList) {
        if (!vertexList.vertices.isEmpty()) {
            vertices.add(vertexList.vertices.get(index));
            vertices.add(vertexList.vertices.get(index + 1));
            vertices.add(vertexList.vertices.get(index + 2));
        }
        if (!vertexList.normVertices.isEmpty()) {
            normVertices.add(vertexList.normVertices.get(index));
            normVertices.add(vertexList.normVertices.get(index + 1));
            normVertices.add(vertexList.normVertices.get(index + 2));
        }

        if (!vertexList.tangentVertices.isEmpty()) {
            tangentVertices.add(vertexList.tangentVertices.get(index));
            tangentVertices.add(vertexList.tangentVertices.get(index + 1));
            tangentVertices.add(vertexList.tangentVertices.get(index + 2));
        }
        if (!vertexList.barycentric.isEmpty()) {
            barycentric.add(vertexList.barycentric.get(index));
            barycentric.add(vertexList.barycentric.get(index + 1));
            barycentric.add(vertexList.barycentric.get(index + 2));
        }
        if (!vertexList.textureCoordinates.isEmpty()) {
            textureCoordinates.add(vertexList.textureCoordinates.get(index));
            textureCoordinates.add(vertexList.textureCoordinates.get(index + 1));
            textureCoordinates.add(vertexList.textureCoordinates.get(index + 2));
        }
        if (!vertexList.edges.isEmpty()) {
            edges.add(vertexList.edges.get(index));
            edges.add(vertexList.edges.get(index + 1));
            edges.add(vertexList.edges.get(index + 2));
        }
        if (!vertexList.slopeFactor.isEmpty()) {
            slopeFactor.add(vertexList.slopeFactor.get(index));
        }
        if (!vertexList.types.isEmpty()) {
            types.add(vertexList.types.get(index));
        }
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Vertex> getNormVertices() {
        return normVertices;
    }

    public List<Vertex> getBarycentric() {
        return barycentric;
    }

    public List<Vertex> getTangentVertices() {
        return tangentVertices;
    }

    public List<TextureCoordinate> getTextureCoordinates() {
        return textureCoordinates;
    }

    public List<Double> getEdges() {
        return edges;
    }

    public List<Double> getSlopeFactor() {
        return slopeFactor;
    }

    public List<Double> getTypesAsDoubles() {
        List<Double> typesAsDoubles = new ArrayList<>();
        for (TerrainMeshVertex.Type type : types) {
            typesAsDoubles.add((double) type.getIntType());
        }
        return typesAsDoubles;
    }

    @Override
    public String toString() {
        return "VertexList{" +
                "name=" + name +
                ", vertices=" + vertices +
                ", normVertices=" + normVertices +
                ", barycentric=" + barycentric +
                ", textureCoordinates=" + textureCoordinates +
                ", edges=" + edges +
                ", slopeFactor=" + slopeFactor +
                ", types=" + types +
                '}';
    }
}
