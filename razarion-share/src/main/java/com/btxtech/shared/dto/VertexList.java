package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Triangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 11.04.2015.
 */
public class VertexList {
    private String name;
    private List<Vertex> vertices = new ArrayList<>();
    private List<Vertex> storedVertices;
    private List<Vertex> normVertices = new ArrayList<>();
    private List<Vertex> tangentVertices = new ArrayList<>();
    private List<Vertex> barycentric = new ArrayList<>();
    @Deprecated
    private List<TextureCoordinate> textureCoordinates = new ArrayList<>();
    private List<Double> splattings = new ArrayList<>();
    // private static final Logger LOGGER = Logger.getLogger(VertexList.class.getName());

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
        triangle.appendEdgesTo(splattings);
    }

    public void add(Vertex vertexA, Vertex normA, Vertex tangentA, double splattingA,
                    Vertex vertexB, Vertex normB, Vertex tangentB, double splattingB,
                    Vertex vertexC, Vertex normC, Vertex tangentC, double splattingC) {
        vertices.add(vertexA);
        vertices.add(vertexB);
        vertices.add(vertexC);
        normVertices.add(normA);
        normVertices.add(normB);
        normVertices.add(normC);
        tangentVertices.add(tangentA);
        tangentVertices.add(tangentB);
        tangentVertices.add(tangentC);
        barycentric.add(new Vertex(1, 0, 0));
        barycentric.add(new Vertex(0, 1, 0));
        barycentric.add(new Vertex(0, 0, 1));
        splattings.add(splattingA);
        splattings.add(splattingB);
        splattings.add(splattingC);
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

    public void addTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, double edge, Vertex barycentric) {
        vertices.add(vertex);
        normVertices.add(norm);
        tangentVertices.add(tangent);
        splattings.add(edge);
        this.barycentric.add(barycentric);
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

//    public void normalize(ImageDescriptor imageDescriptor) {
//        List<TextureCoordinate> normalized = new ArrayList<>();
//        for (TextureCoordinate textureCoordinate : textureCoordinates) {
//            normalized.add(textureCoordinate.divide(imageDescriptor.getQuadraticEdge()));
//        }
//        textureCoordinates = normalized;
//    }

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
            if (!tangentVertices.isEmpty()) {
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
        splattings.addAll(vertexList.splattings);
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
        if (!vertexList.splattings.isEmpty()) {
            splattings.add(vertexList.splattings.get(index));
            splattings.add(vertexList.splattings.get(index + 1));
            splattings.add(vertexList.splattings.get(index + 2));
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

    public List<Double> getSplattings() {
        return splattings;
    }

    public void verify() {
        int count = getVerticesCount();
        if (count != normVertices.size()) {
            throw new IllegalArgumentException("Norm count is wrong. Expected: " + count + " actual: " + normVertices.size());
        }
        if (count != tangentVertices.size()) {
            throw new IllegalArgumentException("Tangent count is wrong. Expected: " + count + " actual: " + tangentVertices.size());
        }
        if (count != splattings.size()) {
            throw new IllegalArgumentException("Splattings count is wrong. Expected: " + count + " actual: " + splattings.size());
        }
    }

    public InterpolatedTerrainTriangle getInterpolatedTerrainTriangle(DecimalPosition absoluteXY) {
        return GeometricUtil.getInterpolatedVertexData(absoluteXY, vertices, normVertices::get, tangentVertices::get, splattings::get);
    }

    @Override
    public String toString() {
        return "VertexList{" +
                "name=" + name +
                ", vertices=" + vertices +
                ", normVertices=" + normVertices +
                ", barycentric=" + barycentric +
                ", textureCoordinates=" + textureCoordinates +
                ", splattings=" + splattings +
                '}';
    }
}
