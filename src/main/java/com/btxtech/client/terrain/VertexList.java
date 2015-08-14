package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.Color;
import com.btxtech.client.math3d.Line3d;
import com.btxtech.client.math3d.TextureCoordinate;
import com.btxtech.client.math3d.Triangle;
import com.btxtech.client.math3d.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 11.04.2015.
 */
public class VertexList {
    List<Vertex> vertices = new ArrayList<>();
    List<Vertex> normVertices = new ArrayList<>();
    List<Vertex> barycentric = new ArrayList<>();
    List<TextureCoordinate> textureCoordinates = new ArrayList<>();
    List<Color> colors = new ArrayList<>();
    List<Double> edges = new ArrayList<>();

    public void add(Triangle triangle) {
        triangle.appendVertexTo(vertices);
        triangle.appendNormVertexTo(normVertices);
        triangle.appendBarycentricTo(barycentric);
        triangle.appendColorsTo(colors);
        triangle.appendTextureCoordinateTo(textureCoordinates);
        triangle.appendEdgesTo(edges);
    }

    public void add(Line3d line3d) {
        line3d.appendVertexTo(vertices);
        line3d.appendNormVerticesTo(normVertices);
        line3d.appendBarycentricTo(barycentric);
        line3d.appendColorsTo(colors);
        line3d.appendTextureCoordinateTo(textureCoordinates);
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
}
